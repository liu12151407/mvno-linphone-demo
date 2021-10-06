package com.test_progect.mvno_linphone_demo

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import org.linphone.core.*
import org.linphone.core.tools.Log

interface LinphoneManager {

    val core: Core
    fun logoutAccount()
    fun registerAccount(
        username: String,
        phoneNumber: String,
        domain: String,
        password: String,
        proxy: String,
        transportType: TransportType,
        coreListenerStub: CoreListenerStub,
    )

    fun initOutgoingCall(phoneNumber: String): Call?
    fun acceptCall()
    fun terminateCurrentCall()
    fun addCoreListenerStub(coreListenerStub: CoreListenerStub)
    fun removeCoreListenerStub(coreListenerStub: CoreListenerStub)
    fun createChatRoom(phoneNumber: String): ChatRoom?
    fun sendMessage(
        chatRoom: ChatRoom,
        message: String,
        messageListener: ChatMessageListenerStub,
        addToViewBlock: (ChatMessage, Content) -> Unit
    )

}

class LinphoneManagerImpl(private val context: Context) : LinphoneManager {

    companion object {

        private const val CONTACT_HEADER_KEY: String = "Contact"
        private const val FROM_HEADER_KEY: String = "From"
        private const val TO_HEADER_KEY: String = "To"
        private const val P_ASSOCIATED_URI_HEADER_KEY: String = "P-Associated-URI"
        private const val AUTHORIZATION_HEADER_KEY: String = "Authorization"

    }

    override val core: Core by lazy {
        Factory.instance().createCore(null, null, context)
    }
    private var imsi: String? = null
    private var phoneNumber: String? = null
    private var domain: String? = null

    override fun logoutAccount() {
        core.currentCall?.terminate()
        core.chatRooms.forEach { core.deleteChatRoom(it) }
        core.defaultAccount?.let { account ->
            core.removeAccount(account)
            core.clearAccounts()
            core.clearAllAuthInfo()
        }
    }

    override fun registerAccount(
        username: String,
        phoneNumber: String,
        domain: String,
        password: String,
        proxy: String,
        transportType: TransportType,
        coreListenerStub: CoreListenerStub,
    ) {
        this.imsi = username
        this.phoneNumber = phoneNumber
        this.domain = domain
        val accountParams = createAccountParams(proxy, transportType)
        val account = createAccount(accountParams)
        val authInfo = Factory.instance().createAuthInfo(
            username,
            "$username@$domain",
            password,
            null,
            null,
            "@$domain"
        )
        core.apply {
            addAuthInfo(authInfo)
            addAccount(account)
            defaultAccount = account
            addListener(coreListenerStub)
            start()
        }
    }

    override fun initOutgoingCall(phoneNumber: String): Call? {
        val remoteAddress = core.interpretUrl(phoneNumber) ?: return null
        val params = createCallParams() ?: return null
        return core.inviteAddressWithParams(remoteAddress, params)
    }

    override fun acceptCall() {
        core.currentCall?.accept()
    }

    override fun terminateCurrentCall() {
        core.currentCall?.terminate()
    }

    override fun addCoreListenerStub(coreListenerStub: CoreListenerStub) {
        core.addListener(coreListenerStub)
    }

    override fun removeCoreListenerStub(coreListenerStub: CoreListenerStub) {
        core.removeListener(coreListenerStub)
    }

    override fun createChatRoom(phoneNumber: String): ChatRoom? {
        val params = core.createDefaultChatRoomParams().setDefaultParams()
        val remoteAddress = core.interpretUrl(phoneNumber) ?: return null
        val localAddress = core.defaultAccount?.params?.identityAddress
        return core.createChatRoom(params, localAddress, arrayOf(remoteAddress))
    }

    override fun sendMessage(
        chatRoom: ChatRoom,
        message: String,
        messageListener: ChatMessageListenerStub,
        addToViewBlock: (ChatMessage, Content) -> Unit
    ) {
        chatRoom.createMessageFromUtf8(message).apply {
            addListener(messageListener)
            contents.forEach { addToViewBlock(this, it) }
            val pAssociatedURI =
                checkNotNull(core.defaultAccount).getCustomHeader(P_ASSOCIATED_URI_HEADER_KEY)
            addCustomHeader(CONTACT_HEADER_KEY, createContactHeaderValue())
            addCustomHeader(FROM_HEADER_KEY, createFromHeaderValue(false))
            addCustomHeader(P_ASSOCIATED_URI_HEADER_KEY, pAssociatedURI)
            send()
        }
    }

    private fun ChatRoomParams.setDefaultParams(): ChatRoomParams {
        backend = ChatRoomBackend.Basic
        enableEncryption(false)
        enableGroup(false)
        return if (isValid) this else throw IllegalArgumentException("Encryption or Group chats are not supported")
    }

    private fun createAccount(accountParams: AccountParams): Account =
        core.createAccount(accountParams).apply {
            setCustomHeader(TO_HEADER_KEY, createToHeaderValue())
            setCustomHeader(FROM_HEADER_KEY, createFromHeaderValue(true))
            setCustomHeader(AUTHORIZATION_HEADER_KEY, createAuthorizationHeaderValue())
            setCustomHeader(CONTACT_HEADER_KEY, createContactHeaderValue())
            addListener { _, state, message ->
                Log.i("[Account] Registration state changed: $state, $message")
            }
        }

    private fun createAccountParams(proxy: String, transportType: TransportType): AccountParams {
        val identity = Factory.instance().createAddress("sip:$imsi@$domain")
        val address = Factory.instance().createAddress("sip:$proxy")
        address?.transport = transportType
        return core.createAccountParams().apply {
            identityAddress = identity
            serverAddress = address
            registerEnabled = true
        }
    }

    private fun createCallParams(): CallParams? {
        val pAssociatedURI =
            checkNotNull(core.defaultAccount).getCustomHeader(P_ASSOCIATED_URI_HEADER_KEY)
        return core.createCallParams(null)?.apply {
            addCustomHeader(CONTACT_HEADER_KEY, createContactHeaderValue())
            addCustomHeader(FROM_HEADER_KEY, createFromHeaderValue(false))
            addCustomHeader(P_ASSOCIATED_URI_HEADER_KEY, pAssociatedURI)
            mediaEncryption = MediaEncryption.None
            enableAudio(true)
        }
    }

    @SuppressLint("HardwareIds")
    private fun createContactHeaderValue(): String {
        val deviceId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return ("<sip:$phoneNumber@172.30.147.209:43530;" +
                "transport=udp;" +
                "pn-provider=tinkoff;" +
                "pn-prid=$deviceId;>;" +
                "gr=urn;" +
                "+g.3gpp.smsip;" +
                "+sip.instance=\"<urn:uuid:d1644492-1103-00f8-a4eb-c7a87d3b41f7>\"")
    }

    private fun createFromHeaderValue(isRegistration: Boolean): String {
        val address = if (isRegistration) imsi else phoneNumber
        return "<sip:$address@$domain>;tag=~UwXzKOlD\n"
    }

    private fun createToHeaderValue(): String = "sip:$imsi@$domain"

    private fun createAuthorizationHeaderValue(): String {
        return "Digest realm=\"$domain\", " +
                "nonce=\"9b4c5fedc08296985b586acee1f16218\", " +
                "algorithm=MD5, username=\"$imsi@$domain\", " +
                "uri=\"sip:$domain\", " +
                "response=\"365b94fb3ad933759f921d7d6d88d257\", " +
                "cnonce=\"HQlmMYSNfKlp66nk\", " +
                "nc=00000001, " +
                "qop=auth"
    }

}
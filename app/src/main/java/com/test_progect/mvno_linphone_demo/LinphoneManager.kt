package com.test_progect.mvno_linphone_demo

import android.location.Location
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import org.linphone.core.*

interface LinphoneManager {

    val core: Core

    fun logoutAccount()
    fun registerAccount(
        accountInfo: NoSimAccountInfo,
        location: Location,
        coreListenerStub: CoreListenerStub,
    )

    fun call(phoneNumber: String, location: Location): Call?
    fun acceptCall(location: Location)
    fun terminateCall()

    fun addCoreListenerStub(coreListenerStub: CoreListenerStub)
    fun removeCoreListenerStub(coreListenerStub: CoreListenerStub)
    fun createChatRoom(phoneNumber: String): ChatRoom?
    fun sendMessage(
        location: Location,
        chatRoom: ChatRoom,
        message: String,
        messageListener: ChatMessageListenerStub,
        addToViewBlock: (ChatMessage, Content) -> Unit
    )

    fun enableSpeaker(enable: Boolean)
    fun enableMicrophone(enable: Boolean)

}

class LinphoneManagerImpl(
    private val activity: AppCompatActivity,
    private val deviceIdProvider: DeviceIdProvider,
) : LinphoneManager {

    override val core: Core by lazy {
        Factory.instance().createCore(null, null, activity)
    }

    private var lifecycleAccountInfo: NoSimAccountInfo? = null
    private val accountInfo: NoSimAccountInfo get() = checkNotNull(lifecycleAccountInfo)
    private val imsi: String get() = accountInfo.imsi
    private val domain: String get() = accountInfo.domain
    private val password: String get() = accountInfo.password
    private val proxy: String get() = accountInfo.proxy
    private val transportType: TransportType
        get() = TransportType.fromInt(accountInfo.transportType)
    private val deviceId: String = deviceIdProvider.getDeviceId()

    @MainThread
    override fun logoutAccount() {
        core.currentCall?.terminate()
        core.chatRooms.forEach { core.deleteChatRoom(it) }
        core.defaultAccount?.let { account ->
            core.removeAccount(account)
            core.clearAccounts()
            core.clearAllAuthInfo()
        }
    }

    @MainThread
    override fun registerAccount(
        accountInfo: NoSimAccountInfo,
        location: Location,
        coreListenerStub: CoreListenerStub,
    ) {
        lifecycleAccountInfo = accountInfo
        val accountParams = createAccountParams(proxy, transportType)
        val account = core.createAccount(accountParams)
            .applyAuthorizationHeaders(accountInfo, location, deviceId)
        val authInfo = Factory.instance().createAuthInfo(
            imsi,
            "$imsi@$domain",
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

    @MainThread
    override fun call(phoneNumber: String, location: Location): Call? {
        val remoteAddress = core.interpretUrl(phoneNumber) ?: return null
        val params = createCallParams(location)
        return core.inviteAddressWithParams(remoteAddress, params)
    }

    @MainThread
    override fun acceptCall(location: Location) {
        val call = checkNotNull(core.currentCall)
        val params = checkNotNull(core.createCallParams(call)).applyLocationHeader(location)
        call.acceptWithParams(params)
    }

    @MainThread
    override fun terminateCall() {
        core.currentCall?.terminate()
    }

    @MainThread
    override fun addCoreListenerStub(coreListenerStub: CoreListenerStub) {
        core.addListener(coreListenerStub)
    }

    @MainThread
    override fun removeCoreListenerStub(coreListenerStub: CoreListenerStub) {
        core.removeListener(coreListenerStub)
    }

    @MainThread
    override fun createChatRoom(phoneNumber: String): ChatRoom? {
        val params = core.createDefaultChatRoomParams().setDefaultParams()
        val remoteAddress = core.interpretUrl(phoneNumber) ?: return null
        val localAddress = core.defaultAccount?.params?.identityAddress
        return core.createChatRoom(params, localAddress, arrayOf(remoteAddress))
    }

    @MainThread
    override fun sendMessage(
        location: Location,
        chatRoom: ChatRoom,
        message: String,
        messageListener: ChatMessageListenerStub,
        addToViewBlock: (ChatMessage, Content) -> Unit
    ) {
        chatRoom.createMessageFromUtf8(message).apply {
            addListener(messageListener)
            contents.forEach { addToViewBlock(this, it) }
            val pAssociatedURI =
                checkNotNull(core.defaultAccount?.getCustomHeader(P_ASSOCIATED_URI_HEADER_KEY))
            applyHeaders(
                accountInfo,
                location,
                deviceId,
                pAssociatedURI,
            )
            send()
        }
    }

    @MainThread
    override fun enableSpeaker(enable: Boolean) {
        val call = checkNotNull(core.currentCall)
        for (audioDevice in core.audioDevices) {
            if (!enable && audioDevice.type == AudioDevice.Type.Earpiece) {
                call.outputAudioDevice = audioDevice
            } else if (enable && audioDevice.type == AudioDevice.Type.Speaker) {
                call.outputAudioDevice = audioDevice
            }
        }
    }

    @MainThread
    override fun enableMicrophone(enable: Boolean) {
        core.enableMic(enable)
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

    private fun createCallParams(location: Location): CallParams {
        val pAssociatedURI =
            checkNotNull(core.defaultAccount?.getCustomHeader(P_ASSOCIATED_URI_HEADER_KEY))
        return checkNotNull(core.createCallParams(null))
            .applyHeaders(accountInfo, location, deviceId, pAssociatedURI)
            .apply {
                mediaEncryption = MediaEncryption.None
                enableAudio(true)

            }
    }

    private fun ChatRoomParams.setDefaultParams(): ChatRoomParams {
        backend = ChatRoomBackend.Basic
        enableEncryption(false)
        enableGroup(false)
        return if (isValid) this else throw IllegalArgumentException("Encryption or Group chats are not supported")
    }

}
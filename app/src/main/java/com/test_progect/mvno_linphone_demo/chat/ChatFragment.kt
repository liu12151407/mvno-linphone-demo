package com.test_progect.mvno_linphone_demo.chat

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.*
import com.test_progect.mvno_linphone_demo.R
import com.test_progect.mvno_linphone_demo.databinding.ChatFragmentBinding
import org.linphone.core.*

class ChatFragment : Fragment(), ChatView.Presenter {

    private lateinit var view: ChatView
    private var chatRoom: ChatRoom? = null
    private var uncheckedBinding: ChatFragmentBinding? = null
    private val binding: ChatFragmentBinding get() = checkNotNull(uncheckedBinding)
    private val sharedPreferences: SharedPreferences by lazy { (requireActivity() as MainActivity).sharedPreferences }
    private val phoneNumber: String
        get() = binding.recipientPhoneNumberInput.editText?.text?.toString() ?: ""
    private val core: Core by lazy { (requireActivity() as MainActivity).core }
    private val router: Router by lazy { requireActivity() as Router }
    private val coreListener = object : CoreListenerStub() {

        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState?,
            message: String
        ) {
            if (state == RegistrationState.Failed) {
                core.clearAllAuthInfo()
                core.clearAccounts()
                router.openRegistration()
            }
        }

        override fun onMessageReceived(core: Core, chatRoom: ChatRoom, message: ChatMessage) {
            if (this@ChatFragment.chatRoom == null) {
                if (chatRoom.hasCapability(ChatRoomCapabilities.Basic.toInt())) {
                    this@ChatFragment.chatRoom = chatRoom
                    view.setPhoneNumber(chatRoom.peerAddress.asStringUriOnly())
                }
            }
            chatRoom.markAsRead()
            message.contents.forEach { view.addMessage(message, it) }
        }
    }
    private val chatMessageListener = object : ChatMessageListenerStub() {

        override fun onMsgStateChanged(message: ChatMessage, state: ChatMessage.State?) {
            val messageView = message.userData as? View
            when (state) {
                ChatMessage.State.InProgress -> {
                    messageView?.setBackgroundColor(requireContext().getColorCompat(R.color.message_in_progress))
                }
                ChatMessage.State.Delivered -> {
                    // The proxy server has acknowledged the message with a 200 OK
                    messageView?.setBackgroundColor(requireContext().getColorCompat(R.color.message_delivered))
                }
                ChatMessage.State.DeliveredToUser -> {
                    // User as received it
                    messageView?.setBackgroundColor(requireContext().getColorCompat(R.color.message_delivered_to_user))
                }
                ChatMessage.State.Displayed -> {
                    // User as read it (client called chatRoom.markAsRead()
                    messageView?.setBackgroundColor(requireContext().getColorCompat(R.color.message_displayed))
                }
                ChatMessage.State.NotDelivered -> {
                    // User might be invalid or not registered
                    messageView?.setBackgroundColor(requireContext().getColorCompat(R.color.message_not_delivered))
                }
                else -> {
                    // empty
                }
            }
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uncheckedBinding = ChatFragmentBinding.inflate(inflater, container, false)
        view = ChatViewImpl(binding, this, sharedPreferences)
        core.addListener(coreListener)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        core.removeListener(coreListener)
    }

    override fun onSendMessageClick(message: String) {
        val isValid = validatePhoneNumber(phoneNumber)
        if (isValid.not()) {
            view.showInvalidPhoneNumberToast()
            return
        }
        sharedPreferences.edit { putString(PREF_CHAT_RECIPIENT_PHONE, phoneNumber) }
        sendMessage(message)
    }

    private fun sendMessage(message: String) {
        if (chatRoom == null) createChatRoom()
        chatRoom?.let {
            it.createMessageFromUtf8(message).sendCustom()
            view.clearMessageInput()
        }
    }

    private fun createChatRoom() {
        val params = core.createDefaultChatRoomParams().setDefaultParams()
        val remoteAddress = core.interpretUrl(phoneNumber)
        if (remoteAddress == null) {
            view.showRemoteAddressErrorToast()
            return
        }
        val localAddress = core.defaultAccount?.params?.identityAddress
        chatRoom = core.createChatRoom(params, localAddress, arrayOf(remoteAddress))
        view.disableRecipientInput()
    }

    private fun ChatMessage.sendCustom() =
        apply {
            addListener(chatMessageListener)
            contents.forEach { view.addMessage(this, it) }
            addCustomHeaders()
            send()
        }

    private fun ChatMessage.addCustomHeaders() {
        val account = checkNotNull(core.defaultAccount)
        val pAssociatedURI = account.getCustomHeader("P-Associated-URI")
        val contactHeader = account.getCustomHeader("Contact")
        val fromHeader = account.getCustomHeader("From")
        addCustomHeader("P-Associated-URI", pAssociatedURI)
        addCustomHeader("Contact", contactHeader)
        addCustomHeader("From", fromHeader)

    }

    private fun ChatRoomParams.setDefaultParams(): ChatRoomParams {
        backend = ChatRoomBackend.Basic
        enableEncryption(false)
        enableGroup(false)
        return if (isValid) this else throw IllegalArgumentException("Encryption or Group chats are not supported")
    }

}
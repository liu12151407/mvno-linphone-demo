package com.test_progect.mvno_linphone_demo.chat

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.FOCUS_DOWN
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.*
import com.test_progect.mvno_linphone_demo.R
import com.test_progect.mvno_linphone_demo.databinding.ChatFragmentBinding
import org.linphone.core.*

private const val PREF_CHAT_RECIPIENT_PHONE = "PREF_CHAT_RECIPIENT_PHONE"

class ChatFragment : Fragment() {

    private var uncheckedBinding: ChatFragmentBinding? = null
    private val binding: ChatFragmentBinding get() = checkNotNull(uncheckedBinding)
    private val sharedPreferences: SharedPreferences by lazy { (requireActivity() as MainActivity).sharedPreferences }
    private val phoneNumber: String
        get() = binding.recipientPhoneNumberInput.editText?.text?.toString() ?: ""
    private val message: String
        get() = binding.messageTextInput.editText?.text?.toString() ?: ""
    private val core: Core by lazy { (requireActivity() as MainActivity).core }
    private val router: Router by lazy { requireActivity() as Router }
    private var chatRoom: ChatRoom? = null
    private val isPhoneNumberValid: Boolean by lazy {
        validatePhoneNumber(phoneNumber, requireContext())
    }
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
                    setPhoneNumber(chatRoom.peerAddress.asStringUriOnly())
                }
            }
            chatRoom.markAsRead()
            message.contents.forEach { addTextMessageInChat(message, it) }
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

    private fun setPhoneNumber(phoneNumber: String) {
        binding.recipientPhoneNumberInput.editText?.setText(phoneNumber)
        binding.recipientPhoneNumberInput.isEnabled = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uncheckedBinding = ChatFragmentBinding.inflate(inflater, container, false)
        core.addListener(coreListener)
        binding.messageTextInput.setEndIconOnClickListener {
            if (isPhoneNumberValid) {
                sharedPreferences.edit { putString(PREF_CHAT_RECIPIENT_PHONE, phoneNumber) }
                sendMessage(message)
            }
        }
        sharedPreferences.getString(PREF_CHAT_RECIPIENT_PHONE, "+7").let {
            binding.recipientPhoneNumberInput.editText?.setText(it)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        core.removeListener(coreListener)
    }

    private fun sendMessage(message: String) {
        if (chatRoom == null) createChatRoom()
        val chatMessage = checkNotNull(chatRoom).createMessageFromUtf8(message)
        chatMessage.addListener(chatMessageListener)
        chatMessage.contents.forEach { addTextMessageInChat(chatMessage, it) }
        chatMessage.addCustomHeaders()
        chatMessage.send()
        onMessageSent()
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

    private fun createChatRoom() {
        val params = core.createDefaultChatRoomParams().apply {
            backend = ChatRoomBackend.Basic
            enableEncryption(false)
            enableGroup(false)
        }
        if (params.isValid) {
            val remoteAddress = core.interpretUrl(phoneNumber) ?: return
            val localAddress = core.defaultAccount?.params?.identityAddress
            val room = core.createChatRoom(params, localAddress, arrayOf(remoteAddress))
            if (room != null) {
                chatRoom = room
                binding.recipientPhoneNumberInput.isEnabled = false
            }
        } else {
            throw IllegalArgumentException("Encryption or Group chats are not supported")
        }
    }

    private fun addTextMessageInChat(chatMessage: ChatMessage, content: Content) {
        val messageView = TextView(requireContext()).apply {
            setPadding(20, 20, 20, 20)
        }
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = if (chatMessage.isOutgoing) Gravity.END else Gravity.START
            setMargins(20, 20, 20, 20)
        }
        messageView.layoutParams = layoutParams
        messageView.text = content.utf8Text
        messageView.setTextMessageBackground(chatMessage.isOutgoing)
        chatMessage.userData = messageView
        addMessageView(messageView)
    }

    private fun addMessageView(messageView: TextView) {
        binding.chatView.addView(messageView)
        binding.scrollView.fullScroll(FOCUS_DOWN)
    }

    private fun onMessageSent() {
        binding.messageTextInput.editText?.text?.clear()
    }

    private fun TextView.setTextMessageBackground(isOutgoing: Boolean) {
        if (isOutgoing) {
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.purple_500)
            )
        }
    }

}
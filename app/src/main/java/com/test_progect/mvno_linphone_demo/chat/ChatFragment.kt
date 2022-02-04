package com.test_progect.mvno_linphone_demo.chat

import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.*
import com.test_progect.mvno_linphone_demo.R
import com.test_progect.mvno_linphone_demo.core_ui.CoroutineJobDelegate
import com.test_progect.mvno_linphone_demo.core_ui.CoroutineJobDelegateImpl
import com.test_progect.mvno_linphone_demo.databinding.ChatFragmentBinding
import org.linphone.core.*
import ru.tcsbank.mvno.coroutines.onError
import ru.tcsbank.mvno.coroutines.onFinish
import ru.tcsbank.mvno.coroutines.onLaunch

class ChatFragment : Fragment(), ChatView.Presenter,
    CoroutineJobDelegate by CoroutineJobDelegateImpl() {

    private lateinit var view: ChatView
    private var chatRoom: ChatRoom? = null
    private var uncheckedBinding: ChatFragmentBinding? = null
    private val binding: ChatFragmentBinding get() = checkNotNull(uncheckedBinding)
    private val sharedPreferences: SharedPreferences by lazy { (requireActivity() as MainActivity).sharedPreferences }
    private val rawPhoneNumber: String
        get() = binding.recipientPhoneNumberInput.editText?.text?.toString() ?: ""
    private var phoneNumber: String? = null
    private val linphoneManager: LinphoneManager by lazy {
        (requireActivity() as MainActivity).linphoneManager
    }
    private val locationProvider: LocationProvider by lazy {
        (requireActivity() as MainActivity).locationProvider
    }
    private var location: Location? = null
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
        linphoneManager.addCoreListenerStub(coreListener)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        initializeCoroutineJob()
    }

    override fun onStop() {
        cancelCoroutineJob()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        linphoneManager.removeCoreListenerStub(coreListener)
    }

    override fun onSendMessageClick(message: String) {
        if (phoneNumber == null) {
            phoneNumber = rawPhoneNumber
            sharedPreferences.edit { putString(PREF_CHAT_RECIPIENT_PHONE, phoneNumber) }
            createChatRoom()
        }
        sendMessage(message)
    }

    private fun sendMessage(message: String) {
        onLaunch { location = locationProvider.getLocation() }
            .onError { }
            .onFinish {
                if (location != null && chatRoom != null) {
                    linphoneManager.sendMessage(
                        checkNotNull(location),
                        checkNotNull(chatRoom),
                        message,
                        chatMessageListener
                    ) { chatMessage, content ->
                        view.addMessage(chatMessage, content)
                    }
                    view.clearMessageInput()
                }
            }
    }

    private fun createChatRoom() {
        chatRoom = linphoneManager.createChatRoom(checkNotNull(phoneNumber))
        if (chatRoom == null) {
            view.showRemoteAddressErrorToast()
            return
        }
        view.disableRecipientInput()
    }

}
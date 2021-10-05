package com.test_progect.mvno_linphone_demo.chat

import android.content.SharedPreferences
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.test_progect.mvno_linphone_demo.R
import com.test_progect.mvno_linphone_demo.core_ui.ToastView
import com.test_progect.mvno_linphone_demo.core_ui.ToastViewImpl
import com.test_progect.mvno_linphone_demo.databinding.ChatFragmentBinding
import org.linphone.core.ChatMessage
import org.linphone.core.Content

const val PREF_CHAT_RECIPIENT_PHONE = "PREF_CHAT_RECIPIENT_PHONE"

interface ChatView : ToastView {

    fun addMessage(chatMessage: ChatMessage, content: Content)
    fun setPhoneNumber(phoneNumber: String)
    fun disableRecipientInput()
    fun clearMessageInput()

    interface Presenter {

        fun onSendMessageClick(message: String)

    }

}

class ChatViewImpl(
    private val binding: ChatFragmentBinding,
    private val presenter: ChatView.Presenter,
    sharedPreferences: SharedPreferences,
) : ChatView,
    ToastView by ToastViewImpl(binding.root.context) {

    private val message: String
        get() = binding.messageTextInput.editText?.text?.toString() ?: ""

    init {
        binding.messageTextInput.setEndIconOnClickListener {
            presenter.onSendMessageClick(message)
        }
        val recipientPhone =
            checkNotNull(sharedPreferences.getString(PREF_CHAT_RECIPIENT_PHONE, "+7"))
        binding.recipientPhoneNumberInput.editText?.setText(recipientPhone)
    }

    override fun addMessage(chatMessage: ChatMessage, content: Content) {
        val messageView = TextView(binding.root.context).apply {
            setPadding(20, 20, 20, 20)
        }
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = if (chatMessage.isOutgoing) Gravity.END else Gravity.START
            if (chatMessage.isOutgoing) {
                setMargins(100, 20, 20, 20)
            } else {
                setMargins(20, 20, 20, 100)
            }
        }
        messageView.layoutParams = layoutParams
        messageView.text = content.utf8Text
        messageView.setTextMessageBackground(chatMessage.isOutgoing)
        chatMessage.userData = messageView
        addMessageView(messageView)
    }

    override fun setPhoneNumber(phoneNumber: String) {
        binding.recipientPhoneNumberInput.editText?.setText(phoneNumber)
        binding.recipientPhoneNumberInput.isEnabled = false
    }

    override fun disableRecipientInput() {
        binding.recipientPhoneNumberInput.isEnabled = false
    }

    override fun clearMessageInput() {
        binding.messageTextInput.editText?.text?.clear()
    }

    private fun addMessageView(messageView: TextView) {
        binding.chatView.addView(messageView)
        binding.scrollView.fullScroll(View.FOCUS_DOWN)
    }

    private fun TextView.setTextMessageBackground(isOutgoing: Boolean) {
        if (isOutgoing) {
            setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        } else {
            setBackgroundColor(
                ContextCompat.getColor(context, R.color.purple_500)
            )
        }
    }

}
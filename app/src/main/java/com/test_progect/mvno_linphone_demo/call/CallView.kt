package com.test_progect.mvno_linphone_demo.call

import android.content.SharedPreferences
import androidx.core.widget.addTextChangedListener
import com.test_progect.mvno_linphone_demo.R
import com.test_progect.mvno_linphone_demo.core_ui.ToastView
import com.test_progect.mvno_linphone_demo.core_ui.ToastViewImpl
import com.test_progect.mvno_linphone_demo.databinding.CallFragmentBinding
import com.test_progect.mvno_linphone_demo.hideKeyboard

interface CallView : ToastView {

    fun setRegistrationState(message: String)

    interface Presenter {

        fun onCallButtonCLicked(phoneNumber: String)

    }

}

class CallViewImpl(
    private val binding: CallFragmentBinding,
    private val presenter: CallView.Presenter,
    sharedPreferences: SharedPreferences,
) : CallView,
    ToastView by ToastViewImpl(binding.root.context) {

    private val context = binding.root.context
    private val phoneNumber: String
        get() = binding.phoneNumberInputLayout.editText?.text?.toString() ?: ""

    init {
        with(binding) {
            registrationStateView.text = context.getString(R.string.linphone_registration_ok)
            root.setOnClickListener { clearFocus() }
            callButton.isEnabled = true
            callButton.setOnClickListener {
                clearFocus()
                presenter.onCallButtonCLicked(phoneNumber)
            }
            phoneNumberInputLayout.editText?.apply {
                setText(sharedPreferences.getString(PREF_LAST_OUTGOING_CAL, "+7"))
                addTextChangedListener {
                    callButton.isEnabled = phoneNumber.isNotBlank()
                }
            }
        }
    }

    override fun setRegistrationState(message: String) {
        binding.registrationStateView.text = message
    }

    private fun clearFocus() {
        binding.phoneNumberInputLayout.clearFocus()
        binding.root.hideKeyboard()
    }

}
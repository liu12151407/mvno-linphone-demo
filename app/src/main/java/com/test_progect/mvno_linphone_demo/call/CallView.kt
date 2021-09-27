package com.test_progect.mvno_linphone_demo.call

import android.view.MenuItem
import androidx.core.widget.addTextChangedListener
import com.test_progect.mvno_linphone_demo.R
import com.test_progect.mvno_linphone_demo.databinding.CallFragmentBinding
import com.test_progect.mvno_linphone_demo.hideKeyboard

interface CallView {

    fun setRegistrationOkState(message: String)
    fun setRegistrationFailedState(message: String)

    interface Presenter {

        fun onCallButtonCLicked(phoneNumber: String)
        fun onMenuItemClicked(item: MenuItem): Boolean

    }

}

class CallViewImpl(
    private val binding: CallFragmentBinding,
    private val presenter: CallView.Presenter,
) : CallView {

    private val context = binding.root.context
    private val phoneNumber: String
        get() = binding.phoneNumberInputLayout.editText?.text?.toString() ?: ""

    init {
        initToolbar()
        with(binding) {
            registrationStateView.text = context.getString(R.string.linphone_registration_ok)
            root.setOnClickListener { phoneNumberInputLayout.clearFocus() }
            callButton.isEnabled = false
            callButton.setOnClickListener {
                phoneNumberInputLayout.clearFocus()
                root.hideKeyboard()
                presenter.onCallButtonCLicked(phoneNumber)
            }
            phoneNumberInputLayout.editText?.addTextChangedListener {
                callButton.isEnabled = phoneNumber.isNotBlank()
            }
        }
    }

    override fun setRegistrationOkState(message: String) {
        binding.registrationStateView.text =
            context.getString(R.string.linphone_registration_ok)
    }


    override fun setRegistrationFailedState(message: String) {
        binding.registrationStateView.text =
            context.getString(R.string.linphone_registration_failed, message)
    }

    private fun initToolbar() {
        binding.toolbar.apply {
            inflateMenu(R.menu.call_menu)
            setOnMenuItemClickListener {
                presenter.onMenuItemClicked(it)
            }
        }
    }

}
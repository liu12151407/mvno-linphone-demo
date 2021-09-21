package com.test_progect.mvno_linphone_demo.account

import android.content.SharedPreferences
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.children
import com.test_progect.mvno_linphone_demo.R
import com.test_progect.mvno_linphone_demo.databinding.AccountFragmentBinding
import com.test_progect.mvno_linphone_demo.hideKeyboard
import org.linphone.core.TransportType

const val PREF_USER_NAME = "PREF_USER_NAME"
const val PREF_PHONE_NUMBER = "PREF_PHONE_NUMBER"
const val PREF_DOMAIN = "PREF_DOMAIN"
const val PREF_PASSWORD = "PREF_PASSWORD"
const val PREF_PROXY = "PREF_PROXY"

interface AccountView {

    fun setRegistrationStateMessage(message: String)
    fun onRegistrationFailed(message: String)

    interface Presenter {

        fun onRegistrationButtonClicked(
            username: String,
            phoneNumber: String,
            domain: String,
            password: String,
            proxy: String,
            transportType: TransportType
        )
    }

}

class AccountViewImpl(
    private val binding: AccountFragmentBinding,
    private val sharedPreferences: SharedPreferences,
    private val presenter: AccountView.Presenter,
    coreVersion: String,
) : AccountView {

    private val context = binding.root.context
    private val username: String by lazy { binding.imsiInputLayout.editText?.text.toString() }
    private val phoneNumber: String by lazy { binding.phoneNumberInputLayout.editText?.text.toString() }
    private val domain: String by lazy { binding.domainInputLayout.editText?.text.toString() }
    private val password: String by lazy { binding.passwordInputLayout.editText?.text.toString() }
    private val proxy: String by lazy { binding.proxyInputLayout.editText?.text.toString() }
    private var transportType: TransportType = TransportType.Udp

    init {
        initAuthInfoViews()
        binding.root.setOnClickListener { clearFocus() }
        binding.coreVersionView.text =
            context.getString(R.string.linphone_core_version, coreVersion)
        binding.registrationButton.setOnClickListener {
            clearFocus()
            presenter.onRegistrationButtonClicked(
                username,
                phoneNumber,
                domain,
                password,
                proxy,
                transportType
            )
            it.isEnabled = false
        }

    }

    override fun setRegistrationStateMessage(message: String) {
        binding.registrationStateView.text = message
    }

    override fun onRegistrationFailed(message: String) {
        binding.registrationButton.isEnabled = true
        binding.registrationStateView.text =
            context.getString(R.string.linphone_registration_failed, message)
    }

    private fun initAuthInfoViews() {
        binding.apply {
            imsiInputLayout.editText?.setText(
                sharedPreferences.getString(PREF_USER_NAME, "250621003718003")
            )
            phoneNumberInputLayout.editText?.setText(
                sharedPreferences.getString(PREF_PHONE_NUMBER, "79950993622")
            )
            domainInputLayout.editText?.setText(
                sharedPreferences.getString(PREF_DOMAIN, "ims.mnc062.mcc250.3gppnetwork.org")
            )
            passwordInputLayout.editText?.setText(
                sharedPreferences.getString(PREF_PASSWORD, "9876543210")
            )
            proxyInputLayout.editText?.setText(
                sharedPreferences.getString(PREF_PROXY, "10.233.75.140:5060")
            )
        }
        initTransportTypeView()
    }

    private fun initTransportTypeView() {
        val transportTypeSet = listOf("UDP", "TCP", "TLS")
        val adapter = ArrayAdapter(
            context,
            R.layout.transport_type_item,
            transportTypeSet
        )
        (binding.transportTypeInputLayout.editText as AutoCompleteTextView).apply {
            setAdapter(adapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                transportType = when (position) {
                    0 -> TransportType.Udp
                    1 -> TransportType.Tcp
                    else -> TransportType.Tls
                }
            }
            setText(transportTypeSet[0], false)
        }
    }

    private fun clearFocus() {
        binding.root.apply {
            children.forEach { view -> view.clearFocus() }
            hideKeyboard()
        }
    }

}
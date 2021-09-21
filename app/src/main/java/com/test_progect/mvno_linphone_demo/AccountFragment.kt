package com.test_progect.mvno_linphone_demo

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings.Secure
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.edit
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.databinding.AccountFragmentBinding
import org.linphone.core.*
import org.linphone.core.tools.Log

private const val PREF_USER_NAME = "PREF_USER_NAME"
private const val PREF_PHONE_NUMBER = "PREF_PHONE_NUMBER"
private const val PREF_DOMAIN = "PREF_DOMAIN"
private const val PREF_PASSWORD = "PREF_PASSWORD"
private const val PREF_PROXY = "PREF_PROXY"

class AccountFragment : Fragment() {

    private var uncheckedBinding: AccountFragmentBinding? = null
    private var transportType: TransportType = TransportType.Udp
    private val binding: AccountFragmentBinding get() = checkNotNull(uncheckedBinding)
    private val router: Router by lazy { requireActivity() as Router }
    private val sharedPreferences: SharedPreferences by lazy { (requireActivity() as MainActivity).sharedPreferences }
    private val core: Core by lazy { (requireActivity() as MainActivity).core }
    private val username: String by lazy { binding.imsiInputLayout.editText?.text.toString() }
    private val phoneNumber: String by lazy { binding.phoneNumberInputLayout.editText?.text.toString() }
    private val domain: String by lazy { binding.domainInputLayout.editText?.text.toString() }
    private val password: String by lazy { binding.passwordInputLayout.editText?.text.toString() }
    private val proxy: String by lazy { binding.proxyInputLayout.editText?.text.toString() }
    private val coreListener = object : CoreListenerStub() {

        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState,
            message: String
        ) {
            binding.registrationStateView.text = message
            if (state == RegistrationState.Failed || state == RegistrationState.Cleared) {
                binding.registrationButton.isEnabled = true
                binding.registrationStateView.text =
                    getString(R.string.linphone_registration_failed, message)
            } else if (state == RegistrationState.Ok) {
                router.openCall()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uncheckedBinding = AccountFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        initView()
    }

    private fun login() {
        saveAuthInfo()
        val authInfo = Factory.instance().createAuthInfo(
            username,
            "$username@$domain",
            password,
            null,
            null,
            "@$domain"
        )
        val identity = Factory.instance().createAddress("sip:$username@$domain")
        val address = Factory.instance().createAddress("sip:$proxy")?.apply {
            transport = transportType
        }
        val accountParams = core.createAccountParams().apply {
            identityAddress = identity
            serverAddress = address
            registerEnabled = true
        }
        val account = core.createAccount(accountParams).apply {
            patchProxy()
            addListener { _, state, message ->
                Log.i("[Account] Registration state changed: $state, $message")
            }
        }
        core.apply {
            addAuthInfo(authInfo)
            addAccount(account)
            defaultAccount = account
            addListener(coreListener)
            start()
        }
    }

    @SuppressLint("HardwareIds")
    private fun Account.patchProxy() {
        val deviceId = Secure.getString(requireActivity().contentResolver, Secure.ANDROID_ID)
        setCustomHeader("To", "sip:$username@$domain")
        setCustomHeader("From", "<sip:$username@$domain>;tag=~UwXzKOlD\n")
        setCustomHeader(
            "Authorization",
            "Digest realm=\"ims.mnc062.mcc250.3gppnetwork.org\", nonce=\"9b4c5fedc08296985b586acee1f16218\", algorithm=MD5, username=\""
                    + username
                    + "@ims.mnc062.mcc250.3gppnetwork.org\", uri=\"sip:ims.mnc062.mcc250.3gppnetwork.org\", response=\"365b94fb3ad933759f921d7d6d88d257\", cnonce=\"HQlmMYSNfKlp66nk\", nc=00000001, qop=auth"
        )
        setCustomHeader(
            "Contact",
            ("<sip:+"
                    + phoneNumber
                    + "@172.30.147.209:43530;transport=udp;pn-provider=tinkoff;"
                    + "pn-prid="
                    + deviceId
                    + ";>;gr=urn;+sip.instance=\"<urn:uuid:d1644492-1103-00f8-a4eb-c7a87d3b41f7>\"")
        )
    }

    private fun initView() {
        initAuthInfoViews()
        binding.root.setOnClickListener { clearFocus() }
        initTransportTypeDropDownView()
        binding.coreVersionView.text = getString(R.string.linphone_core_version, core.version)
        binding.registrationButton.setOnClickListener {
            login()
            it.isEnabled = false
            clearFocus()
        }
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
    }

    private fun saveAuthInfo() {
        sharedPreferences.edit {
            putString(PREF_USER_NAME, username)
            putString(PREF_PHONE_NUMBER, phoneNumber)
            putString(PREF_DOMAIN, domain)
            putString(PREF_PASSWORD, password)
            putString(PREF_PROXY, proxy)
        }
    }

    private fun initTransportTypeDropDownView() {
        val transportTypeSet = listOf("UDP", "TCP", "TLS")
        val adapter = ArrayAdapter(
            requireActivity(),
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
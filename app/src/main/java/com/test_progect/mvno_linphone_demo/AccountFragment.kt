package com.test_progect.mvno_linphone_demo

import android.os.Bundle
import android.provider.Settings.Secure
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.databinding.AccountFragmentBinding
import org.linphone.core.*
import org.linphone.core.tools.Log

fun createAccountFragment(): AccountFragment = AccountFragment()

class AccountFragment : Fragment() {

    private var _binding: AccountFragmentBinding? = null
    private val binding: AccountFragmentBinding
        get() = checkNotNull(_binding)

    private val core: Core get() = (requireActivity() as MainActivity).core

    private val username: String by lazy {
        binding.imsiInputLayout.editText?.text.toString()
    }
    private val domain: String by lazy {
        binding.domainInputLayout.editText?.text.toString()
    }

    private var transportType: TransportType = TransportType.Udp

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
            } else if (state == RegistrationState.Ok) {
                binding.logoutButton.isEnabled = true
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AccountFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        initView()
    }

    private fun login() {
        val password = binding.passwordInputLayout.editText?.text.toString()
        val proxy = binding.proxyInputLayout.editText?.text.toString()
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

    private fun unregister() {
        val account = core.defaultAccount ?: return
        core.apply {
            removeAccount(account)
            clearAccounts()
            clearAllAuthInfo()
        }
    }

    private fun Account.patchProxy() {
        val phoneNumber = binding.phoneNumberInputLayout.editText?.text.toString()
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
        binding.root.setOnClickListener { clearFocus() }
        initTransportTypeDropDownView()
        binding.coreVersionView.text = getString(R.string.linphone_core_version, core.version)
        binding.registrationButton.setOnClickListener {
            login()
            it.isEnabled = false
            clearFocus()
        }
        binding.logoutButton.setOnClickListener {
            unregister()
            it.isEnabled = false
            clearFocus()
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
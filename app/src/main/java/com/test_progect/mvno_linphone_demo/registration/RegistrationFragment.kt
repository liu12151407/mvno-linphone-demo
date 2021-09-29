package com.test_progect.mvno_linphone_demo.registration

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings.Secure
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.MainActivity
import com.test_progect.mvno_linphone_demo.Router
import com.test_progect.mvno_linphone_demo.databinding.RegistrationFragmentBinding
import com.test_progect.mvno_linphone_demo.validatePhoneNumber
import org.linphone.core.*
import org.linphone.core.tools.Log

class RegistrationFragment : Fragment(), RegistrationView.Presenter {

    private lateinit var view: RegistrationView
    private var uncheckedBinding: RegistrationFragmentBinding? = null
    private val binding: RegistrationFragmentBinding get() = checkNotNull(uncheckedBinding)
    private val router: Router by lazy { requireActivity() as Router }
    private val sharedPreferences: SharedPreferences by lazy { (requireActivity() as MainActivity).sharedPreferences }
    private val core: Core by lazy { (requireActivity() as MainActivity).core }
    private val coreListener = object : CoreListenerStub() {

        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState,
            message: String
        ) {
            view.setRegistrationStateMessage(message)
            if (state == RegistrationState.Failed || state == RegistrationState.Cleared) {
                view.setRegistrationFailedState(message)
            } else if (state == RegistrationState.Ok) {
                view.setRegistrationOkState(message)
                router.openAccount()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uncheckedBinding = RegistrationFragmentBinding.inflate(inflater, container, false)
        view = RegistrationViewImpl(binding, sharedPreferences, this, core.version)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        core.removeListener(coreListener)
    }

    override fun onRegistrationButtonClicked(
        username: String,
        phoneNumber: String,
        domain: String,
        password: String,
        proxy: String,
        transportType: TransportType
    ) {
        saveAuthInfo(username, phoneNumber, domain, password, proxy)
        val isPhoneNumberValid = validatePhoneNumber(phoneNumber, requireContext())
        if (isPhoneNumberValid) {
            val authInfo = Factory.instance().createAuthInfo(
                username,
                "$username@$domain",
                password,
                null,
                null,
                "@$domain"
            )
            val identity = Factory.instance().createAddress("sip:$username@$domain")
            val address = Factory.instance().createAddress("sip:$proxy")
            address?.transport = transportType
            val accountParams = core.createAccountParams().apply {
                identityAddress = identity
                serverAddress = address
                registerEnabled = true
            }
            val account = core.createAccount(accountParams)
            account.patchProxy(username, phoneNumber, domain)
            account.addListener { _, state, message ->
                Log.i("[Account] Registration state changed: $state, $message")
            }
            core.apply {
                addAuthInfo(authInfo)
                addAccount(account)
                defaultAccount = account
                addListener(coreListener)
                start()
            }
        }
    }

    private fun saveAuthInfo(
        username: String,
        phoneNumber: String,
        domain: String,
        password: String,
        proxy: String,
    ) {
        sharedPreferences.edit {
            putString(PREF_USER_NAME, username)
            putString(PREF_PHONE_NUMBER, phoneNumber)
            putString(PREF_DOMAIN, domain)
            putString(PREF_PASSWORD, password)
            putString(PREF_PROXY, proxy)
        }
    }


    @SuppressLint("HardwareIds")
    private fun Account.patchProxy(username: String, phoneNumber: String, domain: String) {
        val deviceId = Secure.getString(requireActivity().contentResolver, Secure.ANDROID_ID)
        setCustomHeader("To", "sip:$username@$domain")
        setCustomHeader("From", "<sip:$username@$domain>;tag=~UwXzKOlD\n")
        setCustomHeader(
            "Authorization",
            "Digest realm=\"$domain\", nonce=\"9b4c5fedc08296985b586acee1f16218\", algorithm=MD5, username=\"$username"
                    + "@$domain\", uri=\"sip:$domain\", response=\"365b94fb3ad933759f921d7d6d88d257\", cnonce=\"HQlmMYSNfKlp66nk\", nc=00000001, qop=auth"
        )
        setCustomHeader(
            "Contact",
            ("<sip:$phoneNumber@172.30.147.209:43530;"
                    + "transport=udp;pn-provider=tinkoff;pn-prid=$deviceId;>;"
                    + "gr=urn;+g.3gpp.smsip;+sip.instance=\"<urn:uuid:d1644492-1103-00f8-a4eb-c7a87d3b41f7>\"")
        )
    }

}
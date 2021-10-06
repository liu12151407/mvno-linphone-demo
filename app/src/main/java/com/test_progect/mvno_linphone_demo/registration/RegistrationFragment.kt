package com.test_progect.mvno_linphone_demo.registration

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.LinphoneManager
import com.test_progect.mvno_linphone_demo.MainActivity
import com.test_progect.mvno_linphone_demo.Router
import com.test_progect.mvno_linphone_demo.databinding.RegistrationFragmentBinding
import com.test_progect.mvno_linphone_demo.validatePhoneNumber
import org.linphone.core.*

class RegistrationFragment : Fragment(), RegistrationView.Presenter {

    private lateinit var view: RegistrationView
    private var uncheckedBinding: RegistrationFragmentBinding? = null
    private val binding: RegistrationFragmentBinding get() = checkNotNull(uncheckedBinding)
    private val router: Router by lazy { requireActivity() as Router }
    private val sharedPreferences: SharedPreferences by lazy { (requireActivity() as MainActivity).sharedPreferences }
    private val linphoneManager: LinphoneManager by lazy {
        (requireActivity() as MainActivity).linphoneManager
    }
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
        view = RegistrationViewImpl(binding, sharedPreferences, this, linphoneManager.core.version)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        linphoneManager.removeCoreListenerStub(coreListener)
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
        val isValid = validatePhoneNumber(phoneNumber)
        if (isValid.not()) {
            view.showInvalidPhoneNumberToast()
            return
        }
        linphoneManager.registerAccount(
            username,
            phoneNumber,
            domain,
            password,
            proxy,
            transportType,
            coreListener
        )
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

}
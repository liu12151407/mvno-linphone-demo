package com.test_progect.mvno_linphone_demo.registration

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.*
import com.test_progect.mvno_linphone_demo.core_ui.CoroutineJobDelegate
import com.test_progect.mvno_linphone_demo.core_ui.CoroutineJobDelegateImpl
import com.test_progect.mvno_linphone_demo.core_ui.registerPermissionRequestLauncher
import com.test_progect.mvno_linphone_demo.databinding.RegistrationFragmentBinding
import org.linphone.core.Account
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.RegistrationState
import ru.tcsbank.mvno.coroutines.onError
import ru.tcsbank.mvno.coroutines.onFinish
import ru.tcsbank.mvno.coroutines.onLaunch

class RegistrationFragment : Fragment(), RegistrationView.Presenter,
    CoroutineJobDelegate by CoroutineJobDelegateImpl() {

    private val requestLauncher = registerPermissionRequestLauncher(
        onPermissionsGranted = { register(checkNotNull(accountInfo)) },
        onPermissionDenied = { view.showLocationPermissionDenied() }
    )

    private lateinit var view: RegistrationView
    private var uncheckedBinding: RegistrationFragmentBinding? = null
    private val binding: RegistrationFragmentBinding get() = checkNotNull(uncheckedBinding)
    private val router: Router by lazy { requireActivity() as Router }
    private val sharedPreferences: SharedPreferences by lazy { (requireActivity() as MainActivity).sharedPreferences }
    private val linphoneManager: LinphoneManager by lazy {
        (requireActivity() as MainActivity).linphoneManager
    }
    private val locationProvider: LocationProvider by lazy {
        (requireActivity() as MainActivity).locationProvider
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

    private var location: Location? = null
    private var accountInfo: NoSimAccountInfo? = null

    override fun onRegistrationButtonClicked(accountInfo: NoSimAccountInfo) {
        this.accountInfo = accountInfo
        requestLauncher.launch(arrayOf(ACCESS_FINE_LOCATION))
    }

    private fun register(accountInfo: NoSimAccountInfo) {
        onLaunch { location = locationProvider.getLocation() }
            .onError { }
            .onFinish {
                accountInfo.apply {
                    saveAuthInfo(imsi, msisdn, domain, password, proxy)
                }
                accountInfo.validatePhoneNumber()
                if (location != null && accountInfo.validatePhoneNumber()) {
                    linphoneManager.registerAccount(
                        accountInfo,
                        checkNotNull(location),
                        coreListener
                    )
                }
            }
    }

    private fun NoSimAccountInfo.validatePhoneNumber(): Boolean {
        if (!validatePhoneNumber(msisdn)) {
            view.showInvalidPhoneNumberToast()
            return false
        }
        return true
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
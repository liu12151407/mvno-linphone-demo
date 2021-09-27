package com.test_progect.mvno_linphone_demo.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.MainActivity
import com.test_progect.mvno_linphone_demo.R
import com.test_progect.mvno_linphone_demo.Router
import com.test_progect.mvno_linphone_demo.databinding.CallFragmentBinding
import org.linphone.core.*

class CallFragment : Fragment(), CallView.Presenter {

    private lateinit var view: CallView
    private var uncheckedBinding: CallFragmentBinding? = null
    private val binding: CallFragmentBinding get() = checkNotNull(uncheckedBinding)
    private val router: Router by lazy { requireActivity() as Router }
    private val core: Core by lazy { (requireActivity() as MainActivity).core }
    private val coreListener = object : CoreListenerStub() {

        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState,
            message: String
        ) {
            when (state) {
                RegistrationState.Failed, RegistrationState.Cleared ->
                    view.setRegistrationFailedState(message)
                RegistrationState.Ok -> view.setRegistrationOkState(message)
                else -> {
                    // empty
                }
            }
        }

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            if (state == Call.State.IncomingReceived) {
                router.openIncomingCall()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uncheckedBinding = CallFragmentBinding.inflate(inflater, container, false)
        view = CallViewImpl(binding, this)
        core.addListener(coreListener)
        return binding.root
    }

    override fun onCallButtonCLicked(phoneNumber: String) {
        router.openOutgoingCall(phoneNumber)
    }

    override fun onMenuItemClicked(item: MenuItem): Boolean {
        if (item.itemId == R.id.logoutMenuItem) {
            core.defaultAccount?.let { account ->
                core.removeAccount(account)
                core.clearAccounts()
                core.clearAllAuthInfo()
            }
            router.openAccount()
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        core.removeListener(coreListener)
    }

}
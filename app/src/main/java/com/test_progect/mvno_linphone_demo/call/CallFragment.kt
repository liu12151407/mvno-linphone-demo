package com.test_progect.mvno_linphone_demo.call

import android.Manifest.permission.RECORD_AUDIO
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.test_progect.mvno_linphone_demo.MainActivity
import com.test_progect.mvno_linphone_demo.R
import com.test_progect.mvno_linphone_demo.call.incoming_call.IncomingCallFragment
import com.test_progect.mvno_linphone_demo.call.outgoing_call.createOutgoingCallFragment
import com.test_progect.mvno_linphone_demo.databinding.CallFragmentBinding
import com.test_progect.mvno_linphone_demo.validatePhoneNumber
import org.linphone.core.*


const val PREF_LAST_OUTGOING_CAL = "PREF_LAST_OUTGOING_CAL"

class CallFragment : Fragment(), CallView.Presenter, CallRouter {

    private lateinit var view: CallView
    private var phoneNumber: String = ""
    private var uncheckedBinding: CallFragmentBinding? = null
    private val binding: CallFragmentBinding get() = checkNotNull(uncheckedBinding)
    private val sharedPreferences: SharedPreferences get() = (requireActivity() as MainActivity).sharedPreferences
    private val core: Core by lazy { (requireActivity() as MainActivity).core }
    private val coreListener = object : CoreListenerStub() {

        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState,
            message: String
        ) {
            val normalizedMessage = when (state) {
                RegistrationState.Failed, RegistrationState.Cleared ->
                    requireContext().getString(R.string.linphone_registration_failed, message)
                RegistrationState.Ok ->
                    requireContext().getString(R.string.linphone_registration_ok)
                else -> message
            }
            view.setRegistrationState(normalizedMessage)
        }

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            if (state == Call.State.IncomingReceived) {
                openIncomingCall()
            }
        }
    }
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openOutgoingCall(phoneNumber)
            } else {
                if (shouldShowRequestPermissionRationale(RECORD_AUDIO)) {
                    showAlertDialog(R.string.microphone_permission_request_rational)
                } else {
                    showAlertDialog(R.string.on_microphone_permission_denied_message)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // empty
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uncheckedBinding = CallFragmentBinding.inflate(inflater, container, false)
        view = CallViewImpl(binding, this, sharedPreferences)
        core.addListener(coreListener)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        core.removeListener(coreListener)
    }

    override fun onCallButtonCLicked(phoneNumber: String) {
        val isPhoneNumberValid = validatePhoneNumber(phoneNumber, requireContext())
        if (isPhoneNumberValid) {
            this.phoneNumber = phoneNumber
            sharedPreferences.edit { putString(PREF_LAST_OUTGOING_CAL, phoneNumber) }
            if (
                checkSelfPermission(requireContext(), RECORD_AUDIO) == PERMISSION_GRANTED
            ) {
                openOutgoingCall(phoneNumber)
            } else {
                activityResultLauncher.launch(RECORD_AUDIO)
            }
        }
    }

    override fun openOutgoingCall(phoneNumber: String) {
        childFragmentManager.commit {
            replace(binding.callsContainer.id, createOutgoingCallFragment(phoneNumber))
        }
    }

    override fun openIncomingCall() {
        childFragmentManager.commit {
            replace(binding.root.id, IncomingCallFragment())
        }
    }

    override fun closeChildFragment(fragment: Fragment) {
        childFragmentManager.commit { remove(fragment) }
    }

    private fun showAlertDialog(@StringRes message: Int) {
        val showRationale = shouldShowRequestPermissionRationale(RECORD_AUDIO)
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("Ok") { dialog, _ ->
                if (showRationale) {
                    activityResultLauncher.launch(RECORD_AUDIO)
                } else {
                    openPermissionSettings()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Dismiss") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openPermissionSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

}
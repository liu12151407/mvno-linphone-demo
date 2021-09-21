package com.test_progect.mvno_linphone_demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.databinding.CallFragmentBinding
import org.linphone.core.*

class CallFragment : Fragment() {

    private var uncheckedBinding: CallFragmentBinding? = null
    private val binding: CallFragmentBinding get() = checkNotNull(uncheckedBinding)
    private val router: Router by lazy { requireActivity() as Router }
    private val core: Core by lazy { (requireActivity() as MainActivity).core }
    private val coreListener = object : CoreListenerStub() {

        override fun onAudioDeviceChanged(core: Core, audioDevice: AudioDevice) {
            // This callback will be triggered when a successful audio device has been changed
        }

        override fun onAudioDevicesListUpdated(core: Core) {
            // This callback will be triggered when the available devices list has changed,
            // for example after a bluetooth headset has been connected/disconnected.
        }

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            binding.callStatusView.text = message
            when (state) {
                Call.State.IncomingReceived -> {
                    binding.apply {
                        callEndButton.isEnabled = true
                        callButton.isEnabled = true
//                        findViewById<EditText>(R.id.remote_address).setText(call.remoteAddress.asStringUriOnly())
                    }
                }
                Call.State.Connected -> {
                    binding.apply {
                        micButton.isEnabled = true
                        speakerButton.isEnabled = true
                    }
                }
                Call.State.Released -> {
                    binding.apply {
                        callEndButton.isEnabled = false
                        callButton.isEnabled = false
                        micButton.isEnabled = false
                        speakerButton.isEnabled = false
//                    findViewById<EditText>(R.id.remote_address).text.clear()
                    }
                }
                else -> {
                    // empty
                }
            }
        }

        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState,
            message: String
        ) {
            when (state) {
                RegistrationState.Failed, RegistrationState.Cleared -> {
                    binding.registrationStateView.text =
                        getString(R.string.linphone_registration_failed, message)
                }
                RegistrationState.Ok -> {
                    binding.registrationStateView.text =
                        getString(R.string.linphone_registration_ok)
                }
                else -> {
                    // empty
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uncheckedBinding = CallFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        core.addListener(coreListener)
        initView()
    }

    private fun toggleSpeaker() {
        val currentAudioDevice = core.currentCall?.outputAudioDevice
        val speakerEnabled = currentAudioDevice?.type == AudioDevice.Type.Speaker
        for (audioDevice in core.audioDevices) {
            if (speakerEnabled && audioDevice.type == AudioDevice.Type.Earpiece) {
                core.currentCall?.outputAudioDevice = audioDevice
                return
            } else if (!speakerEnabled && audioDevice.type == AudioDevice.Type.Speaker) {
                core.currentCall?.outputAudioDevice = audioDevice
                return
            } else if (audioDevice.type == AudioDevice.Type.Bluetooth) {
                core.currentCall?.outputAudioDevice = audioDevice
            }
        }
    }


    private fun initView() {
        initToolbar()
        binding.apply {
            registrationStateView.text = getString(R.string.linphone_registration_ok)
            callEndButton.isEnabled = false
            callButton.isEnabled = false
            micButton.isEnabled = false
            speakerButton.isEnabled = false
            callEndButton.setOnClickListener {
                core.currentCall?.terminate()
            }
            callButton.setOnClickListener {
                core.currentCall?.accept()
            }
            micButton.setOnClickListener {
                core.enableMic(!core.micEnabled())
            }
            speakerButton.setOnClickListener {
                toggleSpeaker()
            }
        }
    }

    private fun initToolbar() {
        binding.toolbar.apply {
            inflateMenu(R.menu.call_menu)
            setOnMenuItemClickListener {
                if (it.itemId == R.id.logoutMenuItem) {
                    core.defaultAccount?.let { account ->
                        core.removeAccount(account)
                        core.clearAccounts()
                        core.clearAllAuthInfo()
                    }
                    router.openAccount()
                }
                true
            }
        }
    }

}
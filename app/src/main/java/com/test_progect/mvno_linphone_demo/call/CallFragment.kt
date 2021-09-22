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
            view.onCallStateChanged(state, message)
        }

        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState,
            message: String
        ) {
            view.onRegistrationStateChanged(state, message)
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

    override fun onCallButtonClicked() {
        core.currentCall?.accept()
    }

    override fun onCallEndButtonClicked() {
        core.currentCall?.terminate()
    }

    override fun onSpeakerButtonClicked() {
        val currentAudioDevice = core.currentCall?.outputAudioDevice
        val speakerEnabled = currentAudioDevice?.type == AudioDevice.Type.Speaker
        for (audioDevice in core.audioDevices) {
            if (speakerEnabled && audioDevice.type == AudioDevice.Type.Earpiece) {
                view.setSpeakerIcon(R.drawable.ic_speaker_off)
                core.currentCall?.outputAudioDevice = audioDevice
                return
            } else if (!speakerEnabled && audioDevice.type == AudioDevice.Type.Speaker) {
                view.setSpeakerIcon(R.drawable.ic_speaker)
                core.currentCall?.outputAudioDevice = audioDevice
                return
            }
        }
    }

    override fun onMicButtonClicked() {
        core.enableMic(!core.micEnabled())
        when (core.micEnabled()) {
            true -> view.setMicIcon(R.drawable.ic_mic)
            false -> view.setMicIcon(R.drawable.ic_mic_off)
        }
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

}
package com.test_progect.mvno_linphone_demo.outgoing_call

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.MainActivity
import com.test_progect.mvno_linphone_demo.R
import com.test_progect.mvno_linphone_demo.Router
import com.test_progect.mvno_linphone_demo.databinding.OutgouingCallFragmentBinding
import org.linphone.core.*

private const val ARG_PHONE_NUMBER = "ARG_PHONE_NUMBER"

fun createOutgoingCallFragment(phone: String): OutgoingCallFragment =
    OutgoingCallFragment().apply {
        arguments = Bundle().apply {
            putString(ARG_PHONE_NUMBER, phone)
        }
    }

class OutgoingCallFragment : Fragment(), OutgoingCallView.Presenter {

    private lateinit var view: OutgoingCallView
    private var uncheckedBinding: OutgouingCallFragmentBinding? = null
    private val binding: OutgouingCallFragmentBinding get() = checkNotNull(uncheckedBinding)
    private val router: Router by lazy { requireActivity() as Router }
    private val core: Core by lazy { (requireActivity() as MainActivity).core }
    private val sharedPreferences: SharedPreferences by lazy { (requireActivity() as MainActivity).sharedPreferences }
    private val coreListener = object : CoreListenerStub() {

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            view.setOutgoingCallState(state, message)
            when (state) {
                Call.State.OutgoingInit -> {
                    // First state an outgoing call will go through
                    Log.d("ImsDebug", "OutgoingInit")
                }
                Call.State.OutgoingProgress -> {
                    // Right after outgoing init
                    Log.d("ImsDebug", "OutgoingProgress")
                }
                Call.State.OutgoingRinging -> {
                    // This state will be reached upon reception of the 180 RINGING
                    Log.d("ImsDebug", "OutgoingRinging")
                }
                Call.State.Connected -> {
                    Log.d("ImsDebug", "OutgoingRinging")
                    // When the 200 OK has been received
                }
                Call.State.StreamsRunning -> {
                    // This state indicates the call is active.
                    // You may reach this state multiple times, for example after a pause/resume
                    // or after the ICE negotiation completes
                    // Wait for the call to be connected before allowing a call update
                    view.enableMicAndSpeaker()
                }
                Call.State.Released -> view.disableButtons()
                else -> {
                    // empty
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uncheckedBinding = OutgouingCallFragmentBinding.inflate(inflater, container, false)
        core.addListener(coreListener)
        view = OutgoingCallViewImpl(binding, this)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        arguments?.getString(ARG_PHONE_NUMBER)?.let {
            outgoingCall(it)
        } ?: router.openCall()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        core.removeListener(coreListener)
    }

    override fun onCallEndButtonClicked() {
        view.disableButtons()
        Log.d("ImsDebug", "onCallEndButtonClicked -> currentCall=${core.currentCall}")
        checkNotNull(core.currentCall).terminate()
//        router.openCall()
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

    private fun outgoingCall(phoneNumber: String) {
        Log.d("ImsDebug", "outgoingCall -> phoneNumber=${phoneNumber}")
        val remoteAddress = core.interpretUrl(phoneNumber) ?: return
        val params: CallParams = core.createCallParams(null) ?: return
        val pAssociatedURI = core.defaultAccount?.getCustomHeader("P-Associated-URI")
        core.defaultAccount?.getCustomHeader("Contact")?.let {
            params.addCustomHeader("Contact", it)
        }
        core.defaultAccount?.getCustomHeader("From")?.let {
            params.addCustomHeader("From", it)
        }
        params.addCustomHeader("P-Associated-URI", pAssociatedURI)
        params.mediaEncryption = MediaEncryption.None
        core.inviteAddressWithParams(remoteAddress, params)
    }

}
package com.test_progect.mvno_linphone_demo.call.outgoing_call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.MainActivity
import com.test_progect.mvno_linphone_demo.R
import com.test_progect.mvno_linphone_demo.call.CallRouter
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
    private val router: CallRouter by lazy { parentFragment as CallRouter }
    private val core: Core by lazy { (requireActivity() as MainActivity).core }
    private val coreListener = object : CoreListenerStub() {

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            view.setOutgoingCallState(message)
            when (state) {
                Call.State.OutgoingInit -> {
                    // First state an outgoing call will go through
                }
                Call.State.OutgoingProgress -> {
                    // Right after outgoing init
                }
                Call.State.OutgoingRinging -> {
                    // This state will be reached upon reception of the 180 RINGING
                }
                Call.State.Connected -> {
                    // When the 200 OK has been received
                }
                Call.State.StreamsRunning -> {
                    // This state indicates the call is active.
                    // You may reach this state multiple times, for example after a pause/resume
                    // or after the ICE negotiation completes
                    // Wait for the call to be connected before allowing a call update
                    view.enableMicAndSpeaker()
                }
                Call.State.Released -> {
                    view.disableButtons()
                    router.closeChildFragment(this@OutgoingCallFragment)
                }
                else -> {
                    // empty
                }
            }
        }

    }
    private var call: Call? = null

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
        val phoneNumber = requireArguments().getString(ARG_PHONE_NUMBER)
        outgoingCall(checkNotNull(phoneNumber))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        core.removeListener(coreListener)
        call?.terminate()
    }

    override fun onCallEndButtonClicked() {
        view.disableButtons()
        checkNotNull(core.currentCall).terminate()
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
        val remoteAddress = core.interpretUrl(phoneNumber) ?: return
        val account = checkNotNull(core.defaultAccount)
        val pAssociatedURI = account.getCustomHeader("P-Associated-URI")
        val contactHeader = account.getCustomHeader("Contact")
        val fromHeader = account.getCustomHeader("From")
        val params: CallParams = core.createCallParams(null)?.apply {
            addCustomHeader("Contact", contactHeader)
            addCustomHeader("From", fromHeader)
            addCustomHeader("P-Associated-URI", pAssociatedURI)
            mediaEncryption = MediaEncryption.None
            enableAudio(true)
        } ?: return
        core.inviteAddressWithParams(remoteAddress, params).also { call = it }
    }

}
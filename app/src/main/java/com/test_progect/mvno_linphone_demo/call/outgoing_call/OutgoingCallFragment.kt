package com.test_progect.mvno_linphone_demo.call.outgoing_call

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.LinphoneManager
import com.test_progect.mvno_linphone_demo.LocationProvider
import com.test_progect.mvno_linphone_demo.MainActivity
import com.test_progect.mvno_linphone_demo.R
import com.test_progect.mvno_linphone_demo.call.CallRouter
import com.test_progect.mvno_linphone_demo.core_ui.CoroutineJobDelegate
import com.test_progect.mvno_linphone_demo.core_ui.CoroutineJobDelegateImpl
import com.test_progect.mvno_linphone_demo.databinding.OutgouingCallFragmentBinding
import org.linphone.core.AudioDevice
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import ru.tcsbank.mvno.coroutines.onError
import ru.tcsbank.mvno.coroutines.onFinish
import ru.tcsbank.mvno.coroutines.onLaunch

private const val ARG_PHONE_NUMBER = "ARG_PHONE_NUMBER"

fun createOutgoingCallFragment(phone: String): OutgoingCallFragment =
    OutgoingCallFragment().apply {
        arguments = Bundle().apply {
            putString(ARG_PHONE_NUMBER, phone)
        }
    }

class OutgoingCallFragment : Fragment(), OutgoingCallView.Presenter,
    CoroutineJobDelegate by CoroutineJobDelegateImpl() {

    private lateinit var view: OutgoingCallView
    private var uncheckedBinding: OutgouingCallFragmentBinding? = null
    private val binding: OutgouingCallFragmentBinding get() = checkNotNull(uncheckedBinding)
    private val router: CallRouter by lazy { parentFragment as CallRouter }
    private val linphoneManager: LinphoneManager by lazy {
        (requireActivity() as MainActivity).linphoneManager
    }
    private val locationProvider: LocationProvider by lazy {
        (requireActivity() as MainActivity).locationProvider
    }
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
        initializeCoroutineJob()
        uncheckedBinding = OutgouingCallFragmentBinding.inflate(inflater, container, false)
        linphoneManager.addCoreListenerStub(coreListener)
        view = OutgoingCallViewImpl(binding, this)
        return binding.root
    }

    private var location: Location? = null

    override fun onStart() {
        super.onStart()
        val phoneNumber = checkNotNull(requireArguments().getString(ARG_PHONE_NUMBER))
        onLaunch { location = locationProvider.getLocation() }
            .onError {
                // TODO MVNO-15956
            }
            .onFinish {
                if (location != null) linphoneManager.call(phoneNumber, checkNotNull(location))
            }
    }

    override fun onDestroyView() {
        cancelCoroutineJob()
        linphoneManager.removeCoreListenerStub(coreListener)
        call?.terminate()
        super.onDestroyView()
    }

    override fun onCallEndButtonClicked() {
        view.disableButtons()
        linphoneManager.terminateCall()
    }

    override fun onSpeakerButtonClicked() {
        val core = linphoneManager.core
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

    override fun onMuteButtonClicked() {
        val core = linphoneManager.core
        core.enableMic(!core.micEnabled())
        when (core.micEnabled()) {
            true -> view.setMuteIcon(R.drawable.ic_mic)
            false -> view.setMuteIcon(R.drawable.ic_mic_off)
        }
    }

}
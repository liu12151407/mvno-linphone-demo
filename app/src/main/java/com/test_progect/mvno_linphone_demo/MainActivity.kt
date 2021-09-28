package com.test_progect.mvno_linphone_demo

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.test_progect.mvno_linphone_demo.account.AccountFragment
import com.test_progect.mvno_linphone_demo.call.CallFragment
import com.test_progect.mvno_linphone_demo.databinding.ActivityMainBinding
import com.test_progect.mvno_linphone_demo.incoming_call.IncomingCallFragment
import com.test_progect.mvno_linphone_demo.outgoing_call.OutgoingCallFragment
import com.test_progect.mvno_linphone_demo.outgoing_call.createOutgoingCallFragment
import org.linphone.core.Core
import org.linphone.core.Factory

class MainActivity : AppCompatActivity(), Router {

    val sharedPreferences: SharedPreferences
        get() = getSharedPreferences(packageName, MODE_PRIVATE)
    val core: Core by lazy {
        Factory.instance().createCore(null, null, this)
    }
    private val binding: ActivityMainBinding get() = checkNotNull(uncheckedBinding)
    private var uncheckedBinding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uncheckedBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Factory.instance().setDebugMode(true, "MvnoLinphone")
    }

    override fun onStart() {
        super.onStart()
        if (supportFragmentManager.fragments.isEmpty()) {
            openAccount()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentByTag(OutgoingCallFragment::class.java.name) == null) {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun openAccount() {
        supportFragmentManager.commit {
            replace(
                binding.fragmentContainer.id,
                AccountFragment(),
                AccountFragment::class.java.name
            )
        }
    }

    override fun openCall() {
        val callFragment = findCallFragment()
        if (callFragment == null) {
            supportFragmentManager.commit {
                replace(
                    binding.fragmentContainer.id,
                    CallFragment(),
                    CallFragment::class.java.name
                )
            }
        } else {
            val fragment = checkNotNull(findOutgoingOrIncomingCallFragment())
            supportFragmentManager.commit {
                remove(fragment)
                show(callFragment)
            }
        }
    }

    override fun openOutgoingCall(phoneNumber: String) {
        val fragment = checkNotNull(findCallFragment())
        supportFragmentManager.commit {
            hide(fragment)
            add(
                binding.fragmentContainer.id,
                createOutgoingCallFragment(phoneNumber),
                OutgoingCallFragment::class.java.name
            )
        }
    }

    override fun openIncomingCall() {
        val fragment = checkNotNull(findCallFragment())
        supportFragmentManager.commit {
            hide(fragment)
            add(
                binding.fragmentContainer.id,
                IncomingCallFragment(),
                IncomingCallFragment::class.java.name
            )
        }
    }

    private fun findCallFragment(): Fragment? =
        supportFragmentManager.findFragmentByTag(CallFragment::class.java.name)

    private fun findOutgoingOrIncomingCallFragment(): Fragment? =
        supportFragmentManager.run {
            findFragmentByTag(OutgoingCallFragment::class.java.name) ?: findFragmentByTag(
                IncomingCallFragment::class.java.name
            )
        }

}
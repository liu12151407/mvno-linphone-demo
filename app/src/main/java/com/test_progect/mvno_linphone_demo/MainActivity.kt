package com.test_progect.mvno_linphone_demo

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.test_progect.mvno_linphone_demo.account.AccountFragment
import com.test_progect.mvno_linphone_demo.databinding.MainActivityBinding
import com.test_progect.mvno_linphone_demo.registration.RegistrationFragment
import org.linphone.core.Core
import org.linphone.core.Factory

class MainActivity : AppCompatActivity(), Router {

    val sharedPreferences: SharedPreferences
        get() = getSharedPreferences(packageName, MODE_PRIVATE)
    val core: Core by lazy {
        Factory.instance().createCore(null, null, this)
    }
    private val binding: MainActivityBinding get() = checkNotNull(uncheckedBinding)
    private var uncheckedBinding: MainActivityBinding? = null
    private var isLaunched: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uncheckedBinding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Factory.instance().setDebugMode(true, "MvnoLinphone")

    }

    override fun onStart() {
        super.onStart()
        if (!isLaunched) {
            isLaunched = true
            openRegistration()
        }
    }

    override fun openRegistration() {
        supportFragmentManager.commit {
            replace(binding.root.id, RegistrationFragment())
        }
    }

    override fun openAccount() {
        supportFragmentManager.commit {
            replace(binding.root.id, AccountFragment())
        }
    }

}
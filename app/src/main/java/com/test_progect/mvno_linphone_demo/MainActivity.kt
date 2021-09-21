package com.test_progect.mvno_linphone_demo

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.test_progect.mvno_linphone_demo.databinding.ActivityMainBinding
import org.linphone.core.Core
import org.linphone.core.Factory

class MainActivity : AppCompatActivity(), Router {

    lateinit var core: Core
    val sharedPreferences: SharedPreferences
        get() = getSharedPreferences(packageName, MODE_PRIVATE)
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = Factory.instance()
        factory.setDebugMode(true, "Hello Linphone")
        core = factory.createCore(null, null, this)
    }

    override fun onStart() {
        super.onStart()
        if (supportFragmentManager.fragments.isEmpty()) {
            openAccount()
        }
    }

    override fun openAccount() {
        supportFragmentManager.commit {
            replace(binding.fragmentContainer.id, AccountFragment())
        }
    }

    override fun openCall() {
        supportFragmentManager.commit {
            replace(binding.fragmentContainer.id, CallFragment())
        }
    }

}
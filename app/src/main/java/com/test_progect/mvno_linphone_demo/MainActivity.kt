package com.test_progect.mvno_linphone_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.test_progect.mvno_linphone_demo.databinding.ActivityMainBinding
import org.linphone.core.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var core: Core

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
        openAccountFragment()
    }

    private fun openAccountFragment() {
        val fragmentNotExists = supportFragmentManager.fragments.isEmpty()
        if (fragmentNotExists) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(binding.fragmentContainer.id, createAccountFragment())
            transaction.commit()
        }
    }

}
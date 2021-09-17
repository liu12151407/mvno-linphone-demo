package com.test_progect.mvno_linphone_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.test_progect.mvno_linphone_demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        openAccountFragment()
    }

    private fun openAccountFragment() {
        val fragmentNotExists = supportFragmentManager.fragments.isEmpty()
        if (fragmentNotExists) {
            val fragment = AccountFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(binding.fragmentContainer.id, fragment)
            transaction.commit()
        }
    }
}
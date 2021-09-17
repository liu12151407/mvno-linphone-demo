package com.test_progect.mvno_linphone_demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.databinding.AccountFragmentBinding

class AccountFragment : Fragment() {

    private var _binding: AccountFragmentBinding? = null
    private val binding: AccountFragmentBinding
        get() = checkNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AccountFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.registrationButton.setOnClickListener {

        }
    }
}
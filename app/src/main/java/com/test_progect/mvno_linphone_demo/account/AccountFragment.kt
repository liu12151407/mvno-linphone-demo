package com.test_progect.mvno_linphone_demo.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.test_progect.mvno_linphone_demo.*
import com.test_progect.mvno_linphone_demo.call.CallFragment
import com.test_progect.mvno_linphone_demo.chat.ChatFragment
import com.test_progect.mvno_linphone_demo.databinding.AccountFragmentBinding

class AccountFragment : Fragment() {

    private var uncheckedBinding: AccountFragmentBinding? = null
    private val binding: AccountFragmentBinding get() = checkNotNull(uncheckedBinding)
    private val router: Router by lazy { requireActivity() as Router }
    private val linphoneManager: LinphoneManager by lazy {
        (requireActivity() as MainActivity).linphoneManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uncheckedBinding = AccountFragmentBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if (childFragmentManager.fragments.isEmpty()) {
            openCall()
        }
    }

    private fun openCall() {
        val transaction = childFragmentManager.beginTransaction()
        val fragment = childFragmentManager.findFragmentByTag(CALL_TAB_TAG)
        if (fragment == null) {
            transaction.add(binding.accountContent.id, CallFragment(), CALL_TAB_TAG)
        } else {
            transaction.show(fragment)
        }
        childFragmentManager.fragments.find { it.tag != CALL_TAB_TAG }?.let {
            transaction.hide(it)
        }
        transaction.commit()
    }

    private fun openChat() {
        val transaction = childFragmentManager.beginTransaction()
        val fragment = childFragmentManager.findFragmentByTag(CHAT_TAB_TAG)
        if (fragment == null) {
            transaction.add(binding.accountContent.id, ChatFragment(), CHAT_TAB_TAG)
        } else {
            transaction.show(fragment)
        }
        childFragmentManager.fragments.find { it.tag != CHAT_TAB_TAG }?.let {
            transaction.hide(it)
        }
        transaction.commit()
    }

    private fun initView() {
        binding.toolbar.apply {
            inflateMenu(R.menu.call_menu)
            setOnMenuItemClickListener {
                onMenuItemClicked(it)
            }
        }
        binding.bottomNavigationBar.setOnItemSelectedListener {
            binding.root.hideKeyboard()
            when (it.itemId) {
                R.id.callTab -> {
                    setToolbarTitle(R.string.call_screen_title)
                    openCall()
                }
                R.id.chatTab -> {
                    setToolbarTitle(R.string.chat_screen_title)
                    openChat()
                }
            }
            true
        }
    }

    private fun setToolbarTitle(@StringRes title: Int) {
        binding.toolbarTitle.setText(title)
    }

    private fun onMenuItemClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logoutMenuItem -> {
                linphoneManager.logoutAccount()
                router.openRegistration()
            }
            R.id.appVersionInfoMenuItem -> {
                val appVersion = BuildConfig.VERSION_NAME
                AlertDialog.Builder(requireContext())
                    .setMessage("App version: $appVersion")
                    .setPositiveButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
        return true
    }

    companion object {
        private val CALL_TAB_TAG = CallFragment::class.java.name
        private val CHAT_TAB_TAG = ChatFragment::class.java.name
    }

}
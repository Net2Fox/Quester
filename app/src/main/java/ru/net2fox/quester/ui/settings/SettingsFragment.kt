package ru.net2fox.quester.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.net2fox.quester.R

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsViewModel = ViewModelProvider(this, SettingsViewModelFactory()).get(SettingsViewModel::class.java)

        settingsViewModel.settingsProgressResetResult.observe(viewLifecycleOwner,
            Observer { settingsResult ->
                settingsResult.error?.let {
                    showToast(it)
                }
                settingsResult.success?.let {
                    showToast(it)
                    updateUI(true)
                }
            })

        settingsViewModel.settingsDeleteAccountResult.observe(viewLifecycleOwner,
            Observer { settingsResult ->
                settingsResult.error?.let {
                    showToast(it)
                }
                settingsResult.success?.let {
                    showToast(it)
                    updateUI()
                }
            })

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val progressResetPreference = findPreference<Preference>("reset")
        val deleteAccountPreference = findPreference<Preference>("delete")
        val signOutPreference = findPreference<Preference>("signout")

        progressResetPreference?.setOnPreferenceClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                settingsViewModel.progressReset()
            }
            true
        }

        deleteAccountPreference?.setOnPreferenceClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                settingsViewModel.deleteAccount()
            }
            true
        }

        signOutPreference?.setOnPreferenceClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                settingsViewModel.signOut()
            }
            updateUI()
            true
        }
    }

    private fun updateUI(back: Boolean = false) {
        if (back){
            findNavController().popBackStack()
        } else {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToSignInFragment())
        }

    }

    private fun showToast(@StringRes messageString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, messageString, Toast.LENGTH_LONG).show()
    }
}
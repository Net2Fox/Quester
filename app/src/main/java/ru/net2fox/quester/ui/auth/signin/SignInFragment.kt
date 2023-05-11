package ru.net2fox.quester.ui.auth.signin

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.net2fox.quester.Quester
import ru.net2fox.quester.databinding.FragmentSignInBinding
import ru.net2fox.quester.R

class SignInFragment : Fragment() {

    private lateinit var signInViewModel: SignInViewModel
    private var _binding: FragmentSignInBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signInViewModel = ViewModelProvider(this, SignInViewModelFactory()).get(SignInViewModel::class.java)

        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val signInButton = binding.signIn
        val signUpButton = binding.signUp
        val loadingProgressBar = binding.loading

        signInViewModel.signInFormState.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                loginFormState ?: return@Observer
                signInButton.isEnabled = loginFormState.isDataValid
                loginFormState.usernameError?.let {
                    //usernameEditText.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    //passwordEditText.error = getString(it)
                }
            })

        signInViewModel.signInResult.observe(viewLifecycleOwner,
            Observer { signInResult ->
                signInResult ?: return@Observer
                loadingProgressBar.visibility = View.GONE
                signInResult.error?.let {
                    showLoginFailed(it)
                }
                signInResult.success?.let {
                    updateUI()
                }
            })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                signInViewModel.signInDataChanged(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                lifecycleScope.launch(Dispatchers.IO) {
                    signInViewModel.signIn(
                        usernameEditText.text.toString(),
                        passwordEditText.text.toString()
                    )
                }
            }
            false
        }

        signInButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            lifecycleScope.launch(Dispatchers.IO) {
                signInViewModel.signIn(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }
        signUpButton.setOnClickListener {
            findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToSignUpFragment())
        }
    }

    private fun updateUI() {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, R.string.sign_in_complete, Toast.LENGTH_LONG).show()
        //val activity = requireActivity() as AppCompatActivity
        //val fragmentManager = parentFragmentManager//activity.supportFragmentManager
        //fragmentManager.beginTransaction()
        //    .replace(R.id.nav_host_fragment_content_main, CharacterFragment())
        //    .commit()
        //findNavController().popBackStack()
        findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToCharacterFragment())
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
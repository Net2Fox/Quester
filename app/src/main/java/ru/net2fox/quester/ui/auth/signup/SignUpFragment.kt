package ru.net2fox.quester.ui.auth.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.net2fox.quester.R
import ru.net2fox.quester.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {

    private lateinit var signUpViewModel: SignUpViewModel
    private var _binding: FragmentSignUpBinding? = null

    // Это свойство действует только между onCreateView и
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signUpViewModel = ViewModelProvider(this, SignUpViewModelFactory())[SignUpViewModel::class.java]

        val usernameEditText = binding.username
        val emailEditText = binding.email
        val passwordEditText = binding.password
        val passwordConfirmEditText = binding.passwordConfirm
        val signUpButton = binding.signUp
        val loadingProgressBar = binding.loading

        signUpViewModel.signUpFormState.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                signUpButton.isEnabled = loginFormState.isDataValid
                loginFormState.usernameError?.let {
                }
                loginFormState.passwordError?.let {
                }
            })

        signUpViewModel.signUpResult.observe(viewLifecycleOwner,
            Observer { signUpResult ->
                signUpResult ?: return@Observer
                loadingProgressBar.visibility = View.GONE
                signUpResult.error?.let {
                    showSignUpFailed(it)
                }
                signUpResult.success?.let {
                    updateUI()
                }
            })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Игнорируем
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Игнорируем
            }

            override fun afterTextChanged(s: Editable) {
                signUpViewModel.signUpDataChanged(
                    usernameEditText.text.toString(),
                    emailEditText.text.toString(),
                    passwordEditText.text.toString(),
                    passwordConfirmEditText.text.toString()
                )
            }
        }
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        emailEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        passwordConfirmEditText.addTextChangedListener(afterTextChangedListener)

        passwordConfirmEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loadingProgressBar.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    signUpViewModel.signUp(
                        usernameEditText.text.toString(),
                        emailEditText.text.toString(),
                        passwordEditText.text.toString()
                    )
                }
            }
            false
        }

        signUpButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            lifecycleScope.launch(Dispatchers.IO) {
                signUpViewModel.signUp(
                    usernameEditText.text.toString(),
                    emailEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }
    }

    private fun updateUI() {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, R.string.sign_up_complete, Toast.LENGTH_LONG).show()
        findNavController().popBackStack()
    }

    private fun showSignUpFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
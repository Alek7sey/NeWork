package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentLoginBinding
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.LoginViewModel

@AndroidEntryPoint
class LoginFragment: Fragment() {
    private val viewModel: LoginViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentLoginBinding.inflate(layoutInflater, container, false)

        binding.loginBtn.setOnClickListener {
          //  AndroidUtils.hideKeyboard(requireView())
            val userName = binding.loginTextField.editText?.text.toString()
            val userPassword = binding.passwordTextField.editText?.text.toString()
            if (userName.isBlank() || userPassword.isBlank()) {
                Snackbar.make(binding.root, getString(R.string.login_or_password_is_empty), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            viewModel.saveIdAndToken(userName, userPassword)
            authViewModel.state.observe(viewLifecycleOwner) {
                if (authViewModel.authorized) {
                    findNavController().navigateUp()
                }
            }
        }
        return binding.root
    }
}
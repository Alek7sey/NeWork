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
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentLoginBinding
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.LoginViewModel
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: LoginViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentLoginBinding.inflate(inflater, container, false)

        val toolbar = binding.toolbarLogin.toolbarSignIn

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.loginBtn.setOnClickListener {
            val userName = binding.loginTextField.editText?.text.toString()
            val userPassword = binding.passwordTextField.editText?.text.toString()
            if (userName.isBlank() || userPassword.isBlank()) {
                Snackbar.make(binding.root, getString(R.string.login_or_password_is_empty), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            viewModel.saveIdAndToken(userName, userPassword)

            authViewModel.data.observe(viewLifecycleOwner) {
                if (authViewModel.authenticated) {
                    findNavController().navigateUp()
                }
            }
        }

        binding.registerBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }

        return binding.root
    }
}
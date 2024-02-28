package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentRegistrationBinding
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.RegisterViewModel

@AndroidEntryPoint
class RegistrationFragment : Fragment() {

    private val viewModel: RegisterViewModel by viewModels()
    private val registerViewModel: RegisterViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        val toolbar = binding.regToolbar.toolbarRegistration

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.loginBtn.setOnClickListener {
            val login = binding.loginTextField.editText?.text.toString()
            val userPassword = binding.passwordTextField.editText?.text.toString()
            val userPasswordConfirm = binding.passwordConfirmTextField.editText?.text.toString()
            val userName = binding.nameTextField.editText?.text.toString()

            val media = registerViewModel.photoState.value

            if (userPassword != userPasswordConfirm) {
                binding.passwordConfirmTextField.isHelperTextEnabled = true
            } else {
                binding.passwordConfirmTextField.isHelperTextEnabled = false
                if (media == null) {
                    viewModel.saveRegisteredUserWithoutAvatar(login, userPassword, userName)
                } else {
                    viewModel.saveRegisteredUser(login, userPassword, userName, media.file)
                }
            }

            binding.passwordTextField.isHelperTextEnabled = userPassword.isBlank()

            if (userName.isBlank() || userPassword.isBlank() || login.isBlank()) {
                Snackbar.make(binding.root, getString(R.string.login_or_name_or_password_is_empty), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            authViewModel.data.observe(viewLifecycleOwner) {
                if (authViewModel.authenticated) {
                    findNavController().navigateUp()
                }
            }
        }

        registerViewModel.photoState.observe(viewLifecycleOwner) { media ->
            Glide.with(this)
                .load(media?.file)
                .circleCrop()
                .override(2048, 2048)
                .into(binding.registerPhoto)
            binding.registerIconCamera.isVisible = false
        }

        binding.registerPhotoContainer.setOnClickListener {
            findNavController().navigate(R.id.action_registrationFragment_to_registrationWithPhotoFragment)
        }

        registerViewModel.registerUserState.observe(viewLifecycleOwner) {
            if (it.error) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.registration_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        return binding.root
    }
}
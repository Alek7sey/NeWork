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
import ru.netology.nework.databinding.FragmentRegistrationBinding
import ru.netology.nework.viewmodel.RegisterViewModel

@AndroidEntryPoint
class RegistrationFragment : Fragment() {

    private val viewModel: RegisterViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentRegistrationBinding.inflate(layoutInflater, container, false)

        binding.loginBtn.setOnClickListener {
            val login = binding.loginTextField.editText?.text.toString()
            val userPassword = binding.passwordTextField.editText?.text.toString()
            val userName = binding.nameTextField.editText?.text.toString()

            if (userName.isBlank() || userPassword.isBlank() || login.isBlank()) {
                Snackbar.make(binding.root, getString(R.string.login_or_name_or_password_is_empty), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            viewModel.image.observe(viewLifecycleOwner) { media ->
                viewModel.saveRegisteredUser(login, userPassword, userName, media.file)
            }
        }
        findNavController().navigateUp()
        return binding.root
    }

}
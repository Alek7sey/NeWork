package ru.netology.nework.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.RegisterLoadImageBinding
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.viewmodel.RegisterViewModel

@AndroidEntryPoint
class RegistrationWithPhotoFragment : Fragment() {

    private val viewModel: RegisterViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = RegisterLoadImageBinding.inflate(inflater, container, false)

        val toolbar = binding.toolbarMyPhoto.myProfilePhoto

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.saveProfileMyPhoto -> {
                    findNavController().navigateUp()
                    true
                }

                else -> false
            }
        }

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val photoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.picking_photo_error),
                            Toast.LENGTH_LONG
                        ).show()
                        return@registerForActivityResult
                    }

                    Activity.RESULT_OK -> {
                        val uri = requireNotNull(it.data?.data)
                        viewModel.setRegisterPhoto(MediaUpload(file = uri.toFile()))
                    }
                }
            }

        viewModel.photoState.observe(viewLifecycleOwner) { media ->
            if (media == null) {
                binding.registerProfilePhoto.visibility = View.GONE
                return@observe
            }
            binding.registerProfilePhoto.visibility = View.VISIBLE
            binding.registerProfilePhoto.setImageURI(Uri.parse(media.file.toString()))
        }

        binding.clearPhoto.setOnClickListener {
            viewModel.photoState.observe(viewLifecycleOwner) { media ->
                if (media != null) {
                    viewModel.deletePhoto()
                }
            }
        }

        binding.cameraAddPhoto.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .maxResultSize(2048, 2048)
                .createIntent(photoLauncher::launch)
        }

        binding.galeryLoadPhoto.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .maxResultSize(2048, 2048)
                .createIntent(photoLauncher::launch)
        }
        return binding.root
    }
}
package ru.netology.nework.activity

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentAddPostBinding
import ru.netology.nework.dto.AttachmentTypeEvent
import ru.netology.nework.model.PhotoModel
import ru.netology.nework.utils.AndroidUtils
import ru.netology.nework.utils.EditTextArg
import ru.netology.nework.viewmodel.PostViewModel


@AndroidEntryPoint
class PostAddFragment : Fragment() {

    private val postViewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentAddPostBinding.inflate(inflater, container, false)

//        val attachUrl = "${arguments?.getString("urlAttach")}"
//        if (attachUrl.toString() != null) {
//            Glide.with(binding.photoAttachment)
//                .load(attachUrl)
//                .placeholder(R.drawable.ic_launcher_foreground)
//                .error(R.drawable.ic_error)
//                .into(binding.photoAttachment)
//        }

        arguments?.editTextArg?.let(binding.editText::setText)
        if (binding.editText.text.isNullOrBlank()) {
            binding.editText.setText(postViewModel.edited.value?.content.toString())
        }
        binding.editText.requestFocus()

        val imageUrl = postViewModel.edited.value?.attachment?.url
        val type = postViewModel.edited.value?.attachment?.type
        val imageIsVisible =
            !imageUrl.isNullOrBlank() && (type.toString() == AttachmentTypeEvent.IMAGE.toString())
        if (imageIsVisible) {
            Glide.with(binding.photoAttachment)
                .load("$imageUrl")
                .centerInside()
                .error(R.drawable.ic_error)
                .into(binding.photoAttachment)
        }

        binding.postPhotoContainer.isVisible = imageIsVisible

//        requireActivity().onBackPressedDispatcher.addCallback(
//            viewLifecycleOwner, object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    if (postViewModel.edited.value?.id == 0L) {
//                        val content = binding.editText.text.toString()
//                        postViewModel.changeContent(content)
//                    } else {
//                        postViewModel.clear()
//                    }
//                    findNavController().navigateUp()
//                }
//            }
//        )

        val toolbar = binding.toolbarPost.toolbarNewPost

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save -> {
                    val content = binding.editText.text.toString()
                    if (content.isNotBlank()) {
                        postViewModel.changeContent(content)
                        postViewModel.save()
                        AndroidUtils.hideKeyboard(requireView())
                    } else {
                        postViewModel.clear()
                        binding.editText.clearFocus()
                    }
                    findNavController().navigateUp()
                    true
                }

                else -> false
            }
        }

        toolbar.setNavigationOnClickListener {
            postViewModel.clear()
            findNavController().navigateUp()
        }

        postViewModel.postCreated.observe(viewLifecycleOwner) {
            postViewModel.loadPosts()
            findNavController().navigateUp()
        }

        postViewModel.photoState.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.postPhotoContainer.visibility = View.GONE
                return@observe
            }
            binding.postPhotoContainer.visibility = View.VISIBLE
            binding.photoAttachment.setImageURI(it.uri)
        }

        binding.peopleBtn.setOnClickListener {
            findNavController().navigate(R.id.action_postAddFragment_to_usersSelectedFragment)
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
                        postViewModel.setPhoto(PhotoModel(uri = uri, file = uri.toFile()))
                    }
                }
            }

        binding.addPhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .cameraOnly()
                .crop()
                .compress(15360)
                .createIntent(photoLauncher::launch)
        }

        binding.loadPhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .galleryOnly()
                .crop()
                .compress(15360)
                .createIntent(photoLauncher::launch)
        }

        binding.postRemovePhoto.setOnClickListener {
//            postViewModel.photoState.observe(viewLifecycleOwner) { photoModel ->
 //               if (photoModel != null) {
                    postViewModel.clearPhoto()
 //               }
 //           }
        }
        return binding.root
    }

    companion object {
        var Bundle.editTextArg: String? by EditTextArg
    }
}
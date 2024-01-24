package ru.netology.nework.activity

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentAddPostBinding
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

        arguments?.editTextArg?.let(binding.editText::setText)
        if (binding.editText.text.isNullOrBlank()) {
            binding.editText.setText(postViewModel.edited.value?.content.toString())
        }
        binding.editText.requestFocus()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (postViewModel.edited.value?.id == 0L) {
                        val content = binding.editText.text.toString()
                        postViewModel.changeContent(content)
                    } else {
                        postViewModel.clear()
                    }
                    findNavController().navigateUp()
                }
            }
        )

//        binding.toolbarPost.toolbarNewPost.menu.clear()

//        requireActivity().addMenuProvider(object : MenuProvider {
//            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                menuInflater.inflate(R.menu.toolbar_new_post, menu)
//            }
        val toolbar = binding.toolbarPost.toolbarNewPost
//        toolbar.inflateMenu(R.menu.toolbar_new_post)
//            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
//                when (menuItem.itemId) {
//                    R.id.check -> {
//                        val content = binding.editText.text.toString()
//                        if (content.isNotBlank()) {
//                            postViewModel.changeContent(content)
//                            postViewModel.save()
//                            AndroidUtils.hideKeyboard(requireView())
//                        } else {
//                            postViewModel.clear()
//                            binding.editText.clearFocus()
//                        }
//                        true
//                    }
//
//                    else -> false
//                }
//        }, viewLifecycleOwner)


        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.check -> {
                    val content = binding.editText.text.toString()
                    if (content.isNotBlank()) {
                        postViewModel.changeContent(content)
                        postViewModel.save()
                        AndroidUtils.hideKeyboard(requireView())
                        findNavController().navigateUp()
                    } else {
                        postViewModel.clear()
                        binding.editText.clearFocus()
                    }
                    true
                }

                else -> false
            }
        }

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

//        postViewModel.postCreated.observe(viewLifecycleOwner) {
//            postViewModel.loadPosts()
//            findNavController().navigateUp()
//        }

        postViewModel.photoState.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.photoAttachAndRemove.visibility = View.GONE
                return@observe
            }
            binding.photoAttachAndRemove.visibility = View.VISIBLE
            binding.photoAttachment.setImageURI(it.uri)
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

        when (binding.navAddPost.bottomNavAddPost.selectedItemId) {
            R.id.people -> {
// findNavController().navigate(на фрагмент выбранных юзаров)
            }

            R.id.photo -> {
                ImagePicker.Builder(this)
                    .cameraOnly()
                    .crop()
                    .compress(15360)
                    .createIntent(photoLauncher::launch)
            }

            R.id.attachFile -> {
                ImagePicker.Builder(this)
                    .galleryOnly()
                    .crop()
                    .compress(15360)
                    .createIntent(photoLauncher::launch)
            }

            R.id.location -> {

            }
        }

        binding.removePhoto.setOnClickListener {
            postViewModel.photoState.observe(viewLifecycleOwner) { photoModel ->
                if (photoModel != null) {
                    postViewModel.clearPhoto()
                }
            }
        }
        return binding.root
    }

    companion object {
        var Bundle.editTextArg: String? by EditTextArg
    }
}
package ru.netology.nework.activity

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentAddPostBinding
import ru.netology.nework.dto.AttachmentTypeEvent
import ru.netology.nework.dto.AttachmentTypePost
import ru.netology.nework.model.AttachmentModelPost
import ru.netology.nework.utils.AndroidUtils
import ru.netology.nework.utils.EditTextArg
import ru.netology.nework.viewmodel.PostViewModel


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PostAddFragment : Fragment() {

    private val postViewModel: PostViewModel by activityViewModels()
    private var placeMark: PlacemarkMapObject? = null
    private val gson = Gson()
    private val pointToken = object : TypeToken<Point>() {}.type
    private val usersToken = object : TypeToken<List<Int>>() {}.type

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

        val toolbar = binding.toolbarPost.toolbarNewPost

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save -> {
                    val content = binding.editText.text.toString()
                    if (content.isNotBlank()) {
                        postViewModel.changeContent(content)
                        postViewModel.save()
                        AndroidUtils.hideKeyboard(requireView())
                        postViewModel.clearAttachment()
                        postViewModel.clear()
                    } else {
                        postViewModel.clear()
                        postViewModel.clearAttachment()
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
            postViewModel.clearAttachment()
            findNavController().navigateUp()
        }

        postViewModel.postCreated.observe(viewLifecycleOwner) {
            postViewModel.loadPosts()
            findNavController().navigateUp()
        }

        binding.peopleBtn.setOnClickListener {
            findNavController().navigate(
                R.id.action_postAddFragment_to_usersSelectedFragment,
                bundleOf("selectedUsers" to true)
            )
        }
        setFragmentResultListener("usersFragment") { _, bundle ->
            val selectedUsers = gson.fromJson<List<Int>>(bundle.getString("selectedUsers"), usersToken)
            if (selectedUsers != null) {
                postViewModel.saveMentionedIds(selectedUsers)
            }
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
                        postViewModel.setAttachment(AttachmentModelPost(uri = uri, file = uri.toFile(), AttachmentTypePost.IMAGE))
                    }
                }
            }

        val videoLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    val size = uri.toFile().length()
                    if (size > MAX_SIZE_VIDEO) {
                        Toast.makeText(requireContext(), getString(R.string.attach_must_not_exceed_15_mb), Toast.LENGTH_LONG).show()
                        return@registerForActivityResult
                    }
                    postViewModel.setAttachment(AttachmentModelPost(uri = uri, file = uri.toFile(), AttachmentTypePost.VIDEO))
                }
            }

        binding.addPhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .cameraOnly()
                .crop()
                .compress(15360)
                .createIntent(photoLauncher::launch)
        }

        binding.loadFile.setOnClickListener {
            videoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }

        binding.postRemovePhoto.setOnClickListener {
            postViewModel.clearAttachment()
        }

        binding.addLocation.setOnClickListener {
            findNavController().navigate(R.id.action_postAddFragment_to_mapsFragment)
        }
        setFragmentResultListener("mapsFragment") { _, bundle ->
            val point = gson.fromJson<Point>(bundle.getString("point"), pointToken)
            point.let { postViewModel.setCoord(it) }
        }

        binding.removeLocation.setOnClickListener {
            postViewModel.removeCoords()
        }

        postViewModel.attachState.observe(viewLifecycleOwner) { attachment ->
            when (attachment?.attachmentTypePost) {
                AttachmentTypePost.IMAGE -> {
                    binding.postPhotoContainer.visibility = View.VISIBLE
                    binding.photoAttachment.setImageURI(attachment.uri)
                }

                AttachmentTypePost.VIDEO -> {
                }

                AttachmentTypePost.AUDIO -> {
                }

                null -> {
                    binding.postPhotoContainer.visibility = View.GONE
                }
            }
        }

        val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.ic_location_pin)

        postViewModel.edited.observe(viewLifecycleOwner) { post ->
            val point =
                if (post.coordinates != null)
                    Point(
                        post.coordinates.latitude.toDouble(),
                        post.coordinates.longitude.toDouble()
                    )
                else null
            if (point != null) {
                if (placeMark == null) {
                    placeMark = binding.map.mapWindow.map.mapObjects.addPlacemark()
                }
                placeMark?.apply {
                    geometry = point
                    setIcon(imageProvider)
                    isVisible = true
                }
                binding.map.mapWindow.map.move(CameraPosition(point, 13.0f, 0f, 0f))
            } else {
                placeMark = null
            }
            binding.mapContainer.isVisible = placeMark != null
        }
        return binding.root
    }

    companion object {
        var Bundle.editTextArg: String? by EditTextArg
        const val MAX_SIZE_VIDEO = 15_728_640
    }
}
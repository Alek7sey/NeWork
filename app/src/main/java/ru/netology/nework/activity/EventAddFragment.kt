package ru.netology.nework.activity

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import ru.netology.nework.databinding.FragmentAddEventBinding
import ru.netology.nework.dto.AttachmentTypeEvent
import ru.netology.nework.model.AttachmentModelEvent
import ru.netology.nework.utils.AndroidUtils
import ru.netology.nework.utils.EditTextArg
import ru.netology.nework.utils.convertServerDateTimeToLocalDateTime
import ru.netology.nework.viewmodel.EventsViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EventAddFragment : Fragment() {
    private val eventsViewModel: EventsViewModel by activityViewModels()
    private val gson = Gson()
    private val pointToken = object : TypeToken<Point>() {}.type
    private val usersToken = object : TypeToken<List<Long>>() {}.type
    private var placeMark: PlacemarkMapObject? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentAddEventBinding.inflate(inflater, container, false)

        if (eventsViewModel.edited.value?.id != 0L && eventsViewModel.edited.value != null) {
            arguments?.editTextArg?.let {
                binding.editText.setText(it)
            }
            if (binding.editText.text.isNullOrBlank()) {
                binding.editText.setText(eventsViewModel.edited.value?.content.toString())
            }
            binding.editText.requestFocus()

            binding.eventDateBtn.text = convertServerDateTimeToLocalDateTime(eventsViewModel.edited.value?.datetime.toString())
            binding.eventDateTypeBtn.text = eventsViewModel.edited.value?.type.toString()

            val imageUrl = eventsViewModel.edited.value?.attachment?.url
            val type = eventsViewModel.edited.value?.attachment?.type
            val imageIsVisible =
                !imageUrl.isNullOrBlank() && (type.toString() == AttachmentTypeEvent.IMAGE.toString())
            if (imageIsVisible) {
                Glide.with(binding.photo)
                    .load("$imageUrl")
                    .centerInside()
                    .error(R.drawable.ic_error)
                    .into(binding.photo)
            }

            binding.photoContainer.isVisible = imageIsVisible
        }

        val toolbar = binding.toolbarAddEvent.toolbarNewEvent

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save -> {
                    val content = binding.editText.text.toString()
                    val datetime = binding.eventDateBtn.text.toString()
                    val typeEvent = binding.eventDateTypeBtn.text.toString()
                    if (content.isNotBlank()) {
                        eventsViewModel.changeContent(content, datetime, typeEvent)
                        eventsViewModel.save()
                        AndroidUtils.hideKeyboard(requireView())
                    } else {
                        eventsViewModel.clear()
                        binding.editText.clearFocus()
                    }
                    findNavController().navigateUp()
                    true
                }

                else -> false
            }
        }

        toolbar.setNavigationOnClickListener {
            eventsViewModel.editType("")
            eventsViewModel.editDateTime("dd.mm.yyyy HH:mm")
            eventsViewModel.clearAttachment()
            eventsViewModel.clear()
            findNavController().navigateUp()
        }

        eventsViewModel.eventCreated.observe(viewLifecycleOwner) {
            eventsViewModel.loadEvents()
            findNavController().navigateUp()
        }

        eventsViewModel.attachState.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }
            binding.photoContainer.visibility = View.VISIBLE
            binding.photo.setImageURI(it.uri)
        }

        binding.peopleBtn.setOnClickListener {
            findNavController().navigate(R.id.action_eventAddFragment_to_usersSelectedFragment)
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
                        eventsViewModel.setAttachment(AttachmentModelEvent(uri = uri, file = uri.toFile(), AttachmentTypeEvent.IMAGE))
                    }
                }
            }

        val videoLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    val size = uri.toFile().length()
                    if (size > PostAddFragment.MAX_SIZE_VIDEO) {
                        Toast.makeText(requireContext(), getString(R.string.attach_must_not_exceed_15_mb), Toast.LENGTH_LONG).show()
                        return@registerForActivityResult
                    }
                    eventsViewModel.setAttachment(AttachmentModelEvent(uri = uri, file = uri.toFile(), AttachmentTypeEvent.VIDEO))
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

        binding.removePhoto.setOnClickListener {
            eventsViewModel.clearAttachment()
        }

        binding.peopleBtn.setOnClickListener {
            findNavController().navigate(
                R.id.action_eventAddFragment_to_usersSelectedFragment,
                bundleOf("selectedUsers" to true)
            )
        }
        setFragmentResultListener("usersFragment") { _, bundle ->
            val selectedUsers = gson.fromJson<List<Int>>(bundle.getString("selectedUsers"), usersToken)
            if (selectedUsers != null) {
                eventsViewModel.saveMentionedIds(selectedUsers)
            }
        }

        binding.addLocation.setOnClickListener {
            findNavController().navigate(R.id.action_eventAddFragment_to_mapsFragment)
        }
        setFragmentResultListener("mapsFragment") { _, bundle ->
            val point = gson.fromJson<Point>(bundle.getString("point"), pointToken)
            point.let { eventsViewModel.setCoord(it) }
        }

        binding.removeLocation.setOnClickListener {
            eventsViewModel.removeCoords()
        }

        eventsViewModel.attachState.observe(viewLifecycleOwner) { attachment ->
            when (attachment?.attachmentTypeEvent) {
                AttachmentTypeEvent.IMAGE -> {
                    binding.photoContainer.visibility = View.VISIBLE
                    binding.photo.setImageURI(attachment.uri)
                }

                AttachmentTypeEvent.VIDEO -> {
                }

                AttachmentTypeEvent.AUDIO -> {
                }

                null -> {
                    binding.photoContainer.visibility = View.GONE
                }
            }
        }


        binding.fab.setOnClickListener {
            val modalBottomSheet = EventModalBottomSheet()
            Dialog(requireContext()).window?.setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )
            modalBottomSheet.show(
                requireActivity().supportFragmentManager, EventModalBottomSheet.TAG
            )
        }

        eventsViewModel.eventTypesState.observe(viewLifecycleOwner) {
            binding.eventDateTypeBtn.text = it
        }

        eventsViewModel.dateTimeState.observe(viewLifecycleOwner) {
            binding.eventDateBtn.text = it
        }

        val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.ic_location_pin)

        eventsViewModel.edited.observe(viewLifecycleOwner) { event ->
            val point =
                if (event.coordinates != null)
                    Point(
                        event.coordinates.latitude.toDouble(),
                        event.coordinates.longitude.toDouble()
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
    }
}
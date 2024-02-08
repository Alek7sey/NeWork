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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentAddEventBinding
import ru.netology.nework.model.PhotoModel
import ru.netology.nework.utils.AndroidUtils
import ru.netology.nework.utils.EditTextArg
import ru.netology.nework.viewmodel.EventsViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class EventAddFragment : Fragment() {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val eventsViewModel: EventsViewModel by activityViewModels()


    private val photoLauncher =
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
                    eventsViewModel.setPhoto(PhotoModel(uri = uri, file = uri.toFile()))
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentAddEventBinding.inflate(inflater, container, false)


        arguments?.editTextArg?.let(binding.editText::setText)
        if (binding.editText.text.isNullOrBlank()) {
            binding.editText.setText(eventsViewModel.edited.value?.content.toString())
        }
        binding.editText.requestFocus()

//        requireActivity().onBackPressedDispatcher.addCallback(
//            viewLifecycleOwner, object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    if (eventsViewModel.edited.value?.id == 0L) {
//                        val content = binding.editText.text.toString()
//                        eventsViewModel.changeContent(content)
//                    } else {
//                        eventsViewModel.clear()
//                    }
//                    findNavController().navigateUp()
//                }
//            }
//        )

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
                     //   findNavController().navigateUp()//(R.id.eventsFeedFragment)
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
            findNavController().navigateUp()
        }

        eventsViewModel.eventCreated.observe(viewLifecycleOwner) {
            eventsViewModel.loadEvents()
            findNavController().navigateUp()
        }

        eventsViewModel.photoState.observe(viewLifecycleOwner) {
            if (it == null) {
//                binding.addPhoto.visibility = View.GONE
//                binding.removePhoto.visibility = View.GONE
                binding.photoContainer.visibility = View.GONE
                return@observe
            }
//            binding.photoAttachment.visibility = View.VISIBLE
//            binding.removePhoto.visibility = View.VISIBLE
            binding.photoContainer.visibility = View.VISIBLE
            binding.photo.setImageURI(it.uri)
        }

        binding.peopleBtn.setOnClickListener {
//            findNavController().navigate(на фрагмент выбранных юзеров)
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

        binding.removePhoto.setOnClickListener {
            eventsViewModel.photoState.observe(viewLifecycleOwner) { photoModel ->
                if (photoModel != null) {
                    eventsViewModel.clearPhoto()
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

        return binding.root
    }

    companion object {
        var Bundle.editTextArg: String? by EditTextArg
    }
}
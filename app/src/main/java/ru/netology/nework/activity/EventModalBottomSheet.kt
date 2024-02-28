package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.databinding.BottomSheetDialogBinding
import ru.netology.nework.utils.convertServerDateTimeToLocalDateTime
import ru.netology.nework.viewmodel.EventsViewModel

@AndroidEntryPoint
@OptIn(ExperimentalCoroutinesApi::class)
class EventModalBottomSheet : BottomSheetDialogFragment() {
    private val viewModel: EventsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = BottomSheetDialogBinding.inflate(inflater, container, false)

        binding.radioBtnOnline.isChecked = true
        if (!viewModel.edited.value?.datetime.isNullOrBlank()) {
            binding.outlinedTextField.editText?.setText(convertServerDateTimeToLocalDateTime(viewModel.edited.value!!.datetime))
        }
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioBtnOnline.id -> viewModel.editType(binding.radioBtnOnline.text.toString())
                binding.radioBtnOffline.id -> viewModel.editType(binding.radioBtnOffline.text.toString())
            }
        }

        requireNotNull(binding.outlinedTextField.editText).doAfterTextChanged {
            viewModel.editDateTime(it?.toString().orEmpty())
        }

        return binding.root
    }

    companion object {
        const val TAG = "EventModalBottomSheet"
    }

}
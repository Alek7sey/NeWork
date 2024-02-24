package ru.netology.nework.activity

import android.app.Dialog
import android.os.Bundle
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.databinding.CardDatesBinding
import ru.netology.nework.viewmodel.JobsViewModel

@AndroidEntryPoint
class DateInputFragment : DialogFragment() {

    private val viewModel: JobsViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val binding = CardDatesBinding.inflate(layoutInflater)
            val builder = MaterialAlertDialogBuilder(it)

            requireNotNull(binding.selectStartDate.editText).doAfterTextChanged { start ->
                viewModel.editStartDate(start?.toString().orEmpty())
            }

            requireNotNull(binding.selectEndDate.editText).doAfterTextChanged { end ->
                viewModel.editEndDate(end?.toString().orEmpty())
            }

            builder.setView(binding.root).let {
                binding.apply {
                    okBtn.setOnClickListener {
                        dialog?.cancel()
                    }

                    cancelBtn.setOnClickListener {
                        dialog?.cancel()
                    }
                }
            }
            builder.create()
        } ?: throw IllegalStateException("Cannot be null")
    }
}
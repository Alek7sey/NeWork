package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.databinding.FragmentAddJobBinding
import ru.netology.nework.utils.AndroidUtils.hideKeyboard
import ru.netology.nework.viewmodel.JobsViewModel

@AndroidEntryPoint
class JobAddFragment : Fragment() {

    private val viewModel: JobsViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding = FragmentAddJobBinding.inflate(inflater, container, false)

        binding.addJobBtn.setOnClickListener {
            viewModel.changeContent(
                binding.jobNameFieldInputText.text.toString(),
                binding.jobPositionFieldInputText.text.toString(),
                binding.jobLinkFieldInputText.text.toString(),
                binding.startData.text.toString(),
                binding.endData.text.toString()
            )
            viewModel.saveJob()
            hideKeyboard(requireView())
        }

        viewModel.jobCreated.observe(viewLifecycleOwner) {
            viewModel.loadMyJobs()
            findNavController().navigateUp()
        }

        val dialog = DateInputFragment()

        viewModel.startDateState.observe(viewLifecycleOwner) {
            binding.startData.text = it
        }

        viewModel.endDateState.observe(viewLifecycleOwner) {
            binding.endData.text = it
        }

        binding.dateLayout.setOnClickListener {
            dialog.show(requireActivity().supportFragmentManager, "DateInputFragment")
        }

        return binding.root
    }
}
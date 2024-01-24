package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentImagePostBinding

@AndroidEntryPoint
class PostImageFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding =  FragmentImagePostBinding.inflate(layoutInflater, container, false)

        val downloadAttachUrl = "${arguments?.getString("urlAttach")}"
        Glide.with(binding.postImageAttachment)
            .load(downloadAttachUrl)
            .centerInside()
            .error(R.drawable.ic_error)
            .into(binding.postImageAttachment)

        return binding.root
    }
}
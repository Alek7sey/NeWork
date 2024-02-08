@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.netology.nework.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.LikersShortListAdapter
import ru.netology.nework.adapter.OnInteractionListener
import ru.netology.nework.adapter.PostsAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentPostDetailsBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.utils.SeparateIdPostArg
import ru.netology.nework.utils.convertServerDateToLocalDate
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.PostViewModel
import ru.netology.nework.viewmodel.UsersViewModel
import javax.inject.Inject

@AndroidEntryPoint
class PostDetailsFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val postViewModel: PostViewModel by activityViewModels()
    private val usersViewModel: UsersViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    private var mapView: MapView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostDetailsBinding.inflate(layoutInflater, container, false)

        val arg = arguments?.let { it.idArg }
        val toolbar = binding.toolbarPost.toolbar

        mapView = binding.cardPostDetails.postMapView

        postViewModel.postData.observe(viewLifecycleOwner) { postModel ->
            //     postModel.posts.map { arg?.let { postId -> it.copy(id = postId) } }
            postModel.posts.find { it.id == arg }?.let { post ->
                binding.cardPostDetails.apply {
                    shareBtn.visibility = View.GONE
                    postMenu.visibility = View.GONE
                    published.visibility = View.INVISIBLE
                    jobPosition.visibility = View.VISIBLE

                    publishedDetails.visibility = View.VISIBLE
                    publishedDetails.text = convertServerDateToLocalDate(post.published)

                    likersTitle.visibility = View.VISIBLE
                    likersList.isVisible = post.likeOwnerIds?.isNotEmpty() == true

                    mentionedTitle.visibility = View.VISIBLE
                    mentionedBtn.text = post.mentionIds?.size.toString()
                    mentionedBtn.visibility = View.VISIBLE
                    mentionedList.isVisible = post.mentionIds?.isNotEmpty() == true

                    postMapView.isVisible = !post.coordinates?.latitude.isNullOrEmpty() && !post.coordinates?.longitude.isNullOrEmpty()

                    PostsAdapter.PostViewHolder(this, object : OnInteractionListener {

                        override fun onLike(post: Post) {
                            if (authViewModel.authorized) {
                                postViewModel.likeById(post)
                            } else {
                                findNavController().navigate(R.id.action_detailsPostFragment_to_loginFragment)
                            }
                        }

                        override fun onEdit(post: Post) {
                            postViewModel.edit(post)
                        }

                        override fun onImage(post: Post) {
                            findNavController().navigate(
                                R.id.action_detailsPostFragment_to_postImageFragment,
                                Bundle().apply { putString("urlAttach", post.attachment?.url) })
                        }

                        override fun onVideo(post: Post) {
                            val videoIntent = Intent(Intent.ACTION_VIEW, Uri.parse(post.attachment?.url))
                            val chooserIntent = Intent.createChooser(
                                videoIntent,
                                getString(R.string.choose_where_open_your_video)
                            )
                            startActivity(chooserIntent)
                        }

                        override fun onAudio(post: Post) {
                            val audioIntent = Intent(Intent.ACTION_VIEW, Uri.parse(post.attachment?.url))
                            val chooserIntent = Intent.createChooser(
                                audioIntent,
                                getString(R.string.choose_where_open_your_audio)
                            )
                            startActivity(chooserIntent)
                        }

                        override fun followTheLink(post: Post) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.link))
                            startActivity(intent)
                        }
                    }).bind(post)

                    toolbar.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.share -> {
                                val intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, post.content)
                                    type = "text/plain"
                                }
                                val shareIntent =
                                    Intent.createChooser(intent, getString(R.string.share_post))
                                startActivity(shareIntent)
                                postViewModel.shareById(post.id)
                                true
                            }

                            else -> false
                        }
                    }

                    toolbar.setNavigationOnClickListener {
                        findNavController().navigateUp()
                    }

                    binding.cardPostDetails.apply {
                        likersMore.setOnClickListener {
                            findNavController().navigate(R.id.action_detailsPostFragment_to_postLikersFragment,
                                Bundle().apply { putSerializable("postKey", post) })
                        }
                        mentionedMore.setOnClickListener {
                            TODO("Фрагмент подписчиков")
//                            findNavController().navigate(на фрагмент подписчиков,
//                            Bundle().apply { putSerializable("postKey", post) })
                        }
                    }

                    val adapter = LikersShortListAdapter()
                    likersList.adapter = adapter
                    mentionedList.adapter = adapter

                    viewLifecycleOwner.lifecycleScope.launch {
                        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                            usersViewModel.data.observe(viewLifecycleOwner) {
                                val likerOwnersIds = post.likeOwnerIds.orEmpty().toSet()
                                if (likerOwnersIds.isNotEmpty()) {
                                    likerOwnersIds.forEach { likerOwnerId ->
                                        val filterUsers =
                                            it.users.filter { it.id == likerOwnerId.toLong() }
                                        adapter.submitList(filterUsers)
                                    }
                                    if (post.likeOwnerIds?.size!! > 5) {
                                        binding.cardPostDetails.likersMore.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }
                    }

                    usersViewModel.data.observe(viewLifecycleOwner) {
                        val mentionedOwnersIds = post.mentionIds.orEmpty().toSet()
                        if (mentionedOwnersIds.isNotEmpty()) {
                            mentionedOwnersIds.forEach { mentionedOwnerId ->
                                val filterUsers =
                                    it.users.filter { it.id == mentionedOwnerId.toLong() }
                                adapter.submitList(filterUsers)

                            }
                            if (post.mentionIds?.size!! > 5) {
                                binding.cardPostDetails.mentionedMore.visibility = View.VISIBLE
                            }
                        }
                    }

                    if (post.coordinates == null) {
                        mapView?.onStop()
                        return@observe
                    }

                    mapView?.isVisible = true
                    MapKitFactory.initialize(requireContext())

                    val latitude = post.coordinates.latitude.toDouble()
                    val longitude = post.coordinates.longitude.toDouble()

                    val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.ic_location_pin)
                    val placemark = mapView?.map?.mapObjects?.addPlacemark().apply {
                        this?.geometry = Point(latitude, longitude)
                        this?.setIcon(imageProvider)
                    }

                    val placemarkTapListener = MapObjectTapListener { _, _ ->
                        Toast.makeText(requireContext(), getString(R.string.this_event_location), Toast.LENGTH_SHORT).show()
                        true
                    }

                    placemark?.addTapListener(placemarkTapListener)
                    placemark?.removeTapListener(placemarkTapListener)

                }
            }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        mapView?.onStop()
        super.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView = null
    }

    companion object {
        var Bundle.idArg: Long by SeparateIdPostArg
    }
}
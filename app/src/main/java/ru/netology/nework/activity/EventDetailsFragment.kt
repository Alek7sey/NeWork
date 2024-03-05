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
import androidx.lifecycle.lifecycleScope
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
import ru.netology.nework.activity.EventsFeedFragment.Companion.eventIdArg
import ru.netology.nework.adapter.EventsAdapter
import ru.netology.nework.adapter.EventsOnInteractionListener
import ru.netology.nework.adapter.UsersFilteredEventAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentEventDetailsBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.model.InvolvedUserType
import ru.netology.nework.utils.AvatarItemDecorator
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.EventsViewModel
import ru.netology.nework.viewmodel.UsersViewModel
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EventDetailsFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: EventsViewModel by activityViewModels()
    private val usersViewModel: UsersViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    private var mapView: MapView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentEventDetailsBinding.inflate(layoutInflater, container, false)
        val toolbar = binding.toolbarEventDetails.toolbarEvent
        mapView = binding.cardEventDetails.eventMapView

        val eventIdArg = arguments?.eventIdArg

        viewModel.eventDetailsData.observe(viewLifecycleOwner) { eventModel ->
            eventModel.eventsList.map { eventIdArg?.let { eventId -> it.copy(id = eventId) } }
            eventModel.eventsList.find { it.id == eventIdArg }?.let { event ->
                binding.cardEventDetails.apply {
                    eventMenu.isVisible = false
                    eventPublished.visibility = View.INVISIBLE
                    eventType.text = event.type
                    jobPosition.visibility = View.VISIBLE
                    eventLikeBtn.visibility = View.GONE
                    eventShareBtn.visibility = View.GONE
                    participantsBtn.visibility = View.GONE

                    speakersTitle.isVisible = event.speakerIds?.isNotEmpty() == true
                    likeShortBtn.isChecked = event.likedByMe
                    likeShortBtn.text = event.likeOwnerIds?.size.toString()
                    likeShortBtn.visibility = View.VISIBLE

                    participantsShortBtn.text = event.participantsIds?.size.toString()
                    participantsShortBtn.visibility = View.VISIBLE

                    eventMapView.isVisible = !event.coordinates?.latitude.isNullOrEmpty() && !event.coordinates?.longitude.isNullOrBlank()

//                    val usersFilterAdapter =
//                        UsersFilteredEventAdapter(object : OnIteractionListenerUsersFiltered {
//                            override fun returnEvent(event: Event) {
//                                likersMore.setOnClickListener {
//                                    findNavController().navigate(
//                                        R.id.action_eventDetailsFragment_to_eventLikersFragment,
//                                        Bundle().apply {
//                                            putSerializable("eventKey", event)
//                                        }
//                                    )
//                                }
//                            }
//                        })

                    val avatarDecorator = AvatarItemDecorator(48)
                    val speakersAdapter = UsersFilteredEventAdapter()
                    val likersAdapter = UsersFilteredEventAdapter()
                    val participantsAdapter = UsersFilteredEventAdapter()

                    likersListShort.apply {
                        addItemDecoration(avatarDecorator)
                        adapter = likersAdapter
                    }

                    speakersList.apply {
                        addItemDecoration(avatarDecorator)
                        adapter = speakersAdapter
                    }

                    participantsListShort.apply {
                        addItemDecoration(avatarDecorator)
                        adapter = participantsAdapter
                    }

                    context?.let {
                        EventsAdapter.EventViewHolder(this, object : EventsOnInteractionListener {

                            override fun onLike(event: Event) {
                                if (authViewModel.authenticated) {
                                    viewModel.likeById(event)
                                } else {
                                    findNavController().navigate(R.id.action_eventDetailsFragment_to_loginFragment)
                                }
                            }

                            override fun onEdit(event: Event) {
                                viewModel.edit(event)
                            }

                            override fun onDelete(event: Event) {
                                viewModel.removeById(event.id)
                            }

                            override fun onParticipate(event: Event) {
                                viewModel.participatedById(event)
                            }

                            override fun onImage(event: Event) {
                                findNavController().navigate(
                                    R.id.action_eventDetailsFragment_to_eventImageFragment,
                                    Bundle().apply { putString("urlAttach", event.attachment?.url) })
                            }

                            override fun onAudio(event: Event) {
                                val audioIntent = Intent(Intent.ACTION_VIEW, Uri.parse(event.attachment?.url))
                                val chooserIntent = Intent.createChooser(
                                    audioIntent,
                                    getString(R.string.choose_where_open_your_audio)
                                )
                                startActivity(chooserIntent)
                            }

                            override fun followTheLink(event: Event) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.link))
                                startActivity(intent)
                            }

                        }, it).bind(event)
                    }

                    toolbar.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.shareEvent -> {
                                val intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, event.content)
                                    type = "text/plain"
                                }
                                val shareIntent =
                                    Intent.createChooser(intent, getString(R.string.share_event))
                                startActivity(shareIntent)
                                viewModel.shareById(event.id)
                                true
                            }

                            else -> false
                        }
                    }

                    toolbar.setNavigationOnClickListener {
                        findNavController().navigateUp()
                    }

                    lifecycleScope.launch {
                        event.likeOwnerIds?.let { viewModel.getInvolvedUser(it, InvolvedUserType.LIKER) }
                    }

                    lifecycleScope.launch {
                        event.speakerIds?.let { viewModel.getInvolvedUser(it, InvolvedUserType.SPEAKER) }
                    }

                    lifecycleScope.launch {
                        event.participantsIds?.let { viewModel.getInvolvedUser(it, InvolvedUserType.PARTICIPANT) }
                    }

                    usersViewModel.data.observe(viewLifecycleOwner) {
                        val likeOwnerIds = event.likeOwnerIds.orEmpty().toSet()
                        val speakerOwnerIds = event.speakerIds.orEmpty().toSet()
                        val participantOwnerIds = event.participantsIds.orEmpty().toSet()
                        if (likeOwnerIds.isNotEmpty()) {
                            likersTitle.visibility = View.VISIBLE
                            likersListShort.visibility = View.VISIBLE
                        }
                        if (speakerOwnerIds.isNotEmpty()) {
                            speakersTitle.visibility = View.VISIBLE
                            speakersList.visibility = View.VISIBLE
                        }
                        if (participantOwnerIds.isNotEmpty()) {
                            participantsTitle.visibility = View.VISIBLE
                            participantsListShort.visibility = View.VISIBLE
                        }
                    }

                    viewModel.involvedData.observe(viewLifecycleOwner) {
                        likersAdapter.submitList(it.likers)
                        speakersAdapter.submitList(it.speakers)
                        participantsAdapter.submitList(it.participants)
                    }

//                        likersMore.setOnClickListener {
//                            findNavController().navigate(
//                                R.id.action_eventDetailsFragment_to_eventLikersFragment,
//                                Bundle().apply {
//                                    putSerializable("eventKey", event)
//                                }
//                            )
//                        }

                    if (event.coordinates == null) {
                        mapView?.findViewById<MapView>(R.id.eventMapView)?.onStop()
                        return@observe
                    }
                    mapView?.findViewById<MapView>(R.id.eventMapView)?.isVisible = true

                    MapKitFactory.initialize(requireContext())

                    val latitude = event.coordinates.latitude.toDouble()
                    val longitude = event.coordinates.longitude.toDouble()

                    val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.ic_location_pin)
                    val placemark = mapView?.map?.mapObjects?.addPlacemark().apply {
                        this?.geometry = Point(latitude, longitude)
                        this?.setIcon(imageProvider)
                    }

                    val placemarkTapListener = MapObjectTapListener { _, _ ->
                        Toast.makeText(context, getString(R.string.this_event_location), Toast.LENGTH_LONG).show()
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
        MapKitFactory.getInstance().onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        mapView?.onStop()
        MapKitFactory.getInstance().onStop()
        viewModel.clearInvolved()
        super.onStop()
    }

    override fun onDestroy() {
        mapView = null
        super.onDestroy()
    }
}

package ru.netology.nework.activity

import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentMapsBinding

class MapsFragment : Fragment(), UserLocationObjectListener {

    private lateinit var userLocation: UserLocationLayer
    private var placeMark: PlacemarkMapObject? = null
    private val gson = Gson()
    private var mapView: MapView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMapsBinding.inflate(inflater, container, false)
        MapKitFactory.initialize(requireContext())

        val mapWindow = binding.map.mapWindow
        val map = mapWindow.map

        mapView = binding.map.apply {
            userLocation = MapKitFactory.getInstance().createUserLocationLayer(mapWindow)
            userLocation.isVisible = true
            userLocation.isHeadingEnabled = false
        }

        val imageProvider =
            ImageProvider.fromResource(requireContext(), R.drawable.ic_location_pin)
        val inputListener = object : InputListener {
            override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) = Unit
            override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {
                if (placeMark == null) {
                    placeMark = map.mapObjects.addPlacemark()
                }
                placeMark?.apply {
                    geometry = point
                    setIcon(imageProvider)
                    isVisible = true
                }
            }
        }
        map.addInputListener(inputListener)

        binding.toolbarMaps.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.save -> {
                    setFragmentResult(
                        "mapsFragment",
                        bundleOf("point" to gson.toJson(placeMark?.geometry))
                    )
                    findNavController().navigateUp()
                    true
                }

                else -> false
            }
        }

        binding.toolbarMaps.setNavigationOnClickListener {
            findNavController().navigateUp()
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
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView = null
    }

    override fun onObjectAdded(userLoactionView: UserLocationView) {
        if (mapView != null) {
            userLocation.setAnchor(
                PointF((mapView!!.width * 0.5).toFloat(), (mapView!!.height * 0.5).toFloat()),
                PointF((mapView!!.width * 0.5).toFloat(), (mapView!!.height * 0.83).toFloat())
            )
            mapView!!.mapWindow.map.move(
                CameraPosition(userLoactionView.arrow.geometry, 17f, 0f, 0f)
            )
        } else {
            return
        }
    }

    override fun onObjectRemoved(p0: UserLocationView) {}

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {}
}
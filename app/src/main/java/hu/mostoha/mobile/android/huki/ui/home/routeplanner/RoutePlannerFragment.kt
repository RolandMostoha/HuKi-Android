package hu.mostoha.mobile.android.huki.ui.home.routeplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.FragmentRoutePlannerBinding
import hu.mostoha.mobile.android.huki.extensions.addFragment
import hu.mostoha.mobile.android.huki.extensions.clearFocusAndHideKeyboard
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.isLocationPermissionGranted
import hu.mostoha.mobile.android.huki.extensions.openUrl
import hu.mostoha.mobile.android.huki.extensions.setMessage
import hu.mostoha.mobile.android.huki.extensions.setMessageOrGone
import hu.mostoha.mobile.android.huki.extensions.showSnackbar
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.extensions.visibleOrGone
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.ui.PermissionResult
import hu.mostoha.mobile.android.huki.model.ui.PlaceFinderFeature
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel
import hu.mostoha.mobile.android.huki.model.ui.WaypointComment
import hu.mostoha.mobile.android.huki.model.ui.WaypointCommentResult
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersViewModel
import hu.mostoha.mobile.android.huki.ui.home.placefinder.PlaceFinderPopup
import hu.mostoha.mobile.android.huki.ui.home.placefinder.PlaceFinderViewModel
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.comment.WaypointCommentBottomSheetDialogFragment
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.comment.WaypointCommentResultViewModel
import hu.mostoha.mobile.android.huki.ui.home.shared.InsetSharedViewModel
import hu.mostoha.mobile.android.huki.ui.home.shared.MapTouchEventSharedViewModel
import hu.mostoha.mobile.android.huki.ui.home.shared.MapTouchEvents
import hu.mostoha.mobile.android.huki.ui.home.shared.PermissionSharedViewModel
import hu.mostoha.mobile.android.huki.ui.home.shared.PickLocationEventSharedViewModel
import hu.mostoha.mobile.android.huki.ui.home.shared.PickLocationEvents
import hu.mostoha.mobile.android.huki.util.PLACE_FINDER_MIN_TRIGGER_LENGTH
import hu.mostoha.mobile.android.huki.util.ROUTE_PLANNER_MAX_WAYPOINT_COUNT
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject

@AndroidEntryPoint
class RoutePlannerFragment : Fragment() {

    companion object {
        private val ARG_BOUNDING_BOX = this::class.java.simpleName + "ARG_BOUNDING_BOX"

        fun addFragment(fragmentManager: FragmentManager, @IdRes containerId: Int, boundingBox: BoundingBox) {
            fragmentManager.addFragment(
                containerId,
                RoutePlannerFragment::class.java,
                Bundle().apply {
                    putParcelable(ARG_BOUNDING_BOX, boundingBox)
                }
            )
        }
    }

    @Inject
    lateinit var analyticsService: AnalyticsService

    private val routePlannerViewModel: RoutePlannerViewModel by activityViewModels()
    private val layersViewModel: LayersViewModel by activityViewModels()
    private val placeFinderViewModel: PlaceFinderViewModel by viewModels()
    private val insetSharedViewModel: InsetSharedViewModel by activityViewModels()
    private val permissionSharedViewModel: PermissionSharedViewModel by activityViewModels()
    private val mapTouchEventSharedViewModel: MapTouchEventSharedViewModel by activityViewModels()
    private val pickLocationEventViewModel: PickLocationEventSharedViewModel by activityViewModels()
    private val commentResultViewModel: WaypointCommentResultViewModel by activityViewModels()

    private var _binding: FragmentRoutePlannerBinding? = null
    private val binding get() = _binding!!

    private val boundingBox by lazy { requireArguments().getParcelable<BoundingBox>(ARG_BOUNDING_BOX)!! }

    private lateinit var waypointAdapter: WaypointAdapter
    private lateinit var placeFinderPopup: PlaceFinderPopup

    private var lastEditedWaypointInput: TextInputLayout? = null
    private var lastEditedWaypointItem: WaypointItem? = null

    private val waypointList by lazy { binding.routePlannerWaypointList }
    private val routeAttributesContainer by lazy {
        binding.routePlannerRouteAttributesContainer.routeAttributesContainer
    }
    private val routePlannerContainer by lazy { binding.routePlannerContainer }
    private val doneButton by lazy { binding.routePlannerDoneButton }
    private val addWaypointButton by lazy { binding.routePlannerAddWaypointButton }
    private val returnToHomeButton by lazy { binding.routePlannerReturnToHomeButton }
    private val backButton by lazy { binding.routePlannerBackButton }
    private val graphhopperLogo by lazy { binding.routePlannerGraphhopperLogo }
    private val errorText by lazy { binding.routePlannerErrorText }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRoutePlannerBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initFlows()

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        clearRoutePlanner()
                    }
                }
            )
    }

    override fun onResume() {
        super.onResume()

        pickLocationEventViewModel.updateEvent(PickLocationEvents.LocationPickDisabled)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun initViews() {
        initWaypoints()
        initPlaceFinderPopup()

        doneButton.setOnClickListener {
            routePlannerViewModel.saveRoutePlan()
        }
        addWaypointButton.setOnClickListener {
            routePlannerViewModel.addEmptyWaypoint()
        }
        returnToHomeButton.setOnClickListener {
            routePlannerViewModel.createRoundTrip()
        }
        backButton.setOnClickListener {
            clearRoutePlanner()
        }
        graphhopperLogo.setOnClickListener {
            requireContext().openUrl(getString(R.string.route_planner_graphhopper_url))
        }
        routePlannerContainer.setOnClickListener {
            lastEditedWaypointInput?.clearFocus()
            placeFinderViewModel.cancelSearch()
        }
    }

    private fun initWaypoints() {
        waypointAdapter = WaypointAdapter(
            onSearchTextFocused = { waypointInput, waypointItem ->
                lastEditedWaypointInput = waypointInput
                lastEditedWaypointItem = waypointItem

                placeFinderViewModel.initPlaceFinder(PlaceFinderFeature.ROUTE_PLANNER)
            },
            onSearchTextChanged = { waypointInput, waypointItem, text ->
                lastEditedWaypointInput = waypointInput
                lastEditedWaypointItem = waypointItem

                if (waypointInput.hasFocus() && text.isNotEmpty()) {
                    if (text.length >= PLACE_FINDER_MIN_TRIGGER_LENGTH) {
                        placeFinderViewModel.loadPlaces(text, boundingBox, PlaceFeature.ROUTE_PLANNER_SEARCH)
                    } else {
                        placeFinderViewModel.initPlaceFinder(PlaceFinderFeature.ROUTE_PLANNER)
                    }
                }
            },
            onSearchTextDoneAction = { textInputEditText ->
                textInputEditText.text?.clear()
                textInputEditText.clearFocusAndHideKeyboard()
                placeFinderViewModel.cancelSearch()
            },
            onWaypointsSizeChanged = { size ->
                if (size >= ROUTE_PLANNER_MAX_WAYPOINT_COUNT) {
                    addWaypointButton.gone()
                } else {
                    addWaypointButton.visible()
                }
            },
            onCommentClicked = { waypointItem ->
                val waypointPrimaryText = waypointItem.primaryText?.resolve(requireContext()) ?: return@WaypointAdapter

                WaypointCommentBottomSheetDialogFragment.showDialog(
                    requireActivity(),
                    WaypointCommentResult(
                        waypointId = waypointItem.id,
                        waypointComment = WaypointComment(
                            name = waypointItem.waypointComment?.name ?: waypointPrimaryText,
                            comment = waypointItem.waypointComment?.comment
                        ),
                    )
                )
            },
            onRemoveWaypointClicked = { waypointItem ->
                routePlannerViewModel.removeWaypoint(waypointItem)
            },
        )
        waypointAdapter.setHasStableIds(true)
        itemTouchHelper.attachToRecyclerView(waypointList)
        waypointList.adapter = waypointAdapter
    }

    private val itemTouchHelper by lazy {
        val itemTouchCallback = object : SimpleCallback(UP or DOWN, 0) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val adapter = recyclerView.adapter as WaypointAdapter
                val currentList = adapter.currentList.toMutableList()
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                Collections.swap(currentList, fromPosition, toPosition)

                adapter.submitList(currentList)

                return true
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                val adapter = recyclerView.adapter as WaypointAdapter
                routePlannerViewModel.swapWaypoints(adapter.currentList)
            }

            override fun isLongPressDragEnabled(): Boolean = true

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

        }
        ItemTouchHelper(itemTouchCallback)
    }

    private fun initPlaceFinderPopup() {
        placeFinderPopup = PlaceFinderPopup(
            context = requireContext(),
            onPlaceClick = { placeUiModel ->
                val waypointInput = lastEditedWaypointInput?.editText ?: return@PlaceFinderPopup
                val lastEditedWaypoint = lastEditedWaypointItem ?: return@PlaceFinderPopup
                val searchText = waypointInput.text?.toString() ?: ""

                analyticsService.placeFinderPlaceClicked(
                    searchText = searchText,
                    placeName = placeUiModel.primaryText.resolve(requireContext()),
                    isFromHistory = placeUiModel.historyInfo != null
                )

                waypointInput.clearFocusAndHideKeyboard()
                placeFinderViewModel.cancelSearch()

                routePlannerViewModel.updateWaypoint(lastEditedWaypoint, placeUiModel, searchText)
            },
            onMyLocationClick = {
                analyticsService.routePlannerMyLocationClicked()

                if (requireContext().isLocationPermissionGranted()) {
                    val waypointInput = lastEditedWaypointInput?.editText ?: return@PlaceFinderPopup
                    val lastEditedWaypoint = lastEditedWaypointItem ?: return@PlaceFinderPopup

                    waypointInput.clearFocusAndHideKeyboard()
                    placeFinderViewModel.cancelSearch()

                    routePlannerViewModel.updateByMyLocation(lastEditedWaypoint)
                } else {
                    permissionSharedViewModel.updateResult(PermissionResult.LOCATION_PERMISSION_NEEDED)
                }
            },
            onPickLocationClick = {
                analyticsService.routePlannerPickLocationClicked()

                val waypointInput = lastEditedWaypointInput?.editText ?: return@PlaceFinderPopup

                waypointInput.clearFocusAndHideKeyboard()
                placeFinderViewModel.cancelSearch()

                showSnackbar(
                    binding.routePlannerContainer,
                    R.string.place_finder_pick_location_message.toMessage(),
                    R.drawable.ic_snackbar_place_finder_pick_location
                )

                pickLocationEventViewModel.updateEvent(PickLocationEvents.RoutePlannerPickEnabled)
            }
        )
    }

    private fun initFlows() {
        initRoutePlannerFlows()
        initPlaceFinderFlows()
        initSharedFlows()
    }

    private fun initRoutePlannerFlows() {
        lifecycleScope.launch {
            routePlannerViewModel.waypointItems
                .flowWithLifecycle(lifecycle)
                .collect { wayPointItems ->
                    if (wayPointItems.isEmpty()) {
                        return@collect
                    }

                    waypointAdapter.submitList(wayPointItems)
                    waypointList.smoothScrollToPosition(wayPointItems.lastIndex)
                }
        }
        lifecycleScope.launch {
            routePlannerViewModel.routePlanUiModel
                .flowWithLifecycle(lifecycle)
                .collect { routePlanUiModel ->
                    initRoutePlan(routePlanUiModel)
                }
        }
        lifecycleScope.launch {
            routePlannerViewModel.isRoutePlanLoading
                .flowWithLifecycle(lifecycle)
                .collect { isLoading ->
                    doneButton.inProgress = isLoading
                }
        }
        lifecycleScope.launch {
            routePlannerViewModel.routePlanErrorMessage
                .flowWithLifecycle(lifecycle)
                .collect { errorMessage ->
                    if (errorMessage != null) {
                        routeAttributesContainer.gone()
                        doneButton.disabled = true
                    }
                    errorText.setMessageOrGone(errorMessage)
                }
        }
        lifecycleScope.launch {
            routePlannerViewModel.routePlanGpxFileUri
                .flowWithLifecycle(lifecycle)
                .collect { gpxFileUri ->
                    layersViewModel.loadRoutePlannerGpx(gpxFileUri)

                    clearRoutePlanner()
                }
        }
        lifecycleScope.launch {
            pickLocationEventViewModel.event
                .flowWithLifecycle(lifecycle)
                .collect { event ->
                    if (event is PickLocationEvents.RoutePlannerPickEnded) {
                        val lastEditedWaypoint = lastEditedWaypointItem ?: return@collect
                        val location = event.geoPoint.toLocation()

                        routePlannerViewModel.updateByPickedLocation(lastEditedWaypoint, location)
                    }
                }
        }
    }

    private fun initPlaceFinderFlows() {
        lifecycleScope.launch {
            placeFinderViewModel.placeFinderItems
                .flowWithLifecycle(lifecycle)
                .collect { placeFinderItems ->
                    if (placeFinderItems != null) {
                        val parent = lastEditedWaypointInput?.parent as? View ?: return@collect

                        placeFinderPopup.initPlaceFinderItems(parent, placeFinderItems)
                    } else {
                        placeFinderPopup.clearPlaceFinderItems()
                    }
                }
        }
    }

    private fun initSharedFlows() {
        lifecycleScope.launch {
            insetSharedViewModel.result
                .flowWithLifecycle(lifecycle)
                .collect { result ->
                    if (result != null) {
                        routePlannerContainer.updatePadding(
                            top = resources.getDimensionPixelSize(R.dimen.space_small) + result.insets.top,
                        )
                    }
                }
        }
        lifecycleScope.launch {
            mapTouchEventSharedViewModel.event
                .flowWithLifecycle(lifecycle)
                .collect { event ->
                    if (event == MapTouchEvents.MAP_TOUCHED) {
                        lastEditedWaypointInput?.clearFocus()
                        placeFinderViewModel.cancelSearch()
                    }
                }
        }
        lifecycleScope.launch {
            commentResultViewModel.result
                .flowWithLifecycle(lifecycle)
                .filterNotNull()
                .collect { result ->
                    routePlannerViewModel.addWaypointComment(result)
                }
        }
    }

    private fun initRoutePlan(routePlanUiModel: RoutePlanUiModel?) {
        if (routePlanUiModel != null) {
            routeAttributesContainer.visible()
            with(binding.routePlannerRouteAttributesContainer) {
                routeAttributesTimeText.setMessage(routePlanUiModel.travelTimeText)
                routeAttributesDistanceText.setMessage(routePlanUiModel.distanceText)
                routeAttributesUphillText.setMessage(routePlanUiModel.altitudeUiModel.uphillText)
                routeAttributesDownhillText.setMessage(routePlanUiModel.altitudeUiModel.downhillText)
            }
            doneButton.disabled = false
            returnToHomeButton.visibleOrGone(routePlanUiModel.isReturnToHomeAvailable)
        } else {
            routeAttributesContainer.gone()
            doneButton.disabled = true
            returnToHomeButton.gone()
        }
    }

    private fun clearRoutePlanner() {
        pickLocationEventViewModel.updateEvent(PickLocationEvents.LocationPickEnabled)
        routePlannerViewModel.clearRoutePlanner()
        commentResultViewModel.clearResult()
        parentFragmentManager.popBackStack()
    }

}

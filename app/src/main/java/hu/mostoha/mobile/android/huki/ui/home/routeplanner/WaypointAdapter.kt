package hu.mostoha.mobile.android.huki.ui.home.routeplanner

import android.content.Context
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ItemRoutePlannerWaypointBinding
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.invisible
import hu.mostoha.mobile.android.huki.extensions.resolve
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.ui.home.placefinder.PlaceFinderPopup.Companion.PLACE_FINDER_MIN_TRIGGER_LENGTH
import hu.mostoha.mobile.android.huki.views.DefaultDiffUtilCallback

class WaypointAdapter(
    private val onSearchTextFocused: (TextInputLayout, WaypointItem) -> Unit,
    private val onSearchTextChanged: (TextInputLayout, WaypointItem, String) -> Unit,
    private val onAddWaypointClicked: () -> Unit,
    private val onReturnClicked: (WaypointItem) -> Unit,
    private val onRemoveWaypointClicked: (WaypointItem) -> Unit,
) : ListAdapter<WaypointItem, RecyclerView.ViewHolder>(DefaultDiffUtilCallback()) {

    companion object {
        private const val MAX_WAYPOINT_COUNT = 6
        private const val POSITION_RETURN_TO_HOME = 0
        private const val POSITION_ADDITION = 1
    }

    private var isAdditionDisabled: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = parent.context.inflater

        return ViewHolderItem(ItemRoutePlannerWaypointBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderItem -> {
                holder.bind((getItem(position) as WaypointItem))
            }
        }
    }

    override fun submitList(list: List<WaypointItem>?) {
        val hasToDisableAddition = list != null && list.size >= MAX_WAYPOINT_COUNT
        val hasToNotifyAdditionChanged = isAdditionDisabled != hasToDisableAddition

        isAdditionDisabled = hasToDisableAddition

        super.submitList(list)

        if (hasToNotifyAdditionChanged) {
            notifyItemChanged(POSITION_ADDITION)
        }
    }

    inner class ViewHolderItem(
        private val binding: ItemRoutePlannerWaypointBinding,
        private val context: Context = binding.root.context,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(wayPointItem: WaypointItem) {
            with(binding) {
                initWaypointInput(wayPointItem)
                initWaypointImage(wayPointItem)
                initActionButton(wayPointItem)
            }
        }

        private fun ItemRoutePlannerWaypointBinding.initWaypointInput(wayPointItem: WaypointItem) {
            routePlannerWaypointInput.setText(wayPointItem.primaryText?.resolve(context))
            routePlannerWaypointInput.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    onSearchTextFocused.invoke(routePlannerWaypointInputLayout, wayPointItem)
                }
            }
            routePlannerWaypointInput.addTextChangedListener { editable ->
                val text = editable.toString()

                if (routePlannerWaypointInput.hasFocus() && text.length >= PLACE_FINDER_MIN_TRIGGER_LENGTH) {
                    onSearchTextChanged.invoke(routePlannerWaypointInputLayout, wayPointItem, text)
                }
            }
            routePlannerWaypointInputLayout.setEndIconOnClickListener {}
        }

        private fun ItemRoutePlannerWaypointBinding.initWaypointImage(wayPointItem: WaypointItem) {
            when (wayPointItem.waypointType) {
                WaypointType.START -> {
                    routePlannerDashedDividerBottom.visible()
                    routePlannerDashedDividerTop.invisible()
                    routePlannerWaypointImage.setImageResource(R.drawable.ic_route_planner_waypoint_start)
                }
                WaypointType.INTERMEDIATE -> {
                    routePlannerDashedDividerTop.visible()
                    routePlannerDashedDividerBottom.visible()
                    routePlannerWaypointImage.setImageResource(R.drawable.ic_route_planner_waypoint_intermediate)
                }
                WaypointType.END -> {
                    routePlannerDashedDividerTop.visible()
                    routePlannerDashedDividerBottom.invisible()
                    routePlannerWaypointImage.setImageResource(R.drawable.ic_route_planner_waypoint_end)
                }
            }
        }

        private fun ItemRoutePlannerWaypointBinding.initActionButton(wayPointItem: WaypointItem) {
            when (wayPointItem.order) {
                POSITION_RETURN_TO_HOME -> {
                    // TODO Add return to home function
                    routePlannerWaypointActionButton.invisible()
                    routePlannerWaypointActionButton.setIconResource(R.drawable.ic_route_planner_return)
                    routePlannerWaypointActionButton.contentDescription = context.getString(
                        R.string.route_planner_accessibility_return_waypoint
                    )
                    routePlannerWaypointActionButton.setOnClickListener {
                        onReturnClicked.invoke(wayPointItem)
                    }
                }
                POSITION_ADDITION -> {
                    if (isAdditionDisabled) {
                        routePlannerWaypointActionButton.invisible()
                    } else {
                        routePlannerWaypointActionButton.visible()
                        routePlannerWaypointActionButton.setIconResource(R.drawable.ic_route_planner_plus)
                        routePlannerWaypointActionButton.contentDescription = context.getString(
                            R.string.route_planner_accessibility_add_waypoint
                        )
                        routePlannerWaypointActionButton.setOnClickListener {
                            onAddWaypointClicked.invoke()
                        }
                    }
                }
                else -> {
                    routePlannerWaypointActionButton.visible()
                    routePlannerWaypointActionButton.setIconResource(R.drawable.ic_route_planner_remove)
                    routePlannerWaypointActionButton.contentDescription = context.getString(
                        R.string.route_planner_accessibility_remove_waypoint
                    )
                    routePlannerWaypointActionButton.setOnClickListener {
                        onRemoveWaypointClicked.invoke(wayPointItem)
                    }
                }
            }
        }
    }

}
package hu.mostoha.mobile.android.huki.ui.home.routeplanner

import android.content.Context
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ItemRoutePlannerWaypointBinding
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.invisible
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.views.DefaultDiffUtilCallback

class WaypointAdapter(
    private val onSearchTextFocused: (TextInputLayout, WaypointItem) -> Unit,
    private val onSearchTextChanged: (TextInputLayout, WaypointItem, String) -> Unit,
    private val onSearchTextDoneAction: (TextInputEditText) -> Unit,
    private val onWaypointsSizeChanged: (Int) -> Unit,
    private val onRemoveWaypointClicked: (WaypointItem) -> Unit,
    private val onCommentClicked: (WaypointItem) -> Unit,
) : ListAdapter<WaypointItem, RecyclerView.ViewHolder>(DefaultDiffUtilCallback()) {

    companion object {
        private const val FIRST_REMOVAL_POSITION = 2
    }

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

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    override fun submitList(list: List<WaypointItem>?) {
        super.submitList(list)

        onWaypointsSizeChanged.invoke(list?.size ?: 0)
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
                onSearchTextChanged.invoke(routePlannerWaypointInputLayout, wayPointItem, editable.toString())
            }
            routePlannerWaypointInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onSearchTextDoneAction.invoke(routePlannerWaypointInput)
                    true
                } else {
                    false
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
                else -> Unit
            }
        }

        private fun ItemRoutePlannerWaypointBinding.initActionButton(wayPointItem: WaypointItem) {
            val isCommentVisible = wayPointItem.primaryText != null

            if (wayPointItem.order >= FIRST_REMOVAL_POSITION) {
                routePlannerWaypointRemoveButton.visible()
                routePlannerWaypointRemoveButton.contentDescription = context.getString(
                    R.string.route_planner_accessibility_remove_waypoint
                )
                routePlannerWaypointRemoveButton.setOnClickListener {
                    onRemoveWaypointClicked.invoke(wayPointItem)
                }
            } else {
                if (isCommentVisible) {
                    routePlannerWaypointRemoveButton.gone()
                } else {
                    routePlannerWaypointRemoveButton.invisible()
                }
            }

            if (isCommentVisible) {
                routePlannerWaypointCommentButton.visible()
                routePlannerWaypointCommentButton.contentDescription = context.getString(
                    R.string.route_planner_accessibility_comment
                )
                routePlannerWaypointCommentButton.setOnClickListener {
                    onCommentClicked.invoke(wayPointItem)
                }
                if (wayPointItem.waypointComment != null) {
                    routePlannerWaypointCommentButton.setIconResource(R.drawable.ic_route_planner_comment_done)
                    routePlannerWaypointCommentButton.setIconTintResource(R.color.colorPrimary)
                } else {
                    routePlannerWaypointCommentButton.setIconResource(R.drawable.ic_route_planner_comment)
                    routePlannerWaypointCommentButton.setIconTintResource(R.color.colorPrimaryIcon)
                }
            } else {
                routePlannerWaypointCommentButton.gone()
            }
        }
    }

}

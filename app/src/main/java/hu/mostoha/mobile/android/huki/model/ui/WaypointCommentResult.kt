package hu.mostoha.mobile.android.huki.model.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WaypointCommentResult(
    val waypointId: Long,
    val waypointComment: WaypointComment
) : Parcelable

@Parcelize
data class WaypointComment(
    val name: String,
    val comment: String?
) : Parcelable

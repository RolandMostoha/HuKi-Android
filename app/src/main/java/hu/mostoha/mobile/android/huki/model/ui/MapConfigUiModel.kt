package hu.mostoha.mobile.android.huki.model.ui

import android.os.Parcelable
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.util.HUNGARY_BOUNDING_BOX
import kotlinx.parcelize.Parcelize

@Parcelize
data class MapConfigUiModel(
    val boundingBox: BoundingBox = HUNGARY_BOUNDING_BOX,
) : Parcelable

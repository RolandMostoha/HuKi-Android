package hu.mostoha.mobile.android.huki.model.ui

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.util.HUNGARY_BOUNDING_BOX

data class MapUiModel(
    val boundingBox: BoundingBox = HUNGARY_BOUNDING_BOX,
    val withDefaultOffset: Boolean = true,
)

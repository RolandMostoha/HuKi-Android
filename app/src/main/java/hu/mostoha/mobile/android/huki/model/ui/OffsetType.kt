package hu.mostoha.mobile.android.huki.model.ui

import androidx.annotation.DimenRes
import hu.mostoha.mobile.android.huki.R

enum class OffsetType(
    @DimenRes val top: Int,
    @DimenRes val bottom: Int,
    @DimenRes val left: Int,
    @DimenRes val right: Int,
) {
    BOTTOM_SHEET(
        top = R.dimen.map_view_default_top_offset,
        bottom = R.dimen.map_view_bottom_sheet_bottom_offset,
        left = R.dimen.map_view_default_start_offset,
        right = R.dimen.map_view_default_end_offset,
    ),
    TOP_SHEET(
        top = R.dimen.map_view_top_sheet_top_offset,
        bottom = R.dimen.map_view_top_sheet_bottom_offset,
        left = R.dimen.map_view_top_sheet_start_offset,
        right = R.dimen.map_view_top_sheet_end_offset,
    ),
    LANDSCAPE(
        top = R.dimen.map_view_landscape_top_offset,
        bottom = R.dimen.map_view_landscape_bottom_offset,
        left = R.dimen.map_view_landscape_start_offset,
        right = R.dimen.map_view_landscape_end_offset,
    ),
    LANDSCAPE_EXTRA_OFFSET(
        top = R.dimen.map_view_landscape_top_offset,
        bottom = R.dimen.map_view_landscape_extra_bottom_offset,
        left = R.dimen.map_view_landscape_extra_start_offset,
        right = R.dimen.map_view_landscape_extra_end_offset,
    ),
    OKT_ROUTES(
        top = R.dimen.map_view_okt_routes_top_offset,
        bottom = R.dimen.map_view_okt_routes_bottom_offset,
        left = R.dimen.map_view_default_start_offset,
        right = R.dimen.map_view_default_end_offset,
    ),
}

package hu.mostoha.mobile.android.huki.ui.home.history

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import hu.mostoha.mobile.android.huki.model.domain.GpxType
import hu.mostoha.mobile.android.huki.ui.home.history.gpx.GpxHistoryFragment
import hu.mostoha.mobile.android.huki.ui.home.history.place.PlaceHistoryFragment

class HistoryViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = HistoryTab.values().size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            HistoryTab.PLACES.ordinal -> PlaceHistoryFragment()
            HistoryTab.ROUTE_PLANNER.ordinal -> GpxHistoryFragment.newInstance(GpxType.ROUTE_PLANNER)
            HistoryTab.EXTERNAL.ordinal -> GpxHistoryFragment.newInstance(GpxType.EXTERNAL)
            else -> throw IllegalArgumentException("Not supported history tab")
        }
    }

}

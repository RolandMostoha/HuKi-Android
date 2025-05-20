package hu.mostoha.mobile.android.huki.ui.home.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.FragmentHistoryBinding
import hu.mostoha.mobile.android.huki.extensions.toDrawable
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.home.shared.InsetSharedViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    @Inject
    lateinit var analyticsService: AnalyticsService

    private val insetSharedViewModel: InsetSharedViewModel by activityViewModels()

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val historyContainer by lazy { binding.historyContainer }
    private val historyTabLayout by lazy { binding.historyTabLayout }
    private val historyViewPager by lazy { binding.historyViewPager }
    private val toolbar by lazy { binding.historyToolbar }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initFlows()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun initViews() {
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        historyViewPager.offscreenPageLimit = HistoryTab.entries.size
        historyViewPager.adapter = HistoryViewPagerAdapter(this)

        TabLayoutMediator(historyTabLayout, historyViewPager) { tab, position ->
            when (position) {
                HistoryTab.PLACES.ordinal -> {
                    tab.text = getString(R.string.history_tab_places)
                    tab.icon = R.drawable.ic_history_places.toDrawable(requireContext())
                }
                HistoryTab.ROUTE_PLANNER.ordinal -> {
                    tab.text = getString(R.string.history_tab_route_planner)
                    tab.icon = R.drawable.ic_history_route_planner.toDrawable(requireContext())
                }
                HistoryTab.EXTERNAL.ordinal -> {
                    tab.text = getString(R.string.history_tab_external_gpx)
                    tab.icon = R.drawable.ic_history_external.toDrawable(requireContext())
                }
                else -> throw IllegalArgumentException("Not supported history tab")
            }
        }.attach()
    }

    private fun initFlows() {
        lifecycleScope.launch {
            insetSharedViewModel.result
                .flowWithLifecycle(lifecycle)
                .collect { result ->
                    if (result != null) {
                        historyContainer.updatePadding(
                            top = resources.getDimensionPixelSize(R.dimen.space_small) + result.insets.top,
                        )
                    }
                }
        }
    }

}

package hu.mostoha.mobile.android.huki.ui.home.history.place

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.huki.databinding.FragmentPlaceHistoryBinding
import hu.mostoha.mobile.android.huki.extensions.showToast
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.home.HomeViewModel
import jp.wasabeef.recyclerview.animators.FadeInAnimator
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaceHistoryFragment : Fragment() {

    @Inject
    lateinit var analyticsService: AnalyticsService

    private val viewModel: PlaceHistoryViewModel by viewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()

    private var _binding: FragmentPlaceHistoryBinding? = null
    private val binding get() = _binding!!

    private val historyList by lazy { binding.placeHistoryList }

    private lateinit var historyAdapter: PlaceHistoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlaceHistoryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initFlows()
    }

    private fun initViews() {
        historyAdapter = PlaceHistoryAdapter(
            onPlaceOpen = { item ->
                analyticsService.placeHistoryItemDelete()

                homeViewModel.loadPlaceDetails(item.placeUiModel)

                requireActivity().supportFragmentManager.popBackStack()
            },
            onPlaceDelete = { item ->
                analyticsService.placeHistoryItemDelete()

                viewModel.deletePlace(item.placeUiModel.osmId)
            },
        )

        historyList.adapter = historyAdapter
        historyList.itemAnimator = FadeInAnimator()
    }

    private fun initFlows() {
        lifecycleScope.launch {
            viewModel.placeHistory
                .flowWithLifecycle(lifecycle)
                .collect { places ->
                    historyAdapter.submitList(places)
                }
        }
        lifecycleScope.launch {
            viewModel.errorMessage
                .flowWithLifecycle(lifecycle)
                .collect { errorMessage ->
                    requireContext().showToast(errorMessage)
                }
        }
    }

}

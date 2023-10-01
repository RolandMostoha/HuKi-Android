package hu.mostoha.mobile.android.huki.ui.home.gpx.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.FragmentGpxHistoryBinding
import hu.mostoha.mobile.android.huki.extensions.shareFile
import hu.mostoha.mobile.android.huki.extensions.showToast
import hu.mostoha.mobile.android.huki.model.domain.GpxType
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersViewModel
import hu.mostoha.mobile.android.huki.ui.home.shared.InsetSharedViewModel
import jp.wasabeef.recyclerview.animators.FadeInAnimator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GpxHistoryFragment : Fragment() {

    @Inject
    lateinit var analyticsService: AnalyticsService

    private val viewModel: GpxHistoryViewModel by viewModels()
    private val layersViewModel: LayersViewModel by activityViewModels()
    private val insetSharedViewModel: InsetSharedViewModel by activityViewModels()
    private val gpxRenameSharedViewModel: GpxRenameSharedViewModel by activityViewModels()

    private var _binding: FragmentGpxHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var gpxHistoryAdapter: GpxHistoryAdapter

    private val gpxHistoryContainer by lazy { binding.gpxHistoryContainer }
    private val gpxHistoryList by lazy { binding.gpxHistoryList }
    private val gpxHistoryTabLayout by lazy { binding.gpxHistoryTabLayout }
    private val toolbar by lazy { binding.gpxHistoryToolbar }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGpxHistoryBinding.inflate(inflater, container, false)

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
        gpxHistoryAdapter = GpxHistoryAdapter(
            onGpxOpen = { gpxHistoryItem ->
                analyticsService.gpxHistoryItemOpened(gpxHistoryItem.gpxType)

                lifecycleScope.launch {
                    when (gpxHistoryItem.gpxType) {
                        GpxType.ROUTE_PLANNER -> layersViewModel.loadRoutePlannerGpx(gpxHistoryItem.fileUri)
                        GpxType.EXTERNAL -> layersViewModel.loadGpx(gpxHistoryItem.fileUri)
                    }
                    parentFragmentManager.popBackStack()
                }
            },
            onGpxShare = { gpxHistoryItem ->
                analyticsService.gpxHistoryItemShared()

                requireContext().shareFile(gpxHistoryItem.fileUri)
            },
            onGpxRename = { item ->
                lifecycleScope.launch {
                    analyticsService.gpxHistoryItemRename()

                    val fileNames = viewModel.gpxHistoryFileNames.first()

                    GpxRenameBottomSheetDialogFragment.showDialog(requireActivity(), item.fileUri, fileNames)
                }
            },
            onGpxDelete = { item ->
                analyticsService.gpxHistoryItemDelete()

                viewModel.deleteGpx(item.fileUri)
            },
        )

        gpxHistoryList.adapter = gpxHistoryAdapter
        gpxHistoryList.itemAnimator = FadeInAnimator()

        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        gpxHistoryTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.tabSelected(
                    when (tab.position) {
                        GpxHistoryTab.ROUTE_PLANNER.ordinal -> GpxHistoryTab.ROUTE_PLANNER
                        GpxHistoryTab.EXTERNAL.ordinal -> GpxHistoryTab.EXTERNAL
                        else -> GpxHistoryTab.ROUTE_PLANNER
                    }
                )
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                /** no-op **/
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                /** no-op **/
            }
        })
    }

    private fun initFlows() {
        lifecycleScope.launch {
            viewModel.gpxHistoryAdapterItems
                .flowWithLifecycle(lifecycle)
                .collect { gpxHistoryItems ->
                    gpxHistoryAdapter.submitList(gpxHistoryItems)
                }
        }
        lifecycleScope.launch {
            viewModel.currentTab
                .flowWithLifecycle(lifecycle)
                .collect {
                    gpxHistoryTabLayout.selectTab(gpxHistoryTabLayout.getTabAt(it.ordinal))
                }
        }
        lifecycleScope.launch {
            viewModel.errorMessage
                .flowWithLifecycle(lifecycle)
                .collect { errorMessage ->
                    requireContext().showToast(errorMessage)
                }
        }
        lifecycleScope.launch {
            insetSharedViewModel.result
                .flowWithLifecycle(lifecycle)
                .collect { result ->
                    if (result != null) {
                        gpxHistoryContainer.updatePadding(
                            top = resources.getDimensionPixelSize(R.dimen.space_small) + result.topInset,
                        )
                    }
                }
        }
        lifecycleScope.launch {
            gpxRenameSharedViewModel.result
                .flowWithLifecycle(lifecycle)
                .collect { result ->
                    if (result != null) {
                        viewModel.renameGpx(result)
                        gpxRenameSharedViewModel.clearResult()
                    }
                }
        }
    }

}

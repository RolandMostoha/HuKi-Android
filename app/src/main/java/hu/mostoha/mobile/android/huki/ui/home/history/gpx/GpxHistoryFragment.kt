package hu.mostoha.mobile.android.huki.ui.home.history.gpx

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
import hu.mostoha.mobile.android.huki.databinding.FragmentGpxHistoryBinding
import hu.mostoha.mobile.android.huki.extensions.shareFile
import hu.mostoha.mobile.android.huki.extensions.showToast
import hu.mostoha.mobile.android.huki.model.domain.GpxType
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.home.history.gpx.rename.GpxRenameBottomSheetDialogFragment
import hu.mostoha.mobile.android.huki.ui.home.history.gpx.rename.GpxRenameResultViewModel
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersViewModel
import jp.wasabeef.recyclerview.animators.FadeInAnimator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GpxHistoryFragment : Fragment() {

    companion object {
        private val ARG_GPX_TYPE = this::class.java.simpleName + "ARG_GPX_TYPE"

        fun newInstance(gpxType: GpxType): GpxHistoryFragment {
            return GpxHistoryFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_GPX_TYPE, gpxType)
                }
            }
        }
    }

    @Inject
    lateinit var analyticsService: AnalyticsService

    private val viewModel: GpxHistoryViewModel by viewModels()
    private val layersViewModel: LayersViewModel by activityViewModels()
    private val gpxRenameResultViewModel: GpxRenameResultViewModel by activityViewModels()

    private var _binding: FragmentGpxHistoryBinding? = null
    private val binding get() = _binding!!

    private val gpxHistoryType by lazy { requireArguments().getSerializable(ARG_GPX_TYPE) as GpxType }

    private val historyList by lazy { binding.gpxHistoryList }

    private lateinit var gpxHistoryAdapter: GpxHistoryAdapter

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
                    requireActivity().supportFragmentManager.popBackStack()
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

        historyList.adapter = gpxHistoryAdapter
        historyList.itemAnimator = FadeInAnimator()
    }

    private fun initFlows() {
        lifecycleScope.launch {
            viewModel.errorMessage
                .flowWithLifecycle(lifecycle)
                .collect { errorMessage ->
                    requireContext().showToast(errorMessage)
                }
        }
        lifecycleScope.launch {
            viewModel.gpxHistory
                .flowWithLifecycle(lifecycle)
                .collect { gpxHistory ->
                    if (gpxHistory != null) {
                        gpxHistoryAdapter.submitList(
                            when (gpxHistoryType) {
                                GpxType.ROUTE_PLANNER -> gpxHistory.routePlannerGpxList
                                GpxType.EXTERNAL -> gpxHistory.externalGpxList
                            }
                        )
                    }
                }
        }
        lifecycleScope.launch {
            gpxRenameResultViewModel.result
                .flowWithLifecycle(lifecycle)
                .collect { result ->
                    if (result != null) {
                        viewModel.renameGpx(result)
                        gpxRenameResultViewModel.clearResult()
                    }
                }
        }
    }

}

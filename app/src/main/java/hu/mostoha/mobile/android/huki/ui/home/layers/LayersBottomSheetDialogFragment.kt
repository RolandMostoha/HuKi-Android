package hu.mostoha.mobile.android.huki.ui.home.layers

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.huki.databinding.FragmentLayersBottomSheetDialogBinding
import hu.mostoha.mobile.android.huki.extensions.clearBackground
import hu.mostoha.mobile.android.huki.extensions.showToast
import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.model.domain.isBase
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersAdapter.Companion.SPAN_COUNT_LAYER_BASE
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersAdapter.Companion.SPAN_COUNT_LAYER_HEADER
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersAdapter.Companion.SPAN_COUNT_LAYER_HIKING
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersAdapter.Companion.SPAN_COUNT_MAX
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LayersBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        private const val GPX_FILE_OPEN_MIME_TYPE = "*/*"

        val TAG = LayersBottomSheetDialogFragment::class.java.simpleName + ".TAG"
    }

    @Inject
    lateinit var analyticsService: AnalyticsService

    private val layersViewModel: LayersViewModel by activityViewModels()

    private var _binding: FragmentLayersBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    private val openGpxFileResultLauncher = registerForActivityResult(OpenDocument()) { gpxFileUri: Uri? ->
        lifecycleScope.launch {
            layersViewModel.loadGpx(gpxFileUri)

            if (gpxFileUri != null) {
                analyticsService.gpxImportedByFileExplorer()
            }

            dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLayersBottomSheetDialogBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (view.parent as View).clearBackground()

        initDialog()
        initFlows()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun initDialog() {
        val sheet = requireDialog() as BottomSheetDialog
        sheet.behavior.skipCollapsed = true
        sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun initFlows() {
        lifecycleScope.launch {
            layersViewModel.layerAdapterItems
                .flowWithLifecycle(lifecycle)
                .collect { layerAdapterItems ->
                    initLayerList(layerAdapterItems)
                }
        }
        lifecycleScope.launch {
            layersViewModel.errorMessage
                .flowWithLifecycle(lifecycle)
                .filterNotNull()
                .collect { messageRes ->
                    requireActivity().showToast(messageRes)
                    layersViewModel.clearError()
                }
        }
    }

    private fun initLayerList(layerAdapterItems: List<LayersAdapterItem>) {
        val layoutManager = GridLayoutManager(requireContext(), SPAN_COUNT_MAX)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (val layer = layerAdapterItems[position]) {
                    is LayersAdapterItem.Header -> SPAN_COUNT_LAYER_HEADER
                    is LayersAdapterItem.Layer -> if (layer.layerType.isBase()) {
                        SPAN_COUNT_LAYER_BASE
                    } else {
                        SPAN_COUNT_LAYER_HIKING
                    }
                }
            }
        }

        val adapter = LayersAdapter(
            onLayerClick = { layerItem ->
                analyticsService.onLayerSelected(layerItem.layerType)
                layersViewModel.selectLayer(layerItem.layerType)
            },
            onActionButtonClick = { layerType ->
                if (layerType == LayerType.GPX) {
                    openGpxFileResultLauncher.launch(arrayOf(GPX_FILE_OPEN_MIME_TYPE))

                    analyticsService.gpxImportClicked()
                }
            }
        )
        binding.layersList.layoutManager = layoutManager
        binding.layersList.adapter = adapter
        adapter.submitList(layerAdapterItems)
    }

}

package hu.mostoha.mobile.android.huki.ui.home.layers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import hu.mostoha.mobile.android.huki.databinding.FragmentLayersBottomSheetDialogBinding
import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersAdapter.Companion.SPAN_COUNT_LAYER_BASE
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersAdapter.Companion.SPAN_COUNT_LAYER_HEADER
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersAdapter.Companion.SPAN_COUNT_LAYER_HIKING
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersAdapter.Companion.SPAN_COUNT_MAX
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class LayersBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        val TAG = LayersBottomSheetDialogFragment::class.java.simpleName + ".TAG"
    }

    private val layersViewModel: LayersViewModel by activityViewModels()

    private var _binding: FragmentLayersBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLayersBottomSheetDialogBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDialog()

        lifecycleScope.launch {
            layersViewModel.layerAdapterItems
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .collect { layerAdapterItems ->
                    initLayerList(layerAdapterItems)
                }
        }
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

    private fun initLayerList(layerAdapterItems: List<LayersAdapterItem>) {
        val layoutManager = GridLayoutManager(requireContext(), SPAN_COUNT_MAX)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (val layersItem = layerAdapterItems[position]) {
                    is LayersAdapterItem.Header -> SPAN_COUNT_LAYER_HEADER
                    is LayersAdapterItem.Layer -> {
                        if (layersItem.layerType == LayerType.ONLINE_HIKING_LAYER) {
                            SPAN_COUNT_LAYER_HIKING
                        } else {
                            SPAN_COUNT_LAYER_BASE
                        }
                    }
                }
            }
        }

        val adapter = LayersAdapter(
            onLayerClick = { layerItem ->
                layersViewModel.selectLayer(layerItem.layerType)
            }
        )

        binding.layersList.layoutManager = layoutManager
        binding.layersList.adapter = adapter

        adapter.submitList(layerAdapterItems)
    }

}

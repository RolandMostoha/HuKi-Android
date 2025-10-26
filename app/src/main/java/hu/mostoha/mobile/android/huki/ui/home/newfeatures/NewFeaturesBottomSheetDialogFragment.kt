package hu.mostoha.mobile.android.huki.ui.home.newfeatures

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.FragmentNewFeaturesBinding
import hu.mostoha.mobile.android.huki.extensions.clearBackground
import hu.mostoha.mobile.android.huki.ui.home.settings.SettingsViewModel

@AndroidEntryPoint
class NewFeaturesBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        private val TAG = NewFeaturesBottomSheetDialogFragment::class.java.simpleName + ".TAG"
        private val ARG_NEW_FEATURES = this::class.java.simpleName + "ARG_NEW_FEATURES"

        fun showDialog(activity: FragmentActivity, newFeatures: String) {
            newInstance(newFeatures).show(activity.supportFragmentManager, TAG)
        }

        private fun newInstance(newFeatures: String): NewFeaturesBottomSheetDialogFragment {
            return NewFeaturesBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NEW_FEATURES, newFeatures)
                }
            }
        }
    }

    private val settingsViewModel: SettingsViewModel by activityViewModels()

    private var _binding: FragmentNewFeaturesBinding? = null
    private val binding get() = _binding!!

    private val newFeatures by lazy { requireArguments().getString(ARG_NEW_FEATURES)!! }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFeaturesBinding.inflate(inflater, container, false)
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    Content(newFeatures)
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (view.parent as View).clearBackground()

        initDialog()

        settingsViewModel.updateNewFeaturesSeen(BuildConfig.VERSION_NAME)
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

    @Composable
    private fun Content(
        features: String
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(R.dimen.space_extra_large),
                    end = dimensionResource(R.dimen.space_medium)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            setImageResource(R.drawable.ic_launcher_small)
                        }
                    },
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    modifier = Modifier
                        .weight(1f),
                    text = stringResource(
                        R.string.new_features_title_template,
                        BuildConfig.VERSION_NAME
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                IconButton(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colorResource(R.color.colorBadgeBackground)),
                    onClick = { dismiss() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bottom_sheet_clear),
                        contentDescription = "close icon"
                    )
                }
            }
            Text(
                text = features,
                fontSize = 13.sp
            )
            Row {
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { dismiss() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.selector_button_stroke_color)
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_done),
                        contentDescription = "close icon"
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.new_features_ok_button)
                    )
                }
            }
        }
    }

    @Preview
    @Composable
    private fun ContentPreview() {
        MaterialTheme {
            Content("- OKT utak és bélyegzőhelyek frissítése\n- Támogatás funkció")
        }
    }
}

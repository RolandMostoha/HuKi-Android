package hu.mostoha.mobile.android.huki.ui.home.support

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.FragmentSupportBinding
import hu.mostoha.mobile.android.huki.extensions.hyperlinkStyle
import hu.mostoha.mobile.android.huki.extensions.startDrawableAnimation
import hu.mostoha.mobile.android.huki.extensions.startEmailIntent
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.home.shared.InsetSharedViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SupportFragment : Fragment() {

    @Inject
    lateinit var analyticsService: AnalyticsService

    private val insetSharedViewModel: InsetSharedViewModel by activityViewModels()

    private var _binding: FragmentSupportBinding? = null
    private val binding get() = _binding!!

    private val toolbar by lazy { binding.supportToolbar }
    private val container by lazy { binding.supportContainer }
    private val swipeRefresh by lazy { binding.supportSwipeRefresh }
    private val animationView by lazy { binding.supportAnimationView }
    private val recurringPaymentsFirstOption by lazy { binding.supportRecurringPaymentsFirstOption }
    private val recurringPaymentsSecondOption by lazy { binding.supportRecurringPaymentsSecondOption }
    private val oneTimePaymentsFirstOption by lazy { binding.supportOneTimePaymentsFirstOption }
    private val oneTimePaymentsSecondOption by lazy { binding.supportOneTimePaymentsSecondOption }
    private val contactEmail by lazy { binding.supportContactEmail }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSupportBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initFlows()
        initBilling()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun initViews() {
        startRandomAnimation()

        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        swipeRefresh.setOnRefreshListener {
            startRandomAnimation()
            swipeRefresh.isRefreshing = false
        }
        recurringPaymentsFirstOption.setOnClickListener {
            analyticsService.supportRecurringFirstOptionClicked()
        }
        recurringPaymentsSecondOption.setOnClickListener {
            analyticsService.supportRecurringSecondOptionClicked()
        }
        oneTimePaymentsFirstOption.setOnClickListener {
            analyticsService.supportOneTimeFirstOptionClicked()
        }
        oneTimePaymentsSecondOption.setOnClickListener {
            analyticsService.supportOneTimeSecondOptionClicked()
        }
        contactEmail.hyperlinkStyle()
        contactEmail.setOnClickListener {
            analyticsService.supportEmailClicked()
            requireContext().startEmailIntent(
                email = getString(R.string.settings_email),
                subject = getString(R.string.settings_email_subject)
            )
        }
    }

    private fun initFlows() {
        lifecycleScope.launch {
            insetSharedViewModel.result
                .flowWithLifecycle(lifecycle)
                .collect { result ->
                    if (result != null) {
                        container.updatePadding(
                            top = resources.getDimensionPixelSize(R.dimen.space_small) + result.topInset
                        )
                    }
                }
        }
    }

    private fun startRandomAnimation() {
        val animations = ANIMATIONS_TO_PADDING.shuffled()
        var animationIndex = 0
        val animation = animations[animationIndex]

        animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) = Unit

            override fun onAnimationEnd(animation: Animator) {
                animationIndex += 1

                if (animationIndex <= animations.size - 1) {
                    val nextAnimation = ANIMATIONS_TO_PADDING[animationIndex]
                    animationView.setAnimation(nextAnimation.first)
                    animationView.setPadding(nextAnimation.second)
                    animationView.startDrawableAnimation()
                }
            }

            override fun onAnimationCancel(animation: Animator) = Unit

            override fun onAnimationRepeat(animation: Animator) = Unit
        })
        animationView.setAnimation(animation.first)
        animationView.setPadding(animation.second)
        animationView.startDrawableAnimation()
    }

    private fun initBilling() {
        BillingClient.newBuilder(requireContext())
            .setListener { billingResult, purchases ->
                Timber.d("Billing: result $billingResult, purchases $purchases")
            }
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()
            .startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Timber.d("Billing: setup finished")
                        // The BillingClient is ready. You can query purchases here.
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Timber.d("Billing: service disconnected")
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            })
    }

    companion object {
        private val ANIMATIONS_TO_PADDING = listOf(
            R.raw.lottie_support_1 to 0,
            R.raw.lottie_support_2 to 0,
            R.raw.lottie_support_3 to 0,
            R.raw.lottie_support_4 to 0,
            R.raw.lottie_support_5 to 0,
            R.raw.lottie_support_6 to 0,
            R.raw.lottie_support_7 to 80,
            R.raw.lottie_support_8 to -120,
        )
    }

}

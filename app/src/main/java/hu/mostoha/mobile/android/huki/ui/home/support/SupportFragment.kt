package hu.mostoha.mobile.android.huki.ui.home.support

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.FragmentSupportBinding
import hu.mostoha.mobile.android.huki.extensions.color
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.hyperlinkStyle
import hu.mostoha.mobile.android.huki.extensions.showOnly
import hu.mostoha.mobile.android.huki.extensions.showToast
import hu.mostoha.mobile.android.huki.extensions.startEmailIntent
import hu.mostoha.mobile.android.huki.extensions.toDrawable
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.model.domain.OneTimeBillingProducts
import hu.mostoha.mobile.android.huki.model.domain.RecurringBillingProducts
import hu.mostoha.mobile.android.huki.model.ui.BillingProduct
import hu.mostoha.mobile.android.huki.model.ui.ProductEvents
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.home.shared.InsetSharedViewModel
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SupportFragment : Fragment() {

    @Inject
    lateinit var analyticsService: AnalyticsService

    private val insetSharedViewModel: InsetSharedViewModel by activityViewModels()
    private val productsViewModel: ProductsViewModel by activityViewModels()

    private var _binding: FragmentSupportBinding? = null
    private val binding get() = _binding!!

    private var supporterAdapter: SupporterAdapter? = null

    private val toolbar by lazy { binding.supportToolbar }
    private val container by lazy { binding.supportContainer }
    private val scrollContainer by lazy { binding.supportScrollContainer }
    private val paymentsContainer by lazy { binding.supportPaymentsContainer }
    private val infoCard by lazy { binding.supportInfoCard }
    private val supporterList by lazy { binding.supportSupporterList }
    private val loadingIndicator by lazy { binding.supportLoadingIndicator.loadingIndicatorContainer }
    private val errorView by lazy { binding.supportErrorView.errorViewContainer }
    private val errorViewText by lazy { binding.supportErrorView.errorViewText }
    private val errorViewRefreshButton by lazy { binding.supportErrorView.errorViewRefreshButton }
    private val oneTimeLevel2Badge by lazy { binding.supportOneTimeLevel2Badge }
    private val oneTimeLevel1Badge by lazy { binding.supportOneTimeLevel1Badge }
    private val recurringLevel2Badge by lazy { binding.supportRecurringLevel2Badge }
    private val recurringLevel1Badge by lazy { binding.supportRecurringLevel1Badge }
    private val contactEmail by lazy { binding.supportContactEmail }

    private val exclusiveViews by lazy {
        listOf(
            paymentsContainer,
            loadingIndicator,
            errorView,
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSupportBinding.inflate(inflater, container, false)

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
        lifecycleScope.launch {
            productsViewModel.productsUiModel
                .map { it.isLoading to it.error }
                .distinctUntilChanged()
                .flowWithLifecycle(lifecycle)
                .collect { (isLoading, error) ->
                    when {
                        isLoading -> exclusiveViews.showOnly(loadingIndicator)
                        error != null -> {
                            exclusiveViews.showOnly(errorView)
                            errorViewText.text = error.resolve(requireContext())
                            errorView.background = R.drawable.background_error_view.toDrawable(requireContext())
                            errorViewRefreshButton.visible()
                            errorViewRefreshButton.setOnClickListener {
                                productsViewModel.initProducts()
                            }
                        }
                        else -> exclusiveViews.showOnly(paymentsContainer)
                    }
                }
        }
        lifecycleScope.launch {
            productsViewModel.productsUiModel
                .map { it.products }
                .distinctUntilChanged()
                .flowWithLifecycle(lifecycle)
                .collect { billingProducts ->
                    initBillingProducts(billingProducts)
                }
        }
        lifecycleScope.launch {
            productsViewModel.productsUiModel
                .map { it.purchases }
                .distinctUntilChanged()
                .flowWithLifecycle(lifecycle)
                .collect { purchases ->
                    if (purchases.isNotEmpty()) {
                        scrollContainer.smoothScrollTo(0, 0)

                        infoCard.gone()
                        supporterList.visible()

                        if (supporterAdapter == null) {
                            supporterAdapter = SupporterAdapter()
                            supporterList.setHasFixedSize(true)
                            supporterList.adapter = supporterAdapter
                            supporterList.itemAnimator = SlideInUpAnimator()
                        }

                        supporterAdapter?.submitList(purchases.map { it.productType })
                    } else {
                        infoCard.visible()
                        supporterList.gone()
                    }
                }
        }
        lifecycleScope.launch {
            productsViewModel.productsEvents
                .flowWithLifecycle(lifecycle)
                .collect { events ->
                    when (events) {
                        is ProductEvents.Error -> {
                            requireContext().showToast(events.error)
                        }
                    }
                }
        }
    }

    private fun initBillingProducts(billingProducts: List<BillingProduct>) {
        billingProducts.forEach { billingProduct ->
            when (val type = billingProduct.type) {
                OneTimeBillingProducts.LEVEL_1 -> {
                    oneTimeLevel1Badge.productColor = requireContext().color(type.productColorRes)
                    oneTimeLevel1Badge.badgeIcon = type.productIcon
                    oneTimeLevel1Badge.badgeTitle = type.productName.resolve(requireContext())
                    oneTimeLevel1Badge.badgeSubtitle = billingProduct.priceText.resolve(requireContext())
                    oneTimeLevel1Badge.setOnClickListener {
                        analyticsService.supportOneTimeLevel1Clicked()

                        launchBillingFlow(billingProduct.productDetails)
                    }
                }
                OneTimeBillingProducts.LEVEL_2 -> {
                    oneTimeLevel2Badge.productColor = requireContext().color(type.productColorRes)
                    oneTimeLevel2Badge.badgeIcon = type.productIcon
                    oneTimeLevel2Badge.badgeTitle = type.productName.resolve(requireContext())
                    oneTimeLevel2Badge.badgeSubtitle = billingProduct.priceText.resolve(requireContext())
                    oneTimeLevel2Badge.setOnClickListener {
                        analyticsService.supportOneTimeLevel2Clicked()

                        launchBillingFlow(billingProduct.productDetails)
                    }
                }
                RecurringBillingProducts.LEVEL_1 -> {
                    recurringLevel1Badge.productColor = requireContext().color(type.productColorRes)
                    recurringLevel1Badge.badgeIcon = type.productIcon
                    recurringLevel1Badge.badgeTitle = type.productName.resolve(requireContext())
                    recurringLevel1Badge.badgeSubtitle = billingProduct.priceText.resolve(requireContext())
                    recurringLevel1Badge.setOnClickListener {
                        analyticsService.supportRecurringLevel1Clicked()

                        launchBillingFlow(billingProduct.productDetails)
                    }
                }
                RecurringBillingProducts.LEVEL_2 -> {
                    recurringLevel2Badge.productColor = requireContext().color(type.productColorRes)
                    recurringLevel2Badge.badgeIcon = type.productIcon
                    recurringLevel2Badge.badgeTitle = type.productName.resolve(requireContext())
                    recurringLevel2Badge.badgeSubtitle = billingProduct.priceText.resolve(requireContext())
                    recurringLevel2Badge.setOnClickListener {
                        analyticsService.supportRecurringLevel2Clicked()

                        launchBillingFlow(billingProduct.productDetails)
                    }
                }
            }
        }
    }

    private fun launchBillingFlow(productDetails: ProductDetails) {
        val billingFlowBuilder = BillingFlowParams.ProductDetailsParams
            .newBuilder()
            .setProductDetails(productDetails)

        productDetails.subscriptionOfferDetails?.let {
            billingFlowBuilder.setOfferToken(it.first().offerToken)
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(billingFlowBuilder.build()))
            .build()

        productsViewModel.launchBillingFlow(requireActivity(), billingFlowParams)
    }

}

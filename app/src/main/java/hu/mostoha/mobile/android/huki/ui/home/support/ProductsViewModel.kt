package hu.mostoha.mobile.android.huki.ui.home.support

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchaseHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.billing.BillingResponseHandler
import hu.mostoha.mobile.android.huki.di.module.IoDispatcher
import hu.mostoha.mobile.android.huki.model.domain.OneTimeBillingProducts
import hu.mostoha.mobile.android.huki.model.domain.RecurringBillingProducts
import hu.mostoha.mobile.android.huki.model.domain.isOneTime
import hu.mostoha.mobile.android.huki.model.mapper.ProductsUiModelMapper
import hu.mostoha.mobile.android.huki.model.ui.BillingAction
import hu.mostoha.mobile.android.huki.model.ui.ProductEvents
import hu.mostoha.mobile.android.huki.model.ui.ProductsUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val billingResponseHandler: BillingResponseHandler,
    private val productsUiModelMapper: ProductsUiModelMapper,
    private val analyticsService: AnalyticsService
) : ViewModel(), PurchasesUpdatedListener {

    private val _productsUiModel = MutableStateFlow(ProductsUiModel())
    val productsUiModel: SharedFlow<ProductsUiModel> = _productsUiModel
        .stateIn(viewModelScope, WhileViewSubscribed, ProductsUiModel())

    private val _productsEvents = MutableSharedFlow<ProductEvents>()
    val productsEvents: SharedFlow<ProductEvents> = _productsEvents.asSharedFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .build()

    init {
        initProducts()
    }

    fun initProducts() {
        startBillingConnection()
    }

    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                billingResponseHandler.handleBillingResponse(
                    billingAction = BillingAction.START_CONNECTION,
                    billingResult = billingResult,
                    onSuccess = {
                        loadPurchaseHistory()
                        loadProducts()
                    },
                    onError = {
                        _productsUiModel.update {
                            it.copy(
                                isLoading = false,
                                error = BillingAction.START_CONNECTION.toMessage()
                            )
                        }
                    }
                )
            }

            override fun onBillingServiceDisconnected() {
                Timber.w("Billing: service disconnected")
                _productsUiModel.update {
                    it.copy(
                        isLoading = false,
                        error = BillingAction.START_CONNECTION.toMessage()
                    )
                }
            }
        })
    }

    fun loadProducts() {
        viewModelScope.launch(ioDispatcher) {
            val oneTimeProductDetails = billingClient.queryProductDetails(
                QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        OneTimeBillingProducts.entries.map {
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(it.productId)
                                .setProductType(it.productType)
                                .build()
                        }
                    )
                    .build()
            )
            val recurringProductDetails = billingClient.queryProductDetails(
                QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        RecurringBillingProducts.entries.map {
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(it.productId)
                                .setProductType(it.productType)
                                .build()
                        }
                    )
                    .build()
            )

            billingResponseHandler.handleBillingResponse(
                billingAction = BillingAction.QUERY_PRODUCTS,
                billingResult = oneTimeProductDetails.billingResult
            )
            billingResponseHandler.handleBillingResponse(
                billingAction = BillingAction.QUERY_PRODUCTS,
                billingResult = recurringProductDetails.billingResult
            )

            val oneTimeList = oneTimeProductDetails.productDetailsList.orEmpty()
            val recurringList = recurringProductDetails.productDetailsList.orEmpty()

            if (oneTimeList.isEmpty() && recurringList.isEmpty()) {
                analyticsService.billingEvent(BillingAction.QUERY_PRODUCTS, BillingResponseCode.ITEM_UNAVAILABLE)

                _productsUiModel.update {
                    it.copy(
                        products = emptyList(),
                        isLoading = false,
                        error = R.string.support_error_query_products.toMessage(),
                    )
                }
            } else {
                _productsUiModel.update {
                    it.copy(
                        products = productsUiModelMapper.mapOneTimeProducts(oneTimeList)
                            .plus(productsUiModelMapper.mapRecurringProducts(recurringList)),
                        isLoading = false,
                        error = null,
                    )
                }
            }
        }
    }

    private fun loadPurchaseHistory() {
        viewModelScope.launch {
            val historyQuery = QueryPurchaseHistoryParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            val historyResult = billingClient.queryPurchaseHistory(historyQuery)

            billingResponseHandler.handleBillingResponse(
                billingAction = BillingAction.QUERY_PURCHASES,
                billingResult = historyResult.billingResult,
                onSuccess = { _ ->
                    val recurringQuery = QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                    billingClient.queryPurchasesAsync(recurringQuery) { billingResult, recurringPurchases ->
                        billingResponseHandler.handleBillingResponse(
                            billingAction = BillingAction.QUERY_PURCHASES,
                            billingResult = billingResult,
                            onSuccess = { _ ->
                                val oneTimePurchases = historyResult.purchaseHistoryRecordList.orEmpty()

                                _productsUiModel.update { uiModel ->
                                    uiModel.copy(
                                        purchases = productsUiModelMapper.mapPurchases(recurringPurchases)
                                            .plus(productsUiModelMapper.mapHistoryPurchases(oneTimePurchases))
                                            .sortedByDescending { it.purchaseTime }
                                    )
                                }
                            },
                            onError = {
                                _productsEvents.tryEmit(
                                    ProductEvents.Error(BillingAction.QUERY_PURCHASES.toMessage())
                                )
                            }
                        )
                    }
                }
            )
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        billingResponseHandler.handleBillingResponse(
            billingAction = BillingAction.PURCHASES_UPDATED,
            billingResult = billingResult,
            onSuccess = { _ ->
                if (!purchases.isNullOrEmpty()) {
                    val purchase = purchases.first()

                    if (purchase.purchaseState == PurchaseState.PURCHASED) {
                        viewModelScope.launch {
                            if (purchase.products.first().isOneTime()) {
                                consume(purchase.purchaseToken)
                            } else {
                                acknowledge(purchase.purchaseToken)
                            }

                            loadPurchaseHistory()
                        }
                    }
                }
            },
            onError = { result ->
                if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
                    viewModelScope.launch {
                        _productsEvents.emit(ProductEvents.Error(BillingAction.PURCHASES_UPDATED.toMessage()))
                    }
                }
            }
        )
    }

    fun launchBillingFlow(activity: Activity, billingFlowParams: BillingFlowParams) {
        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

        billingResponseHandler.handleBillingResponse(
            billingAction = BillingAction.LAUNCH_BILLING_FLOW,
            billingResult = billingResult,
            onError = { _ ->
                viewModelScope.launch {
                    _productsEvents.emit(ProductEvents.Error(BillingAction.LAUNCH_BILLING_FLOW.toMessage()))
                }
            }
        )
    }

    private suspend fun acknowledge(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()
        val acknowledgeResult = billingClient.acknowledgePurchase(params)

        billingResponseHandler.handleBillingResponse(
            billingAction = BillingAction.ACKNOWLEDGE_PURCHASE,
            billingResult = acknowledgeResult,
            onError = {
                viewModelScope.launch {
                    _productsEvents.emit(ProductEvents.Error(BillingAction.ACKNOWLEDGE_PURCHASE.toMessage()))
                }
            }
        )
    }

    private suspend fun consume(purchaseToken: String) {
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
        val consumeResult = billingClient.consumePurchase(params)

        billingResponseHandler.handleBillingResponse(
            billingAction = BillingAction.CONSUME_PURCHASE,
            billingResult = consumeResult.billingResult,
            onError = {
                viewModelScope.launch {
                    _productsEvents.emit(ProductEvents.Error(BillingAction.CONSUME_PURCHASE.toMessage()))
                }
            }
        )
    }

}

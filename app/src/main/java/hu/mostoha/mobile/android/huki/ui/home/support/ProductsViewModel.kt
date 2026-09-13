package hu.mostoha.mobile.android.huki.ui.home.support

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ConnectionState
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.billing.BillingResponseHandler
import hu.mostoha.mobile.android.huki.di.module.IoDispatcher
import hu.mostoha.mobile.android.huki.model.domain.BillingProductType
import hu.mostoha.mobile.android.huki.model.domain.OneTimeBillingProducts
import hu.mostoha.mobile.android.huki.model.domain.RecurringBillingProducts
import hu.mostoha.mobile.android.huki.model.domain.isOneTime
import hu.mostoha.mobile.android.huki.model.mapper.ProductsUiModelMapper
import hu.mostoha.mobile.android.huki.model.ui.BillingAction
import hu.mostoha.mobile.android.huki.model.ui.ProductEvents
import hu.mostoha.mobile.android.huki.model.ui.ProductsUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.repository.SupportRepository
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val billingResponseHandler: BillingResponseHandler,
    private val productsUiModelMapper: ProductsUiModelMapper,
    private val analyticsService: AnalyticsService,
    private val supportRepository: SupportRepository
) : ViewModel(), PurchasesUpdatedListener {

    private val _productsUiModel = MutableStateFlow(ProductsUiModel())
    val productsUiModel: SharedFlow<ProductsUiModel> = _productsUiModel
        .stateIn(viewModelScope, WhileViewSubscribed, ProductsUiModel())

    private val _productsEvents = MutableSharedFlow<ProductEvents>()
    val productsEvents: SharedFlow<ProductEvents> = _productsEvents.asSharedFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enableAutoServiceReconnection()
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .build()

    private val loadMutex = Mutex()

    private var isConnectionStarted = false

    init {
        initProducts()
    }

    fun initProducts() {
        startBillingConnection()
    }

    fun refresh() {
        if (billingClient.connectionState == ConnectionState.CONNECTED) {
            launchLoad { loadPurchaseHistory() }
        } else {
            startBillingConnection()
        }
    }

    /**
     * Loads are serialized instead of dropped while another one runs, so the load started right
     * after the connection - the only one querying products - is never lost to a [refresh] that
     * happens to be in flight.
     */
    private fun launchLoad(load: suspend () -> Unit) {
        viewModelScope.launch {
            loadMutex.withLock {
                load()
            }
        }
    }

    /**
     * [BillingClient.Builder.enableAutoServiceReconnection] makes the library reconnect on its own,
     * so startConnection must be called once only: a second call while a connection is in flight -
     * ours or one of the library's own retries - fails with DEVELOPER_ERROR. Note that
     * [BillingClient.isReady] already returns true while connecting, so only
     * [ConnectionState.CONNECTED] tells a finished connection apart from a pending one.
     */
    private fun startBillingConnection() {
        if (isConnectionStarted && billingClient.connectionState != ConnectionState.DISCONNECTED) {
            Timber.d("Billing: connection already started, state=${billingClient.connectionState}")

            return
        }

        isConnectionStarted = true

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.isReconnectionCollision()) {
                    Timber.d("Billing: reconnection collided with a connection in flight, ignoring")

                    return
                }

                billingResponseHandler.handleBillingResponse(
                    billingAction = BillingAction.START_CONNECTION,
                    billingResult = billingResult,
                    onSuccess = {
                        launchLoad {
                            loadPurchaseHistory()
                            loadProducts()
                        }
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

            /**
             * The library routes the failures of its own reconnections to this listener too. Such a
             * failure means a connection is already in flight and its real result still arrives in a
             * separate callback, so it must not be reported as an error.
             */
            private fun BillingResult.isReconnectionCollision(): Boolean {
                return responseCode == BillingResponseCode.DEVELOPER_ERROR &&
                    billingClient.connectionState != ConnectionState.DISCONNECTED
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

    private suspend fun loadProducts() = withContext(ioDispatcher) {
        val oneTimeProductDetails = queryProductDetails(OneTimeBillingProducts.entries)
        val recurringProductDetails = queryProductDetails(RecurringBillingProducts.entries)

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
        val isQueryFailed = oneTimeProductDetails.billingResult.responseCode != BillingResponseCode.OK ||
            recurringProductDetails.billingResult.responseCode != BillingResponseCode.OK

        if (oneTimeList.isEmpty() && recurringList.isEmpty()) {
            analyticsService.billingEvent(BillingAction.QUERY_PRODUCTS, BillingResponseCode.ITEM_UNAVAILABLE)
        }

        // A failed query returns no products at all, so rendering what the other one returned would
        // leave the products of the failed one as empty placeholders with no way to notice it.
        if (isQueryFailed || (oneTimeList.isEmpty() && recurringList.isEmpty())) {
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

    /**
     * Play answers with a transient SERVICE_UNAVAILABLE now and then - right after an install, for
     * example -, so such a query is given one more chance before it is reported as an error.
     */
    private suspend fun queryProductDetails(products: List<BillingProductType>): ProductDetailsResult {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                products.map {
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(it.productId)
                        .setProductType(it.productType)
                        .build()
                }
            )
            .build()
        val productDetails = billingClient.queryProductDetails(params)

        if (productDetails.billingResult.responseCode != BillingResponseCode.SERVICE_UNAVAILABLE) {
            return productDetails
        }

        Timber.w("Billing: product query is unavailable, retrying")

        delay(PRODUCT_QUERY_RETRY_DELAY)

        return billingClient.queryProductDetails(params)
    }

    /**
     * Play only reports subscriptions that are currently active, so a cancelled or expired one
     * drops out of the list here and its badge disappears. One-time support is permanent and comes
     * from the local record instead.
     */
    private suspend fun loadPurchaseHistory() {
        recordAndConsumeOwnedOneTimePurchases()

        val recurringQuery = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val recurringResult = billingClient.queryPurchasesAsync(recurringQuery)

        billingResponseHandler.handleBillingResponse(
            billingAction = BillingAction.QUERY_PURCHASES,
            billingResult = recurringResult.billingResult,
            onError = {
                viewModelScope.launch {
                    _productsEvents.emit(ProductEvents.Error(BillingAction.QUERY_PURCHASES.toMessage()))
                }
            }
        )

        if (recurringResult.billingResult.responseCode != BillingResponseCode.OK) {
            return
        }

        val oneTimePurchaseHistory = try {
            supportRepository.getOneTimePurchaseHistory().first()
        } catch (exception: Exception) {
            Timber.e(exception, "Billing: reading one-time purchase history failed")

            emptyList()
        }

        Timber.d("Billing: one-time purchase history = $oneTimePurchaseHistory")
        Timber.d("Billing: active purchases = ${recurringResult.purchasesList.map { it.products }}")

        val purchases = productsUiModelMapper.mapActivePurchases(recurringResult.purchasesList)
            .plus(productsUiModelMapper.mapPurchaseHistory(oneTimePurchaseHistory))
            .sortedByDescending { it.purchaseTime }

        _productsUiModel.update { uiModel ->
            uiModel.copy(purchases = purchases)
        }
    }

    /**
     * One-time support purchases are consumed right after purchase so they can be bought again.
     * If [onPurchasesUpdated] couldn't finish consuming one - app killed, consume call failed -
     * Play still reports it as owned, which blocks re-purchasing it and gets auto-refunded after
     * three days. Catch those here on every billing connection: record them, then consume them.
     * Recording is idempotent per purchase token, so a re-swept purchase is never counted twice.
     */
    private suspend fun recordAndConsumeOwnedOneTimePurchases() {
        try {
            val oneTimeQuery = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            val oneTimeResult = billingClient.queryPurchasesAsync(oneTimeQuery)

            if (oneTimeResult.billingResult.responseCode != BillingResponseCode.OK) {
                Timber.w("Billing: owned one-time purchases query failed, skipping sweep for now")

                return
            }

            val ownedPurchases = oneTimeResult.purchasesList.filter { it.purchaseState == PurchaseState.PURCHASED }

            ownedPurchases.forEach { purchase -> recordAndConsumeOneTimePurchase(purchase) }
        } catch (exception: Exception) {
            Timber.e(exception, "Billing: consuming owned one-time purchases failed")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        billingResponseHandler.handleBillingResponse(
            billingAction = BillingAction.PURCHASES_UPDATED,
            billingResult = billingResult,
            onSuccess = { _ ->
                val purchasedItems = purchases.orEmpty().filter { it.purchaseState == PurchaseState.PURCHASED }

                if (purchasedItems.isNotEmpty()) {
                    viewModelScope.launch {
                        purchasedItems.forEach { purchase ->
                            if (purchase.products.firstOrNull()?.isOneTime() == true) {
                                recordAndConsumeOneTimePurchase(purchase)
                            } else {
                                acknowledge(purchase.purchaseToken)
                            }
                        }

                        loadPurchaseHistory()
                    }
                }
            },
            onError = { result ->
                if (result.responseCode != BillingResponseCode.USER_CANCELED) {
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

    private suspend fun recordAndConsumeOneTimePurchase(purchase: Purchase) {
        val productId = purchase.products.firstOrNull() ?: return

        Timber.d("Billing: recording one-time purchase, productId=$productId, token=${purchase.purchaseToken}")

        try {
            supportRepository.recordOneTimePurchase(productId, purchase.purchaseToken, purchase.purchaseTime)
        } catch (exception: Exception) {
            Timber.e(exception, "Billing: recording one-time purchase failed, leaving it unconsumed")

            return
        }

        val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        val consumeResult = billingClient.consumePurchase(params)

        if (consumeResult.billingResult.responseCode == BillingResponseCode.ITEM_NOT_OWNED) {
            Timber.d("Billing: one-time purchase is already consumed, productId=$productId")

            return
        }

        billingResponseHandler.handleBillingResponse(
            billingAction = BillingAction.CONSUME_PURCHASE,
            billingResult = consumeResult.billingResult,
            onSuccess = {
                Timber.d("Billing: consumed one-time purchase, productId=$productId")
            },
            onError = {
                viewModelScope.launch {
                    _productsEvents.emit(ProductEvents.Error(BillingAction.CONSUME_PURCHASE.toMessage()))
                }
            }
        )
    }

    companion object {
        private const val PRODUCT_QUERY_RETRY_DELAY = 1000L
    }

}

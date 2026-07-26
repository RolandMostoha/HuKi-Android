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
import com.android.billingclient.api.queryPurchasesAsync
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
import hu.mostoha.mobile.android.huki.repository.SupportRepository
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
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
            migrateLegacyOneTimePurchaseHistory()
            recordAndConsumeOwnedOneTimePurchases()

            val recurringQuery = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            val recurringResult = billingClient.queryPurchasesAsync(recurringQuery)

            billingResponseHandler.handleBillingResponse(
                billingAction = BillingAction.QUERY_PURCHASES,
                billingResult = recurringResult.billingResult,
                onSuccess = { _ ->
                    viewModelScope.launch {
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
                },
                onError = {
                    viewModelScope.launch {
                        _productsEvents.emit(ProductEvents.Error(BillingAction.QUERY_PURCHASES.toMessage()))
                    }
                }
            )
        }
    }

    /**
     * Bridge migration only: Billing 8.x removes queryPurchaseHistoryAsync entirely, so this is
     * the last release able to read full one-time purchase history from Play and backfill it into
     * [SupportRepository] for existing supporters. Remove this once the 8.x upgrade ships and most
     * active users have picked up this release.
     *
     * Runs at most once per install: retried on every connection until Play answers OK, never
     * after that. Backfilled purchases claim their token, so the sweep below can't re-count them.
     */
    private suspend fun migrateLegacyOneTimePurchaseHistory() {
        try {
            if (supportRepository.isLegacyHistoryMigrated()) {
                return
            }

            val historyQuery = QueryPurchaseHistoryParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            val historyResult = billingClient.queryPurchaseHistory(historyQuery)

            if (historyResult.billingResult.responseCode != BillingResponseCode.OK) {
                Timber.w("Billing: legacy history query failed, skipping backfill for now")

                return
            }

            val historyRecords = historyResult.purchaseHistoryRecordList.orEmpty()

            Timber.d("Billing: legacy purchase history found = ${historyRecords.map { it.products }}")

            historyRecords.forEach { record ->
                val productId = record.products.firstOrNull()?.takeIf { it.isOneTime() } ?: return@forEach

                Timber.d("Billing: backfilling legacy purchase if missing, productId=$productId")

                val isSeeded = supportRepository.recordLegacyPurchase(
                    productId = productId,
                    purchaseToken = record.purchaseToken,
                    purchaseTimeMillis = record.purchaseTime,
                )

                if (isSeeded) {
                    analyticsService.legacyPurchaseBackfilled(productId)
                }
            }

            supportRepository.setLegacyHistoryMigrated()
        } catch (exception: Exception) {
            Timber.e(exception, "Billing: legacy purchase history migration failed")
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
                if (!purchases.isNullOrEmpty()) {
                    val purchase = purchases.first()

                    if (purchase.purchaseState == PurchaseState.PURCHASED) {
                        viewModelScope.launch {
                            if (purchase.products.firstOrNull()?.isOneTime() == true) {
                                recordAndConsumeOneTimePurchase(purchase)
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

}

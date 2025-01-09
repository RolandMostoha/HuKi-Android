package hu.mostoha.mobile.android.huki.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.configuration.AppConfiguration
import hu.mostoha.mobile.android.huki.database.PlaceHistoryDao
import hu.mostoha.mobile.android.huki.di.module.DbDispatcher
import hu.mostoha.mobile.android.huki.extensions.toLocalDateTime
import hu.mostoha.mobile.android.huki.model.db.PlaceHistoryEntity
import hu.mostoha.mobile.android.huki.model.domain.HistoryInfo
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.util.replaceVowelsWithWildcards
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlaceHistoryRepository @Inject constructor(
    @DbDispatcher private val dbDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context,
    private val appConfiguration: AppConfiguration,
    private val placeHistoryDao: PlaceHistoryDao,
) {

    fun getPlaces(): Flow<List<Place>> {
        return placeHistoryDao.getEntities()
            .map { placeEntities ->
                placeEntities
                    .sortedByDescending { it.lastModified }
                    .map { placeEntity ->
                        Place(
                            osmId = placeEntity.osmId,
                            name = placeEntity.name.toMessage(),
                            fullAddress = placeEntity.address,
                            placeType = PlaceType.NODE,
                            location = Location(placeEntity.latitude, placeEntity.longitude),
                            boundingBox = placeEntity.boundingBox,
                            placeFeature = placeEntity.placeFeature,
                            historyInfo = HistoryInfo(
                                isFavourite = placeEntity.isFavourite,
                                storeDateTime = placeEntity.lastModified.toLocalDateTime()
                            )
                        )
                    }
            }
            .flowOn(dbDispatcher)
    }

    fun getPlacesBy(searchText: String, limit: Int = -1): Flow<List<Place>> {
        val normalizedSearchText = searchText
            .lowercase()
            .replaceVowelsWithWildcards()
            .trim()

        return placeHistoryDao.getEntities(normalizedSearchText, limit)
            .map { placeEntities ->
                placeEntities
                    .sortedByDescending { it.lastModified }
                    .map { placeEntity ->
                        Place(
                            osmId = placeEntity.osmId,
                            name = placeEntity.name.toMessage(),
                            fullAddress = placeEntity.address,
                            placeType = PlaceType.NODE,
                            location = Location(placeEntity.latitude, placeEntity.longitude),
                            boundingBox = placeEntity.boundingBox,
                            placeFeature = placeEntity.placeFeature,
                            historyInfo = HistoryInfo(
                                isFavourite = placeEntity.isFavourite,
                                storeDateTime = placeEntity.lastModified.toLocalDateTime()
                            )
                        )
                    }
            }
            .flowOn(dbDispatcher)
    }

    suspend fun savePlace(placeUiModel: PlaceUiModel, actualDate: Long) {
        withContext(dbDispatcher) {
            placeHistoryDao.insertAll(
                PlaceHistoryEntity(
                    osmId = placeUiModel.osmId,
                    name = placeUiModel.primaryText.resolve(context),
                    address = placeUiModel.secondaryText.resolve(context),
                    isFavourite = false,
                    placeType = placeUiModel.placeType,
                    placeFeature = placeUiModel.placeFeature,
                    latitude = placeUiModel.geoPoint.toLocation().latitude,
                    longitude = placeUiModel.geoPoint.toLocation().longitude,
                    lastModified = actualDate,
                    comment = null,
                    boundingBox = placeUiModel.boundingBox,
                )
            )
        }
    }

    suspend fun deletePlace(osmId: String) {
        withContext(dbDispatcher) {
            placeHistoryDao.delete(osmId)
        }
    }

    suspend fun clearOldPlaces() {
        withContext(dbDispatcher) {
            val rowCount = placeHistoryDao.getRowCount()
            val maxRowCount = appConfiguration.getPlaceHistoryMaxRowCount()

            if (rowCount > maxRowCount) {
                placeHistoryDao.deleteLastRows(rowCount - maxRowCount)
            }
        }
    }

    suspend fun setFavourite(osmId: String, isFavourite: Boolean) {
        withContext(dbDispatcher) {
            placeHistoryDao.updateFavorite(osmId, isFavourite)
        }
    }

    suspend fun deleteAll() {
        withContext(dbDispatcher) {
            placeHistoryDao.deleteAll()
        }
    }

}

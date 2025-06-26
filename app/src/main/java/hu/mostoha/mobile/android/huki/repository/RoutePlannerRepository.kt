package hu.mostoha.mobile.android.huki.repository

import android.net.Uri
import com.codebutchery.androidgpx.data.GPXDocument
import com.codebutchery.androidgpx.data.GPXSegment
import com.codebutchery.androidgpx.data.GPXTrack
import com.codebutchery.androidgpx.data.GPXTrackPoint
import com.codebutchery.androidgpx.data.GPXWayPoint
import com.codebutchery.androidgpx.print.GPXFilePrinter
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.configuration.GpxConfiguration
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.RoutePlan
import hu.mostoha.mobile.android.huki.model.domain.RoutePlanType
import hu.mostoha.mobile.android.huki.model.mapper.RoutePlannerNetworkModelMapper
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel
import hu.mostoha.mobile.android.huki.network.GraphhopperService
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.WaypointItem
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RoutePlannerRepository @Inject constructor(
    private val graphhopperService: GraphhopperService,
    private val routePlannerNetworkModelMapper: RoutePlannerNetworkModelMapper,
    private val gpxConfiguration: GpxConfiguration,
) {

    suspend fun getRoutePlan(planType: RoutePlanType, waypoints: List<Location>): RoutePlan {
        val routeRequest = routePlannerNetworkModelMapper.createRouteRequest(planType, waypoints)

        val routeResponse = graphhopperService.getRoute(routeRequest)

        return routePlannerNetworkModelMapper.mapRouteResponse(planType, routeResponse)
    }

    suspend fun saveRoutePlan(routePlan: RoutePlanUiModel, waypoints: List<WaypointItem>): Uri? {
        val routePlannerFilesDirPath = gpxConfiguration.getRoutePlannerGpxDirectory()
        val geoPoints = routePlan.geoPoints

        val gpxTrack = GPXTrack()
        val gpxSegment = GPXSegment()

        geoPoints.forEach { geoPoint ->
            val gpxTrackPoint = GPXTrackPoint(geoPoint.latitude.toFloat(), geoPoint.longitude.toFloat())
            gpxTrackPoint.elevation = geoPoint.altitude.toFloat()

            gpxSegment.addPoint(gpxTrackPoint)
        }
        gpxTrack.name = routePlan.name
        gpxTrack.addSegment(gpxSegment)

        val gpxTracks = listOf(gpxTrack)

        val gpxWaypoints = waypoints.mapNotNull {
            val location = it.location ?: return@mapNotNull null
            val gpxWayPoint = GPXWayPoint(location.latitude.toFloat(), location.longitude.toFloat())

            gpxWayPoint.name = it.waypointComment?.name
            gpxWayPoint.description = it.waypointComment?.comment

            gpxWayPoint
        }

        val gpxDocument = GPXDocument(gpxWaypoints, gpxTracks, emptyList())
        val filePath = "$routePlannerFilesDirPath/${routePlan.name}.gpx"

        val fileName = saveGpxFile(filePath, gpxDocument)
        val file = File(fileName)

        return Uri.fromFile(file)
    }

    private suspend fun saveGpxFile(filePath: String, gpxDocument: GPXDocument) = suspendCoroutine {
        val listener = object : GPXFilePrinter.GPXFilePrinterListener {
            override fun onGPXPrintStarted() = Unit

            override fun onGPXPrintCompleted() {
                it.resume(filePath)
            }

            override fun onGPXPrintError(message: String?) {
                Timber.w(message)

                it.resumeWithException(DomainException(Message.Res(R.string.route_planner_general_error_message)))
            }
        }

        val printer = GPXFilePrinter(listener)

        printer.print(gpxDocument, filePath)
    }

}

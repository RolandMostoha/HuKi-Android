package hu.mostoha.mobile.android.huki.repository

import android.content.Context
import android.net.Uri
import com.codebutchery.androidgpx.data.GPXDocument
import com.codebutchery.androidgpx.data.GPXSegment
import com.codebutchery.androidgpx.data.GPXTrack
import com.codebutchery.androidgpx.data.GPXTrackPoint
import com.codebutchery.androidgpx.print.GPXFilePrinter
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.RoutePlan
import hu.mostoha.mobile.android.huki.model.mapper.RoutePlannerNetworkModelMapper
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel
import hu.mostoha.mobile.android.huki.network.GraphhopperService
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RoutePlannerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val graphhopperService: GraphhopperService,
    private val routePlannerNetworkModelMapper: RoutePlannerNetworkModelMapper
) {

    suspend fun getRoutePlan(waypoints: List<Location>): RoutePlan {
        val routeRequest = routePlannerNetworkModelMapper.createRouteRequest(waypoints)

        val routeResponse = graphhopperService.getRoute(routeRequest)

        return routePlannerNetworkModelMapper.mapRouteResponse(routeResponse)
    }

    suspend fun saveRoutePlan(routePlan: RoutePlanUiModel): Uri? {
        val filesDir = context.filesDir
        val geoPoints = routePlan.geoPoints

        val gpxTrack = GPXTrack()
        val gpxSegment = GPXSegment()

        geoPoints.forEach { geoPoint ->
            val gpxTrackPoint = GPXTrackPoint(geoPoint.latitude.toFloat(), geoPoint.longitude.toFloat())
            gpxTrackPoint.elevation = geoPoint.altitude.toFloat()

            gpxSegment.addPoint(gpxTrackPoint)
        }
        gpxTrack.addSegment(gpxSegment)

        val gpxTracks = listOf(gpxTrack)
        val gpxDocument = GPXDocument(emptyList(), gpxTracks, emptyList())
        val filePath = "${filesDir.path}/${routePlan.name}"

        val fileName = saveGpxFile(filePath, gpxDocument)
        val file = File(fileName)

        return Uri.fromFile(file)
    }

    private suspend fun saveGpxFile(filePath: String, gpxDocument: GPXDocument) = suspendCoroutine {
        val listener = object : GPXFilePrinter.GPXFilePrinterListener {
            override fun onGPXPrintStarted() {
                // no-op
            }

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
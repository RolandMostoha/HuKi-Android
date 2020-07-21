package hu.mostoha.mobile.android.turistautak.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.turistautak.di.module.ServiceModule
import hu.mostoha.mobile.android.turistautak.extensions.fixQueryErrors
import hu.mostoha.mobile.android.turistautak.network.OverpassService
import hu.supercluster.overpasser.library.output.OutputFormat
import hu.supercluster.overpasser.library.output.OutputModificator
import hu.supercluster.overpasser.library.output.OutputOrder
import hu.supercluster.overpasser.library.output.OutputVerbosity
import hu.supercluster.overpasser.library.query.OverpassQuery
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@MediumTest
@HiltAndroidTest
@UninstallModules(ServiceModule::class)
class OverpassServiceTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var overpassService: OverpassService

    @Test
    fun givenExampleQuery_whenFixQueryErrors_thenQuotesRemovedFromSettingPart() {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(10)
            .filterQuery()
            .node()
            .amenity("parking")
            .tagNot("access", "private")
            .boundingBox(
                47.48047027491862, 19.039797484874725,
                47.51331674014172, 19.07404761761427
            )
            .end()
            .output(OutputVerbosity.BODY, OutputModificator.CENTER, OutputOrder.QT, 100)
            .build()

        val result = query.fixQueryErrors()

        assertEquals(
            "[out:json][timeout:10]; (node[\"amenity\"=\"parking\"][\"access\"!=\"private\"](47.48047027491862,19.039797484874725,47.51331674014172,19.07404761761427);<;); out body center qt 100;",
            result
        )
    }

    @Test
    fun givenExampleQuery_whenOverpassApiCalled_thenValidResultReturns() {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(10)
            .filterQuery()
            .node()
            .amenity("parking")
            .tagNot("access", "private")
            .boundingBox(
                47.48047027491862, 19.039797484874725,
                47.51331674014172, 19.07404761761427
            )
            .end()
            .output(OutputVerbosity.BODY, OutputModificator.CENTER, OutputOrder.QT, 100)
            .build()
            .fixQueryErrors()

        runBlocking {
            val result = overpassService.interpreter(query)

            Assert.assertNotNull(result)
        }
    }

}


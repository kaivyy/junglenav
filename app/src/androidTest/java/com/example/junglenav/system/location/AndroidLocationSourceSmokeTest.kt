package com.example.junglenav.system.location

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidLocationSourceSmokeTest {
    @Test
    fun locationSourceCanBeCreatedFromSystemService() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val source = AndroidLocationSource(locationManager = manager)

        assertNotNull(source.observeLocationSamples())
    }

    @Test
    fun locationSourceCanRegisterFromBackgroundDispatcher() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val packageName = instrumentation.targetContext.packageName
        instrumentation.uiAutomation.executeShellCommand(
            "pm grant $packageName ${Manifest.permission.ACCESS_FINE_LOCATION}",
        ).close()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val source = AndroidLocationSource(locationManager = manager)
        val isFinished = AtomicBoolean(false)
        val failure = AtomicReference<Throwable?>(null)

        val job = launch(Dispatchers.IO) {
            try {
                source.observeLocationSamples().collect { }
            } catch (throwable: Throwable) {
                failure.set(throwable)
            } finally {
                isFinished.set(true)
            }
        }

        delay(500L)

        if (isFinished.get()) {
            assertNull(failure.get())
        }
        job.cancel()
        job.join()
    }
}

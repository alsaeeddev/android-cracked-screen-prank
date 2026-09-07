package com.alsaeeddev

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.alsaeeddev.data.service.PrankMonitorService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Fake Cracked Screen", appName)
  }

  @Test
  fun `FakeCrackedScreenApp initializes AppContainer cleanly`() {
    val app = ApplicationProvider.getApplicationContext<FakeCrackedScreenApp>()
    assertNotNull(app.appContainer)
    assertNotNull(app.appContainer.overlayWindowController)
    assertNotNull(app.appContainer.prankModeState)
  }

  @Test
  fun `PrankMonitorService creates and starts monitoring without crashing`() {
    val controller = Robolectric.buildService(PrankMonitorService::class.java)
    val service = controller.create().get()
    assertNotNull(service)

    val startIntent = Intent(ApplicationProvider.getApplicationContext(), PrankMonitorService::class.java).apply {
      action = PrankMonitorService.ACTION_START_MONITORING
    }
    controller.withIntent(startIntent).startCommand(0, 1)

    val stopIntent = Intent(ApplicationProvider.getApplicationContext(), PrankMonitorService::class.java).apply {
      action = PrankMonitorService.ACTION_STOP_MONITORING
    }
    controller.withIntent(stopIntent).startCommand(0, 2)
    controller.destroy()
  }
}


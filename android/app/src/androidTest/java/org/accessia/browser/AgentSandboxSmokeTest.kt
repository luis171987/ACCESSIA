package org.accessia.browser

import android.content.Intent
import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Runs only in the explicitly addressed ephemeral emulator in agent-sandbox.yml. */
@RunWith(AndroidJUnit4::class)
class AgentSandboxSmokeTest {
    @Test fun launchesWithoutModelAndOffersOptionalAssistant() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val activity = instrumentation.startActivitySync(Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) as MainActivity
        try {
            instrumentation.waitForIdleSync()
            assertFalse("No model may download on launch", File(context.filesDir, "local-agent/qwen3-0.6b.litertlm").exists())
            instrumentation.runOnMainSync {
                MainActivity::class.java.getDeclaredMethod("showMenu").apply { isAccessible = true }.invoke(activity)
            }
            instrumentation.waitForIdleSync()
            val root = instrumentation.uiAutomation.rootInActiveWindow
            assertNotNull("Real Android accessibility window required", root)
            val nodes = root.findAccessibilityNodeInfosByText("Asistente local")
            assertTrue("Assistant entry must be accessible", nodes.isNotEmpty())
            instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                File(context.filesDir, "sandbox-menu.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
        } finally { instrumentation.runOnMainSync { activity.finish() } }
    }
}

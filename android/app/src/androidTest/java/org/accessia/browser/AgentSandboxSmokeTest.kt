package org.accessia.browser

import android.content.Intent
import android.graphics.Bitmap
import android.view.accessibility.AccessibilityNodeInfo
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
        // Test-only choice: avoid the optional first-run IP question and all its networking.
        context.getSharedPreferences("accessia_v02", 0).edit().putBoolean("ip_region_consent", false).commit()
        val activity = instrumentation.startActivitySync(Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) as MainActivity
        try {
            instrumentation.waitForIdleSync()
            assertFalse("No model may download on launch", File(context.filesDir, "local-agent/qwen3-0.6b.litertlm").exists())
            fun capture(name: String) {
                instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                    File(context.filesDir, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    bitmap.recycle()
                }
            }
            capture("sandbox-01-inicio.png")
            instrumentation.runOnMainSync {
                MainActivity::class.java.getDeclaredMethod("showMenu").apply { isAccessible = true }.invoke(activity)
            }
            instrumentation.waitForIdleSync()
            fun scroll(node: AccessibilityNodeInfo): Boolean {
                if (node.isScrollable && node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) return true
                for (i in 0 until node.childCount) node.getChild(i)?.let { if (scroll(it)) return true }
                return false
            }
            var entry: AccessibilityNodeInfo? = null
            for (attempt in 0 until 12) {
                val root = instrumentation.uiAutomation.rootInActiveWindow
                assertNotNull("Real Android accessibility window required", root)
                entry = root.findAccessibilityNodeInfosByText("Asistente local").firstOrNull()
                if (entry != null) break
                if (!scroll(root)) break
                instrumentation.waitForIdleSync()
                instrumentation.uiAutomation.waitForIdle(200, 3000)
            }
            capture("sandbox-02-menu.png")
            assertNotNull("Assistant entry must be reachable by accessibility scrolling", entry)
            var click = entry
            while (click != null && !click.isClickable) click = click.parent
            assertTrue("Assistant entry must open its configuration", click?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true)
            instrumentation.waitForIdleSync()
            instrumentation.uiAutomation.waitForIdle(200, 3000)
            val assistantRoot = instrumentation.uiAutomation.rootInActiveWindow
            assertTrue("Optional local assistant dialog must open", assistantRoot.findAccessibilityNodeInfosByText("Asistente local experimental").isNotEmpty())
            capture("sandbox-03-asistente.png")
        } finally { instrumentation.runOnMainSync { activity.finish() } }
    }
}

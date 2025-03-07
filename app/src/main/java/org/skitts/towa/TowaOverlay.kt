package org.skitts.towa

import android.os.Bundle
import android.app.AlertDialog
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.launch

const val DB_NAME           = "towa.db"
const val DB_REL_ASSET_PATH = "databases/$DB_NAME"

class TowaOverlay : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.coroutineScope.launch {
            // TODO: Would be nice to avoid waiting for this somehow
            ThemeManager.loadThemeForSession(this@TowaOverlay)

            if (intent.action == Intent.ACTION_PROCESS_TEXT) {
                val text: String = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT) ?: ""
                showOverlay(text)
            } else if (intent.action == Intent.ACTION_SEND) {
                val text: String = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                showOverlay(text)
            }
        }
    }

    private fun showOverlay(text: String) {
        val resultsLayout = TowaSearchResultsLayout.create(this@TowaOverlay, this, text)

        val builder = AlertDialog.Builder(this, ThemeManager.overlayTheme)
        builder.setView(resultsLayout).setOnDismissListener { finish() }
        builder.show()
    }
}
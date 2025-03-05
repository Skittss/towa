package org.skitts.towa

import android.os.Bundle
import android.app.AlertDialog
import android.view.View
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.content.Intent
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
                showDictionary(sanitizeInput(text))
            } else if (intent.action == Intent.ACTION_SEND) {
                val text: String = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                showDictionary(sanitizeInput(text))
            }
        }
    }

    private fun sanitizeInput(text: String): String {
        var res: String = text
        res = res.replace("\\s".toRegex(), "")
        res = sanitizerMap.entries.fold(res) { acc, (k, v) -> acc.replace(k, v) }

        return res
    }

    private val sanitizerMap: Map<String, String> = mapOf(
        "0" to "０",
        "1" to "１",
        "2" to "２",
        "3" to "３",
        "4" to "４",
        "5" to "５",
        "6" to "６",
        "7" to "７",
        "8" to "８",
        "9" to "９",
        "!" to "！",
        "?" to "？"
    )

    private val notFoundMsgsEN: List<String> = listOf(
        "Can't find it～ sorry! (´・ω・)",
        "Huh? I couldn't find it... (°ロ°;)",
        "Hmmm, looks like its nowhere to be found... (´・へ・)",
        "I looked all over, but came up empty-handed! (´；ω；)",
        "Sorry... I couldn't find it ( ；∀；)",
        "Huh? Where'd it go?! (・∀・)",
        "Hmmm, can't seem to find it ( ๑˙ϖ˙๑ )",
        "Aw man - it's went POOF! (∩｀-´)⊃━☆ﾟ.*･｡",
        "Oops, did it get lost? (￣▽￣)ニヤリ",
        "Fufufu... maybe it's hiding? (๑˃̵ᴗ˂̵)و"
    )

    private val notFoundMsgsJP: List<String> = listOf(
        "見つからないよ～ (´・ω・)",
        "あれれ？ 見つからなかった。。。 (°ロ°;)",
        "うーん、どこにもないみたい (´・へ・)",
        "そこら中探したけど、見つからなかったよ (´；ω；)",
        "ごめんね、見つけられなかった。。。( ；∀；)",
        "あれっ？どこいった？(・∀・)",
        "むむっ、見当たらないぞ？( ๑˙ϖ˙๑ )",
        "ないっぽい！ドロン！(∩｀-´)⊃━☆ﾟ.*･｡",
        "おっと、消えちゃった？(￣▽￣)ニヤリ",
        "秘密の場所に隠れてるかも？(๑˃̵ᴗ˂̵)و"
    )

    private fun showDictionary(text: String) {

        val vertLayout = LinearLayout(this)
        vertLayout.orientation = VERTICAL

        val loadingBar = ProgressBar(this)
        loadingBar.setPadding(0, 80, 0, 40)
        vertLayout.addView(loadingBar)

        val loadingText = TextView(this)
        loadingText.text = "検索中～。。。"
        loadingText.setTextColor(ThemeManager.colTextPrimary)
        loadingText.gravity = Gravity.CENTER_HORIZONTAL
        loadingText.setPadding(0, 0, 0, 80)
        vertLayout.addView(loadingText)

        val builder = AlertDialog.Builder(this, ThemeManager.overlayTheme)
        builder.setView(vertLayout).setOnDismissListener { finish() }
        val alertDialog = builder.show()

        lifecycle.coroutineScope.launch {

            val parser = DictEntryParser(this@TowaOverlay)
            val entries: List<DictEntry> = parser.queryDictionaryEntries(text)

            val dictView = buildDictUI(text, entries)

            alertDialog.setContentView(dictView)
        }

        // TODO: Fetch more button at bottom + on drag far down.

        //alertDialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }

    private fun buildDictUI(query: String, entries: List<DictEntry>) : View {
        if (entries.isEmpty()) {
            val noFoundLayout = NotFoundLayout(this)
            noFoundLayout.populate(
                "No entries found for \"${query}\".",
                notFoundMsgsJP[notFoundMsgsJP.indices.random()],
                "(｡•́︿•̀｡)?"
            )

            return noFoundLayout
        }

        val frameLayout = FrameLayout(this)

        val verticalList = LinearLayout(this)
        verticalList.orientation = VERTICAL

        for (entry in entries) {
            val entryLayout = DictEntryLayout(this);
            entryLayout.populate(this, frameLayout,  entry)
            verticalList.addView(entryLayout)
        }

        val scroll = ScrollView(this)
        scroll.addView(verticalList)

        frameLayout.addView(scroll)

        val cons = ConstraintLayout(this)
        cons.addView(frameLayout)

        return cons
    }

}
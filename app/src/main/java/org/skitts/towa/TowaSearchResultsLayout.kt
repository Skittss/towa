package org.skitts.towa

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.launch

class TowaSearchResultsLayout private constructor(
    context: Context,
) : LinearLayout(context) {

    companion object {
        fun create(
            context: Context,
            activity: ComponentActivity,
            query: String
        ): TowaSearchResultsLayout {
            return TowaSearchResultsLayout(context).apply {
                setupView(activity, sanitizeInput(query));
            }
        }
    }

    private fun setupView(activity: ComponentActivity, query: String) {
        val loadingView = TowaSearchResultsLoadingLayout(context)
        addView(loadingView)

        activity.lifecycle.coroutineScope.launch {

            val parser = DictEntryParser(context)
            val entries: List<DictEntry> = parser.queryDictionaryEntries(query)

            val dictView = buildUI(activity, query, entries)
            removeView(loadingView)
            addView(dictView)
        }
    }

    private fun buildUI(
        activity: ComponentActivity,
        query: String,
        entries: List<DictEntry>
    ): View {
        if (entries.isEmpty()) {
            val noFoundLayout = NotFoundLayout(context)
            noFoundLayout.populate(
                "No entries found for \"${query}\".",
                notFoundMsgsJP[notFoundMsgsJP.indices.random()],
                "(｡•́︿•̀｡)?"
            )

            return noFoundLayout
        }

        val frameLayout = FrameLayout(context)

        val verticalList = LinearLayout(context)
        verticalList.orientation = VERTICAL

        for (entry in entries) {
            val entryLayout = DictEntryLayout(context);
            entryLayout.populate(activity, frameLayout,  entry)
            verticalList.addView(entryLayout)
        }

        val scroll = ScrollView(context)
        scroll.addView(verticalList)

        frameLayout.addView(scroll)

        val cons = ConstraintLayout(context)
        cons.addView(frameLayout)

        return cons
    }

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

}

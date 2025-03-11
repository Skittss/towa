package org.skitts.towa

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class TowaSearchResultsLayout private constructor(
    context: Context,
) : LinearLayout(context) {
    val scope = MainScope()

    fun destroy() {
        scope.cancel()
    }

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

        scope.launch {
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
                context.getString(R.string.entry_not_found_text).replace("{}", query),
                notFoundMsgs[notFoundMsgs.indices.random()],
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

    private val notFoundMsgs: List<String> = listOf(
        context.getString(R.string.not_found_msg_1),
        context.getString(R.string.not_found_msg_2),
        context.getString(R.string.not_found_msg_3),
        context.getString(R.string.not_found_msg_4),
        context.getString(R.string.not_found_msg_5),
        context.getString(R.string.not_found_msg_6),
        context.getString(R.string.not_found_msg_7),
        context.getString(R.string.not_found_msg_8),
        context.getString(R.string.not_found_msg_9),
        context.getString(R.string.not_found_msg_10)
    )
}

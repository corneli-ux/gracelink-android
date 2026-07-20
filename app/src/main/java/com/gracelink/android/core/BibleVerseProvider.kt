package com.gracelink.android.core

import android.content.Context
import org.json.JSONArray
import java.util.Calendar

data class BibleVerse(val text: String, val reference: String)

/**
 * Verse of the Day -- reads a bundled asset (bible_verses.json) rather than
 * a database table. This is deliberate: it's read-only content that never
 * changes per-user, so there's no reason to risk another schema migration
 * for it. Picks the same verse all day (deterministic by day-of-year),
 * rotating through the pool.
 */
object BibleVerseProvider {

    private var cache: List<BibleVerse>? = null

    private fun loadAll(context: Context): List<BibleVerse> {
        cache?.let { return it }
        return try {
            val json = context.assets.open("bible_verses.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(json)
            val verses = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                BibleVerse(text = o.getString("text"), reference = o.getString("reference"))
            }
            cache = verses
            verses
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun verseOfTheDay(context: Context): BibleVerse? {
        val verses = loadAll(context.applicationContext)
        if (verses.isEmpty()) return null
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return verses[dayOfYear % verses.size]
    }
}

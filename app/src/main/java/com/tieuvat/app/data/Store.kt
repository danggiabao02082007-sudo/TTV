package com.tieuvat.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

data class WeekSummary(
    val start: LocalDate,
    val income: Long,        // gồm cả tiền tiêu vặt cố định
    val expense: Long,
    val entries: List<Entry>
) {
    val balance: Long get() = income - expense
    val end: LocalDate get() = start.plusDays(6)
}

class Store(context: Context) {
    private val file = File(context.filesDir, "tieuvat.json")
    val entries = mutableStateListOf<Entry>()
    var settings by mutableStateOf(Settings())
        private set

    init { load() }

    private fun load() {
        if (!file.exists()) return
        try {
            val root = JSONObject(file.readText())
            val arr = root.optJSONArray("entries") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                entries.add(
                    Entry(
                        id = o.getLong("id"),
                        type = if (o.optString("type") == "INCOME") EntryType.INCOME else EntryType.EXPENSE,
                        name = o.optString("name"),
                        amount = o.optLong("amount"),
                        category = Category.of(o.optString("category")),
                        date = LocalDate.ofEpochDay(o.optLong("date")),
                        note = o.optString("note")
                    )
                )
            }
            val s = root.optJSONObject("settings")
            if (s != null) {
                settings = Settings(
                    weeklyAllowance = s.optLong("weeklyAllowance"),
                    weeklyLimit = s.optLong("weeklyLimit"),
                    savingGoal = s.optLong("savingGoal"),
                    savingGoalName = s.optString("savingGoalName")
                )
            }
        } catch (_: Exception) {
            // file hỏng: bắt đầu lại từ đầu
        }
    }

    private fun save() {
        val arr = JSONArray()
        entries.forEach { e ->
            arr.put(JSONObject().apply {
                put("id", e.id)
                put("type", e.type.name)
                put("name", e.name)
                put("amount", e.amount)
                put("category", e.category.name)
                put("date", e.date.toEpochDay())
                put("note", e.note)
            })
        }
        val s = JSONObject().apply {
            put("weeklyAllowance", settings.weeklyAllowance)
            put("weeklyLimit", settings.weeklyLimit)
            put("savingGoal", settings.savingGoal)
            put("savingGoalName", settings.savingGoalName)
        }
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(JSONObject().put("entries", arr).put("settings", s).toString())
        tmp.renameTo(file)
    }

    fun upsert(e: Entry) {
        val idx = entries.indexOfFirst { it.id == e.id }
        if (idx >= 0) entries[idx] = e else entries.add(e)
        save()
    }

    fun delete(id: Long) {
        entries.removeAll { it.id == id }
        save()
    }

    fun updateSettings(s: Settings) {
        settings = s
        save()
    }

    fun clearAll() {
        entries.clear()
        save()
    }

    fun week(start: LocalDate): WeekSummary {
        val end = start.plusDays(6)
        val list = entries.filter { !it.date.isBefore(start) && !it.date.isAfter(end) }
            .sortedWith(compareByDescending<Entry> { it.date }.thenByDescending { it.id })
        val inc = list.filter { it.type == EntryType.INCOME }.sumOf { it.amount } + settings.weeklyAllowance
        val exp = list.filter { it.type == EntryType.EXPENSE }.sumOf { it.amount }
        return WeekSummary(start, inc, exp, list)
    }

    /** Tổng dư tích lũy từ tuần đầu tiên có dữ liệu đến hết tuần hiện tại. */
    fun totalSaved(today: LocalDate): Long {
        if (entries.isEmpty()) return 0
        val first = entries.minOf { it.date }.weekStart()
        var w = first
        var sum = 0L
        val last = today.weekStart()
        while (!w.isAfter(last)) {
            sum += week(w).balance
            w = w.plusWeeks(1)
        }
        return sum
    }
}

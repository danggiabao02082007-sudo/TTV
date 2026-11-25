package com.tieuvat.app.data

import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class EntryType { INCOME, EXPENSE }

enum class Category(val label: String, val emoji: String, val color: Color, val forIncome: Boolean) {
    FOOD("Ăn uống", "🍜", Color(0xFFFF7043), false),
    DRINK("Trà sữa & nước", "🧋", Color(0xFFAB47BC), false),
    STUDY("Học tập", "📚", Color(0xFF42A5F5), false),
    TRANSPORT("Đi lại", "🛵", Color(0xFF26A69A), false),
    SHOPPING("Mua sắm", "🛍️", Color(0xFFEC407A), false),
    GAME("Giải trí", "🎮", Color(0xFF7E57C2), false),
    GIFT("Quà tặng", "🎁", Color(0xFFFFCA28), false),
    PHONE("Nạp thẻ / Internet", "📱", Color(0xFF5C6BC0), false),
    HEALTH("Sức khỏe", "💊", Color(0xFF66BB6A), false),
    OTHER("Khác", "✨", Color(0xFF8D6E63), false),
    ALLOWANCE("Tiền tiêu vặt", "💰", Color(0xFF43A047), true),
    BONUS("Thưởng / Lì xì", "🧧", Color(0xFFE53935), true),
    PARTTIME("Làm thêm", "💼", Color(0xFF1E88E5), true),
    OTHER_IN("Thu khác", "🌟", Color(0xFFFFB300), true);

    companion object {
        fun of(name: String): Category = entries.firstOrNull { it.name == name } ?: OTHER
        fun forType(t: EntryType) = entries.filter { it.forIncome == (t == EntryType.INCOME) }
    }
}

data class Entry(
    val id: Long,
    val type: EntryType,
    val name: String,
    val amount: Long,
    val category: Category,
    val date: LocalDate,
    val note: String = ""
)

data class Settings(
    val weeklyAllowance: Long = 0,   // tiền tiêu vặt cố định mỗi tuần (tự cộng vào thu)
    val weeklyLimit: Long = 0,       // hạn mức chi tối đa mỗi tuần
    val savingGoal: Long = 0,        // mục tiêu tiết kiệm
    val savingGoalName: String = ""
)

object Fmt {
    private val nf: NumberFormat = NumberFormat.getInstance(Locale("vi", "VN"))
    fun money(v: Long): String = nf.format(v) + " ₫"
    fun signed(v: Long): String = (if (v > 0) "+" else if (v < 0) "−" else "") + money(kotlin.math.abs(v))
    private val dm = DateTimeFormatter.ofPattern("dd/MM")
    private val dmy = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    fun dm(d: LocalDate): String = d.format(dm)
    fun dmy(d: LocalDate): String = d.format(dmy)
    fun dayName(d: LocalDate): String = when (d.dayOfWeek) {
        DayOfWeek.MONDAY -> "T2"
        DayOfWeek.TUESDAY -> "T3"
        DayOfWeek.WEDNESDAY -> "T4"
        DayOfWeek.THURSDAY -> "T5"
        DayOfWeek.FRIDAY -> "T6"
        DayOfWeek.SATURDAY -> "T7"
        else -> "CN"
    }
}

fun LocalDate.weekStart(): LocalDate = with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

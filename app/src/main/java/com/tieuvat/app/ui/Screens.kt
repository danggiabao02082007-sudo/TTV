package com.tieuvat.app.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tieuvat.app.data.Entry
import com.tieuvat.app.data.EntryType
import com.tieuvat.app.data.Fmt
import com.tieuvat.app.data.Settings
import com.tieuvat.app.data.Store
import com.tieuvat.app.data.WeekSummary
import com.tieuvat.app.data.weekStart
import java.time.LocalDate

/* ---------------------------- TỔNG QUAN TUẦN ---------------------------- */

@Composable
fun WeekSelector(week: LocalDate, onChange: (LocalDate) -> Unit) {
    val isThisWeek = week == LocalDate.now().weekStart()
    GlassCard(corner = 20.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            CircleBtn("◀") { onChange(week.minusWeeks(1)) }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (isThisWeek) "🌈 Tuần này" else "🗓️ Tuần trước đó", fontWeight = FontWeight.Bold, color = InkDark)
                Text("${Fmt.dm(week)} → ${Fmt.dmy(week.plusDays(6))}", fontSize = 13.sp, color = InkDark.copy(alpha = 0.7f))
            }
            CircleBtn("▶") { onChange(week.plusWeeks(1)) }
        }
        if (!isThisWeek) {
            Text("↩ Về tuần này", color = Color(0xFF7048E8), fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally).clickable { onChange(LocalDate.now().weekStart()) }.padding(top = 6.dp))
        }
    }
}

@Composable
private fun CircleBtn(t: String, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFFFF8FAB), Color(0xFF9775FA))))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(t, color = Color.White, fontWeight = FontWeight.Bold) }
}

@Composable
fun SummaryCard(s: WeekSummary, settings: Settings) {
    val bal = s.balance
    GlassCard(border = listOf(Color(0xFFFFD43B), Color(0xFFFF6B6B), Color(0xFFCC5DE8), Color(0xFF339AF0))) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("💵 Thu vào", Fmt.money(s.income), listOf(Color(0xFF38D9A9), Color(0xFF69DB7C)), Modifier.weight(1f))
            StatTile("🛒 Chi ra", Fmt.money(s.expense), listOf(Color(0xFFFF8787), Color(0xFFF783AC)), Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        val (label, colors, emoji) = when {
            bal > 0 -> Triple("Còn dư (lãi)", listOf(Color(0xFF12B886), Color(0xFF4DABF7)), "🤑")
            bal < 0 -> Triple("Bị lỗ (chi vượt thu)", listOf(Color(0xFFFA5252), Color(0xFFFD7E14)), "😱")
            else -> Triple("Hòa vốn", listOf(Color(0xFF845EF7), Color(0xFF5C7CFA)), "😌")
        }
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                .background(Brush.horizontalGradient(colors)).padding(14.dp)
        ) {
            Column {
                Text("$emoji $label", color = Color.White, fontWeight = FontWeight.Bold)
                Text(Fmt.signed(bal), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                if (settings.weeklyAllowance > 0)
                    Text("Đã gồm tiền tiêu vặt cố định ${Fmt.money(settings.weeklyAllowance)}/tuần",
                        color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
            }
        }
        if (settings.weeklyLimit > 0) {
            Spacer(Modifier.height(12.dp))
            val f = s.expense.toFloat() / settings.weeklyLimit
            Text("🎯 Hạn mức chi tuần: ${Fmt.money(s.expense)} / ${Fmt.money(settings.weeklyLimit)}",
                fontWeight = FontWeight.SemiBold, color = InkDark, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            GradientProgress(f, if (f < 0.7f) listOf(Color(0xFF69DB7C), Color(0xFF38D9A9))
                else if (f < 1f) listOf(Color(0xFFFFD43B), Color(0xFFFF922B)) else listOf(Color(0xFFFF6B6B), Color(0xFFE03131)))
            if (f >= 1f) Text("⚠️ Đã vượt hạn mức ${Fmt.money(s.expense - settings.weeklyLimit)}!", color = ExpenseRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.height(10.dp))
        Text(adviceFor(s, settings), color = InkDark, fontSize = 14.sp)
    }
}

private fun adviceFor(s: WeekSummary, st: Settings): String {
    val today = LocalDate.now()
    val daysLeft = if (today.weekStart() == s.start) (6 - (today.dayOfWeek.value - 1)) else 0
    return when {
        s.entries.isEmpty() && s.income == 0L -> "🌱 Chưa có giao dịch nào. Bấm nút ➕ để ghi khoản đầu tiên nhé!"
        st.weeklyLimit > 0 && s.expense > st.weeklyLimit -> "🍂 Đã vượt hạn mức tuần. Những ngày còn lại nên hạn chế mua sắm nhé."
        s.balance < 0 -> "🍂 Tuần này chi nhiều hơn thu ${Fmt.money(-s.balance)}. Thử cắt bớt mục tốn nhất ở tab Thống kê."
        daysLeft > 0 && s.balance > 0 -> "🌳 Còn $daysLeft ngày, bạn có thể tiêu khoảng ${Fmt.money(s.balance / (daysLeft + 1))}/ngày mà không lỗ."
        st.weeklyLimit > 0 && s.expense <= st.weeklyLimit -> "🌸 Giữ chi tiêu trong hạn mức rất tốt!"
        else -> "🌻 Cây tiết kiệm của bạn đang lớn dần. Cố lên!"
    }
}

@Composable
private fun StatTile(title: String, value: String, colors: List<Color>, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(colors)).padding(12.dp)
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun EntryRow(e: Entry, onClick: () -> Unit) {
    val c = e.category.color
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .background(Brush.horizontalGradient(listOf(c.copy(alpha = 0.25f), Color.Transparent)))
            .clickable(onClick = onClick).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(Brush.linearGradient(listOf(c, c.copy(alpha = 0.6f)))),
            contentAlignment = Alignment.Center
        ) { Text(e.category.emoji, fontSize = 22.sp) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(e.name, fontWeight = FontWeight.Bold, color = InkDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${Fmt.dayName(e.date)} ${Fmt.dm(e.date)} • ${e.category.label}" + if (e.note.isNotBlank()) " • ${e.note}" else "",
                fontSize = 12.sp, color = InkDark.copy(alpha = 0.65f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(
            (if (e.type == EntryType.INCOME) "+" else "−") + Fmt.money(e.amount),
            color = if (e.type == EntryType.INCOME) IncomeGreen else ExpenseRed, fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun HomeScreen(store: Store, week: LocalDate, onWeek: (LocalDate) -> Unit, pad: PaddingValues, onEdit: (Entry) -> Unit) {
    val s = store.week(week)
    LazyColumn(
        Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, pad.calculateTopPadding() + 8.dp, 16.dp, pad.calculateBottomPadding() + 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Header() }
        item { WeekSelector(week, onWeek) }
        item { SummaryCard(s, store.settings) }
        item { SectionTitle("🧾 Giao dịch trong tuần (${s.entries.size})") }
        if (s.entries.isEmpty()) item { EmptyHint() }
        items(s.entries, key = { it.id }) { e -> EntryRow(e) { onEdit(e) } }
    }
}

@Composable
fun Header() {
    GlassCard(border = listOf(Color(0xFF69DB7C), Color(0xFF4DABF7), Color(0xFFF783AC))) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🌳", fontSize = 40.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                RainbowTitle("Kế hoạch thu chi", 24)
                RainbowTitle("tiền tiêu vặt của tôi", 18)
            }
        }
    }
}

@Composable
fun SectionTitle(t: String) {
    Box(
        Modifier.clip(RoundedCornerShape(50))
            .background(Brush.horizontalGradient(listOf(Color(0xFF7950F2), Color(0xFFE64980), Color(0xFFFD7E14))))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) { Text(t, color = Color.White, fontWeight = FontWeight.Bold) }
}

@Composable
private fun EmptyHint() {
    GlassCard {
        Text("🌼 Trống trơn! Mỗi lần chuyển khoản mua gì, hãy bấm nút ➕ tròn phía dưới để ghi lại tên món và số tiền.",
            color = InkDark)
    }
}

/* ------------------------------ THỐNG KÊ ------------------------------ */

@Composable
fun StatsScreen(store: Store, week: LocalDate, onWeek: (LocalDate) -> Unit, pad: PaddingValues) {
    val s = store.week(week)
    val prev = store.week(week.minusWeeks(1))
    val byCat = s.entries.filter { it.type == EntryType.EXPENSE }
        .groupBy { it.category }.mapValues { (_, v) -> v.sumOf { it.amount } }
        .toList().sortedByDescending { it.second }
    val days = (0..6).map { week.plusDays(it.toLong()) }
    val perDay = days.map { d -> s.entries.filter { it.date == d && it.type == EntryType.EXPENSE }.sumOf { it.amount } }

    LazyColumn(
        Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, pad.calculateTopPadding() + 8.dp, 16.dp, pad.calculateBottomPadding() + 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { WeekSelector(week, onWeek) }
        item {
            GlassCard {
                SectionTitle("🍩 Chi theo danh mục")
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        DonutChart(byCat.map { it.first.color to it.second }, Modifier.size(140.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tổng chi", fontSize = 11.sp, color = InkDark)
                            Text(Fmt.money(s.expense), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = InkDark)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        if (byCat.isEmpty()) Text("Chưa có khoản chi.", color = InkDark)
                        byCat.forEach { (c, v) ->
                            val pct = if (s.expense > 0) v * 100 / s.expense else 0
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                Box(Modifier.size(12.dp).clip(CircleShape).background(c.color))
                                Spacer(Modifier.width(6.dp))
                                Text("${c.emoji} ${c.label} $pct%", fontSize = 12.sp, color = InkDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
        item {
            GlassCard {
                SectionTitle("📊 Chi tiêu từng ngày")
                Spacer(Modifier.height(10.dp))
                DayBars(days.map { Fmt.dayName(it) }, perDay)
                val maxIdx = perDay.indices.maxByOrNull { perDay[it] } ?: 0
                if (perDay[maxIdx] > 0) Text("🔥 Tiêu nhiều nhất: ${Fmt.dayName(days[maxIdx])} (${Fmt.money(perDay[maxIdx])})",
                    color = InkDark, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
            }
        }
        item {
            GlassCard {
                SectionTitle("⚖️ So với tuần trước")
                Spacer(Modifier.height(8.dp))
                val diff = s.expense - prev.expense
                val pct = if (prev.expense > 0) diff * 100 / prev.expense else null
                Text("Tuần trước chi ${Fmt.money(prev.expense)}, tuần này ${Fmt.money(s.expense)}.", color = InkDark)
                Text(
                    when {
                        diff > 0 -> "📈 Tiêu nhiều hơn ${Fmt.money(diff)}" + (pct?.let { " (+$it%)" } ?: "")
                        diff < 0 -> "📉 Tiết kiệm hơn ${Fmt.money(-diff)}" + (pct?.let { " ($it%)" } ?: "")
                        else -> "➖ Bằng tuần trước"
                    },
                    color = if (diff > 0) ExpenseRed else IncomeGreen, fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            val top = s.entries.filter { it.type == EntryType.EXPENSE }.sortedByDescending { it.amount }.take(3)
            GlassCard {
                SectionTitle("🏆 Top món tốn tiền nhất")
                Spacer(Modifier.height(8.dp))
                if (top.isEmpty()) Text("Chưa có dữ liệu.", color = InkDark)
                top.forEachIndexed { i, e ->
                    Text("${listOf("🥇", "🥈", "🥉")[i]} ${e.name} — ${Fmt.money(e.amount)}", color = InkDark, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
        item {
            GlassCard {
                SectionTitle("🌿 6 tuần gần nhất")
                Spacer(Modifier.height(8.dp))
                (5 downTo 0).forEach { k ->
                    val ws = week.minusWeeks(k.toLong())
                    val w = store.week(ws)
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Text("${Fmt.dm(ws)}–${Fmt.dm(ws.plusDays(6))}", Modifier.weight(1f), color = InkDark, fontSize = 13.sp)
                        Text("chi ${Fmt.money(w.expense)}", Modifier.weight(1f), color = ExpenseRed, fontSize = 13.sp)
                        Text(Fmt.signed(w.balance), color = if (w.balance >= 0) IncomeGreen else ExpenseRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/* ------------------------------ LỊCH SỬ ------------------------------ */

@Composable
fun HistoryScreen(store: Store, pad: PaddingValues, onEdit: (Entry) -> Unit) {
    var q by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<EntryType?>(null) }
    val list = store.entries
        .filter { filter == null || it.type == filter }
        .filter { q.isBlank() || it.name.contains(q, true) || it.note.contains(q, true) || it.category.label.contains(q, true) }
        .sortedWith(compareByDescending<Entry> { it.date }.thenByDescending { it.id })
    val grouped = list.groupBy { it.date }

    LazyColumn(
        Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, pad.calculateTopPadding() + 8.dp, 16.dp, pad.calculateBottomPadding() + 90.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            GlassCard {
                RainbowTitle("📜 Lịch sử giao dịch", 22)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(q, { q = it }, label = { Text("🔎 Tìm theo tên, ghi chú, danh mục") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GradientPill("Tất cả", listOf(Color(0xFF845EF7), Color(0xFF339AF0)), Modifier.weight(1f), filter == null) { filter = null }
                    GradientPill("Chi", listOf(Color(0xFFFF6B6B), Color(0xFFF06595)), Modifier.weight(1f), filter == EntryType.EXPENSE) { filter = EntryType.EXPENSE }
                    GradientPill("Thu", listOf(Color(0xFF20C997), Color(0xFF51CF66)), Modifier.weight(1f), filter == EntryType.INCOME) { filter = EntryType.INCOME }
                }
                Spacer(Modifier.height(6.dp))
                Text("Tổng chi: ${Fmt.money(list.filter { it.type == EntryType.EXPENSE }.sumOf { it.amount })} • " +
                        "Tổng thu: ${Fmt.money(list.filter { it.type == EntryType.INCOME }.sumOf { it.amount })}",
                    fontSize = 12.sp, color = InkDark)
            }
        }
        if (list.isEmpty()) item { GlassCard { Text("Không có giao dịch phù hợp.", color = InkDark) } }
        grouped.forEach { (d, es) ->
            item(key = "h$d") {
                Text("📅 ${Fmt.dayName(d)}, ${Fmt.dmy(d)}", fontWeight = FontWeight.ExtraBold, color = Color.White,
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(Color(0x99512DA8)).padding(horizontal = 12.dp, vertical = 4.dp))
            }
            items(es, key = { it.id }) { e -> EntryRow(e) { onEdit(e) } }
        }
    }
}

/* ------------------------------ CÀI ĐẶT ------------------------------ */

@Composable
fun SettingsScreen(store: Store, week: LocalDate, pad: PaddingValues) {
    val ctx = LocalContext.current
    val st = store.settings
    var allowance by remember { mutableStateOf(if (st.weeklyAllowance > 0) st.weeklyAllowance.toString() else "") }
    var limit by remember { mutableStateOf(if (st.weeklyLimit > 0) st.weeklyLimit.toString() else "") }
    var goal by remember { mutableStateOf(if (st.savingGoal > 0) st.savingGoal.toString() else "") }
    var goalName by remember { mutableStateOf(st.savingGoalName) }
    var saved by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, pad.calculateTopPadding() + 8.dp, 16.dp, pad.calculateBottomPadding() + 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            val total = store.totalSaved(LocalDate.now())
            GlassCard(border = listOf(Color(0xFF51CF66), Color(0xFFFCC419), Color(0xFFFF8787))) {
                RainbowTitle("🐷 Heo đất tiết kiệm", 22)
                Spacer(Modifier.height(6.dp))
                Text("Tổng dư tích lũy: ${Fmt.signed(total)}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp,
                    color = if (total >= 0) IncomeGreen else ExpenseRed)
                if (st.savingGoal > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text("Mục tiêu: ${st.savingGoalName.ifBlank { "Tiết kiệm" }} — ${Fmt.money(st.savingGoal)}", color = InkDark)
                    Spacer(Modifier.height(4.dp))
                    GradientProgress(total.toFloat() / st.savingGoal, listOf(Color(0xFFFFD43B), Color(0xFFFF922B), Color(0xFFF06595)))
                    val remain = st.savingGoal - total
                    Text(if (remain <= 0) "🎉 Đã đạt mục tiêu!" else "Còn thiếu ${Fmt.money(remain)}", color = InkDark, fontSize = 13.sp)
                }
            }
        }
        item {
            GlassCard {
                RainbowTitle("⚙️ Thiết lập", 22)
                Spacer(Modifier.height(8.dp))
                MoneyField("💰 Tiền tiêu vặt cố định mỗi tuần", allowance) { allowance = it; saved = false }
                Text("Tự động tính vào khoản thu của mỗi tuần. Để trống nếu không cố định.", fontSize = 12.sp, color = InkDark.copy(alpha = 0.7f))
                Spacer(Modifier.height(8.dp))
                MoneyField("🎯 Hạn mức chi tối đa mỗi tuần", limit) { limit = it; saved = false }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(goalName, { goalName = it.take(40); saved = false }, label = { Text("🎁 Tên mục tiêu (vd: Mua tai nghe)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                MoneyField("🐷 Số tiền mục tiêu", goal) { goal = it; saved = false }
                Spacer(Modifier.height(10.dp))
                GradientPill(if (saved) "✅ Đã lưu" else "💾 Lưu thiết lập", listOf(Color(0xFF7950F2), Color(0xFF4DABF7)), Modifier.fillMaxWidth()) {
                    store.updateSettings(Settings(allowance.toLongOrNull() ?: 0, limit.toLongOrNull() ?: 0, goal.toLongOrNull() ?: 0, goalName.trim()))
                    saved = true
                }
            }
        }
        item {
            GlassCard {
                RainbowTitle("📤 Chia sẻ & dữ liệu", 22)
                Spacer(Modifier.height(8.dp))
                GradientPill("📤 Gửi bản tổng kết tuần đang xem", listOf(Color(0xFFFF922B), Color(0xFFF06595)), Modifier.fillMaxWidth()) {
                    val text = shareText(store, week)
                    val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
                    ctx.startActivity(Intent.createChooser(i, "Chia sẻ tổng kết"))
                }
                Spacer(Modifier.height(8.dp))
                GradientPill("🗑️ Xóa toàn bộ giao dịch", listOf(Color(0xFFFA5252), Color(0xFFC92A2A)), Modifier.fillMaxWidth()) { confirmClear = true }
                Spacer(Modifier.height(8.dp))
                Text("Dữ liệu chỉ lưu trên điện thoại này, không gửi lên mạng. Gỡ ứng dụng sẽ mất dữ liệu.", fontSize = 12.sp, color = InkDark.copy(alpha = 0.7f))
            }
        }
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Xóa toàn bộ?") },
            text = { Text("Tất cả giao dịch sẽ bị xóa vĩnh viễn. Thiết lập vẫn được giữ.") },
            confirmButton = { TextButton(onClick = { store.clearAll(); confirmClear = false }) { Text("Xóa", color = ExpenseRed) } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Hủy") } }
        )
    }
}

@Composable
private fun MoneyField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value, { s -> onChange(s.filter { it.isDigit() }.take(12).trimStart('0')) },
        label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        supportingText = { value.toLongOrNull()?.let { Text(Fmt.money(it)) } },
        modifier = Modifier.fillMaxWidth()
    )
}

private fun shareText(store: Store, week: LocalDate): String {
    val s = store.week(week)
    val sb = StringBuilder()
    sb.append("🌳 Tổng kết tiền tiêu vặt ${Fmt.dm(s.start)}–${Fmt.dmy(s.end)}\n")
    sb.append("💵 Thu: ${Fmt.money(s.income)}\n🛒 Chi: ${Fmt.money(s.expense)}\n")
    sb.append(if (s.balance >= 0) "🤑 Dư: ${Fmt.money(s.balance)}\n" else "😱 Lỗ: ${Fmt.money(-s.balance)}\n")
    sb.append("\nChi tiết:\n")
    s.entries.forEach { e ->
        sb.append("${Fmt.dm(e.date)} ${e.category.emoji} ${e.name}: ${if (e.type == EntryType.INCOME) "+" else "−"}${Fmt.money(e.amount)}\n")
    }
    return sb.toString()
}


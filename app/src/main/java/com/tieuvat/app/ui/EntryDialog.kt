package com.tieuvat.app.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tieuvat.app.data.Category
import com.tieuvat.app.data.Entry
import com.tieuvat.app.data.EntryType
import com.tieuvat.app.data.Fmt
import java.time.LocalDate

@Composable
fun EntryDialog(
    initial: Entry?,
    defaultType: EntryType,
    onDismiss: () -> Unit,
    onSave: (Entry) -> Unit,
    onDelete: ((Long) -> Unit)?
) {
    val ctx = LocalContext.current
    var type by remember { mutableStateOf(initial?.type ?: defaultType) }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var amountText by remember { mutableStateOf(initial?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: Category.forType(type).first()) }
    var date by remember { mutableStateOf(initial?.date ?: LocalDate.now()) }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    val amount = amountText.toLongOrNull() ?: 0L
    val typeColors = if (type == EntryType.EXPENSE) listOf(Color(0xFFFF6B6B), Color(0xFFF06595))
    else listOf(Color(0xFF20C997), Color(0xFF51CF66))

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFFFBFE),
        title = {
            RainbowTitle(if (initial == null) (if (type == EntryType.EXPENSE) "🛒 Ghi khoản chi" else "💵 Ghi khoản thu")
            else "✏️ Sửa giao dịch", 22)
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GradientPill("➖ Chi ra", listOf(Color(0xFFFF6B6B), Color(0xFFF06595)), Modifier.weight(1f), type == EntryType.EXPENSE) {
                        if (type != EntryType.EXPENSE) { type = EntryType.EXPENSE; category = Category.forType(type).first() }
                    }
                    GradientPill("➕ Thu vào", listOf(Color(0xFF20C997), Color(0xFF51CF66)), Modifier.weight(1f), type == EntryType.INCOME) {
                        if (type != EntryType.INCOME) { type = EntryType.INCOME; category = Category.forType(type).first() }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name, onValueChange = { name = it.take(60) },
                    label = { Text(if (type == EntryType.EXPENSE) "Tên món đã mua" else "Nguồn tiền") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { s -> amountText = s.filter { it.isDigit() }.take(12).trimStart('0') },
                    label = { Text("Số tiền (₫)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text(if (amount > 0) Fmt.money(amount) else "Nhập số, ví dụ 25000") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(5_000L, 10_000L, 20_000L, 50_000L).forEachIndexed { i, v ->
                        Box(
                            Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(Rainbow[i], Rainbow[i + 1])))
                                .clickable { amountText = (amount + v).toString() }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("+${v / 1000}k", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Danh mục", fontWeight = FontWeight.Bold, color = InkDark)
                Spacer(Modifier.height(6.dp))
                Category.forType(type).chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                        row.forEach { c ->
                            val sel = c == category
                            Column(
                                Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                                    .background(if (sel) c.color else c.color.copy(alpha = 0.12f))
                                    .border(1.5.dp, c.color, RoundedCornerShape(14.dp))
                                    .clickable { category = c }
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(c.emoji, fontSize = 20.sp)
                                Text(c.label, fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 2,
                                    color = if (sel) Color.White else InkDark, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFE7F5FF), Color(0xFFF3D9FA))))
                        .clickable {
                            DatePickerDialog(ctx, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) },
                                date.year, date.monthValue - 1, date.dayOfMonth).show()
                        }
                        .padding(12.dp)
                ) { Text("📅 Ngày: ${Fmt.dayName(date)}, ${Fmt.dmy(date)}  (chạm để đổi)", color = InkDark, fontWeight = FontWeight.SemiBold) }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note, onValueChange = { note = it.take(120) },
                    label = { Text("Ghi chú (không bắt buộc)") }, modifier = Modifier.fillMaxWidth()
                )
                error?.let { Text(it, color = ExpenseRed, modifier = Modifier.padding(top = 6.dp)) }
                if (initial != null && onDelete != null) {
                    TextButton(onClick = { onDelete(initial.id) }) { Text("🗑️ Xóa giao dịch này", color = ExpenseRed) }
                }
            }
        },
        confirmButton = {
            GradientPill("💾 Lưu", typeColors) {
                when {
                    name.isBlank() -> error = "Hãy nhập tên khoản ${if (type == EntryType.EXPENSE) "chi" else "thu"}"
                    amount <= 0 -> error = "Số tiền phải lớn hơn 0"
                    else -> onSave(
                        Entry(
                            id = initial?.id ?: System.currentTimeMillis(),
                            type = type, name = name.trim(), amount = amount,
                            category = category, date = date, note = note.trim()
                        )
                    )
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

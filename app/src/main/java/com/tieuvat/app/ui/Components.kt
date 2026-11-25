package com.tieuvat.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Rainbow = listOf(
    Color(0xFFFF5F9E), Color(0xFFFFA94D), Color(0xFFFFE066),
    Color(0xFF69DB7C), Color(0xFF4DABF7), Color(0xFF9775FA)
)
val IncomeGreen = Color(0xFF12B886)
val ExpenseRed = Color(0xFFFA5252)
val InkDark = Color(0xFF2B2140)

/** Thẻ kính mờ có viền cầu vồng. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    border: List<Color> = Rainbow,
    corner: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(corner)
    Column(
        modifier
            .fillMaxWidth()
            .shadow(10.dp, shape, ambientColor = border.first(), spotColor = border.last())
            .clip(shape)
            .background(Color.White.copy(alpha = 0.9f))
            .border(2.dp, Brush.linearGradient(border), shape)
            .padding(16.dp),
        content = content
    )
}

/** Nút viên thuốc có nền gradient. */
@Composable
fun GradientPill(
    text: String,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    selected: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .clip(shape)
            .background(
                if (selected) Brush.horizontalGradient(colors)
                else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0.7f)))
            )
            .border(1.5.dp, Brush.horizontalGradient(colors), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Color.White else InkDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

/** Chữ tiêu đề tô màu cầu vồng. */
@Composable
fun RainbowTitle(text: String, size: Int = 24) {
    Text(
        text,
        style = androidx.compose.ui.text.TextStyle(
            brush = Brush.linearGradient(listOf(Color(0xFFE64980), Color(0xFFF76707), Color(0xFF7048E8), Color(0xFF1C7ED6))),
            fontSize = size.sp,
            fontWeight = FontWeight.ExtraBold
        )
    )
}

/** Thanh tiến độ gradient. */
@Composable
fun GradientProgress(fraction: Float, colors: List<Color>, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(shape)
            .background(Color(0x22000000))
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(14.dp)
                .clip(shape)
                .background(Brush.horizontalGradient(colors))
        )
    }
}

/** Biểu đồ vành khuyên theo danh mục. */
@Composable
fun DonutChart(values: List<Pair<Color, Long>>, modifier: Modifier = Modifier) {
    val total = values.sumOf { it.second }.coerceAtLeast(1L).toFloat()
    Canvas(modifier) {
        val stroke = size.minDimension * 0.18f
        val d = size.minDimension - stroke
        val tl = Offset((size.width - d) / 2f, (size.height - d) / 2f)
        if (values.isEmpty()) {
            drawArc(Color(0x22000000), 0f, 360f, false, tl, Size(d, d), style = Stroke(stroke))
            return@Canvas
        }
        var start = -90f
        values.forEach { (c, v) ->
            val sweep = v / total * 360f
            drawArc(c, start, sweep - 1.5f, false, tl, Size(d, d), style = Stroke(stroke))
            start += sweep
        }
    }
}

/** Biểu đồ cột chi tiêu theo ngày. */
@Composable
fun DayBars(labels: List<String>, values: List<Long>, modifier: Modifier = Modifier) {
    val max = (values.maxOrNull() ?: 0L).coerceAtLeast(1L).toFloat()
    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            val n = values.size
            val slot = size.width / n
            val bw = slot * 0.55f
            values.forEachIndexed { i, v ->
                val bh = (v / max) * (size.height - 8f)
                val x = i * slot + (slot - bw) / 2f
                drawRoundRect(
                    Brush.verticalGradient(listOf(Rainbow[i % Rainbow.size], Rainbow[(i + 2) % Rainbow.size]), size.height - bh, size.height),
                    Offset(x, size.height - bh), Size(bw, bh.coerceAtLeast(4f)),
                    androidx.compose.ui.geometry.CornerRadius(bw / 3f)
                )
            }
        }
        androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth()) {
            labels.forEach {
                Text(it, Modifier.weight(1f).padding(top = 4.dp), fontSize = 12.sp, color = InkDark,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

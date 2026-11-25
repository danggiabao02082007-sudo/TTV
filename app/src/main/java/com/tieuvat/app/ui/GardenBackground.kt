package com.tieuvat.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class TreeSpec(val x: Float, val kind: Int, val scale: Float, val hue: Int)
private data class Flower(val x: Float, val y: Float, val color: Color, val r: Float)
private data class Blade(val x: Float, val h: Float, val lean: Float, val color: Color)

private val leafPalettes = listOf(
    listOf(Color(0xFFB9F6CA), Color(0xFF00C853)),
    listOf(Color(0xFFCCFF90), Color(0xFF64DD17)),
    listOf(Color(0xFFA7FFEB), Color(0xFF00BFA5)),
    listOf(Color(0xFFFFF59D), Color(0xFF9CCC65)),
    listOf(Color(0xFFF8BBD0), Color(0xFFEC407A)), // cây hoa anh đào
    listOf(Color(0xFFFFE0B2), Color(0xFFFF9800))  // cây lá cam
)
private val flowerColors = listOf(
    Color(0xFFFF4081), Color(0xFFFFEB3B), Color(0xFFE040FB), Color(0xFF40C4FF),
    Color(0xFFFF6E40), Color(0xFFFFFFFF), Color(0xFF69F0AE)
)

/** Nền khu vườn chi tiết, có chuyển động nhẹ: mặt trời xoay, mây trôi, bướm bay, cỏ đung đưa. */
@Composable
fun GardenBackground(modifier: Modifier = Modifier) {
    val rnd = remember { Random(2026) }
    val farTrees = remember { List(14) { TreeSpec(rnd.nextFloat(), rnd.nextInt(3), 0.45f + rnd.nextFloat() * 0.25f, rnd.nextInt(leafPalettes.size)) } }
    val nearTrees = remember { List(7) { i -> TreeSpec((i + rnd.nextFloat() * 0.6f) / 7f, rnd.nextInt(3), 0.8f + rnd.nextFloat() * 0.4f, rnd.nextInt(leafPalettes.size)) } }
    val flowers = remember { List(70) { Flower(rnd.nextFloat(), 0.88f + rnd.nextFloat() * 0.12f, flowerColors[rnd.nextInt(flowerColors.size)], 3f + rnd.nextFloat() * 5f) } }
    val blades = remember {
        List(220) {
            Blade(rnd.nextFloat(), 10f + rnd.nextFloat() * 26f, -0.5f + rnd.nextFloat(),
                listOf(Color(0xFF2E7D32), Color(0xFF43A047), Color(0xFF66BB6A), Color(0xFF9CCC65))[rnd.nextInt(4)])
        }
    }

    val t = rememberInfiniteTransition(label = "garden")
    val sunAngle by t.animateFloat(0f, 360f, infiniteRepeatable(tween(60000, easing = LinearEasing)), label = "sun")
    val cloud by t.animateFloat(0f, 1f, infiniteRepeatable(tween(90000, easing = LinearEasing)), label = "cloud")
    val sway by t.animateFloat(-1f, 1f, infiniteRepeatable(tween(2600), RepeatMode.Reverse), label = "sway")
    val fly by t.animateFloat(0f, 1f, infiniteRepeatable(tween(24000, easing = LinearEasing)), label = "fly")
    val flap by t.animateFloat(0.3f, 1f, infiniteRepeatable(tween(260), RepeatMode.Reverse), label = "flap")

    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Bầu trời pha nhiều màu
        drawRect(
            Brush.verticalGradient(
                0f to Color(0xFF8EC5FF),
                0.25f to Color(0xFFC3B1FF),
                0.45f to Color(0xFFFFC1E3),
                0.62f to Color(0xFFFFE5B4),
                1f to Color(0xFFE8FFD6)
            )
        )

        // 2. Cầu vồng
        val rainbow = listOf(
            Color(0x66FF1744), Color(0x66FF9100), Color(0x66FFEA00), Color(0x6600E676),
            Color(0x662979FF), Color(0x66651FFF), Color(0x66D500F9)
        )
        val rc = Offset(w * 0.62f, h * 0.62f)
        rainbow.forEachIndexed { i, c ->
            val r = w * 0.62f - i * w * 0.028f
            drawArc(c, 180f, 180f, false, Offset(rc.x - r, rc.y - r), Size(r * 2, r * 2), style = Stroke(w * 0.028f))
        }

        // 3. Mặt trời và tia nắng
        val sun = Offset(w * 0.18f, h * 0.12f)
        val sr = w * 0.09f
        drawCircle(Brush.radialGradient(listOf(Color(0x88FFF59D), Color(0x00FFF59D)), sun, sr * 3.2f), sr * 3.2f, sun)
        rotate(sunAngle, sun) {
            for (i in 0 until 16) {
                val a = (i * 22.5f) * (PI / 180f).toFloat()
                val len = if (i % 2 == 0) sr * 1.9f else sr * 1.5f
                drawLine(
                    Color(0xCCFFD54F), Offset(sun.x + cos(a) * sr * 1.15f, sun.y + sin(a) * sr * 1.15f),
                    Offset(sun.x + cos(a) * len, sun.y + sin(a) * len), strokeWidth = sr * 0.18f, cap = StrokeCap.Round
                )
            }
        }
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFFFDE7), Color(0xFFFFD54F), Color(0xFFFFA000)), sun, sr), sr, sun)

        // 4. Mây trôi
        fun cloudAt(cx: Float, cy: Float, s: Float, alpha: Float) {
            val c = Color.White.copy(alpha = alpha)
            drawCircle(c, 26f * s, Offset(cx, cy))
            drawCircle(c, 34f * s, Offset(cx + 30f * s, cy - 12f * s))
            drawCircle(c, 24f * s, Offset(cx + 62f * s, cy))
            drawRoundRect(c, Offset(cx - 10f * s, cy - 4f * s), Size(84f * s, 30f * s),
                androidx.compose.ui.geometry.CornerRadius(15f * s))
        }
        val span = w + 300f
        cloudAt((cloud * span + w * 0.1f) % span - 150f, h * 0.08f, 1.2f, 0.9f)
        cloudAt((cloud * span * 0.7f + w * 0.6f) % span - 150f, h * 0.18f, 0.9f, 0.8f)
        cloudAt((cloud * span * 1.3f + w * 0.35f) % span - 150f, h * 0.27f, 0.7f, 0.75f)

        // 5. Dãy núi xa
        mountain(w, h, 0.55f, listOf(Color(0xFFB39DDB), Color(0xFFD1C4E9)), 0.1f)
        mountain(w, h, 0.6f, listOf(Color(0xFF80CBC4), Color(0xFFB2DFDB)), 0.6f)

        // 6. Đồi xa + cây xa
        hill(w, h, 0.68f, Brush.verticalGradient(listOf(Color(0xFFA5D6A7), Color(0xFF81C784)), h * 0.64f, h))
        farTrees.forEach { tree(it, w * it.x, h * 0.7f, w * 0.12f * it.scale, sway * 0.5f) }

        // 7. Đồi gần + cây gần
        hill(w, h, 0.8f, Brush.verticalGradient(listOf(Color(0xFF8BE78B), Color(0xFF43A047)), h * 0.76f, h), phase = 1.7f)
        nearTrees.forEach { tree(it, w * it.x, h * 0.83f, w * 0.2f * it.scale, sway) }

        // 8. Thảm cỏ, hoa
        drawRect(Brush.verticalGradient(listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)), h * 0.88f, h), Offset(0f, h * 0.88f), Size(w, h * 0.12f))
        blades.forEach { b ->
            val bx = b.x * w
            val by = h * 0.9f + (b.h % 7f) * 8f
            drawLine(b.color, Offset(bx, by), Offset(bx + (b.lean + sway * 0.4f) * b.h * 0.6f, by - b.h), 2.4f, StrokeCap.Round)
        }
        flowers.forEach { f ->
            val c = Offset(f.x * w, f.y * h)
            for (k in 0 until 5) {
                val a = k * 72f * (PI / 180f).toFloat()
                drawCircle(f.color, f.r, Offset(c.x + cos(a) * f.r * 1.1f, c.y + sin(a) * f.r * 1.1f))
            }
            drawCircle(Color(0xFFFFC107), f.r * 0.7f, c)
        }

        // 9. Bướm bay
        butterfly(Offset(w * fly, h * 0.5f + sin(fly * 12f) * h * 0.04f), 14f, flap, Color(0xFFFF4081), Color(0xFF7C4DFF))
        butterfly(Offset(w * ((fly + 0.5f) % 1f), h * 0.4f + cos(fly * 10f) * h * 0.05f), 11f, flap, Color(0xFFFFAB00), Color(0xFF00B8D4))
        butterfly(Offset(w * (1f - fly), h * 0.62f + sin(fly * 8f) * h * 0.03f), 10f, flap, Color(0xFF00E676), Color(0xFFFFEA00))

        // 10. Lớp phủ nhẹ giúp chữ dễ đọc
        drawRect(Color.White.copy(alpha = 0.08f))
    }
}

private fun DrawScope.mountain(w: Float, h: Float, baseY: Float, colors: List<Color>, phase: Float) {
    val p = Path().apply {
        moveTo(0f, h * baseY)
        var x = 0f
        var i = 0
        while (x <= w) {
            val peak = h * (baseY - 0.08f - 0.06f * sin(i * 1.3f + phase))
            lineTo(x + w * 0.09f, peak)
            lineTo(x + w * 0.18f, h * baseY - h * 0.02f)
            x += w * 0.18f
            i++
        }
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(p, Brush.verticalGradient(colors, h * (baseY - 0.15f), h * baseY))
}

private fun DrawScope.hill(w: Float, h: Float, y: Float, brush: Brush, phase: Float = 0f) {
    val p = Path().apply {
        moveTo(0f, h * y)
        val steps = 40
        for (i in 0..steps) {
            val x = w * i / steps
            lineTo(x, h * y - h * 0.025f * sin(i / steps.toFloat() * 2f * PI.toFloat() * 1.2f + phase))
        }
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(p, brush)
}

private fun DrawScope.tree(spec: TreeSpec, x: Float, groundY: Float, size: Float, sway: Float) {
    val pal = leafPalettes[spec.hue]
    val trunkH = size * 0.55f
    val trunkW = size * 0.12f
    val topX = x + sway * size * 0.04f
    // thân cây
    val trunk = Path().apply {
        moveTo(x - trunkW, groundY)
        lineTo(topX - trunkW * 0.5f, groundY - trunkH)
        lineTo(topX + trunkW * 0.5f, groundY - trunkH)
        lineTo(x + trunkW, groundY)
        close()
    }
    drawPath(trunk, Brush.horizontalGradient(listOf(Color(0xFF6D4C41), Color(0xFFA1887F), Color(0xFF5D4037)), x - trunkW, x + trunkW))
    drawOval(Color(0x33000000), Offset(x - size * 0.35f, groundY - size * 0.04f), Size(size * 0.7f, size * 0.08f))
    val leaf = Brush.verticalGradient(pal, groundY - trunkH - size * 0.7f, groundY - trunkH * 0.6f)
    when (spec.kind) {
        0 -> { // cây tán tròn nhiều chùm
            val cy = groundY - trunkH - size * 0.2f
            listOf(
                Triple(-0.28f, 0.05f, 0.3f), Triple(0.28f, 0.05f, 0.3f), Triple(0f, -0.18f, 0.36f),
                Triple(-0.15f, -0.02f, 0.3f), Triple(0.15f, -0.02f, 0.3f)
            ).forEach { (dx, dy, r) ->
                drawCircle(leaf, size * r, Offset(topX + dx * size, cy + dy * size))
            }
            // điểm nhấn: quả / hoa
            for (k in 0 until 5) {
                val a = k * 1.25f
                drawCircle(if (spec.hue >= 4) Color.White else Color(0xFFFF5252), size * 0.035f,
                    Offset(topX + cos(a) * size * 0.25f, cy - size * 0.05f + sin(a) * size * 0.18f))
            }
        }
        1 -> { // cây thông nhiều tầng
            for (k in 0 until 3) {
                val baseY = groundY - trunkH * 0.6f - k * size * 0.28f
                val half = size * (0.42f - k * 0.1f)
                val p = Path().apply {
                    moveTo(topX - half, baseY)
                    lineTo(topX, baseY - size * 0.45f)
                    lineTo(topX + half, baseY)
                    close()
                }
                drawPath(p, leaf)
            }
            drawCircle(Color(0xFFFFEB3B), size * 0.04f, Offset(topX, groundY - trunkH * 0.6f - size * 1.0f))
        }
        else -> { // cây tán bầu dục
            val cy = groundY - trunkH - size * 0.25f
            drawOval(leaf, Offset(topX - size * 0.3f, cy - size * 0.45f), Size(size * 0.6f, size * 0.8f))
            drawOval(Color.White.copy(alpha = 0.25f), Offset(topX - size * 0.18f, cy - size * 0.36f), Size(size * 0.16f, size * 0.3f))
        }
    }
}

private fun DrawScope.butterfly(c: Offset, s: Float, flap: Float, c1: Color, c2: Color) {
    val wingW = s * flap
    drawOval(c1, Offset(c.x - wingW * 1.6f, c.y - s), Size(wingW * 1.6f, s * 1.2f))
    drawOval(c1, Offset(c.x, c.y - s), Size(wingW * 1.6f, s * 1.2f))
    drawOval(c2, Offset(c.x - wingW * 1.2f, c.y), Size(wingW * 1.2f, s * 0.9f))
    drawOval(c2, Offset(c.x, c.y), Size(wingW * 1.2f, s * 0.9f))
    drawLine(Color(0xFF3E2723), Offset(c.x, c.y - s * 0.9f), Offset(c.x, c.y + s * 0.8f), s * 0.22f, StrokeCap.Round)
}

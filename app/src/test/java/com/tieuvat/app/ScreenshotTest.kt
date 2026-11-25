package com.tieuvat.app

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.content.ContextCompat
import com.tieuvat.app.data.Category
import com.tieuvat.app.data.Entry
import com.tieuvat.app.data.EntryType
import com.tieuvat.app.data.Settings
import com.tieuvat.app.data.Store
import com.tieuvat.app.data.weekStart
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class ScreenshotTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private val out = File(System.getProperty("shotDir") ?: "build/shots").apply { mkdirs() }

    private fun save(name: String) {
        rule.mainClock.advanceTimeBy(1500)
        val v = rule.activity.window.decorView
        val bmp = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { v.draw(Canvas(bmp)) }
        FileOutputStream(File(out, "$name.png")).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    companion object {
        init {
            // dữ liệu mẫu ghi trước khi Activity mở
            val ctx = RuntimeEnvironment.getApplication()
            val st = Store(ctx)
            if (st.entries.isEmpty()) {
                val ws = LocalDate.now().weekStart()
                var id = 1L
                fun add(t: EntryType, n: String, a: Long, c: Category, d: Long) =
                    st.upsert(Entry(id++, t, n, a, c, ws.plusDays(d)))
                add(EntryType.EXPENSE, "Trà sữa trân châu", 35000, Category.DRINK, 0)
                add(EntryType.EXPENSE, "Bánh mì + xôi", 25000, Category.FOOD, 1)
                add(EntryType.EXPENSE, "Vở + bút highlight", 48000, Category.STUDY, 1)
                add(EntryType.EXPENSE, "Nạp game", 100000, Category.GAME, 2)
                add(EntryType.EXPENSE, "Xăng xe", 50000, Category.TRANSPORT, 3)
                add(EntryType.INCOME, "Mẹ cho thêm", 50000, Category.BONUS, 2)
                st.updateSettings(Settings(weeklyAllowance = 250000, weeklyLimit = 220000, savingGoal = 500000, savingGoalName = "Tai nghe"))
            }
        }
    }

    @Test fun shots() {
        rule.mainClock.autoAdvance = false
        save("1_tong_quan")
        rule.onNodeWithText("Thống kê").performClick(); save("2_thong_ke")
        rule.onNodeWithText("Lịch sử").performClick(); save("3_lich_su")
        rule.onNodeWithText("Tiết kiệm").performClick(); save("4_tiet_kiem")
        rule.onNodeWithText("Tổng quan").performClick(); save("1_tong_quan")
        // icon
        val ctx = RuntimeEnvironment.getApplication()
        val d = ContextCompat.getDrawable(ctx, R.mipmap.ic_launcher)!!
        val b = Bitmap.createBitmap(432, 432, Bitmap.Config.ARGB_8888)
        d.setBounds(0, 0, 432, 432); d.draw(Canvas(b))
        FileOutputStream(File(out, "0_icon.png")).use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}

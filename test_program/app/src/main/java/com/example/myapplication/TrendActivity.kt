package com.example.myapplication

import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.HistoryAdapter
import com.example.myapplication.ui.LineChartView
import com.example.myapplication.viewmodel.TrendViewModel

/**
 * ============================================================
 *  模块十三：BMI 趋势页（View 层，Assignment07）
 *
 *  上半部分：自绘折线图（LineChartView）看 BMI 变化趋势；
 *  下半部分：RecyclerView 列表逐条显示记录。
 *
 *  Context Menu（知识点 8）：长按列表某一行 → 弹「删除该记录 / 清空全部」。
 *  实现：Activity 里 registerForContextMenu(rv)，adapter 长按时记录行号。
 * ============================================================
 */
class TrendActivity : AppCompatActivity() {

    private val vm: TrendViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trend)

        val chart = findViewById<LineChartView>(R.id.lineChart)
        val rv = findViewById<RecyclerView>(R.id.rvHistory)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)

        // ---- RecyclerView 三件套：LayoutManager + Adapter（知识点 9） ----
        rv.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(emptyList())
        rv.adapter = adapter
        registerForContextMenu(rv)                    // ① 注册长按弹出 Context Menu

        // ---- 观察 VM 的数据：同时喂给列表和折线图 ----
        vm.records.observe(this) { list ->
            adapter.submit(list)
            chart.setData(list.map { LineChartView.ChartPoint(it.date, it.bmi.toFloat()) })
            tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    /** 回到本页时刷新（刚在主页算了一次 BMI） */
    override fun onResume() {
        super.onResume()
        vm.refresh()
    }

    // ================= Context Menu（知识点 8） =================

    /** ② 长按弹出时填菜单 */
    override fun onCreateContextMenu(menu: ContextMenu, v: View,
                                     menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.menu_trend_context, menu)
    }

    /** ③ 点了菜单里的哪一项 */
    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.ctx_delete -> {
            val pos = adapter.lastLongClickPosition          // adapter 记录的长按行
            val list = vm.records.value.orEmpty()
            if (pos in list.indices) vm.delete(list[pos].date)   // 先判越界再删（防闪退）
            true
        }
        R.id.ctx_clear -> {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.ctx_clear))
                .setMessage(getString(R.string.confirm_clear))
                .setPositiveButton(getString(R.string.ok)) { _, _ -> vm.clearAll() }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
            true
        }
        else -> super.onContextItemSelected(item)
    }
}

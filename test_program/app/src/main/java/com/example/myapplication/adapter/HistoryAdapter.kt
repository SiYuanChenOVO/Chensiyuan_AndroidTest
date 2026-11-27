package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.BmiRecord
import java.util.Locale

/**
 * ============================================================
 *  模块九：历史记录列表 Adapter（知识点 9：RecyclerView 三件套）
 *
 *  【可移植性】换场景时改三处：item 布局、VH 里的控件、
 *  onBindViewHolder 的绑定逻辑。三个 override 方法的签名不要动。
 *
 *  长按一行：先在本 adapter 记录行号（lastLongClickPosition），
 *  再返回 false 让系统弹出 Context Menu（由 TrendActivity 处理）。
 * ============================================================
 */
class HistoryAdapter(private var items: List<BmiRecord>) :
    RecyclerView.Adapter<HistoryAdapter.VH>() {

    /** 最近一次长按的行下标（Context Menu 弹出前记录），Activity 据此知道删哪条 */
    var lastLongClickPosition = -1

    /** ② ViewHolder：缓存一行的控件引用，避免重复 findViewById */
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvInfo: TextView = view.findViewById(R.id.tvInfo)
        val tvBmi: TextView = view.findViewById(R.id.tvBmi)
    }

    /** ① 创建行：把 item_record.xml inflate 出来并包成 VH */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)   // 第三参 false 固定写法
        return VH(v)
    }

    /** ③ 绑定：把 position 位置的数据填进这一行 */
    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = items[position]
        holder.tvDate.text = r.date
        holder.tvBmi.text = String.format(Locale.US, "%.1f", r.bmi)
        holder.tvInfo.text = "%.0fcm / %.0fkg / %d%s".format(
            r.heightCm, r.weightKg, r.age, if (r.isMale) "M" else "F"
        )
        // 长按整行（绑 itemView 才灵敏）：记录行号后返回 false → 系统弹 Context Menu
        holder.itemView.setOnLongClickListener {
            lastLongClickPosition = holder.bindingAdapterPosition
            false
        }
    }

    override fun getItemCount() = items.size

    /** 数据整体替换后通知刷新（数据源是 VM 发来的新 List） */
    fun submit(newList: List<BmiRecord>) {
        items = newList
        notifyDataSetChanged()
    }
}

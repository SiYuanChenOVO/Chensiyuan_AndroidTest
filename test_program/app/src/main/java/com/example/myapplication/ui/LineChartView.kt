package com.example.myapplication.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.example.myapplication.R

/**
 * ============================================================
 *  模块十：BMI 趋势折线图（自绘 View，Assignment07 要求的 line chart）
 *
 *  【可移植性】本控件只认「标签 + 数值」的点列表，与 BMI 无关：
 *    setData(listOf(ChartPoint("10-01", 22.5f), ...)) 即可画任何折线。
 *
 *  原理：继承 View，重写 onDraw() 用 Paint/Path/Canvas 手绘
 *  坐标轴、折线、数据点和首末日期标签（onDraw 在 UI 线程执行，
 *  只做绘制、不做耗时计算）。
 * ============================================================
 */
class LineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    /** 一个数据点：x 轴标签（日期）+ y 轴数值（BMI） */
    data class ChartPoint(val label: String, val value: Float)

    private var points: List<ChartPoint> = emptyList()

    // ---- 四支画笔：折线 / 圆点 / 坐标轴 / 文字 ----
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.parseColor("#1976D2")
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#1976D2")
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#BDBDBD")
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#616161")
        textSize = 30f
    }

    /** 外部唯一入口：传入新数据并重绘（points.size < 2 时显示提示文字） */
    fun setData(list: List<ChartPoint>) {
        points = list
        invalidate()            // 触发一次 onDraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.size < 2) {
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                context.getString(R.string.chart_empty),
                width / 2f, height / 2f, textPaint
            )
            return
        }

        // ---- ① 绘图区域留白：左给 y 轴数值，下给日期标签 ----
        val padL = 110f
        val padT = 60f
        val padR = 40f
        val padB = 90f
        val plotW = width - padL - padR
        val plotH = height - padT - padB

        // ---- ② y 轴范围：上下各留 10% 余量，避免点顶到边 ----
        val minV = points.minOf { it.value }
        val maxV = points.maxOf { it.value }
        val span = if (maxV - minV < 0.001f) 1f else (maxV - minV) * 1.2f
        val mid = (maxV + minV) / 2f
        val lo = mid - span / 2f
        val hi = mid + span / 2f

        // ---- ③ 数据 → 屏幕坐标的两个换算函数 ----
        fun px(i: Int) = padL + i * plotW / (points.size - 1)
        fun py(v: Float) = padT + (1f - (v - lo) / span) * plotH

        // ---- ④ 坐标轴 ----
        canvas.drawLine(padL, padT, padL, padT + plotH, axisPaint)          // y 轴
        canvas.drawLine(padL, padT + plotH, padL + plotW, padT + plotH, axisPaint) // x 轴

        // ---- ⑤ 折线 ----
        val path = Path()
        points.forEachIndexed { i, p ->
            if (i == 0) path.moveTo(px(i), py(p.value)) else path.lineTo(px(i), py(p.value))
        }
        canvas.drawPath(path, linePaint)

        // ---- ⑥ 数据点圆点 ----
        points.forEachIndexed { i, p ->
            canvas.drawCircle(px(i), py(p.value), 8f, dotPaint)
        }

        // ---- ⑦ 文字：y 轴上下界 + 首末日期 + 最新值 ----
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("%.1f".format(hi), padL - 12f, padT + 30f, textPaint)
        canvas.drawText("%.1f".format(lo), padL - 12f, padT + plotH, textPaint)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(points.first().label, px(0), padT + plotH + 44f, textPaint)
        canvas.drawText(points.last().label, px(points.size - 1), padT + plotH + 44f, textPaint)
        canvas.drawText(
            "%.1f".format(points.last().value),
            px(points.size - 1), py(points.last().value) - 24f, textPaint
        )
    }
}

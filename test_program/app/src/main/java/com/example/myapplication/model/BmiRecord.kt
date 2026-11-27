package com.example.myapplication.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ============================================================
 *  模块二：BMI 历史记录数据类（model 层）
 *
 *  date（yyyy-MM-dd）作为数据库主键 —— 一天最多一条，
 *  重复计算当天直接覆盖旧值（Assignment07 要求「同一天保留最新」）。
 * ============================================================
 */
data class BmiRecord(
    val date: String,       // yyyy-MM-dd，主键
    val bmi: Double,
    val heightCm: Double,
    val weightKg: Double,
    val age: Int,
    val isMale: Boolean
)

private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000

/** 统一的日期格式器（线程内各自 new，SimpleDateFormat 非线程安全） */
private fun dayFormat(): SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

/** 今天的日期字符串，插入数据库时用 */
fun todayString(): String = dayFormat().format(Date())

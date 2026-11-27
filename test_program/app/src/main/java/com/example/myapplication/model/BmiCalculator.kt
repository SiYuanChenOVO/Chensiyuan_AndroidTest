package com.example.myapplication.model

import com.example.myapplication.R

/**
 * ============================================================
 *  模块一：BMI 计算与分级（model 层，纯 Kotlin 逻辑，不碰 UI）
 *
 *  【可移植性】换类似应用场合时只需改两处：
 *    ① calculate() 里的计算公式
 *    ② 分级表：成人切点 classifyAdult() / 儿童百分位表 boysData/girlsData
 *
 *  出处：Assignment02 —— 成人用亚洲 BMI 切点，6~18 岁用按性别分表
 *  的 P3/P5/P90/P97 百分位（对应课件 BMI_chart_adult / BMI_chart_child）。
 * ============================================================
 */

/**
 * 分级结果枚举：
 *  labelRes  = 该分级对应的字符串资源 id（自动适配中英文）
 *  isWarning = 是否触发「严重偏低/偏高」警示设计（红字 + 弹窗，Assignment02 要求）
 */
enum class BmiLevel(val labelRes: Int, val isWarning: Boolean) {
    SEVERELY_UNDERWEIGHT(R.string.level_severe_under, true),
    UNDERWEIGHT(R.string.level_under, false),
    NORMAL(R.string.level_normal, false),
    OVERWEIGHT(R.string.level_over, false),
    OBESE(R.string.level_obese, true),           // 成人肥胖按 A02 触发警示
    SEVERELY_OVERWEIGHT(R.string.level_severe_over, true),
    NO_DATA(R.string.level_no_data, false),      // 儿童表查不到该年龄
    OUT_OF_RANGE(R.string.level_out_range, false) // 年龄 < 6 岁超出图表范围
}

/** 计算结果：BMI 数值 + 分级 */
data class BmiResult(val bmi: Double, val level: BmiLevel)

/** 儿童百分位表的一行：同年龄同性别下的 4 个 BMI 切点 */
data class Percentiles(val p3: Double, val p5: Double, val p90: Double, val p97: Double)

object BmiCalculator {

    // ---- ② 儿童百分位表（男生），key = 年龄 6~18（Assignment02 原表） ----
    private val boysData = mapOf(
        6 to Percentiles(12.8, 13.1, 18.8, 21.4),
        7 to Percentiles(13.0, 13.3, 19.8, 23.0),
        8 to Percentiles(13.2, 13.6, 20.9, 24.6),
        9 to Percentiles(13.5, 13.8, 21.8, 26.0),
        10 to Percentiles(13.8, 14.1, 22.7, 27.3),
        11 to Percentiles(14.1, 14.5, 23.6, 28.3),
        12 to Percentiles(14.4, 14.8, 24.3, 29.2),
        13 to Percentiles(14.7, 15.1, 25.0, 30.0),
        14 to Percentiles(15.0, 15.4, 25.5, 30.6),
        15 to Percentiles(15.3, 15.8, 26.1, 31.2),
        16 to Percentiles(15.6, 16.1, 26.5, 31.7),
        17 to Percentiles(15.9, 16.3, 27.0, 32.1),
        18 to Percentiles(16.1, 16.6, 27.4, 32.4)
    )

    // ---- ② 儿童百分位表（女生） ----
    private val girlsData = mapOf(
        6 to Percentiles(12.6, 12.8, 18.3, 20.5),
        7 to Percentiles(12.8, 13.1, 19.1, 21.8),
        8 to Percentiles(13.1, 13.4, 20.1, 23.1),
        9 to Percentiles(13.4, 13.7, 21.0, 24.4),
        10 to Percentiles(13.7, 14.1, 21.9, 25.6),
        11 to Percentiles(14.1, 14.4, 22.7, 26.6),
        12 to Percentiles(14.4, 14.8, 23.4, 27.5),
        13 to Percentiles(14.8, 15.2, 24.0, 28.3),
        14 to Percentiles(15.1, 15.5, 25.6, 28.9),
        15 to Percentiles(15.4, 15.8, 25.0, 29.4),
        16 to Percentiles(15.7, 16.1, 25.4, 29.7),
        17 to Percentiles(15.9, 16.3, 25.7, 30.0),
        18 to Percentiles(16.1, 16.5, 25.9, 30.3)
    )

    /** 成人分级（亚洲切点）：<16 重度偏瘦 → <18.5 偏瘦 → <23 正常 → <25 超重 → ≥25 肥胖 */
    private fun classifyAdult(bmi: Double): BmiLevel = when {
        bmi < 16.0 -> BmiLevel.SEVERELY_UNDERWEIGHT
        bmi < 18.5 -> BmiLevel.UNDERWEIGHT
        bmi < 23.0 -> BmiLevel.NORMAL
        bmi < 25.0 -> BmiLevel.OVERWEIGHT
        else -> BmiLevel.OBESE
    }

    /** 儿童分级：查百分位表后按 P3/P5/P90/P97 五段分级 */
    private fun classifyChild(bmi: Double, age: Int, isMale: Boolean): BmiLevel {
        val chart = (if (isMale) boysData else girlsData)[age] ?: return BmiLevel.NO_DATA
        return when {
            bmi <= chart.p3 -> BmiLevel.SEVERELY_UNDERWEIGHT
            bmi <= chart.p5 -> BmiLevel.UNDERWEIGHT
            bmi <= chart.p90 -> BmiLevel.NORMAL
            bmi <= chart.p97 -> BmiLevel.OVERWEIGHT
            else -> BmiLevel.SEVERELY_OVERWEIGHT
        }
    }

    /** ① 主入口：身高(cm) + 体重(kg) + 年龄 + 性别 → BMI 值与分级 */
    fun calculate(heightCm: Double, weightKg: Double, age: Int, isMale: Boolean): BmiResult {
        val meter = heightCm / 100.0
        val bmi = weightKg / (meter * meter)               // BMI = 体重(kg) / 身高²(m²)
        val level = if (age <= 18) {
            if (age < 6) BmiLevel.OUT_OF_RANGE else classifyChild(bmi, age, isMale)
        } else {
            classifyAdult(bmi)
        }
        return BmiResult(bmi, level)
    }
}

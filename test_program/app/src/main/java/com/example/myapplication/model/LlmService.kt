package com.example.myapplication.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * ============================================================
 *  模块五：LLM 健康建议服务（model 层，Assignment04）
 *
 *  【可移植性】换 LLM 服务商只改 LlmDefaults 的三个常量
 *  （下列服务商都提供 OpenAI 兼容的 /chat/completions 接口）：
 *    DeepSeek : https://api.deepseek.com          model: deepseek-chat
 *    Kimi     : https://api.moonshot.cn           model: moonshot-v1-8k
 *    通义千问 : https://dashscope.aliyuncs.com/compatible-mode/v1   model: qwen-plus
 *
 *  【线程要求】fetchSuggestions() 是同步阻塞的网络请求，
 *  必须在后台线程调用（由 ReportViewModel 负责），见知识点 13。
 *  请求/响应 JSON 用内置的 org.json 解析，无需第三方依赖。
 *
 *  出处：Assignment04 —— 把 BMI + 年龄 + 性别发给 LLM，
 *  按当前 App 语言（中文模式 → 中文回复）返回健康建议。
 * ============================================================
 */

/** LLM 服务商连接参数：换服务商（Kimi/千问等）只改这三个常量 */
object LlmDefaults {
    const val BASE_URL = "https://api.deepseek.com"
    const val CHAT_PATH = "/chat/completions"
    const val DEFAULT_MODEL = "deepseek-chat"
}

/** 当前 App 的界面语言（跟随 AppCompatDelegate 设置的 per-app locale） */
enum class AppLanguage(val tag: String) {
    ZH("zh"), EN("en");

    companion object {
        fun from(context: Context): AppLanguage {
            val locale = context.resources.configuration.locales.get(0)
            return if (locale?.language == "zh") ZH else EN
        }
    }
}

/** LLM 连接参数（API Key 只存本机 SharedPreferences，不进代码仓库） */
data class LlmSettings(val apiKey: String, val model: String)

/** LLM 参数的本机存取（知识点 12：SharedPreferences） */
class LlmSettingsStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): LlmSettings = LlmSettings(
        apiKey = prefs.getString(KEY_API_KEY, "").orEmpty().trim(),
        model = prefs.getString(KEY_MODEL, null)?.takeIf { it.isNotBlank() }
            ?: LlmDefaults.DEFAULT_MODEL
    )

    fun save(settings: LlmSettings) {
        prefs.edit()
            .putString(KEY_API_KEY, settings.apiKey.trim())
            .putString(KEY_MODEL, settings.model.trim())
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "llm_settings"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL = "model"
    }
}

/** 发给 LLM 的最小健康信息（不含姓名、身高体重等隐私） */
data class HealthProfile(
    val bmi: Double,
    val age: Int,
    val isMale: Boolean,
    val categoryLabel: String   // App 当前语言下的分级文字
)

/** LLM 请求结果：成功拿到建议文本 / 失败（带错误类型，View 层再翻译成提示语） */
sealed interface LlmOutcome {
    data class Success(val text: String) : LlmOutcome
    data class Failure(val kind: LlmErrorKind) : LlmOutcome
}

enum class LlmErrorKind { NO_CONFIG, AUTH, RATE_LIMIT, SERVER, NETWORK, EMPTY }

// ---------------- Prompt 构造（纯函数，方便测试） ----------------
object HealthPromptBuilder {

    /** System Prompt：告知 LLM 我们 App 的分级标准，并约束回复语言和格式 */
    fun systemPrompt(language: AppLanguage): String = when (language) {
        AppLanguage.ZH -> """
            你是一款 BMI 计算应用中的健康科普助手。
            应用对成人采用亚洲切点：低于 18.5 偏瘦，18.5~23 正常，23~25 超重，25 及以上肥胖；
            对 6~18 岁用户采用按年龄性别的 BMI 百分位（P3/P5/P90/P97）。
            请根据用户提供的 BMI、年龄、性别和分类，给出 3~5 条具体、可执行、温和的建议，
            覆盖饮食、运动、睡眠或日常习惯。
            要求：只使用简体中文回答；只输出建议本身，用 • 开头逐条列出；
            不要医学诊断、药物、补剂、极端节食；若分类为重度偏瘦/重度超重或用户未成年，建议咨询医生。
        """.trimIndent()

        AppLanguage.EN -> """
            You are a health-education assistant inside a BMI calculator app.
            The app classifies adults with Asian cutoffs: below 18.5 underweight, 18.5-23 normal,
            23-25 overweight, 25+ obese. For ages 6-18 it uses age/sex BMI percentiles (P3/P5/P90/P97).
            Based on the user's BMI, age, sex and category, give 3-5 specific, actionable, supportive
            suggestions covering diet, activity, sleep or daily habits.
            Requirements: reply in English only; output only the suggestions as bullet lines starting
            with a bullet (•); no diagnosis, medication, supplements or extreme dieting; if the category
            is severely under/overweight or the user is a minor, advise consulting a doctor.
        """.trimIndent()
    }

    /** User Prompt：用户数据本身 */
    fun userPrompt(profile: HealthProfile, language: AppLanguage): String = when (language) {
        AppLanguage.ZH -> "BMI：%.2f\n年龄：%d\n性别：%s\n分类：%s".format(
            profile.bmi, profile.age, if (profile.isMale) "男" else "女", profile.categoryLabel
        )
        AppLanguage.EN -> "BMI: %.2f\nAge: %d\nSex: %s\nCategory: %s".format(
            profile.bmi, profile.age, if (profile.isMale) "Male" else "Female", profile.categoryLabel
        )
    }
}

// ---------------- HTTP 客户端（HttpURLConnection，知识点 13 的写法） ----------------
object LlmService {

    fun fetchSuggestions(settings: LlmSettings, profile: HealthProfile, language: AppLanguage): LlmOutcome {
        // ① 未配置 Key 直接返回，不发请求
        if (settings.apiKey.isBlank()) return LlmOutcome.Failure(LlmErrorKind.NO_CONFIG)

        // ② 组装 OpenAI 兼容的请求体（org.json 手工拼 JSON）
        val body = JSONObject().apply {
            put("model", settings.model)
            put("stream", false)
            put("temperature", 0.7)
            put("max_tokens", 800)
            put(
                "messages", JSONArray()
                    .put(JSONObject().put("role", "system")
                        .put("content", HealthPromptBuilder.systemPrompt(language)))
                    .put(JSONObject().put("role", "user")
                        .put("content", HealthPromptBuilder.userPrompt(profile, language)))
            )
        }

        var conn: HttpURLConnection? = null
        return try {
            // ③ 发 POST 请求（必须在后台线程调用本函数）
            conn = (URL(LlmDefaults.BASE_URL + LlmDefaults.CHAT_PATH)
                .openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 60_000
                doOutput = true
                setRequestProperty("Authorization", "Bearer ${settings.apiKey}")
                setRequestProperty("Content-Type", "application/json")
            }
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            // ④ 读响应（出错时响应体在 errorStream）
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.readText().orEmpty()

            if (code !in 200..299) {
                LlmOutcome.Failure(mapHttpError(code))
            } else {
                // ⑤ 解析：choices[0].message.content
                val content = JSONObject(text)
                    .getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").optString("content").trim()
                if (content.isEmpty()) LlmOutcome.Failure(LlmErrorKind.EMPTY)
                else LlmOutcome.Success(content)
            }
        } catch (e: IOException) {
            LlmOutcome.Failure(LlmErrorKind.NETWORK)
        } catch (e: JSONException) {
            LlmOutcome.Failure(LlmErrorKind.EMPTY)
        } finally {
            conn?.disconnect()
        }
    }

    private fun mapHttpError(code: Int): LlmErrorKind = when (code) {
        401, 402, 403 -> LlmErrorKind.AUTH       // Key 错误 / 欠费
        429 -> LlmErrorKind.RATE_LIMIT
        else -> LlmErrorKind.SERVER
    }
}

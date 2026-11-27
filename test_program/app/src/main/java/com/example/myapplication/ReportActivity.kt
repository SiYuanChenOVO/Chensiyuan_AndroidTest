package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.model.AppLanguage
import com.example.myapplication.model.BmiLevel
import com.example.myapplication.model.HealthProfile
import com.example.myapplication.model.LlmDefaults
import com.example.myapplication.model.LlmErrorKind
import com.example.myapplication.model.LlmSettings
import com.example.myapplication.viewmodel.ReportState
import com.example.myapplication.viewmodel.ReportViewModel

/**
 * ============================================================
 *  模块十二：健康报告页（View 层，Assignment04）
 *
 *  从 MainActivity 收到 BMI/年龄/性别（Intent 传值，知识点 5），
 *  调 LLM 生成健康建议。按状态机渲染四种界面：
 *    Idle（提示语）/ Loading（转圈）/ Success（建议文本）/ Failure（错误原因）。
 *  「生成建议」失败后再点一次 = 重试。
 * ============================================================
 */
class ReportActivity : AppCompatActivity() {

    private val vm: ReportViewModel by viewModels()
    private lateinit var profile: HealthProfile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        val tvProfile = findViewById<TextView>(R.id.tvProfile)
        val tvSuggestion = findViewById<TextView>(R.id.tvSuggestion)
        val pb = findViewById<ProgressBar>(R.id.pb)

        // ---- ① 从 Intent 取数据（key 与 MainActivity 的常量一致！） ----
        val bmi = intent.getDoubleExtra(MainActivity.EXTRA_BMI, 0.0)
        val age = intent.getIntExtra(MainActivity.EXTRA_AGE, 0)
        val isMale = intent.getBooleanExtra(MainActivity.EXTRA_IS_MALE, true)
        val level = try {
            BmiLevel.valueOf(intent.getStringExtra(MainActivity.EXTRA_LEVEL) ?: "")
        } catch (_: Exception) {
            BmiLevel.NORMAL
        }

        // ---- ② 组装发给 LLM 的档案（分级文字用当前语言） ----
        profile = HealthProfile(bmi, age, isMale, getString(level.labelRes))
        val gender = getString(if (isMale) R.string.male else R.string.female)
        tvProfile.text = listOf(
            getString(R.string.result_bmi, bmi),
            getString(R.string.result_meta, age, gender),
            getString(level.labelRes)
        ).joinToString("  ·  ")

        // ---- ③ 生成建议：VM 里发后台线程请求 ----
        findViewById<Button>(R.id.btnAsk).setOnClickListener {
            vm.request(profile, AppLanguage.from(this))
        }

        // ---- ④ API 设置对话框（Key 存 SharedPreferences，见 LlmSettingsStore） ----
        findViewById<Button>(R.id.btnSettings).setOnClickListener { showSettingsDialog() }

        // ---- ⑤ 观察 LLM 状态机，渲染四种界面 ----
        vm.state.observe(this) { state ->
            when (state) {
                is ReportState.Idle -> {
                    pb.visibility = View.GONE
                    tvSuggestion.text = getString(R.string.report_idle)
                }
                is ReportState.Loading -> {
                    pb.visibility = View.VISIBLE
                    tvSuggestion.text = getString(R.string.loading)
                }
                is ReportState.Success -> {
                    pb.visibility = View.GONE
                    tvSuggestion.text = state.text
                }
                is ReportState.Failure -> {
                    pb.visibility = View.GONE
                    tvSuggestion.text = errorMsg(state.kind)
                }
            }
        }
    }

    /** 错误类型 → 本地化提示文字 */
    private fun errorMsg(kind: LlmErrorKind): String = getString(
        when (kind) {
            LlmErrorKind.NO_CONFIG -> R.string.llm_no_config
            LlmErrorKind.AUTH -> R.string.llm_auth
            LlmErrorKind.RATE_LIMIT -> R.string.llm_rate
            LlmErrorKind.SERVER -> R.string.llm_server
            LlmErrorKind.NETWORK -> R.string.llm_network
            LlmErrorKind.EMPTY -> R.string.llm_empty
        }
    )

    /** 代码动态搭对话框（两个输入框：API Key + 模型名） */
    private fun showSettingsDialog() {
        val saved = vm.settingsStore.load()
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 32, 64, 0)
        }
        val etKey = EditText(this).apply {
            hint = getString(R.string.settings_key_hint)
            setText(saved.apiKey)
        }
        val etModel = EditText(this).apply {
            hint = getString(R.string.settings_model_hint)
            setText(saved.model)
        }
        container.addView(etKey)
        container.addView(etModel)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.btn_settings))
            .setView(container)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                vm.settingsStore.save(
                    LlmSettings(
                        apiKey = etKey.text.toString(),
                        model = etModel.text.toString().ifBlank { LlmDefaults.DEFAULT_MODEL }
                    )
                )
                Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}

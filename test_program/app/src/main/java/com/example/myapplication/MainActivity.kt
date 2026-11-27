package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.myapplication.model.AppLanguage
import com.example.myapplication.viewmodel.BmiViewModel

/**
 * ============================================================
 *  模块十一：主界面（MVVM 之 View 层）
 *
 *  职责（只做这三件事，L04「关注点分离」）：
 *    ① 显示数据：observe ViewModel 的 LiveData，数据变了自动刷 UI
 *    ② 捕获操作：按钮点击 / 菜单选择，转发给 ViewModel
 *    ③ 严重偏低/偏高时弹警示对话框（Assignment02 的 warning-specific design）
 *
 *  覆盖考点：Options Menu（右上角溢出菜单）、语言切换（Assignment03）、
 *  表单自动回填（Assignment07）、屏幕旋转不丢结果（ViewModel）。
 * ============================================================
 */
class MainActivity : AppCompatActivity() {

    private val vm: BmiViewModel by viewModels()   // 委托获取 VM；旋转后拿回同一实例

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)      // 必须在所有 findViewById 之前！

        // ---- 把 Toolbar 设为 ActionBar：Options Menu 才有地方挂 ----
        // （主题是 NoActionBar，缺了这行右上角就没有溢出菜单 ⋮ —— Debug 手册第八节）
        setSupportActionBar(findViewById(R.id.toolbar))

        // ---- 绑定控件（纵向/横向两套布局里 id 一致，代码不用改） ----
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etAge = findViewById<EditText>(R.id.etAge)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val rbMale = findViewById<android.widget.RadioButton>(R.id.rbMale)
        val rbFemale = findViewById<android.widget.RadioButton>(R.id.rbFemale)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val tvError = findViewById<TextView>(R.id.tvError)

        // ---- ① 启动时自动回填上次的输入（Assignment07：SharedPreferences） ----
        val saved = vm.loadSavedInputs()
        etHeight.setText(saved.height)
        etWeight.setText(saved.weight)
        etAge.setText(saved.age)
        rgGender.check(if (saved.isMale) rbMale.id else rbFemale.id)

        // ---- ② 观察计算结果：一有新结果就渲染（含警示设计） ----
        vm.result.observe(this) { s ->
            tvError.text = ""
            val gender = getString(if (s.isMale) R.string.male else R.string.female)
            tvResult.text = buildString {
                append(getString(R.string.result_bmi, s.bmi))
                append("\n")
                append(getString(s.level.labelRes))              // 分级文字（自动中英文）
                append("\n")
                append(getString(R.string.result_meta, s.age, gender))
            }
            if (s.level.isWarning) {
                // 严重偏低/偏高：红字 + 弹窗警示（Assignment02 warning-specific design）
                tvResult.setTextColor(Color.parseColor("#C62800"))
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.warning_dialog_title))
                    .setMessage(getString(R.string.warning_dialog_msg))
                    .setPositiveButton(getString(R.string.ok), null)
                    .show()
            } else {
                tvResult.setTextColor(Color.parseColor("#1B5E20"))
            }
        }

        // ---- ③ 观察输入错误事件 ----
        vm.inputError.observe(this) {
            tvError.text = getString(R.string.error_invalid)
        }

        // ---- ④ 计算按钮：View 只发命令，逻辑全在 VM ----
        btnCalculate.setOnClickListener {
            vm.calculate(
                heightText = etHeight.text.toString(),
                weightText = etWeight.text.toString(),
                ageText = etAge.text.toString(),
                isMale = rbMale.isChecked
            )
        }
    }

    // ================= Options Menu（知识点 8） =================

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)   // 参数：菜单资源, 目标 Menu
        return true                                    // 必须返回 true 才显示
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_report -> { goReport(); true }
        R.id.action_trend -> { startActivity(Intent(this, TrendActivity::class.java)); true }
        R.id.action_language -> { switchLanguage(); true }
        R.id.action_clear -> { confirmClear(); true }
        else -> super.onOptionsItemSelected(item)
    }

    /** 跳报告页前必须有计算结果，把当前数据打包进 Intent（知识点 5） */
    private fun goReport() {
        val s = vm.result.value
        if (s == null) {
            Toast.makeText(this, R.string.toast_need_calc, Toast.LENGTH_SHORT).show()
            return
        }
        Intent(this, ReportActivity::class.java)
            .putExtra(EXTRA_BMI, s.bmi)
            .putExtra(EXTRA_AGE, s.age)
            .putExtra(EXTRA_IS_MALE, s.isMale)
            .putExtra(EXTRA_LEVEL, s.level.name)       // 传分级枚举名，报告页再转成本地化文字
            .let { startActivity(it) }
    }

    /** 中英切换（Assignment03）：设置 per-app locale，系统自动重建 Activity */
    private fun switchLanguage() {
        val target = if (AppLanguage.from(this) == AppLanguage.ZH) "en" else "zh"
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(target))
    }

    /** 清空历史前先确认（AlertDialog 知识点 7） */
    private fun confirmClear() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.menu_clear))
            .setMessage(getString(R.string.confirm_clear))
            .setPositiveButton(getString(R.string.ok)) { _, _ -> vm.clearHistory() }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    companion object {
        const val EXTRA_BMI = "extra_bmi"
        const val EXTRA_AGE = "extra_age"
        const val EXTRA_IS_MALE = "extra_is_male"
        const val EXTRA_LEVEL = "extra_level"
    }
}

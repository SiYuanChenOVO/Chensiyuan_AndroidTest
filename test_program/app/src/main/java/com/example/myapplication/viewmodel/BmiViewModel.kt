package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.model.BmiCalculator
import com.example.myapplication.model.BmiRecord
import com.example.myapplication.model.BmiRepository
import com.example.myapplication.model.todayString
import java.util.concurrent.Executors

/**
 * ============================================================
 *  模块六：主界面 ViewModel（MVVM 之 VM，知识点 12）
 *
 *  职责：校验输入 → 计算 BMI → 保存偏好与历史 → 通过 LiveData 通知 View。
 *  View（MainActivity）只 observe + 发命令，不持有业务数据。
 *
 *  为什么用 AndroidViewModel：需要在 VM 里拿 Repository（需要 Context），
 *  系统默认工厂能直接创建它，无需手写 Factory。
 *
 *  为什么用 Executors + postValue：DB 写入是耗时操作不能放主线程
 *  （知识点 11），后台线程用 postValue 把结果送回主线程（知识点 13）。
 * ============================================================
 */
class BmiViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = BmiRepository.getInstance(application)

    /** 单线程后台执行器：DB 读写都在这里跑 */
    private val executor = Executors.newSingleThreadExecutor()

    /** 计算结果：UI 状态（旋转屏幕后新 Activity 重新 observe 拿到同一份数据） */
    val result = MutableLiveData<BmiUiState>()

    /** 输入非法事件：View 收到后显示错误提示 */
    val inputError = MutableLiveData<Unit>()

    /** 一条计算结果对应的全部 UI 数据 */
    data class BmiUiState(
        val bmi: Double,
        val level: com.example.myapplication.model.BmiLevel,
        val heightCm: Double,
        val weightKg: Double,
        val age: Int,
        val isMale: Boolean
    )

    /** 供 View 在 onCreate 里回填表单（Assignment07：记住上次输入） */
    fun loadSavedInputs(): BmiRepository.SavedInput = repo.loadInputs()

    /**
     * 计算入口（View 的按钮点击调用）。
     * 流程：校验 → 存偏好 → 计算 → 发结果 → 后台写历史记录。
     */
    fun calculate(heightText: String, weightText: String, ageText: String, isMale: Boolean) {
        // ① 校验：任何一项不是有效正数就发错误事件（toIntOrNull/toDoubleOrNull 不会崩溃）
        val h = heightText.toDoubleOrNull()
        val w = weightText.toDoubleOrNull()
        val age = ageText.toIntOrNull()
        if (h == null || w == null || age == null || h <= 0.0 || w <= 0.0 || age <= 0) {
            inputError.value = Unit
            return
        }

        // ② 记住输入（apply 异步写盘，主线程调用无压力）
        repo.saveInputs(heightText, weightText, ageText, isMale)

        // ③ 计算（纯逻辑，微秒级，主线程可直接调）
        val r = BmiCalculator.calculate(h, w, age, isMale)
        result.value = BmiUiState(r.bmi, r.level, h, w, age, isMale)

        // ④ 历史记录写 SQLite：放后台线程，同一天重复计算会被 REPLACE 覆盖
        executor.execute {
            repo.insertRecord(BmiRecord(todayString(), r.bmi, h, w, age, isMale))
        }
    }

    /** 菜单「清空历史」 */
    fun clearHistory() {
        executor.execute { repo.clearRecords() }
    }

    /** VM 销毁（Activity 真正退出）时释放线程池，防泄漏 */
    override fun onCleared() {
        executor.shutdown()
    }
}

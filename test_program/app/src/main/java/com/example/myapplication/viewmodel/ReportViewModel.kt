package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.model.AppLanguage
import com.example.myapplication.model.HealthProfile
import com.example.myapplication.model.LlmOutcome
import com.example.myapplication.model.LlmService
import com.example.myapplication.model.LlmSettingsStore
import java.util.concurrent.Executors

/**
 * ============================================================
 *  模块八：报告页 ViewModel（MVVM 之 VM，Assignment04）
 *
 *  LLM 请求的状态机：Idle → Loading → Success / Failure。
 *  网络请求在后台线程执行（HttpURLConnection 是阻塞调用），
 *  结果用 postValue 送回主线程，View 只根据 state 渲染四种界面。
 * ============================================================
 */

/** 报告页的四种 UI 状态（View 据此显示：提示 / 转圈 / 建议 / 错误） */
sealed class ReportState() {
    object Idle : ReportState()
    object Loading : ReportState()
    data class Success(val text: String) : ReportState()
    data class Failure(val kind: com.example.myapplication.model.LlmErrorKind) : ReportState()
}

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    /** API Key / 模型名的存取（View 的「API 设置」对话框用它） */
    val settingsStore = LlmSettingsStore(application)

    private val executor = Executors.newSingleThreadExecutor()

    /** 当前状态，ReportActivity observe 它来刷新界面 */
    val state = MutableLiveData<ReportState>(ReportState.Idle)

    /** 发起 LLM 请求（View 按钮点击调用；可在失败后重复调用 = 重试） */
    fun request(profile: HealthProfile, language: AppLanguage) {
        state.value = ReportState.Loading                       // 主线程：setValue
        executor.execute {
            val outcome = LlmService.fetchSuggestions(
                settingsStore.load(), profile, language
            )
            state.postValue(                                    // 后台线程：postValue
                when (outcome) {
                    is LlmOutcome.Success -> ReportState.Success(outcome.text)
                    is LlmOutcome.Failure -> ReportState.Failure(outcome.kind)
                }
            )
        }
    }

    override fun onCleared() {
        executor.shutdown()
    }
}

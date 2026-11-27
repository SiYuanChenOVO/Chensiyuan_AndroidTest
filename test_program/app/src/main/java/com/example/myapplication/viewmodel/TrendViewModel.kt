package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.model.BmiRepository
import java.util.concurrent.Executors

/**
 * ============================================================
 *  模块七：趋势页 ViewModel（MVVM 之 VM）
 *
 *  职责：从 SQLite 查历史 → LiveData 通知列表和折线图刷新；
 *  删除/清空也走这里（View 不直接碰数据库）。
 * ============================================================
 */
class TrendViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = BmiRepository.getInstance(application)
    private val executor = Executors.newSingleThreadExecutor()

    /** 历史记录列表（按日期升序），TrendActivity 同时喂给列表和折线图 */
    val records = MutableLiveData<List<com.example.myapplication.model.BmiRecord>>(emptyList())

    /** 刷新：查库（后台线程）→ postValue 回主线程 */
    fun refresh() {
        executor.execute { records.postValue(repo.queryAllRecords()) }
    }

    /** 长按删除某天的记录 */
    fun delete(date: String) {
        executor.execute {
            repo.deleteRecord(date)
            records.postValue(repo.queryAllRecords())
        }
    }

    /** 清空全部 */
    fun clearAll() {
        executor.execute {
            repo.clearRecords()
            records.postValue(emptyList())
        }
    }

    override fun onCleared() {
        executor.shutdown()
    }
}

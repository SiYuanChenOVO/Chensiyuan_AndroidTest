package com.example.myapplication.model

import android.content.Context

/**
 * ============================================================
 *  模块四：数据仓库 Repository（model 层）
 *
 *  MVVM 的数据统一入口：ViewModel 只找 Repository 要数据，
 *  不直接碰 SharedPreferences / SQLite（关注点分离，L04）。
 *
 *  职责：
 *    ① SharedPreferences —— 记住身高/体重/年龄/性别，下次启动自动回填（Assignment07）
 *    ② SQLite —— BMI 历史记录的增删查（Assignment07）
 *
 *  单例：全 App 共用一个数据库连接（双检锁写法，Storage 课件第 64 页）。
 * ============================================================
 */
class BmiRepository private constructor(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val dbHelper = BmiDbHelper(context)

    // ---------------- ① 输入记忆（SharedPreferences） ----------------

    /** 一次保存的全部输入，用于回填表单 */
    data class SavedInput(
        val height: String, val weight: String, val age: String, val isMale: Boolean
    )

    fun loadInputs(): SavedInput = SavedInput(
        height = prefs.getString(KEY_HEIGHT, "").orEmpty(),
        weight = prefs.getString(KEY_WEIGHT, "").orEmpty(),
        age = prefs.getString(KEY_AGE, "").orEmpty(),
        isMale = prefs.getBoolean(KEY_IS_MALE, true)
    )

    /** 写入用四步：edit() → putXxx → apply()（异步，不阻塞主线程） */
    fun saveInputs(height: String, weight: String, age: String, isMale: Boolean) {
        prefs.edit()
            .putString(KEY_HEIGHT, height)
            .putString(KEY_WEIGHT, weight)
            .putString(KEY_AGE, age)
            .putBoolean(KEY_IS_MALE, isMale)
            .apply()
    }

    // ---------------- ② 历史记录（SQLite） ----------------

    fun insertRecord(record: BmiRecord) = dbHelper.insertOrReplace(record)

    fun queryAllRecords(): List<BmiRecord> = dbHelper.queryAll()

    fun deleteRecord(date: String) = dbHelper.deleteByDate(date)

    fun clearRecords() = dbHelper.clearAll()

    companion object {
        private const val PREFS_NAME = "user_bmi_prefs"
        private const val KEY_HEIGHT = "saved_height"
        private const val KEY_WEIGHT = "saved_weight"
        private const val KEY_AGE = "saved_age"
        private const val KEY_IS_MALE = "saved_is_male"

        // ---- 单例：@Volatile + synchronized 双检锁 ----
        @Volatile
        private var instance: BmiRepository? = null

        fun getInstance(context: Context): BmiRepository =
            instance ?: synchronized(this) {
                instance ?: BmiRepository(context).also { instance = it }
            }
    }
}

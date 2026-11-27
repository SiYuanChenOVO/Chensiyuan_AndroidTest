package com.example.myapplication.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * ============================================================
 *  模块三：SQLite 数据库助手（model 层）
 *
 *  【可移植性】这是一个标准的 SQLiteOpenHelper 模板：
 *    换场景时只改 companion object 里的表名/列名/建表语句，
 *    以及 toContentValues / fromCursor 两个转换函数。
 *
 *  出处：Assignment07 —— 保存每天最新的 BMI 结果，按日期查看变化。
 *  表结构：date 为主键（同一天覆盖写入 → 保留当天最新一条）。
 * ============================================================
 */
class BmiDbHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION
) {
    /** 首次使用时建表（只被系统调用一次） */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_DATE TEXT PRIMARY KEY NOT NULL,
                $COLUMN_BMI REAL NOT NULL,
                $COLUMN_HEIGHT REAL NOT NULL,
                $COLUMN_WEIGHT REAL NOT NULL,
                $COLUMN_AGE INTEGER NOT NULL,
                $COLUMN_IS_MALE INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    /** 数据库版本升级时调用：演示项目直接删表重建 */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    /** 插入（date 主键冲突时 REPLACE 覆盖 → 同一天保留最新一条） */
    fun insertOrReplace(record: BmiRecord) {
        writableDatabase.insertWithOnConflict(
            TABLE_NAME, null, toContentValues(record),
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    /** 全部记录，按日期升序（折线图需要从左到右按时间排列） */
    fun queryAll(): List<BmiRecord> {
        val records = mutableListOf<BmiRecord>()
        readableDatabase.query(
            TABLE_NAME, null, null, null, null, null, "$COLUMN_DATE ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) records.add(fromCursor(cursor))
        }
        return records
    }

    /** 按日期删除一条（Trend 页长按删除） */
    fun deleteByDate(date: String) {
        writableDatabase.delete(TABLE_NAME, "$COLUMN_DATE = ?", arrayOf(date))
    }

    /** 清空全部（菜单「清空历史」） */
    fun clearAll() {
        writableDatabase.delete(TABLE_NAME, null, null)
    }

    // ---- Java 对象 → 数据库行 ----
    private fun toContentValues(r: BmiRecord) = ContentValues().apply {
        put(COLUMN_DATE, r.date)
        put(COLUMN_BMI, r.bmi)
        put(COLUMN_HEIGHT, r.heightCm)
        put(COLUMN_WEIGHT, r.weightKg)
        put(COLUMN_AGE, r.age)
        put(COLUMN_IS_MALE, if (r.isMale) 1 else 0)
    }

    // ---- 数据库行 → Java 对象 ----
    private fun fromCursor(c: Cursor) = BmiRecord(
        date = c.getString(c.getColumnIndexOrThrow(COLUMN_DATE)),
        bmi = c.getDouble(c.getColumnIndexOrThrow(COLUMN_BMI)),
        heightCm = c.getDouble(c.getColumnIndexOrThrow(COLUMN_HEIGHT)),
        weightKg = c.getDouble(c.getColumnIndexOrThrow(COLUMN_WEIGHT)),
        age = c.getInt(c.getColumnIndexOrThrow(COLUMN_AGE)),
        isMale = c.getInt(c.getColumnIndexOrThrow(COLUMN_IS_MALE)) == 1
    )

    companion object {
        // ★ 换场景时改这里：库名 / 表名 / 列名
        private const val DATABASE_NAME = "bmi_records.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "bmi_records"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_BMI = "bmi"
        private const val COLUMN_HEIGHT = "height_cm"
        private const val COLUMN_WEIGHT = "weight_kg"
        private const val COLUMN_AGE = "age"
        private const val COLUMN_IS_MALE = "is_male"
    }
}

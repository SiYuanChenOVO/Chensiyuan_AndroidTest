# EE5415 机考参考（BMI App + Debug 手册）

> ⚠️ **本仓库仅用于考前复习。** 考试中只允许查阅课件 + 自己的 Canvas 作业 + Google 文档查询，请遵守考场规定。

## 仓库内容

| 文件/目录 | 说明 |
|---|---|
| `知识点详解与Debug手册.md` | 14 个知识点的「概述 + API 用法表 + 易错点与解决办法」，含考试中实际踩过的坑 |
| `test_program/` | BMI 计算器完整项目（传统 View + XML，MVVM 架构），覆盖 Assignment 02/03/04/07 全部要求 |

## test_project 功能对照

| 作业 | 功能 | 主要代码 |
|---|---|---|
| A02 | 年龄/性别输入、成人亚洲切点 + 6~18 岁百分位表、严重偏低/偏高警示 | `model/BmiCalculator.kt`、`MainActivity` |
| A03 | 中英双语、Options/Context Menu、横屏布局、MVVM | `res/values-zh/`、`res/menu/`、`res/layout-land/`、`viewmodel/` |
| A04 | LLM API 健康建议（按 App 语言回复） | `model/LlmService.kt`、`ReportActivity` |
| A07 | SharedPreferences 记住输入、SQLite 按天存最新、折线图趋势 | `model/BmiRepository.kt`、`model/BmiDbHelper.kt`、`ui/LineChartView.kt` |

## 环境要求

- Android Studio **2026.2.1+**（项目使用 AGP 9.4.1 / compileSdk 37 / Java 17）
- 打开后直接 Run 即可；LLM 功能需在报告页「API 设置」里填 API Key（DeepSeek/Kimi/千问等 OpenAI 兼容接口均可，改 `model/LlmService.kt` 里的 `LlmDefaults` 即可换服务商）

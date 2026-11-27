# 知识点详解与 Debug 手册

> 配套文件：《复习提纲.md》（速查）、《上机编程练习.md》（练手题）、《机考准备指南.md》（考场策略）。
> 本手册把备考过程中问过的问题逐节整理成「① 概述 → ② 代码操作流程 → ③ 易错点与解决办法」三段式。
> 标【你问过】= 备考中实际踩过的坑，考试前重点看。
>
> **官方手册地址**（平时收藏；考试时经 Google 搜索查阅）：
> - Android 开发者官方文档：https://developer.android.com
> - Android Studio 用户指南（IDE 操作 / 快捷键）：https://developer.android.com/studio/intro
> - Android API 参考（查类、函数签名）：https://developer.android.com/reference
> - Kotlin 语言文档：https://kotlinlang.org/docs/home.html
>
> **目录**：
> 一、Android Studio 工程与文件操作 ✅
> 二、Kotlin 基础语法 ✅
> 三、Activity 生命周期 ✅
> 四、资源系统与 R 类 ✅
> 五、Intent 与多 Activity ✅
> 六、UI 布局与控件 ✅
> 七、事件处理 ✅
> 八、菜单与 Toolbar ✅
> 九、RecyclerView ✅
> 十、Fragment ✅
> 十一、多线程 ✅
> 十二、数据存储 ✅
> 十三、ViewModel + LiveData ✅
> 十四、网络 ✅

---

## 一、Android Studio 工程与文件操作

### 1.1 概述

新建项目（Empty Views Activity 模板）自动生成 4 个核心文件，它们的关系是：

```
点桌面图标启动 App
   → AndroidManifest.xml 找到"入口"是 MainActivity
   → MainActivity.onCreate() 里 setContentView 挂载 activity_main.xml
   → 布局里的文字/名字引用 strings.xml
```

| 文件 | 路径 | 作用 |
|---|---|---|
| MainActivity.kt | `app/src/main/java/包名/` | 入口 Activity 的代码 |
| activity_main.xml | `app/src/main/res/layout/` | 界面布局 |
| AndroidManifest.xml | `app/src/main/` | App 清单：注册 Activity、声明权限、图标主题 |
| strings.xml | `app/src/main/res/values/` | 字符串资源 |

---

### 1.2 代码操作流程：初始模板逐行详解

#### ① MainActivity.kt 模板

```kotlin
package com.example.myapplication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
```

| 模板代码 | 什么意思 | 怎么写 / 参数怎么填 | 要改怎么改 |
|---|---|---|---|
| `package com.example.myapplication` | 声明本文件属于哪个包（= 文件所在文件夹的路径） | 建项目时按你填的 package name 自动生成，不用手写 | 移动 kt 文件到其他文件夹时，包名必须跟着改，否则别的类找不到它 |
| `import android.os.Bundle` | 导入类，之后才能直接写短名 `Bundle`（不写就得用全名 `android.os.Bundle`） | IDE 自动加；红色 Unresolved reference 优先查 import | 缺 import → 光标放红字上按 Alt+Enter 自动补；**导错同名类**会出怪错（如 `java.util.logging.Handler`，见第十一节）【你问过】 |
| `class MainActivity : AppCompatActivity()` | 定义类 MainActivity，继承 AppCompatActivity（AndroidX 增强版 Activity） | 格式 `class 名字 : 父类()`，括号 = 调用父类构造器 | 改类名必须同步改 Manifest 里的 `android:name`；必须继承 AppCompatActivity 才能用 `setSupportActionBar`、`supportFragmentManager` 这些功能 |
| `override fun onCreate(savedInstanceState: Bundle?)` | 覆写父类的 onCreate——Activity 创建时系统**自动调用的第一个回调**，所有初始化代码写在这里 | `override` = 覆写标记，函数签名必须和父类完全一致；参数 `savedInstanceState: Bundle?` = Activity 被重建（旋转屏幕等）时系统塞回的旧状态，首次启动为 null，`?` 表示可为空 | 签名不要动；自己的代码写在函数体里（findViewById、监听器等） |
| `super.onCreate(savedInstanceState)` | 先执行父类 onCreate 的必要内部初始化 | 必须写，且放函数第一行 | 不能删——删了运行时崩溃 `super not called` |
| `enableEdgeToEdge()` | 让内容延伸到状态栏/导航栏底下（沉浸式，新版模板自带） | 无参数 | 可整行删掉，只是不沉浸式了，功能无影响 |
| `setContentView(R.layout.activity_main)` | 把 `res/layout/activity_main.xml` 渲染成本界面的界面 | 参数 = 布局文件的资源 ID（Int 类型）；`R.layout.文件名` 与 layout 目录下的 xml 一一对应 | **必须放在所有 `findViewById` 之前**，否则闪退【你问过的闪退根因】；换布局文件就换参数 |
| `ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets -> ... }` | 设置"系统栏高度"回调：把状态栏/导航栏的高度转成 padding，防止内容被系统栏盖住（配合 enableEdgeToEdge） | 参数 1 = 根视图（模板根布局 id 为 `main`）；参数 2 = 处理 insets 的 lambda | 考试手写时整块删掉即可（不沉浸式就只需要 setContentView）；删的话 enableEdgeToEdge 和相关 import 一起删 |

#### ② activity_main.xml 模板

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello World!"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

| 模板代码 | 什么意思 | 怎么写 / 参数怎么填 | 要改怎么改 |
|---|---|---|---|
| `<?xml version="1.0" encoding="utf-8"?>` | XML 文件声明 | 固定写法，每个 xml 第一行 | 不改 |
| `<androidx.constraintlayout.widget.ConstraintLayout>` | 根布局标签：决定子控件如何排列（ConstraintLayout 用"约束关系"定位控件） | 一个 xml **只能有一个根标签**，其他控件都写在它里面 | 考试手写建议直接换成 `<LinearLayout ... android:orientation="vertical">`，线性上下排，好写不易错 |
| `xmlns:android=".../res/android"` | 命名空间声明：`android:` 前缀的属性由**系统框架**提供 | 固定写法，只能出现在根标签上 | 不改【你问过 android: 与 app: 的区别】 |
| `xmlns:app=".../res-auto"` | 命名空间：`app:` 前缀的属性来自**支持库/第三方库**（如 `app:layout_constraintXxx`） | 固定写法 | 不改 |
| `xmlns:tools="..."` | 命名空间：`tools:` 属性**只在预览生效**，不会打包进 App | 固定写法 | 不改 |
| `android:id="@+id/main"` | 给根视图起 id；`@+id/` = "新建一个 id"，代码里用 `R.id.main` 找它 | 格式 `@+id/自定义名`（小写字母/数字/下划线） | 只有需要在代码里 findViewById 的控件才需要 id；不需要的可以不写 |
| `android:layout_width` / `android:layout_height` | 控件宽高，**每个控件必填两项** | `match_parent` = 撑满父容器；`wrap_content` = 刚好包住内容；固定值如 `200dp` | 最常改的属性；LinearLayout 子控件可用 `0dp` + `layout_weight` 按比例分空间（Fragment container 高度为 0 的坑就出在这，见第十节） |
| `tools:context=".MainActivity"` | 告诉预览器这个布局属于哪个 Activity（仅预览用） | 点号开头 = 本包下的类名 | 不改也行 |
| `<TextView ...>` | 显示文字的控件 | width/height 必填；`android:text` 设显示文字 | 文字建议引用 `@string/xxx` 而不是硬编码中文（本地化、复用都方便） |
| `app:layout_constraintXxx...（四行）` | 把 TextView 四边约束到父容器四边 → 水平垂直居中 | 格式 `app:layout_constraint[本控件哪条边]_to[目标哪条边]Of="目标 id 或 parent"` | 换成 LinearLayout 根布局后，这四行全删 |

#### ③ AndroidManifest.xml 模板

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApplication">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

| 模板代码 | 什么意思 | 怎么写 / 参数怎么填 | 要改怎么改 |
|---|---|---|---|
| `<manifest>` | 清单根标签，整个文件只有一个 | 固定写法 | 不改 |
| `<application ...>` | App 级配置块 | `allowBackup` / `dataExtractionRules` / `fullBackupContent` 都是备份策略，不用动 | 新增 `<uses-permission>` 加在 application **外面**；新增 `<activity>` 加在 application **里面** |
| `android:label="@string/app_name"` | App 显示名（桌面图标下的名字），引用 strings.xml | `@string/资源名` 格式 | 改 App 名去 strings.xml 改 `app_name` 的值，不要在这里硬编码 |
| `android:icon` / `android:roundIcon` | 桌面图标 | 引用 mipmap 资源 | 一般不改 |
| `android:theme="@style/Theme.MyApplication"` | 全局主题，定义在 `res/values/themes.xml` | 引用 style 资源 | 想要顶部标题栏（ActionBar）→ 把主题父类从 `...NoActionBar` 换成带 ActionBar 的（右上角没有溢出菜单的坑，见第八节） |
| `<activity android:name=".MainActivity">` | **注册 Activity**；点号开头 = 本包下的相对类名 | 每个 Activity 都必须注册，漏一个崩一个 | 新建 SecondActivity 必须加 `<activity android:name=".SecondActivity"/>`，否则 Intent 启动时崩溃（见第五节） |
| `android:exported="true"` | 是否允许其他 App 调起本组件 | 入口 Activity 必须 true（系统启动器要调它）；普通页面一般 false | 不用改 |
| `<intent-filter>` + `MAIN` + `LAUNCHER` | 声明这是"入口"：点桌面图标启动的就是它 | 固定写法，只给入口 Activity 用 | 其他 Activity **不写** intent-filter；把这个 filter 删了 App 就没有桌面图标 |
| `<uses-permission>`（模板里没有） | 权限声明 | 加在 `<application>` 之外 | 联网必加 `<uses-permission android:name="android.permission.INTERNET"/>`（见第十四节） |

#### ④ strings.xml 模板

```xml
<resources>
    <string name="app_name">My Application</string>
</resources>
```

| 模板代码 | 什么意思 | 怎么写 / 参数怎么填 | 要改怎么改 |
|---|---|---|---|
| `<resources>` | 字符串资源的根标签 | 固定写法 | 不改 |
| `<string name="app_name">My Application</string>` | 一条字符串资源：`name` = key（引用时用），标签体 = 实际文字 | 格式 `<string name="标识名">文字</string>`；标识名必须小写 | 多加一行就多一个资源；代码里 `getString(R.string.xxx)` 取值，XML 里 `@string/xxx` 引用【你问过格式】 |
| 特殊字符写法 | XML 里有些字符要转义 | `&` 写成 `&amp;`；撇号写成 `\'`，或把整个值用双引号包住 | 直接写会报 "Apostrophe not preceded by \\" 之类的错 |

#### ⑤ app/build.gradle.kts（Module）关键行

```kotlin
android {
    namespace = "com.example.myapplication"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 37
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
```

| 模板代码 | 什么意思 | 怎么写 / 参数怎么填 | 要改怎么改 |
|---|---|---|---|
| `namespace` | 代码包名，R 类就生成在这个包里 | 建项目时自动生成 | kt 文件的 `package` 必须和它一致，否则 R / 自定义类引用不到【你问过 kt 放哪】 |
| `compileSdk` / `targetSdk` | 用哪个 Android SDK 版本编译 / 目标版本 | 一般不动 | 部分新控件/属性要求高 compileSdk |
| `minSdk` | 支持的最低 Android 版本 | 一般不动 | 用了高版本 API 而 minSdk 低会报 lint 警告 |
| `dependencies { implementation(...) }` | 第三方库声明；**不加这里，代码里就 import 不了对应类** | 格式 `implementation("组名:模块名:版本")`；`libs.xxx` 是版本目录写法，效果相同 | 【你问过】`Cannot resolve class androidx.recyclerview.widget.RecyclerView` → 加 `implementation("androidx.recyclerview:recyclerview:1.3.2")` 后点 Sync Now |
| `compileOptions`（+ kotlin 的 jvmTarget） | Java/Kotlin 编译目标级别 | 与依赖库的要求对齐 | 【你问过】`Compact source files are not supported at language level '11'` → 三处统一升 17（详见第十三节） |

> ⚠️ 改完 build.gradle.kts 一定要点右上角 **Sync Now**，否则改动不生效。

---

### 1.3 常用文件操作

| 操作 | 步骤 |
|---|---|
| **新建 res/layout 文件夹**（模板没生成时） | 右键 `res` → New → **Android Resource Directory** → Resource type 下拉选 `layout` → OK；再右键新建的 layout → New → **Layout Resource File** → 输文件名（小写）→ 根标签选 LinearLayout【你问过：没有 layout 文件夹是正常的，按需生成】 |
| **新建 res/menu 文件夹** | 同上，Resource type 选 `menu`；然后 New → **Menu Resource File**【你问过：默认项目没有 menu 目录】 |
| **新建 kt 文件** | 右键 `java/com.example.myapplication`（你的包）→ New → **Kotlin Class/File** → 选 Class，输类名。放在这个包下（和 MainActivity 同一文件夹）→ 同包**不用 import** 直接互相引用【你问过 kt 能否和 MainActivity 同目录：可以，且这是最简单的做法】。FragmentA、NumberAdapter 都这么加 |
| **Design / Code 视图切换** | 打开 xml 后，右上角三个图标：**Code**（纯代码）/ **Split**（左代码右预览）/ **Design**（纯图形）。看到图形界面想改代码 → 点 Code【你问过】 |
| **查函数/控件帮助文档** | 光标停在类/函数名上：悬停出快速文档；**Ctrl+Q** 看详细文档；**Ctrl+B** 跳到定义处；外部查 developer.android.com 和课件 PDF【你问过"函数有没有帮助手册"】 |

---

### 1.4 易错点与解决办法

| # | 现象 | 原因 | 解决 |
|---|---|---|---|
| 1 | 【你问过】res 下没有 layout / menu 文件夹 | 模板按需生成，没用到就不存在 | 不是坏了。New → Android Resource Directory → 选对应 type 创建 |
| 2 | 【你问过】新建资源文件报错 | res 资源文件名只能**小写字母、数字、下划线**，不能中文/大写 | 全小写命名：`item_view.xml`、`menu_options.xml` |
| 3 | 【你问过】代码引用别的类报 `Unresolved reference` | kt 文件放错包 / 类没定义 / 缺 import | 三查：package 是否一致 → 类是否已定义 → 是否缺 import（Alt+Enter 补） |
| 4 | 启动第二个 Activity 闪退 `ActivityNotFoundException` | Manifest 忘了注册新 Activity | 在 `<application>` 里加 `<activity android:name=".XxxActivity"/>` |
| 5 | 改了 xml 界面没变化 | 看的是 build/ 目录里的资源拷贝，或没重新运行 | 确认打开的是 `app/src/main/res/layout/` 下的文件（不是 build/ 里的）；重新 Run |
| 6 | XML 报错：标签未闭合 / 属性值没引号 | 手写 XML 笔误 | 自闭合标签 `<TextView ... />` 结尾要有 `/`；属性必须 `="值"` 带引号；按报错行号排查 |

---

## 二、Kotlin 基础语法

### 2.1 概述

- 机考只考 Kotlin 基础：变量声明与类型推断、集合、`when`、区间、lambda、空安全。
- 对应课件 L01/L02 语法部分；有 Qt/C++ 底子的人重点适应两点：**类型写在名字后面**、**lambda 无处不在**（事件回调全靠它）。

### 2.2 代码操作流程

| 语法 | 用途 | 写法 / 参数 | 例子（都来自前面写过的代码） |
|---|---|---|---|
| `val` | 声明**只读**变量（引用不可再赋值） | `val 名字: 类型 = 值`，类型可省略 | `val name = "张三"` |
| `var` | 声明**可变**变量 | `var 名字: 类型 = 值` | `var count = 0` 然后 `count++` |
| `String` | 字符串类型，**首字母必须大写** | `String` | `mutableListOf<String>()` |
| `when` | 多分支选择（switch 增强版），可有返回值 | `when (值) { 分支 -> 结果; else -> 默认 }` | `when (rgGender.checkedRadioButtonId) { R.id.rbMale -> "男"; else -> "未选择" }` |
| 区间 | 闭区间含两端，配合 `for`/`in` | `a..b`（含尾）、`a until b`（不含尾）、`b downTo a`（倒序） | `for (i in 10 downTo 1)`（倒计时） |
| `toIntOrNull()` | 安全转 Int，失败返回 null 而不崩溃 | `"123".toIntOrNull()` | `val n = et.text.toString().toIntOrNull() ?: 0` |
| `mutableListOf<T>()` | 创建可变列表 | `mutableListOf<String>()` | `val hobbies = mutableListOf<String>()` |
| `?:`（Elvis） | 空值兜底 | `可能为空的表达式 ?: 默认值` | `(_count.value ?: 0) + 1` |
| 字符串模板 | 在字符串里嵌变量/表达式 | `"$变量"`、`${任意表达式}` | `"姓名=$name 爱好=${hobbies.joinToString(",")}"` |
| `joinToString(",")` | 列表拼成字符串 | 参数 = 分隔符 | `hobbies.joinToString(",")` |
| lambda `{ }` | 匿名函数，事件回调的核心 | `{ 参数 -> 代码 }`，无参时省略箭头 | `btn.setOnClickListener { tv.text = "hi" }` |
| `lateinit var` | 类成员延迟初始化（先声明后赋值） | 只能修饰 var、非空类型 | `private lateinit var adapter: NumberAdapter` |

### 2.3 易错点与解决办法

| 现象 | 原因 | 解决 |
|---|---|---|
| 【你问过】`mutableListOf<string>()` 报 Unresolved reference | Kotlin 区分大小写，字符串类型是 `String`（大写 S） | 改成 `mutableListOf<String>()` |
| 【你问过】"val 能不能自动转成对应类型？" | val 不是类型，是"只读声明"；类型推断只在声明那一刻确定一次 | val 的类型此后固定，只能赋兼容的值；换类型要转换函数（`toInt()` 等），不会"自动变" |
| 【你问过】`if(cbSport.isChecked) hobbies.add("运动")` 语法对不对 | 对。单行 if 不带大括号是合法写法：条件为真执行紧随的一条语句 | 正常用；两条以上语句必须加大括号 `if (...) { ... }` |
| lambda 里引用外面的 var，显示结果不对 | lambda 捕获的是变量引用，循环里 var 已被改掉 | 先存局部 `val n = i`，lambda 里用 n（详见第十一节） |

---

## 三、Activity 生命周期

### 3.1 概述

- Activity 从生到死由系统按固定顺序回调；**旋转屏幕 = 销毁重建**（configuration change），这是大量"数据丢了/界面重置"问题的根源。
- 顺序：`onCreate → onStart → onResume`（前台可用）→ 运行 → `onPause → onStop → onDestroy`；后台回前台走 `onRestart → onStart → onResume`。
- `onSaveInstanceState(Bundle)`：可能被杀之前系统调用，让你存少量状态；`onCreate` 的参数里取回。

### 3.2 代码操作流程

| 回调 | 何时被调 | 常用来做什么 |
|---|---|---|
| `onCreate(savedInstanceState)` | 创建，第一个回调 | `setContentView` + `findViewById` + 初始化 |
| `onStart` / `onResume` | 即将可见 / 可以交互 | 恢复动画、监听 |
| `onPause` / `onStop` | 失去焦点 / 完全不可见 | 暂停播放、保存数据 |
| `onDestroy` | 销毁 | 释放资源（如 `handler.removeCallbacksAndMessages(null)`，见第十一节） |
| `onRestart` | 从后台重新回来 | — |
| `onSaveInstanceState(outState)` | 可能被杀之前 | `outState.putString("key", 值)` |

观察生命周期的办法（Logcat 验证）：

```kotlin
override fun onStart() { super.onStart(); Log.d("LIFE", "onStart") }
override fun onResume() { super.onResume(); Log.d("LIFE", "onResume") }
// 每个回调都打一行 Log，Run 后旋转屏幕，在 Logcat 过滤 LIFE 看顺序
```

保存 / 恢复：

```kotlin
override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString("name", etName.text.toString())
}
// onCreate 里取回：
val saved = savedInstanceState?.getString("name") ?: ""
```

### 3.3 易错点与解决办法

| 现象 | 原因 | 解决 |
|---|---|---|
| 【你问过】App 一启动就闪退，还没进自己的代码就崩 | 把 `val btn = findViewById<Button>(...)` 写成**类成员字段初始化**（onCreate 外面）——此时还没 `setContentView`，布局没挂载，findViewById 必然失败 | 改成 `private lateinit var btn: Button` 声明，在 onCreate 里 `btn = findViewById(...)`。**铁律：findViewById 必须在 setContentView 之后** |
| 运行时崩溃 `super not called` | 覆写生命周期方法忘了调 super | 每个覆写的第一行都写对应的 `super.onCreate(...)` 等 |
| 旋转屏幕后数据丢了 | Activity 销毁重建，字段归零 | 少量状态用 `onSaveInstanceState`；UI 数据用 ViewModel（见第十三节） |

---

## 四、资源系统与 R 类

### 4.1 概述

- res 下每个资源（布局 / 字符串 / 控件 id / 菜单）编译后都会在 **R 类**生成一个 **Int 型资源 ID**。`R.layout.activity_main`、`R.id.btnSubmit`、`R.string.app_name` 全是 Int，不是字符串也不是控件本身。
- strings.xml 格式：`<string name="key">value</string>`；XML 里用 `@string/key`，代码里 `R.string.key` 得到 Int，`getString(R.string.key)` 才得到字符串。

### 4.2 代码操作流程

| 写法 | 用在哪 | 得到什么 |
|---|---|---|
| `@string/app_name` | XML 属性里 | 直接得到字符串值 |
| `R.string.app_name` | 代码里 | Int 资源 ID |
| `getString(R.string.app_name)` | 代码里 | String 值 |
| `R.id.btnSubmit` | 代码里 | 控件 id 的 Int |
| `findViewById<Button>(R.id.btnSubmit)` | 代码里 | 控件对象 |
| `R.layout.activity_main` | `setContentView` / inflate | 布局资源 Int |
| `R.menu.menu_options` | `menuInflater.inflate(...)` | 菜单资源 Int |

### 4.3 易错点与解决办法

| 现象 | 原因 | 解决 |
|---|---|---|
| 【你问过】`Argument type mismatch: actual type is 'String', but 'R.string' was expected` | API 的参数要求**资源 ID（Int）**，你传了普通字符串。最典型：`getString("app_name")` —— getString 的参数是 `resId: Int` | 取"字符串资源的内容"用 `getString(R.string.app_name)`；直接给文字就别套 getString（如 `tv.text = "姓名"`） |
| 【你问过】import 里混进了 `android.R.string` | `android.R` 是系统内置资源，容易和本应用的 R 混淆引发引用错乱 | 删掉这行 import；只用本包的 R（包名 = build.gradle 的 namespace） |
| `R.id.xxx` 报 Unresolved | 控件没写 id / 名字拼错 / 没编译 | 在 xml 里确认 `android:id="@+id/xxx"`；重新编译 |

---

## 五、Intent 与多 Activity

### 5.1 概述

- Intent = "我想做什么"的消息对象。**显式 Intent** 指明目标类（App 内跳转用这个）；隐式靠 action 匹配（了解即可）。
- 可以通过 `putExtra(key, value)` 携带键值对把数据带给下一个页面。

### 5.2 代码操作流程

发送方：

```kotlin
val intent = Intent(this, SecondActivity::class.java)   // 参数1: Context；参数2: 目标类的 Class
intent.putExtra("name", etName.text.toString())         // key, 值（String/Int/Boolean…）
startActivity(intent)                                   // 启动
```

接收方（SecondActivity 的 onCreate 里）：

```kotlin
val name = intent.getStringExtra("name") ?: ""   // key 不存在返回 null → 用 ?: 兜底
val age = intent.getIntExtra("age", 0)           // 原始类型必须给默认值
```

### 5.3 易错点与解决办法

| 现象 | 原因 | 解决 |
|---|---|---|
| 点按钮闪退 `ActivityNotFoundException` | SecondActivity 没在 Manifest 注册 | `<application>` 里加 `<activity android:name=".SecondActivity"/>` |
| 取到的 Extra 是 null / 0 | 发送和接收的 **key 不一致**，或 put/get 类型不配对 | 两边 key 用同一个字符串常量；putXxx 与 getXxx 类型一一对应 |
| `SecondActivity.class` 写法报错 | 那是 Java 语法 | Kotlin 用 `SecondActivity::class.java` |

---

## 六、UI 布局与控件

### 6.1 概述

- 传统 View 体系：XML 描述布局，代码 `findViewById` 取控件。常用布局：LinearLayout（线性排列）、FrameLayout（叠层 / Fragment 容器）、ScrollView（滚动，**只能一个直接子控件**）。
- 表单五件套：EditText / RadioGroup+RadioButton / CheckBox / RatingBar / Button。

### 6.2 代码操作流程

| 控件 | XML 关键属性 | 代码取值 / 操作 |
|---|---|---|
| EditText | `android:hint="请输入"`、`android:inputType` | `et.text.toString()`（text 是 Editable，必须 toString） |
| RadioGroup | `android:orientation="horizontal"` | `rg.checkedRadioButtonId` → 用 when 映射 |
| RadioButton | `android:text="男"` | 必须放在 RadioGroup 里才自动互斥 |
| CheckBox | `android:text="运动"` | `cb.isChecked`（Boolean） |
| RatingBar | `android:numStars="5"`、`android:stepSize` | `ratingBar.rating`（Float） |
| TextView | `android:text`、`android:textSize="16sp"` | `tv.text = "..."`、`tv.setTextColor(Color.RED)` |
| ImageView | `app:srcCompat` / `android:src` | `iv.setImageResource(R.drawable.xxx)` |

表单读取例（你 test_program 的提交逻辑）：

```kotlin
val name = etName.text.toString()
val gender = when (rgGender.checkedRadioButtonId) {
    R.id.rbMale -> "男"
    R.id.rbFemale -> "女"
    else -> "未选择"
}
val hobbies = mutableListOf<String>()
if (cbSport.isChecked) hobbies.add("运动")
if (cbRead.isChecked) hobbies.add("读书")
tvResult.text = "姓名=$name\n性别=$gender\n爱好=${hobbies.joinToString(",")}"
```

### 6.3 易错点与解决办法

| 现象 | 原因 | 解决 |
|---|---|---|
| 【你问过】往 ScrollView 里塞了 Toolbar + 内容直接闪退 `ScrollView can host only one direct child` | ScrollView **只允许一个直接子控件** | 外面套一层垂直 LinearLayout：ScrollView 里只留那一个 LinearLayout，其他内容全放它里面 |
| 【你问过】`app:xxx` 属性报错 / 不认识 | 命名空间用错：系统属性是 `android:`，支持库/第三方属性是 `app:`（根标签要声明 xmlns:app） | ConstraintLayout 的 `layout_constraintXxx` 用 `app:`；`padding/text/...` 等系统属性用 `android:` |
| RatingBar 星星显示不全 | 宽度设了 match_parent 被拉伸 | `layout_width="wrap_content"` |
| `et.text` 直接当 String 用报类型错 | EditText.getText() 返回 Editable | `et.text.toString()` |
| RadioGroup 里单选不互斥 | RadioButton 散落在外/嵌套错层 | 直接子级放进同一个 RadioGroup |

---

## 七、事件处理（点击 / 长按 / 提示）

### 7.1 概述

- 监听器模式：`控件.setXxxListener { 回调 }`，等价 Qt 的 connect + signal-slot。
- 点击 = `setOnClickListener`；长按 = `setOnLongClickListener`（**返回 Boolean**）。
- 三种提示：Toast（小气泡）、Snackbar（底部条，可带按钮）、AlertDialog（对话框，标题+内容+按钮）。

### 7.2 代码操作流程

| 事件/提示 | 写法 | 参数说明 |
|---|---|---|
| 点击 | `btn.setOnClickListener { ... }` | lambda 的 `it` = 被点击的 View |
| 长按 | `btn.setOnLongClickListener { ...; true }` | 返回 `true` = 已消费事件；返回 false 还会继续触发点击 |
| Toast | `Toast.makeText(this, "文字", Toast.LENGTH_SHORT).show()` | 参数：Context、内容、时长；**别忘 .show()** |
| Snackbar | `Snackbar.make(it, "文字", Snackbar.LENGTH_SHORT).show()` | 第一个参数要 **View**（用 `it` 或 findViewById 结果） |
| AlertDialog | `AlertDialog.Builder(this).setTitle("标题").setMessage("内容").setPositiveButton("确定", null).show()` | 链式配置，最后 `.show()`；import `androidx.appcompat.app.AlertDialog` |

例（你 test_program 里的长按对话框）：

```kotlin
btnSubmit.setOnLongClickListener {
    AlertDialog.Builder(this)
        .setTitle("提示").setMessage("你长按了我")
        .setPositiveButton("确定", null)
        .show()
    true   // ← 必须返回 true，否则长按"没反应"
}
```

### 7.3 易错点与解决办法

| 现象 | 原因 | 解决 |
|---|---|---|
| 【你问过】长按不弹 AlertDialog | `setOnLongClickListener` 的 lambda 没有 `return true`，事件未被消费 | lambda 最后一行写 `true` |
| 【你问过】点"提交"只弹 Snackbar、不显示表单结果 | 对同一控件**设置了两次 setOnClickListener**（你 test_program 第 74 / 100 行），第二次覆盖第一次 | 合并成一个监听器：先更新结果，再弹 Snackbar；一个控件的同种监听器只设一次 |
| Toast / AlertDialog 写了没弹 | 忘了 `.show()` | 补上 |
| `Snackbar.make(this, ...)` 编译错 | 第一个参数要 View 不是 Activity | 用 `it`（点击的 View）或 `findViewById` 的结果 |
| 点击事件怎么都不触发 | 监听器写在 setContentView 之前 / 控件 id 取错 | 初始化和监听器都放 onCreate 里；核对 R.id |

---

## 八、菜单（Options + Context）与 Toolbar

### 8.1 概述

- **Options Menu**（右上角溢出菜单）：`res/menu/menu_options.xml` → `onCreateOptionsMenu` 里 inflate → `onOptionsItemSelected` 响应。
- **Context Menu**（长按某控件弹出）：`registerForContextMenu(view)` 注册 → `onCreateContextMenu` inflate → `onContextItemSelected` 响应。
- 新模板是 **NoActionBar 主题**，界面顶部没有原生 ActionBar → 菜单没地方挂 → 要么自己放 Toolbar 并 `setSupportActionBar`，要么换主题。

### 8.2 代码操作流程

菜单 XML（res/menu/menu_options.xml）：

```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:id="@+id/red"  android:title="红色"/>
    <item android:id="@+id/blue" android:title="蓝色"/>
</menu>
```

Options Menu 两步：

```kotlin
override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_options, menu)   // 参数: 菜单资源, 目标 Menu
    return true                                       // 必须返回 true 才显示
}

override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    R.id.red  -> { tv.setTextColor(Color.RED); true }
    R.id.blue -> { tv.setTextColor(Color.BLUE); true }
    else -> super.onOptionsItemSelected(item)
}
```

Toolbar 三步（让溢出菜单出现）：

```xml
<androidx.appcompat.widget.Toolbar
    android:id="@+id/toolbar"
    android:layout_width="match_parent"
    android:layout_height="?attr/actionBarSize"
    android:background="?attr/colorPrimary" />
```

```kotlin
val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
setSupportActionBar(toolbar)   // 之后 onCreateOptionsMenu 才会挂到这个 Toolbar 上
```

Context Menu 三步：

```kotlin
// ① onCreate 里注册哪个控件长按生效
registerForContextMenu(tv)

// ② 长按时填菜单
override fun onCreateContextMenu(menu: ContextMenu, v: View,
                                 menuInfo: ContextMenu.ContextMenuInfo?) {
    super.onCreateContextMenu(menu, v, menuInfo)
    menuInflater.inflate(R.menu.menu_context, menu)
}

// ③ 点了哪一项
override fun onContextItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    R.id.action_reset -> { tv.setTextColor(Color.BLACK); true }
    else -> super.onContextItemSelected(item)
}
```

### 8.3 易错点与解决办法

| 现象 | 原因 | 解决 |
|---|---|---|
| 【你问过】右上角看不到三个点（溢出菜单） | Empty Views Activity 模板是 NoActionBar 主题，没有 ActionBar 可挂菜单 | ① 布局加 Toolbar + `setSupportActionBar(toolbar)`；或 ② themes.xml 把主题父类换成带 ActionBar 的（如 `Theme.Material3.DayNight`，去掉 NoActionBar） |
| 【你问过】menu_context.xml 定义了却没反应 | 只写了 `registerForContextMenu`，没写 `onCreateContextMenu` / `onContextItemSelected` 两个回调 | 三个环节缺一不可：注册 + inflate + 响应 |
| 菜单不显示 | `onCreateOptionsMenu` 返回了 false / inflate 的资源 id 错 | `return true`；核对 `R.menu.menu_options` |
| `onCreateOptionsMenu(menu: Menu?)` | 参数写成可空能用但不规范 | 建议写成非空 `menu: Menu` |
| Toolbar 找不到 setSupportActionBar | import 错成 `android.widget.Toolbar` | 用 `androidx.appcompat.widget.Toolbar`（带 Menu 支持的那套） |

---

## 九、RecyclerView

### 9.1 概述

- 列表控件三件套：**RecyclerView**（容器）+ **Adapter**（把数据一条条装进 item 布局）+ **LayoutManager**（排版方式：线性 / 网格）。
- Adapter 模式 ≈ Qt 的 QAbstractListModel + delegate：数据变了通知 Adapter 刷新，而不是手动改控件。
- 工作流程：`onCreateViewHolder`（inflate item_view.xml 造一行）→ `onBindViewHolder`（把 position 位置的数据填进去）→ `getItemCount`（总行数）。

### 9.2 代码操作流程

依赖（app/build.gradle.kts，加完 **Sync Now**）：

```kotlin
implementation("androidx.recyclerview:recyclerview:1.3.2")
```

item_view.xml（一行的长相）：

```xml
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/tvItem"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="16dp"
    android:textSize="18sp" />
```

Adapter 完整模板（三个方法签名**一字不能差**）：

```kotlin
class NumberAdapter(
    private val data: MutableList<String>,
    private val onItemClick: (Int) -> Unit        // 点击回调
) : RecyclerView.Adapter<NumberAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tv: TextView = view.findViewById(R.id.tvItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view, parent, false)   // 第三参 false 固定写法
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tv.text = data[position]
        holder.itemView.setOnClickListener { onItemClick(position) }   // 绑整行
    }

    override fun getItemCount() = data.size
}
```

Activity 里接线（防越界 + 删除，test_program 修正版）：

```kotlin
val data = MutableList(20) { "Item $it" }
rv.layoutManager = LinearLayoutManager(this)          // 线性；网格用 GridLayoutManager(this, 2)
adapter = NumberAdapter(data) { pos ->
    if (pos in data.indices) {                        // ① 先检查位置有效（防越界）
        data.removeAt(pos)
        adapter.notifyItemRemoved(pos)                // ② 精准刷新这一行
    }
}
rv.adapter = adapter
```

### 9.3 易错点与解决办法

| 现象 | 原因 | 解决 |
|---|---|---|
| 【你问过】`Cannot resolve class androidx.recyclerview.widget.RecyclerView` | 没加依赖 | build.gradle.kts 加 `implementation("androidx.recyclerview:recyclerview:1.3.2")` → Sync Now |
| 【你问过】`Unresolved reference 'NumberAdapter'` | 类没定义，或定义到了别的包 | 同一个文件里（类外顶层）定义，或同包另开 kt 文件 |
| 【你问过】`Class 'NumberAdapter' is not abstract and does not implement...` | 三个抽象方法没实现全 / 签名不一致 | `onCreateViewHolder`（返回 VH）、`onBindViewHolder(holder: VH, position: Int)`、`getItemCount()` 全部 override，签名照抄模板 |
| 【你问过】`Unresolved reference 'adapter'`（删除回调里） | lambda 里引用了正在声明的自己 | 声明成员 `private lateinit var adapter: NumberAdapter` → onCreate 里赋值 → 回调里就能用 |
| 【你问过】点击某行闪退 `IndexOutOfBoundsException` | 删除后 position 过期，又拿旧位置去 `data.removeAt(...)` | 回调里先 `if (pos in data.indices)` 再删 |
| 【你问过】点击"不灵敏"（点文字区域没反应） | 监听器绑在内部 TextView 上，点到 padding 没反应 | 绑 `holder.itemView`（整行），不要绑 holder.tv |
| 列表一片空白 | 忘了设 layoutManager，或 RecyclerView 高度为 0 | `rv.layoutManager = LinearLayoutManager(this)`；检查布局宽高（结合第十节的 weight 问题） |

---

## 十、Fragment

### 10.1 概述

- Fragment = "子界面"，必须依附 Activity。创建四步：① 写布局 `fragment_a.xml` → ② 类继承 `androidx.fragment.app.Fragment` → ③ 覆写 `onCreateView` inflate 布局 → ④ 用事务 replace 到容器。
- 容器：主布局里放一个 FrameLayout（`@+id/container`）作为 Fragment 的"槽位"。

### 10.2 代码操作流程

Fragment 类（FragmentA.kt，与 MainActivity 同包）：

```kotlin
class FragmentA : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 参数: 布局资源 / 父容器 / 是否立即挂到父容器（false 固定写法）
        return inflater.inflate(R.layout.fragment_a, container, false)
    }
}
```

切换（test_program 原码）：

```kotlin
btnSwitch.setOnClickListener {
    supportFragmentManager.beginTransaction()   // ① 开事务
        .replace(R.id.container, FragmentA())   // ② 把容器内容换成 FragmentA
        .commit()                               // ③ 提交（必写！）
}
btnSwitch.setOnLongClickListener {
    supportFragmentManager.beginTransaction()
        .replace(R.id.container, FragmentB())
        .commit()
    true
}
```

容器 XML（关键是高度必须有）：

```xml
<FrameLayout
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_weight="1" />
```

### 10.3 易错点与解决办法

| 现象 | 原因 | 解决 |
|---|---|---|
| 【你问过】点"切换 Fragment"没反应、看不到内容 | 代码没问题，**容器高度被算成 0**：LinearLayout 里前面的兄弟控件用 match_parent 把空间吃完，`container`（0dp+weight）分到 0，Fragment 换进去了也看不见；RecyclerView 同理被挤出屏幕 | 三个内容区（ScrollView / container / RecyclerView）**全部**用 `0dp` + `layout_weight="1"`，各占 1/3 |
| import 错基类 `android.app.Fragment` | 系统 Fragment 是老 API | import `androidx.fragment.app.Fragment`，配合 `supportFragmentManager` |
| 点了什么都不发生 | 忘写 `.commit()` | 事务三步缺一不可 |
| 崩溃 `IllegalArgumentException: No view found for id` | replace 的容器 id 写错 / 不在当前布局里 | 核对 xml 里的 `@+id/container` 与代码 `R.id.container` |
| Fragment 里 findViewById 报错/找不到 | Fragment 的 view 是 onCreateView 返回的那个，不能直接 findViewById | 在 `onViewCreated` 里用 `view?.findViewById(...)` |

---

## 十一、多线程

### 11.1 概述

- 只有主线程（UI thread）能碰 UI 控件；耗时操作（sleep / 网络 / IO）放后台线程，否则卡界面甚至 ANR（L09 第 23–27 页）。
- 后台 → 主线程三件套：`view.post` / `runOnUiThread` / `Handler(Looper.getMainLooper()).post`（L09 第 27 页）。
- Handler 原理：主线程有 Message Queue（Looper 循环取消息），`handler.post {}` 把任务投递进队列在主线程执行；`removeCallbacksAndMessages(null)` 清空待执行任务防泄漏（L09 第 25、39 页）。

### 11.2 代码操作流程

| API | 用途 | 参数 / 注意 |
|---|---|---|
| `Thread { ... }.start()` | 开后台线程执行 lambda | **必须 start()**；`run()` 只是普通调用不开线程 |
| `runOnUiThread { ... }` | 切回主线程更新 UI | 在 Activity 里调；lambda 无参 |
| `view.post { ... }` | 同上，任何 View 都能调 | |
| `Handler(Looper.getMainLooper())` | 绑主线程的 Handler | `handler.post {}` 立即投递；`handler.postDelayed({}, 1000)` 延迟毫秒 |
| `handler.removeCallbacksAndMessages(null)` | 清空所有待执行任务 | onDestroy 里调，防内存泄漏 |

倒计时例（Thread + runOnUiThread）：

```kotlin
btnStart.isEnabled = false
Thread {
    for (i in 10 downTo 1) {
        runOnUiThread { tv.text = "倒计时 $i" }
        Thread.sleep(1000)
    }
    runOnUiThread { tv.text = "完成！"; btnStart.isEnabled = true }
}.start()
```

Handler 例（进度条 + 生命周期管理）：

```kotlin
private val handler = Handler(Looper.getMainLooper())
// import android.os.Handler / android.os.Looper

// 后台线程里：
Thread {
    for (i in 1..10) {
        Thread.sleep(500)
        val p = i * 10
        handler.post { pb.progress = p }          // 投递回主线程
    }
}.start()

override fun onDestroy() {
    super.onDestroy()
    handler.removeCallbacksAndMessages(null)      // 清空未执行任务
}
```

### 11.3 易错点与解决办法

| 现象 | 原因 | 解决 |
|---|---|---|
| 【你问过】`Too many arguments for 'constructor(): Handler'` | **导错类**：import 成了 `java.util.logging.Handler`（它只有无参构造） | 换成 `import android.os.Handler` + `import android.os.Looper`；光标放 Handler 上按 Alt+Enter 选 android.os.Handler |
| 后台线程直接 `tv.text = ...` 崩溃 `Only the original thread that created a view hierarchy can touch its views` | UI 控件非线程安全，只有主线程能碰 | 更新 UI 的语句包进 `runOnUiThread { }` / `handler.post { }` |
| 界面卡死 N 秒 | 写了 `Thread { ... }.run()`，或直接在主线程 sleep | 必须 `.start()` 开新线程 |
| 退出页面后崩溃 / 内存泄漏 | Activity 销毁后线程还回来改 UI | Handler 方案：onDestroy 里 `removeCallbacksAndMessages(null)`（L09 第 37–39 页） |
| 循环里显示全是最后一个数 | lambda 捕获的是 var 引用 | 先存局部 `val n = i`，lambda 里用 n |

---

## 十二、数据存储

### 12.1 概述

- 三层存储：**SharedPreferences**（键值对小配置，底层是 XML 文件，机考重点）→ 内部文件（`openFileOutput/Input`）→ Room（SQLite 封装，三件套，进阶）。
- SharedPreferences 只支持原始类型（Boolean / Float / Long / Int / String），默认不加密，别存密码（Storage 第 12–13、17 页）。

### 12.2 代码操作流程

| API | 用途 | 参数 / 注意 |
|---|---|---|
| `getSharedPreferences("myPref", MODE_PRIVATE)` | 打开（或创建）配置文件 | 参数：文件名、模式（MODE_PRIVATE 是唯一可用模式，Storage 第 14 页） |
| `.edit()` | 拿编辑器 | 之后链式 putXxx |
| `.putString(key, v)` / `putInt` / `putBoolean` | 写键值 | key 是 String，value 按类型 |
| `.apply()` | 提交，**异步不阻塞**（默认用这个，Storage 第 15 页） | |
| `.commit()` | 提交，**同步阻塞**，返回 Boolean | 只在必须立刻确认成败时用 |
| `prefs.getString("name", "")` | 读，**必须给默认值** | key 找不到返回默认值（Storage 第 16 页） |
| `openFileOutput("a.txt", MODE_PRIVATE)` / `openFileInput("a.txt")` | 内部文件写 / 读（Storage 第 34–37 页） | 配 `.use { }` 自动关流 |

记住姓名例：

```kotlin
val prefs = getSharedPreferences("myPref", MODE_PRIVATE)

etName.setText(prefs.getString("name", ""))                  // 启动时回填

btnSave.setOnClickListener {
    prefs.edit()
        .putString("name", etName.text.toString())
        .apply()                                             // 四步：edit → put → apply
    tv.text = "已保存"
}
```

### 12.3 易错点与解决办法

| 现象 | 原因 | 解决 |
|---|---|---|
| 保存后重开 App 数据没了 | 链式调用**漏了 `.apply()` / `.commit()`**，编辑器没提交 | 每次写完必须接 `.apply()` |
| 读数据 NPE | `getString(key)` 单参版本可能返回 null | `getString(key, "")` 永远给默认值 |
| 用 SharedPreferences 存对象 | 只支持原始类型 | 序列化成 JSON 字符串存，或用 Room |
| Room 操作崩溃 `Cannot access database on the main thread` | DAO 默认禁止主线程操作 | 包 `Thread { }` 或协程 `Dispatchers.IO`；Room 三件套结构见《复习提纲》第九节 |
| `commit()` 到处用 | 同步写盘阻塞 UI | 默认 `apply()`，只确认成败才 `commit()` |

---

## 十三、ViewModel + LiveData

### 13.1 概述

- **ViewModel**：专门保管 UI 数据的类，**旋转屏幕（configuration change）不丢**；生命周期 = Activity 创建 → 真正退出（返回键），由 ViewModelStore 托管（L04 第 32 页）。
- **LiveData**：生命周期感知的可观察数据容器。规范：内部 `MutableLiveData`（私有可写），对外暴露只读 `LiveData`（L04 第 30–31、33 页）。
- MVVM 分层：View 显示+发命令并 observe；ViewModel 持数据+逻辑；Model 取数（L04 第 25–26 页）。

### 13.2 代码操作流程

依赖：

```kotlin
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
```

ViewModel（计数器）：

```kotlin
class CounterViewModel : ViewModel() {
    private val _count = MutableLiveData(0)        // 私有可写
    val count: LiveData<Int> get() = _count        // 对外只读
    fun increment() { _count.value = (_count.value ?: 0) + 1 }
}
```

Activity：

```kotlin
private val vm: CounterViewModel by viewModels()   // 委托获取；旋转后拿回同一实例

onCreate 里：
vm.count.observe(this) { c -> tv.text = "计数：$c" }   // 观察；this = LifecycleOwner
btnAdd.setOnClickListener { vm.increment() }            // View 只发命令，不碰数据
```

| API | 用途 | 参数 / 限制 |
|---|---|---|
| `by viewModels()` | 获取绑定本 Activity 的 VM | 来自 activity-ktx |
| `observe(owner) { }` | 订阅数据变化，变了自动回调 | owner 传 `this`（Activity/Fragment） |
| `.value = x`（即 setValue） | 主线程写值 | **只能主线程** |
| `postValue(x)` | 后台线程写值 | 内部自动切主线程再通知（L04 第 31 页） |

### 13.3 易错点与解决办法

| 现象 | 原因 | 解决 |
|---|---|---|
| 【你问过】`Compact source files are not supported at language level '11'` | compileOptions 用 Java 11，而较新的依赖库（lifecycle 2.8+ 等）需要 Java 17 字节码 | build.gradle.kts 三处统一 17：`sourceCompatibility = JavaVersion.VERSION_17`、`targetCompatibility = ...17`、`jvmTarget = "17"`；并检查 Settings → Build → Gradle → **Gradle JDK** 选 17/21，然后 Sync |
| 后台线程 `_count.value = x` 崩溃 `Cannot invoke setValue on a background thread` | setValue 只准主线程 | 后台改用 `_count.postValue(x)` |
| View 里直接改 `_count` 编译错 | `_count` 是 private（这正是封装目的） | 调 VM 暴露的方法，如 `vm.increment()` |
| observe 一绑定就收到一次当前值 | LiveData 正常行为：新观察者立刻收到当前值（好处：UI 初始状态自动对齐） | 不是 bug；要区分"首次/变化"自行加标志位 |
| VM 里持有 Activity/Context | 内存泄漏（VM 活得比 Activity 久） | VM 只放数据和逻辑；要 Context 用 `AndroidViewModel(application)` |

---

## 十四、网络

### 14.1 概述

- 联网三步（T09 第 6 页）：① Manifest 加 `INTERNET` 权限 → ② 网络操作放后台线程 → ③ `HttpURLConnection` 发请求读响应。
- JSON 用内置的 `org.json` 解析（`JSONObject` / `JSONArray`），无需依赖（T09 第 35–36 页）。
- HTTP 客户端选型：机考用基础 `HttpURLConnection`；OkHttp / Retrofit 是上层封装，了解即可（T09 第 5 页）。

### 14.2 代码操作流程

Manifest 两处：

```xml
<uses-permission android:name="android.permission.INTERNET"/>

<!-- <application> 标签里加属性（访问 http:// 明文站点必需）：
     android:usesCleartextTraffic="true" -->
```

请求 + 解析（拉汇率，floatrates 数据源 T09 第 12 页）：

```kotlin
btnFetch.setOnClickListener {
    Thread {                                              // ② 后台线程
        var conn: HttpURLConnection? = null
        try {
            val url = URL("http://www.floatrates.com/daily/hkd.json")
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val text = conn.inputStream.bufferedReader().readText()   // 读全部响应
            val rate = JSONObject(text)
                .getJSONObject("usd").getDouble("rate")                // 逐层解析
            runOnUiThread { tv.text = "1 HKD ≈ $rate USD" }            // ⑤ 回主线程
        } catch (e: Exception) {
            runOnUiThread { tv.text = "出错：${e.message}" }
        } finally {
            conn?.disconnect()                                         // 释放连接
        }
    }.start()
}
```

| API | 用途 | 参数 / 返回 |
|---|---|---|
| `URL("...")` | 建 URL 对象 | 参数：地址字符串 |
| `url.openConnection() as HttpURLConnection` | 打开连接 | 强转为 HTTP 连接 |
| `conn.connectTimeout / readTimeout` | 超时（毫秒） | 建议都设 5000 |
| `conn.inputStream.bufferedReader().readText()` | 读全部响应 | 返回 String |
| `JSONObject(text)` | 把文本解析成 JSON 对象 | `.getJSONObject("usd")` 取子对象、`.getDouble("rate")` 取数值、`.getString("code")` 取字符串 |
| `JSONObject(text).getJSONArray("earthquakes")` | 解析数组 | `arr.getJSONObject(i)` 逐项取；`arr.length()` 取长度 |

### 14.3 易错点与解决办法

| 现象 | 原因 | 解决 |
|---|---|---|
| 闪退 `SecurityException` | 没加 INTERNET 权限（T09 第 7 页） | Manifest 加 `<uses-permission android:name="android.permission.INTERNET"/>` |
| 请求一直失败（http 站点） | Android 9+ 默认禁止明文 http | `<application>` 加 `android:usesCleartextTraffic="true"` |
| 崩溃 `NetworkOnMainThreadException` | 网络代码写在主线程 | 整段包进 `Thread { ... }.start()` |
| 崩溃 `Only the original thread...` | 拿到结果直接 setText | 包进 `runOnUiThread { }` |
| `JSONException: No value for xxx` | key 拼错 / 层级取错 | 对照 JSON 原文一层层取：先 getJSONObject 再 getDouble |
| 连接一直挂着 | 没 disconnect | `finally` 里 `conn?.disconnect()` |

---

> **考场索引**：表单类题 → 六/七节；列表题 → 九节；跳转传值 → 五节；倒计时/进度条 → 十一节；记住状态 → 十二/十三节；联网 → 十四节。每节的「代码操作流程」表就是考场可直接抄写的代码骨架。

---

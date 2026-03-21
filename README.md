# 考研助手

一个基于 **Kotlin + Jetpack Compose + Room** 的本地 Android 考研学习管理应用，面向考研复习过程中的日常记录与自我管理。

当前版本聚焦 5 个核心页面：

- `计时`：专注计时、切换科目、后台前台服务通知
- `打卡`：今日打卡、本月打卡日历、休息日、每日详情
- `统计`：日 / 周 / 月学习占比统计
- `待办`：按日期管理待办、完成状态、提醒
- `科目`：科目增删改、颜色管理

项目当前为**纯本地数据应用**，不依赖云端服务。

---

## 1. 功能概览

### 1.1 科目管理

- 默认内置科目：
  - 数学
  - 英语
  - 政治
  - 算法
  - 计组
  - OS
  - 计网
- 支持新增、编辑、删除科目
- 支持为每个科目设置独立颜色
- 颜色会在计时、统计、待办等页面全局联动

### 1.2 专注计时

- 选择科目后开始专注
- 正在计时时会启动前台服务通知
- 支持锁屏 / 后台持续计时
- 同一科目当天重复开始时，会续接当天累计学习时长
- 停止计时后会生成学习记录

### 1.3 打卡

- 仅允许对当天执行打卡 / 取消打卡
- 支持将某一天标记为休息日
- 休息日不会中断连续打卡
- 月历格子内显示当天学习时长
- 可点击某一天查看“每日详情”

### 1.4 学习统计

- 支持日统计、周统计、月统计
- 通过饼图展示各科时间占比
- 周统计按“当前选中日期作为起点，向后 7 天”计算
- 月统计按自然月统计

### 1.5 待办清单

- 待办按“所属日期”管理，而不是截止日期
- 支持新增、编辑、删除、勾选完成
- 已完成任务显示更粗的划线效果
- 支持按天查看某一天的待办
- 可配置晚间待办提醒

### 1.6 提醒与通知

- 早上打卡提醒
- 晚间待办提醒
- 学习计时前台通知
- 支持系统通知渠道
- 启动时自动重排提醒
- 支持开机后恢复提醒

---

## 2. 技术栈

- 语言：Kotlin
- UI：Jetpack Compose + Material 3
- 数据库：Room
- 架构：`UI + ViewModel + Repository + Room`
- 后台能力：
  - Foreground Service
  - AlarmManager
  - BroadcastReceiver
- 构建工具：Gradle Kotlin DSL

---

## 3. 运行环境

建议环境：

- Android Studio 最新稳定版
- JDK 17
- Android SDK 36
- Android 7.0 及以上设备

项目当前配置见：

- [`app/build.gradle.kts`](./app/build.gradle.kts)
- [`gradle/libs.versions.toml`](./gradle/libs.versions.toml)

关键版本：

- `compileSdk = 36`
- `targetSdk = 36`
- `minSdk = 24`
- Kotlin `2.2.10`

---

## 4. 本地运行

### 4.1 克隆项目

```bash
git clone <your-repo-url>
cd kaoYanAssistant
```

### 4.2 用 Android Studio 打开

直接打开项目根目录：

```text
/Users/Zhuanz/Desktop/Project/kaoYanAssistant
```

### 4.3 同步依赖

首次同步会下载：

- Android Gradle Plugin
- Compose 相关依赖
- Room 相关依赖
- SplashScreen 依赖

如果网络较慢，项目已经在 [`settings.gradle.kts`](./settings.gradle.kts) 中配置了国内镜像兜底。

### 4.4 运行到设备

选择模拟器或真机后，直接运行 `app` 模块即可。

---

## 5. 常用命令

### 5.1 仅检查 Kotlin 编译

```bash
./gradlew :app:compileDebugKotlin --console=plain
```

### 5.2 构建 Debug 包

```bash
./gradlew :app:assembleDebug --console=plain
```

### 5.3 构建 Release 包

```bash
./gradlew :app:assembleRelease --console=plain
```

注意：直接命令行构建 `release` 只适用于你已经在 Gradle 中完成签名配置的情况。  
如果你还没有签名配置，推荐直接使用 Android Studio 生成签名 APK。

---

## 6. 项目结构

```text
app/src/main/java/com/example/kaoyanassistant
├── core
│   └── AppContainer.kt
├── data
│   ├── local
│   │   ├── dao
│   │   ├── entity
│   │   └── AppDatabase.kt
│   └── repository
├── notification
│   ├── BootReceiver.kt
│   ├── NotificationHelper.kt
│   ├── ReminderReceiver.kt
│   └── ReminderScheduler.kt
├── service
│   └── StudyTimerService.kt
├── ui
│   ├── components
│   ├── model
│   ├── navigation
│   ├── screens
│   ├── theme
│   ├── AppViewModel.kt
│   └── KaoYanAssistantApp.kt
├── util
│   ├── DateTimeUtils.kt
│   └── Formatters.kt
├── KaoYanAssistantApplication.kt
└── MainActivity.kt
```

### 6.1 页面文件

- [`TimerScreen.kt`](./app/src/main/java/com/example/kaoyanassistant/ui/screens/TimerScreen.kt)
- [`CheckInScreen.kt`](./app/src/main/java/com/example/kaoyanassistant/ui/screens/CheckInScreen.kt)
- [`StatisticsScreen.kt`](./app/src/main/java/com/example/kaoyanassistant/ui/screens/StatisticsScreen.kt)
- [`TodoScreen.kt`](./app/src/main/java/com/example/kaoyanassistant/ui/screens/TodoScreen.kt)
- [`SubjectsScreen.kt`](./app/src/main/java/com/example/kaoyanassistant/ui/screens/SubjectsScreen.kt)
- [`DailyDetailScreen.kt`](./app/src/main/java/com/example/kaoyanassistant/ui/screens/DailyDetailScreen.kt)
- [`TodoEditorScreen.kt`](./app/src/main/java/com/example/kaoyanassistant/ui/screens/TodoEditorScreen.kt)
- [`SettingsScreen.kt`](./app/src/main/java/com/example/kaoyanassistant/ui/screens/SettingsScreen.kt)

---

## 7. 数据层说明

本项目使用 Room 本地数据库，数据库入口：

- [`AppDatabase.kt`](./app/src/main/java/com/example/kaoyanassistant/data/local/AppDatabase.kt)

核心表：

- `SubjectEntity`：科目
- `StudySessionEntity`：学习记录
- `ActiveTimerEntity`：当前正在计时的会话
- `TodoEntity`：待办
- `DayRecordEntity`：打卡 / 休息日记录
- `SettingsEntity`：提醒设置

### 7.1 数据初始化

应用首次启动时会自动：

- 创建通知渠道
- 插入默认科目
- 插入默认提醒设置
- 根据设置重排提醒任务

逻辑入口在：

- [`KaoYanAssistantApplication.kt`](./app/src/main/java/com/example/kaoyanassistant/KaoYanAssistantApplication.kt)
- [`AppContainer.kt`](./app/src/main/java/com/example/kaoyanassistant/core/AppContainer.kt)

### 7.2 迁移说明

数据库当前使用：

```kotlin
fallbackToDestructiveMigration(true)
```

这意味着：

- 数据结构版本变化时，旧数据可能会被清空并重建
- 当前更适合个人项目开发阶段
- 如果后续需要正式发布，建议补齐 Room Migration

---

## 8. 通知与权限

Manifest 中已声明：

- `POST_NOTIFICATIONS`
- `RECEIVE_BOOT_COMPLETED`
- `SCHEDULE_EXACT_ALARM`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_DATA_SYNC`

相关文件：

- [`AndroidManifest.xml`](./app/src/main/AndroidManifest.xml)
- [`NotificationHelper.kt`](./app/src/main/java/com/example/kaoyanassistant/notification/NotificationHelper.kt)
- [`ReminderScheduler.kt`](./app/src/main/java/com/example/kaoyanassistant/notification/ReminderScheduler.kt)
- [`ReminderReceiver.kt`](./app/src/main/java/com/example/kaoyanassistant/notification/ReminderReceiver.kt)

注意：

- Android 13 及以上需要手动授予通知权限
- 某些 ROM 对精确闹钟、锁屏通知、后台提醒有限制
- 真机提醒效果可能受系统策略影响

---

## 9. 应用名称与图标

### 9.1 修改应用名称

桌面显示名在：

- [`app/src/main/res/values/strings.xml`](./app/src/main/res/values/strings.xml)

修改：

```xml
<string name="app_name">你的应用名字</string>
```

### 9.2 修改应用图标

推荐方式：

1. Android Studio 右键 `app`
2. `New`
3. `Image Asset`
4. 选择 `Launcher Icons (Adaptive and Legacy)`
5. 导入你的图标素材

关键文件：

- [`app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`](./app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml)
- [`app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`](./app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml)
- [`app/src/main/res/drawable/ic_launcher_foreground.xml`](./app/src/main/res/drawable/ic_launcher_foreground.xml)
- [`app/src/main/res/drawable/ic_launcher_background.xml`](./app/src/main/res/drawable/ic_launcher_background.xml)

### 9.3 启动图标不一致问题

如果你改了桌面图标，但启动时仍出现旧图标或默认安卓图标，通常是因为：

- 只替换了 `mipmap` 图标
- 没有同步替换 `adaptive icon` 的前景层
- 没有配置统一的启动主题

当前项目已经接入：

- Android 12+ SplashScreen
- 独立启动主题
- 统一的启动图标链路

相关文件：

- [`MainActivity.kt`](./app/src/main/java/com/example/kaoyanassistant/MainActivity.kt)
- [`themes.xml`](./app/src/main/res/values/themes.xml)

---

## 10. 如何生成发行版 APK

### 10.1 推荐方式：Android Studio 图形界面

1. 打开 Android Studio
2. 点击 `Build`
3. 选择 `Generate Signed Bundle / APK`
4. 选择 `APK`
5. 创建或选择已有 `keystore`
6. 选择 `release`
7. 完成后生成签名 APK

默认输出目录通常在：

```text
app/build/outputs/apk/release/
```

### 10.2 如果要上架应用商店

建议生成：

- `Android App Bundle (.aab)`

路径：

1. `Build`
2. `Generate Signed Bundle / APK`
3. 选择 `Android App Bundle`

输出目录通常在：

```text
app/build/outputs/bundle/release/
```

### 10.3 发布前建议检查

- 应用名是否正确
- 图标是否一致
- `versionCode` 是否递增
- `versionName` 是否更新
- 通知权限与提醒逻辑是否正常
- 真机后台计时和提醒是否符合预期

版本号位置：

- [`app/build.gradle.kts`](./app/build.gradle.kts)

---

## 11. 当前限制

- 暂未接入云同步
- 暂未接入账号系统
- 暂未接入自动化测试
- 当前数据库迁移为 destructive migration
- 部分提醒与锁屏通知效果受设备系统策略影响

---

## 12. 后续可扩展方向

- 导出学习记录
- 统计页更多图表样式
- 更完整的复盘页面
- 科目排序与归档
- 深色模式
- Room 正式迁移方案
- 自动化测试与 CI

---

## 13. License

如果你准备公开发布到 GitHub，建议补一个明确的 License，例如：

- MIT
- Apache-2.0
- GPL-3.0

如果暂时不想开放使用权限，也可以先不加，但建议尽早明确。

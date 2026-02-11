# Google TV Tetris (D-pad操作)

Android TV / Google TV 向けの、**D-pad 前提**で遊べる Tetris 実装です。  
UI は Jetpack Compose + Canvas、ゲームロジックは Reducer 中心の純 Kotlin 実装に分離しています。

## 動作要件

- Android Studio Jellyfish 以降推奨
- JDK **17 または 21**（重要）
- Android SDK 34
- TV エミュレータ または Android TV / Google TV 実機

> このリポジトリは Android Gradle Plugin を使うため、JDK 25 ではビルドできません。

## セットアップ

1. Android Studio でこのプロジェクトを開く
2. `File > Settings > Build, Execution, Deployment > Build Tools > Gradle` で **Gradle JDK を 17 or 21** に設定
3. Gradle Sync を実行

## 起動方法（Android Studio）

1. Device Manager から TV エミュレータを作成（例: Google TV 1080p）
2. Run Configuration で `app` を選択
3. Run ▶ を押して起動
4. TV ホームのアプリ一覧に `TV Tetris` が出ることを確認

## 起動方法（CLI）

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
adb shell monkey -p com.example.googletvtetris 1
```

## 操作方法（リモコン / D-pad）

- `DPAD_LEFT` / `DPAD_RIGHT`: 横移動
- `DPAD_DOWN`: ソフトドロップ（押下中）
- `DPAD_UP`: ハードドロップ
- `DPAD_CENTER` または `ENTER`: 回転
- `BACK`:
  - Playing 中 → Pause
  - Paused / GameOver 中 → アプリ終了（ホームへ戻る）

## 実装のポイント

- 盤面は 10x20
- ゲームロジックは `game/TetrisReducer.kt` に集約
- UI は `MainActivity.kt` で state を描画する構成
- overscan を考慮し、重要 UI は画面端に配置しない

## TV 向け Manifest 要件

以下を満たしています。

- `MAIN + LEANBACK_LAUNCHER`
- `uses-feature android.software.leanback`
- `uses-feature android.hardware.touchscreen required=false`
- `application` に `android:banner` と `android:icon`

## テスト

```bash
./gradlew :app:testDebugUnitTest
```

Reducer の JVM テストが含まれます（得点、速度、ライン消去、Back→Pause）。

## 既知の注意点

- キー入力はフォーカスが前提です。TV では初期フォーカスを `FocusRequester` で付与しています。
- 環境 JDK が 25 の場合、Gradle タスクは失敗します。JDK 17/21 に切り替えてください。

# タスク一覧: mcp-chat-webapp

## フェーズ1: 設定・基盤

- [x] 1.1 McpConfig クラスの実装
  - `@ConfigurationProperties(prefix = "mcp.server")` を使用
  - `url`（必須）、`connectionTimeout`（デフォルト5000）、`readTimeout`（デフォルト30000）フィールド
  - `@Validated` + `@NotBlank` でURL未設定時の起動中断
  - パッケージ: `com.github.miyohide.mymcp.config`

- [x] 1.2 application.properties へのMCP設定追加
  - `mcp.server.url`、`mcp.server.connection-timeout`、`mcp.server.read-timeout` を追記

- [x] 1.3 カスタム例外クラスの実装
  - `McpException`（基底）
  - `McpTimeoutException`
  - `McpConnectionException`
  - パッケージ: `com.github.miyohide.mymcp.exception`

- [ ]* 1.4 build.gradle への依存関係追加
  - jqwik（プロパティベーステスト用）: `net.jqwik:jqwik:1.9.0`

## フェーズ2: データモデル・セッション

- [x] 2.1 ChatMessage レコードの実装
  - フィールド: `userMessage`、`botResponse`、`timestamp`、`isError`
  - パッケージ: `com.github.miyohide.mymcp.model`

- [x] 2.2 ChatSession コンポーネントの実装
  - `@Component` + `@SessionScope`
  - `addMessage(String userMessage, String botResponse)` メソッド
  - `addErrorMessage(String userMessage, String errorMessage)` メソッド
  - `getMessages()` メソッド
  - `clear()` メソッド
  - パッケージ: `com.github.miyohide.mymcp.session`

## フェーズ3: MCPクライアントサービス

- [ ] 3.1 McpClientService の実装
  - `@Service` アノテーション
  - `@PostConstruct` で `initialize` リクエスト送信
  - `sendMessage(String message)` メソッド（`tools/call` 呼び出し）
  - JSON-RPC 2.0形式のリクエスト構築
  - レスポンスの `content[].text` をテキストに変換
  - タイムアウト時は `McpTimeoutException` をスロー
  - 接続失敗時は `McpConnectionException` をスロー
  - MCPエラーレスポンス時は `McpException` をスロー
  - パッケージ: `com.github.miyohide.mymcp.service`

## フェーズ4: コントローラー

- [ ] 4.1 ChatController の実装
  - `GET /chat` → チャット画面表示
  - `POST /chat` → メッセージ送信・応答表示
    - 空・空白メッセージは送信せずに再表示
    - 例外時はエラーメッセージをChatSessionに追加
  - `POST /chat/clear` → 会話履歴クリア
  - パッケージ: `com.github.miyohide.mymcp.controller`

## フェーズ5: Thymeleafテンプレート

- [ ] 5.1 chat.html の実装
  - メッセージ入力フォーム（`<form method="post" action="/chat">`）
  - 送信ボタン
  - 会話履歴エリア（ユーザー/ボットを異なるCSSクラスで区別）
  - エラーメッセージ表示（`isError` フラグで判定）
  - 新しいメッセージが下に表示される順序
  - パス: `src/main/resources/templates/chat.html`

- [ ] 5.2 静的リソース（CSS/JS）の実装
  - `src/main/resources/static/css/chat.css`: ユーザー/ボットのスタイル定義
  - `src/main/resources/static/js/chat.js`:
    - Enterキーでフォーム送信
    - 送信中の送信ボタン無効化
    - 新しい応答追加後の自動スクロール
    - ページ読み込み時の入力フォームへのフォーカス

## フェーズ6: テスト

- [ ] 6.1 McpConfig のユニットテスト
  - 設定値の正常読み込み確認（要件3.1, 3.2, 3.3）
  - URL未設定時の起動失敗確認（エッジケース: 要件3.4）
  - タイムアウトのデフォルト値確認（エッジケース: 要件3.5, 3.6）

- [ ] 6.2 ChatSession のユニットテスト
  - `clear()` 後にリストが空になることを確認（要件6.5）
  - 複数セッションの独立性確認（要件6.1）

- [ ]* 6.3 ChatSession のプロパティテスト
  - `// Feature: mcp-chat-webapp, Property 1: メッセージ追加後の会話履歴順序保持`
  - 任意のメッセージリストに対して追加順と取得順が一致することを確認（要件1.5, 6.2, 6.4）

- [ ] 6.4 ChatController のユニットテスト
  - `GET /chat` が200を返すことを確認（要件1.1）
  - チャット画面の必須UI要素（フォーム、ボタン、履歴エリア）確認（要件1.2, 1.3, 1.4）
  - 空履歴時の表示確認（要件1.6）
  - タイムアウト時の特定メッセージ表示（エッジケース: 要件5.2）
  - 接続不可時の特定メッセージ表示（エッジケース: 要件5.3）

- [ ]* 6.5 ChatController のプロパティテスト
  - `// Feature: mcp-chat-webapp, Property 2: 空・空白メッセージの拒否`
  - 任意の空白のみ文字列でサービスが呼ばれないことを確認（要件2.5）
  - `// Feature: mcp-chat-webapp, Property 3: 応答のChatSessionへの追加`
  - 任意のメッセージ/応答ペアがChatSessionに正しく追加されることを確認（要件2.3）
  - `// Feature: mcp-chat-webapp, Property 7: 例外発生時のエラー表示とフォーム維持`
  - 任意の例外に対してエラー表示とフォーム維持を確認（要件5.1, 5.5）

- [ ] 6.6 McpClientService のユニットテスト
  - 起動時のinitializeリクエスト送信確認（要件4.2）
  - 接続失敗時の例外スロー確認（エッジケース: 要件4.5）
  - エラーレスポンス時の例外スロー確認（エッジケース: 要件4.6）

- [ ]* 6.7 McpClientService のプロパティテスト
  - `// Feature: mcp-chat-webapp, Property 5: JSON-RPC 2.0形式のリクエスト送信`
  - 任意のメッセージに対してJSON-RPC 2.0形式のリクエストが生成されることを確認（要件4.1, 4.3）
  - `// Feature: mcp-chat-webapp, Property 6: MCPレスポンスのテキスト変換`
  - 任意のMCPレスポンスに対してcontent[].textが正しく結合されることを確認（要件4.4）

- [ ]* 6.8 Thymeleafテンプレートのプロパティテスト
  - `// Feature: mcp-chat-webapp, Property 4: ユーザーメッセージとボット応答の視覚的区別`
  - 任意のChatMessageリストに対してユーザー/ボットが異なるCSSクラスで表示されることを確認（要件2.4, 7.5）

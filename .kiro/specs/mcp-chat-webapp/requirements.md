# 要件定義書

## はじめに

本機能は、Spring Boot（4.0.3）、Java 25、Thymeleaf、Spring MVCで構築されたWebアプリケーションにおいて、ユーザーがチャット画面からメッセージを入力し、設定済みのMCPサーバー（Model Context Protocol）に接続して応答を受け取り、結果を画面に表示する機能を提供する。

## 用語集

- **MCPサーバー**: Model Context Protocolに準拠したサーバー。AIモデルやツールへのアクセスを標準化されたプロトコルで提供する
- **MCPクライアント**: MCPサーバーに接続し、ツール呼び出しやリソース取得を行うクライアントコンポーネント
- **チャット画面**: ユーザーがメッセージを入力し、MCPサーバーからの応答を確認するWebUI
- **メッセージ**: ユーザーがチャット画面に入力するテキスト
- **応答**: MCPサーバーが返すテキスト形式の結果
- **会話履歴**: チャットセッション内のメッセージと応答のやり取りの記録
- **ChatController**: チャット画面のHTTPリクエストを処理するSpring MVCコントローラー
- **McpClientService**: MCPサーバーへの接続・通信を担当するサービスクラス
- **ChatSession**: 1ユーザーセッション内の会話履歴を保持するオブジェクト
- **McpConfig**: MCPサーバーの接続設定（URL、タイムアウト等）を保持する設定クラス

---

## 要件

### 要件1: チャット画面の表示

**ユーザーストーリー:** 開発者として、チャット画面にアクセスして会話履歴とメッセージ入力フォームを確認したい。そうすることで、MCPサーバーとのやり取りをブラウザ上で行える。

#### 受け入れ基準

1. THE ChatController SHALL `/chat` エンドポイントへのGETリクエストに対してチャット画面を返す
2. THE チャット画面 SHALL メッセージ入力フォームを表示する
3. THE チャット画面 SHALL 送信ボタンを表示する
4. THE チャット画面 SHALL 会話履歴エリアを表示する
5. WHEN セッションに会話履歴が存在する場合、THE チャット画面 SHALL 過去のメッセージと応答を時系列順に表示する
6. WHEN セッションに会話履歴が存在しない場合、THE チャット画面 SHALL 空の会話履歴エリアを表示する

---

### 要件2: メッセージの送信と応答の表示

**ユーザーストーリー:** 開発者として、チャット画面でメッセージを入力して送信し、MCPサーバーからの応答を確認したい。そうすることで、MCPサーバーの機能をインタラクティブに利用できる。

#### 受け入れ基準

1. WHEN ユーザーがメッセージを入力して送信ボタンを押した場合、THE ChatController SHALL `/chat` エンドポイントへのPOSTリクエストを受け付ける
2. WHEN POSTリクエストを受け付けた場合、THE ChatController SHALL McpClientService を呼び出してMCPサーバーにメッセージを送信する
3. WHEN McpClientService がMCPサーバーから応答を受け取った場合、THE ChatController SHALL 応答をChatSessionに追加してチャット画面を更新する
4. THE チャット画面 SHALL ユーザーのメッセージと対応するMCPサーバーの応答を区別して表示する
5. IF メッセージが空文字列の場合、THEN THE ChatController SHALL メッセージを送信せずにチャット画面を再表示する

---

### 要件3: MCPサーバーへの接続設定

**ユーザーストーリー:** 開発者として、接続先のMCPサーバーをapplication.propertiesで設定したい。そうすることで、環境ごとに接続先を切り替えられる。

#### 受け入れ基準

1. THE McpConfig SHALL `application.properties` の `mcp.server.url` プロパティからMCPサーバーのURLを読み込む
2. THE McpConfig SHALL `application.properties` の `mcp.server.connection-timeout` プロパティから接続タイムアウト値（ミリ秒）を読み込む
3. THE McpConfig SHALL `application.properties` の `mcp.server.read-timeout` プロパティから読み取りタイムアウト値（ミリ秒）を読み込む
4. IF `mcp.server.url` が設定されていない場合、THEN THE アプリケーション SHALL 起動時にエラーをログに出力して起動を中断する
5. WHERE `mcp.server.connection-timeout` が未設定の場合、THE McpConfig SHALL デフォルト値として5000ミリ秒を使用する
6. WHERE `mcp.server.read-timeout` が未設定の場合、THE McpConfig SHALL デフォルト値として30000ミリ秒を使用する

---

### 要件4: MCPサーバーとの通信

**ユーザーストーリー:** 開発者として、アプリケーションがMCPプロトコルに従ってMCPサーバーと通信してほしい。そうすることで、標準化されたプロトコルで様々なMCPサーバーと連携できる。

#### 受け入れ基準

1. THE McpClientService SHALL MCPプロトコル（JSON-RPC 2.0ベース）に従ってMCPサーバーにリクエストを送信する
2. WHEN アプリケーション起動時、THE McpClientService SHALL MCPサーバーに対して初期化（`initialize`）リクエストを送信して接続を確立する
3. WHEN ユーザーメッセージを受け取った場合、THE McpClientService SHALL MCPサーバーの `tools/call` または対応するメソッドを呼び出す
4. THE McpClientService SHALL MCPサーバーからのレスポンスをテキスト形式に変換して返す
5. IF MCPサーバーへの接続が失敗した場合、THEN THE McpClientService SHALL 接続エラーを示す例外をスローする
6. IF MCPサーバーがエラーレスポンスを返した場合、THEN THE McpClientService SHALL エラー内容を含む例外をスローする

---

### 要件5: エラーハンドリング

**ユーザーストーリー:** 開発者として、MCPサーバーとの通信エラーが発生した場合にユーザーに分かりやすいエラーメッセージを表示してほしい。そうすることで、問題発生時にユーザーが状況を把握できる。

#### 受け入れ基準

1. IF McpClientService が例外をスローした場合、THEN THE ChatController SHALL エラーメッセージをチャット画面に表示する
2. IF MCPサーバーへの接続がタイムアウトした場合、THEN THE ChatController SHALL 「MCPサーバーへの接続がタイムアウトしました」というメッセージをチャット画面に表示する
3. IF MCPサーバーが利用不可の場合、THEN THE ChatController SHALL 「MCPサーバーに接続できません。設定を確認してください。」というメッセージをチャット画面に表示する
4. THE ChatController SHALL エラー発生時にスタックトレースをアプリケーションログに出力する
5. WHEN エラーが発生した場合、THE チャット画面 SHALL エラーメッセージを会話履歴エリアに表示してユーザーが引き続きメッセージを送信できる状態を維持する

---

### 要件6: 会話履歴の管理

**ユーザーストーリー:** 開発者として、チャットセッション内の会話履歴がHTTPセッションで管理されてほしい。そうすることで、ページをリロードしても会話の流れが保持される。

#### 受け入れ基準

1. THE ChatSession SHALL HTTPセッションスコープで管理され、ユーザーごとに独立した会話履歴を保持する
2. THE ChatSession SHALL メッセージと応答のペアを送信順に保持する
3. WHEN ユーザーがブラウザセッションを終了した場合、THE ChatSession SHALL 破棄される
4. THE チャット画面 SHALL 会話履歴を新しいメッセージが下に表示されるよう順序付けて表示する
5. WHERE 会話履歴のクリア機能が有効な場合、THE ChatController SHALL `/chat/clear` エンドポイントへのPOSTリクエストで会話履歴をリセットする

---

### 要件7: チャットUIのアクセシビリティと操作性

**ユーザーストーリー:** 開発者として、チャット画面がキーボード操作に対応し、使いやすいUIを提供してほしい。そうすることで、効率よくMCPサーバーとやり取りできる。

#### 受け入れ基準

1. THE チャット画面 SHALL メッセージ入力フォームにフォーカスが当たった状態で表示される
2. WHEN ユーザーが入力フォームでEnterキーを押した場合、THE チャット画面 SHALL メッセージを送信する
3. WHEN メッセージ送信中の場合、THE チャット画面 SHALL 送信ボタンを無効化してユーザーに処理中であることを示す
4. WHEN 新しい応答が追加された場合、THE チャット画面 SHALL 会話履歴エリアを最新メッセージが見えるようにスクロールする
5. THE チャット画面 SHALL ユーザーのメッセージと MCPサーバーの応答を視覚的に区別できるスタイルで表示する

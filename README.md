# SNS API Spring Boot回答コード

SNS教材のJava / Spring Boot + JPAバックエンド回答コードです。Reactフロントエンドは別リポジトリ `curriculum-react-projects-answer` の `apps/sns` を使います。

このリポジトリはローカルで動く回答コードに絞っています。AWSデプロイ、CDK、ECS、RDSなどのインフラ構築は含めません。

## 構成

```text
curriculum-sns-spring-answer/
├── compose.yaml
├── mvnw
├── pom.xml
├── src/main/java/com/example/sns/
└── skills/APP_OVERVIEW.md
```

## ポート

| 役割 | URL |
|---|---|
| API | `http://localhost:8000` |
| Socket.IO | `http://localhost:8001` |
| PostgreSQL | `localhost:5432` |
| React | `http://localhost:5173` |

## 初回セットアップ

```bash
docker compose up -d
./mvnw test
```

## 起動

```bash
./mvnw spring-boot:run
```

Reactフロントエンド側:

```bash
cd ../curriculum-react-projects-answer/apps/sns
pnpm install
cp .env.example .env
# .env の VITE_API_URL を http://localhost:8000 にする
# .env の VITE_SOCKET_URL を http://localhost:8001 にする
pnpm run dev
```

ブラウザで `http://localhost:5173/` を開きます。

## ローカルで確認できる機能

- ユーザー登録
- コンソール出力の確認URLによるメール確認
- ログイン
- 投稿作成、一覧、削除
- いいね、いいね解除
- フォロー、フォロー解除
- フォロー中タイムライン
- プロフィール編集
- DMの会話作成、履歴取得、リアルタイム送信

## リアルタイムチャットについて

現行React SNSフロントは Socket.IO client を使っています。Spring Boot標準のWebSocket/STOMPとはプロトコルが違うため、このSpring版では `netty-socketio` を使ってSocket.IO互換サーバーを別ポートで起動します。

- REST API: `http://localhost:8000`
- Socket.IO: `http://localhost:8001/chat`
- React側 `.env`: `VITE_API_URL="http://localhost:8000"`、`VITE_SOCKET_URL="http://localhost:8001"`

## テスト

```bash
./mvnw test
```

テストではH2のインメモリDBを使います。ローカル開発ではDocker ComposeのPostgreSQLを使います。

## 開発メモ

- デプロイやインフラ構築はこのリポジトリでは扱いません。
- DBだけDocker Composeで起動し、APIはローカルのSpring Bootで実行します。
- React互換を優先し、ログイン成功時は `{ "accessToken": "..." }` を返します。
- `SOCKET_IO_ENABLED=false` を設定するとSocket.IOサーバーだけ無効化できます。

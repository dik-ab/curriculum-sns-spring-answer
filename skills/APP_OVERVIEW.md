# SNS Spring Bootバックエンドの概要

このリポジトリは、SNS教材のSpring Boot + JPA回答コードです。

## 機能

- 認証: 登録、メール確認、ログイン、`GET /auth/me`
- 投稿: 作成、一覧、削除
- いいね: 追加、解除
- フォロー: フォロー、解除、フォロー中タイムライン
- プロフィール: 表示名、自己紹介、アバターURL更新
- DM: RESTでの会話作成、履歴取得、Socket.IOでのリアルタイム送信

## 技術構成

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Spring Security crypto（BCrypt）
- netty-socketio（ReactのSocket.IO client互換）
- PostgreSQL 16（Docker Compose）
- H2（テスト用）

## 現行Reactとの接続

React側は `Authorization: Bearer <token>` を送るため、このSpring版もログイン時に `accessToken` を返します。トークンは `session_tokens` テーブルに保存します。

## リアルタイム

REST APIは `8000`、Socket.IOは `8001` で起動します。React側は `VITE_API_URL` と `VITE_SOCKET_URL` を分けて設定します。

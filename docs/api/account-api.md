# Account Service API 契約（v0.1）

## 基本資訊

* Base URL（預設）: `/api`
* Media Type: `application/json; charset=utf-8`
* 語言/時區: `UTC`，時間格式 ISO 8601（例：`2025-08-08T07:00:00Z`）
* 認證: Bearer JWT（除註冊、登入外皆需）
* 版本：v0.1（初版草案）

## 共用結構

### Error Object

```json
{
  "error": "validation_error",
  "message": "email is invalid",
  "details": {
    "email": ["must be a valid email"]
  }
}
```

* `error`: 穩定機器可讀碼（snake\_case）
* `message`: 人類可讀文字
* `details`: 欄位級錯誤（可選）

---

## 1) 註冊 Register

* **Method/Path**: `POST /api/account/register`
* **Auth**: 不需要
* **描述**: 建立新帳號

### Request Body

```json
{
  "email": "aer@rewr.com",
  "password": "1234567",
  "username": "asd"
}
```

### 驗證規則

* `email`: 必填、必須為 email 格式、不得已被註冊
* `password`: 必填、長度 ≥ 7（建議 ≥ 8）、可追加複雜度規則
* `username`: 必填、長度 3–32、可重複

### Responses

* `201 Created`

```json
{
  "msg": "register success",
  "user": {
    "id": "usr_01HXYZ...",
    "email": "aer@rewr.com",
    "username": "asd",
    "createdAt": "2025-08-08T07:00:00Z"
  }
}
```

* `400 Bad Request`（驗證不過）

```json
{
  "error": "validation_error",
  "message": "invalid fields",
  "details": {"email": ["must be a valid email"]}
}
```

* `409 Conflict`（email/username 已存在）

```json
{"error":"conflict","message":"email already registered"}
```

---

## 2) 登入 Login

* **Method/Path**: `POST /api/account/login`
* **Auth**: 不需要
* **描述**: 以 email + password 取得 JWT（與 refresh token）

### Request Body

```json
{
  "email": "aer@rewr.com",
  "password": "1234567"
}
```

### 驗證規則

* `email`: 必填、email 格式
* `password`: 必填

### Responses

* `200 OK`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "rft_01HXYZ...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "usr_01HXYZ...",
    "email": "aer@rewr.com",
    "username": "asd"
  }
}
```

* `401 Unauthorized`

```json
{"error":"invalid_credentials","message":"email or password is incorrect"}
```

---

## 3) 查詢使用者 Get User

* **Method/Path**: `GET /api/account/users/{id}` 或 `GET /api/account/me`
* **Auth**: 需要（Bearer JWT）
* **描述**: 取得使用者資訊；`/me` 取得自己

### Path Params

* `id`: 使用者 ID（若用 `/me` 則不需）

### Responses

* `200 OK`

```json
{
  "id": "usr_01HXYZ...",
  "email": "aer@rewr.com",
  "username": "asd",
  "createdAt": "2025-08-08T07:00:00Z",
  "updatedAt": "2025-08-08T07:00:00Z"
}
```

* `401 Unauthorized` / `403 Forbidden`（無權）
* `404 Not Found`

```json
{"error":"not_found","message":"user not found"}
```

---

## 認證與安全

* **Authorization**: `Authorization: Bearer <accessToken>`
* Access Token 生命週期：預設 1 小時（`expiresIn`=3600）
* Refresh Token：可交換新 access token，建議獨立撤銷清單
* 建議回應標頭：

  * `WWW-Authenticate`（401）
  * `RateLimit-Remaining` / `RateLimit-Reset`（若有速率限制）

## 狀態碼規範（節錄）

* 200 OK：成功
* 201 Created：建立資源
* 400 Bad Request：輸入錯誤
* 401 Unauthorized：未登入/Token 無效
* 403 Forbidden：已登入但無權限
* 404 Not Found：資源不存在
* 409 Conflict：資源衝突（如 email 已註冊）

## 範例 cURL

```bash
# 註冊
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"aer@rewr.com","password":"1234567","username":"asd"}' \
  http://localhost:8080/api/account/register

# 登入
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"aer@rewr.com","password":"1234567"}' \
  http://localhost:8080/api/account/login

# 查詢自己
curl -H "Authorization: Bearer <ACCESS_TOKEN>" \
  http://localhost:8080/api/account/me


---

## 後續輸出 Swagger（草案）

OpenAPI 3.1 將對應上述路由與結構；會議通過後輸出 `openapi.yaml` 並掛載 Swagger UI（/swagger 或 /docs）。

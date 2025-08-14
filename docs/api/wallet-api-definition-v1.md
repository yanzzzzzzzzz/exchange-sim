# Wallet 服務 API 定義（v1.0.0）

## 1. 簡介

**服務名稱**：wallet
**Base Path**：`/api/wallet`（經由 API Gateway）
**版本策略**：Header `X-Api-Version: 1`
**安全機制**：JWT（RS256 推薦），Scopes/Authorities 見各端點
**通用標頭**：

* `Authorization: Bearer <JWT>`
* `X-Request-Id`（可選；Gateway 自動生成並回傳）
* `Idempotency-Key`（用於非冪等 POST 請求）

**通用錯誤格式**：

```json
{
  "timestamp": "2025-08-13T09:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "ERROR_CODE",
  "message": "詳細錯誤描述",
  "path": "/api/wallet/...",
  "requestId": "c7c1d8a1ad6e..."
}
```

---

## 2. Scopes 與權限

* `SCOPE_wallet.read`：讀取餘額/台帳/資產清單
* `SCOPE_wallet.write`：存提資金、鎖定/解鎖、結算（通常僅系統流程與內部服務使用）

---

## 3. API 端點

### 3.1 查詢資產餘額

**方法與路徑**：`GET /api/wallet/balances`
**Scopes**：`SCOPE_wallet.read`
**描述**：回傳使用者所有或指定資產的 `available/locked/total`。

**查詢參數**

| 名稱    | 類型     | 必填 | 描述                    |
| ----- | ------ | -- | --------------------- |
| asset | string | 否  | 資產代碼（如 `USDT`），不填回傳全部 |

**回應**

```json
[
  {
    "asset": "USDT",
    "available": "1000.00000000",
    "locked": "50.00000000",
    "total": "1050.00000000",
    "updatedAt": "2025-08-13T09:00:00Z"
  }
]
```

---

### 3.2 存入資金（模擬）

**方法與路徑**：`POST /api/wallet/deposit`
**Scopes**：`SCOPE_wallet.write`
**描述**：模擬充幣，增加 `available` 餘額。

**請求體**

```json
{
  "asset": "USDT",
  "amount": "500.00000000"
}
```

**驗證規則**

* `amount > 0`
* `asset` 必須是系統支援的資產

**回應**

```json
{
  "balance": {
    "asset": "USDT",
    "available": "1500.00000000",
    "locked": "50.00000000",
    "total": "1550.00000000"
  }
}
```

---

### 3.3 提領資金（模擬）

**方法與路徑**：`POST /api/wallet/withdraw`
**Scopes**：`SCOPE_wallet.write`
**描述**：模擬提幣，扣除 `available` 餘額。

**請求體**

```json
{
  "asset": "USDT",
  "amount": "200.00000000"
}
```

**驗證規則**

* `amount > 0`
* `available >= amount`

**回應**

```json
{
  "balance": {
    "asset": "USDT",
    "available": "1300.00000000",
    "locked": "50.00000000",
    "total": "1350.00000000"
  }
}
```

---

### 3.4 鎖定資金（下單前）

**方法與路徑**：`POST /api/wallet/lock`
**Scopes**：`SCOPE_wallet.write`
**描述**：下單前鎖定資金，不可用於其他訂單或提領。

**請求體**

```json
{
  "asset": "USDT",
  "amount": "100.00000000",
  "referenceId": "order-uuid-123",
  "reason": "ORDER_LOCK"
}
```

**驗證規則**

* `available >= amount`
* `referenceId` 必填且唯一對應一筆業務（如訂單）

**回應**

```json
{
  "locked": true,
  "balance": {
    "asset": "USDT",
    "available": "1200.00000000",
    "locked": "150.00000000",
    "total": "1350.00000000"
  }
}
```

---

### 3.5 解鎖資金（撤單/失敗）

**方法與路徑**：`POST /api/wallet/unlock`
**Scopes**：`SCOPE_wallet.write`
**描述**：撤單或下單失敗時解鎖資金。

**請求體**

```json
{
  "asset": "USDT",
  "amount": "100.00000000",
  "referenceId": "order-uuid-123",
  "reason": "ORDER_CANCEL"
}
```

**回應**

```json
{
  "unlocked": true,
  "balance": {
    "asset": "USDT",
    "available": "1300.00000000",
    "locked": "50.00000000",
    "total": "1350.00000000"
  }
}
```

---

### 3.6 結算資金（撮合後）

**方法與路徑**：`POST /api/wallet/settle`
**Scopes**：`SCOPE_wallet.write`
**描述**：撮合後將成交結果結算至買賣雙方餘額。

**請求體**

```json
{
  "settlementId": "settle-uuid-123",
  "trades": [
    {
      "buyerId": "user-uuid-buyer",
      "sellerId": "user-uuid-seller",
      "baseAsset": "BTC",
      "quoteAsset": "USDT",
      "price": "30000.00",
      "quantity": "0.1",
      "fee": "3.00",
      "feeAsset": "USDT"
    }
  ]
}
```

**驗證規則**

* `settlementId` 不可重複（去重保護）
* `trades[*].quantity > 0`，`price > 0`
* 依 `buyerId/sellerId` 做資產增減與解鎖剩餘鎖定金額

**回應**

```json
{
  "settled": true,
  "processedTrades": 1
}
```

---

### 3.7 查詢交易台帳（可選）

**方法與路徑**：`GET /api/wallet/ledger`
**Scopes**：`SCOPE_wallet.read`
**描述**：查詢資金異動台帳（double-entry）。

**查詢參數**

| 名稱     | 類型                | 必填 | 描述                 |
| ------ | ----------------- | -- | ------------------ |
| asset  | string            | 否  | 過濾資產               |
| from   | string(date-time) | 否  | 起始時間（ISO8601）      |
| to     | string(date-time) | 否  | 結束時間（ISO8601）      |
| cursor | string            | 否  | 游標分頁               |
| limit  | int               | 否  | 單頁筆數（預設 50，最大 200） |

**回應**

```json
{
  "items": [
    {
      "entryId": "ledg-001",
      "asset": "USDT",
      "amount": "-100.00000000",
      "balanceAfter": "900.00000000",
      "reason": "ORDER_LOCK",
      "referenceId": "order-uuid-123",
      "createdAt": "2025-08-13T09:00:00Z"
    }
  ],
  "nextCursor": "eyJvZmZzZXQiOjEwMH0="
}
```

---

## 4. Kafka 事件（Wallet 範圍）

### 4.1 `wallet.settlements`

**描述**：撮合完成的結算事件；由撮合或結算器發佈，Wallet 消費。

**Key**：`settlementId`（或 `symbol+timestamp`，依分區策略）

**事件內容**

```json
{
  "settlementId": "settle-uuid-123",
  "timestamp": "2025-08-13T09:00:00Z",
  "trades": [
    {
      "buyerId": "user-uuid-buyer",
      "sellerId": "user-uuid-seller",
      "baseAsset": "BTC",
      "quoteAsset": "USDT",
      "price": "30000.00",
      "quantity": "0.1",
      "fee": "3.00",
      "feeAsset": "USDT"
    }
  ]
}
```

---

## 5. 狀態碼與錯誤碼

| HTTP 狀態 | 錯誤碼                           | 描述                     |
| ------- | ----------------------------- | ---------------------- |
| 400     | WALLET\_INVALID\_PARAM        | 請求參數錯誤                 |
| 400     | WALLET\_INSUFFICIENT\_FUNDS   | 可用餘額不足                 |
| 400     | WALLET\_ASSET\_NOT\_SUPPORTED | 不支援的資產                 |
| 401     | AUTH\_INVALID\_TOKEN          | Token 無效或過期            |
| 403     | AUTH\_FORBIDDEN               | 權限不足                   |
| 404     | WALLET\_BALANCE\_NOT\_FOUND   | 查無餘額資料                 |
| 409     | WALLET\_IDEMPOTENCY\_CONFLICT | Idempotency-Key 與內容衝突  |
| 409     | WALLET\_DUPLICATE\_SETTLEMENT | 重複結算（settlementId 已處理） |
| 429     | RATE\_LIMITED                 | 觸發限流                   |
| 500     | WALLET\_INTERNAL\_ERROR       | 內部錯誤                   |

---

## 6. 行為準則（非功能性）

* **Idempotency**：`deposit/withdraw/lock/unlock/settle` 皆需支援 `Idempotency-Key`，重送回第一次結果；內容不同則回 409。
* **一致性**：使用 Outbox Pattern 發佈事件（如有），避免雙寫問題；台帳以 double-entry 設計，借貸恆等。
* **觀測性**：暴露 Prometheus 指標（請求率、錯誤率、p99 latency）、Zipkin/OTel trace，所有請求與事件攜帶 `X-Request-Id` 或 `traceparent`。
* **分頁**：`ledger` 使用游標分頁以避免深度 OFFSET 成本。
* **金額精度**：金額與數量以字串傳遞，內部以整數 + 精度或高精度小數處理（避免浮點誤差）。

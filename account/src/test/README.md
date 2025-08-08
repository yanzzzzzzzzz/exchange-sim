# Account Service 測試說明

本目錄包含 Account Service 的完整測試套件，基於 `docs/api/account-api.md` 中定義的 API 規格。

## 測試結構

### 控制器測試 (Controller Tests)

- `AccountControllerTest.java` - 主要的 API 端點測試
- `AccountControllerWithUtilsTest.java` - 使用測試工具類別的簡化測試

### 整合測試 (Integration Tests)

- `AccountIntegrationTest.java` - 完整的端到端流程測試

### 單元測試 (Unit Tests)

- `ValidationTest.java` - 驗證邏輯的單元測試
- `JwtTokenTest.java` - JWT token 相關功能測試

### 工具類別 (Utilities)

- `TestUtils.java` - 測試用的工具方法和常數

## 測試涵蓋的功能

### 1. 註冊 (Register)

- ✅ 成功註冊新帳號
- ✅ Email 格式驗證
- ✅ 密碼長度驗證 (≥7 字元)
- ✅ Username 長度和格式驗證 (3-32 字元，英數字和 _/-)
- ✅ 重複 email 處理 (409 Conflict)
- ✅ 重複 username 處理

### 2. 登入 (Login)

- ✅ 成功登入並取得 JWT token
- ✅ 錯誤密碼處理 (401 Unauthorized)
- ✅ 不存在帳號處理 (401 Unauthorized)
- ✅ 無效 email 格式處理 (400 Bad Request)
- ✅ Token 格式驗證 (Bearer, expiresIn: 3600)

### 3. 查詢使用者 (Get User)

- ✅ 查詢自己的資訊 (`/api/account/me`)
- ✅ 查詢指定使用者資訊 (`/api/account/users/{id}`)
- ✅ 無 token 存取處理 (401 Unauthorized)
- ✅ 無效 token 處理 (401 Unauthorized)
- ✅ 不存在使用者處理 (404 Not Found)

### 4. 更新暱稱 (Update Nickname)

- ✅ 更新自己的暱稱 (`/api/account/me/nickname`)
- ✅ 更新指定使用者暱稱 (`/api/account/users/{id}/nickname`)
- ✅ 暱稱長度驗證 (1-32 字元)
- ✅ 空暱稱處理 (400 Bad Request)
- ✅ 權限驗證 (401/403)

### 5. 錯誤處理

- ✅ 統一錯誤回應格式
- ✅ 驗證錯誤詳細資訊
- ✅ HTTP 狀態碼正確性
- ✅ 錯誤訊息國際化準備

### 6. 安全性

- ✅ JWT token 生成和驗證
- ✅ Refresh token 機制
- ✅ Authorization header 處理
- ✅ Token 過期處理

## 執行測試

### 執行所有測試

```bash
./gradlew :account:test
```

### 執行特定測試類別

```bash
./gradlew :account:test --tests "AccountControllerTest"
```

### 執行特定測試方法

```bash
./gradlew :account:test --tests "AccountControllerTest.shouldRegisterSuccessfully"
```

### 產生測試報告

```bash
./gradlew :account:test jacocoTestReport
```

## 測試配置

### 測試環境配置

- `application-test.yml` - 測試專用配置
- 使用 H2 記憶體資料庫
- JWT 測試金鑰
- 除錯日誌等級

### 測試資料

- 使用 `TestUtils.TestData` 中定義的常數
- 動態生成唯一的測試資料 (email, username)
- 避免測試間的資料衝突

## 最佳實踐

1. **測試隔離**: 每個測試都是獨立的，不依賴其他測試的狀態
2. **資料清理**: 使用記憶體資料庫，每次測試後自動清理
3. **工具類別**: 使用 `TestUtils` 減少重複程式碼
4. **描述性命名**: 測試方法名稱清楚描述測試目的
5. **邊界值測試**: 測試各種邊界條件和異常情況
6. **完整流程**: 整合測試涵蓋完整的使用者操作流程

## 待實作功能

以下功能的測試已準備好，但需要對應的實作：

- [ ] 實際的 JWT service 實作
- [ ] 資料庫實體和 repository
- [ ] 密碼加密和驗證
- [ ] Refresh token 儲存和管理
- [ ] 使用者權限管理
- [ ] 速率限制測試

## 注意事項

1. 某些測試 (如 `JwtTokenTest`) 目前使用 mock 實作，需要實際的 JWT service
2. 測試中的 token 解析需要完整的 JSON 處理邏輯
3. 實際部署時需要更新測試配置中的安全設定
4. 考慮加入效能測試和負載測試

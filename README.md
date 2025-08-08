# Exchange-Sim Microservices

本專案為 Spring Boot 多模組微服務架構範例，目前包含：

* **gateway**：Spring Cloud Gateway，負責路由、限流、過濾器。
* **account**：使用 WebFlux 實作的帳號服務。
* **common**：共用程式碼與模型。

## 系統需求

* Java 17+
* Gradle 8+
* Docker（本地 Redis 測試用）

## 專案結構

```text
.
├── gateway/       # API Gateway (Spring Cloud Gateway)
│   └── src/main/java/gateway/
├── account/       # Account 微服務 (WebFlux + R2DBC)
│   └── src/main/java/account/
├── common/        # 共用程式碼
├── docs/          # API 文件
└── build.gradle.kts / settings.gradle.kts
```

## 包名結構

所有微服務都採用簡潔的包名結構：

* **Gateway**: `gateway.*`
* **Account**: `account.*` (controller, service, repository, model, config)
* **Common**: `common.*`

## 啟動方式

### 1. 啟動 Redis（限流使用）

```bash
docker run -p 6379:6379 redis
```

### 2. 啟動 Account Service

```bash
./gradlew :account:bootRun
```

啟動完成後，可在 [http://localhost:8081/account/whoami](http://localhost:8081/account/whoami) 測試。

### 3. 啟動 Gateway

```bash
./gradlew :gateway:bootRun
```

Gateway 啟動後，訪問：

```text
http://localhost:8080/api/account/whoami
```

請求會透過 Gateway 轉發到 Account 服務，並套用 RequestRateLimiter。

## 測試限流

以 PowerShell 測試（同步 30 次請求）：

```powershell
for ($i = 1; $i -le 30; $i++) {
    try {
        Invoke-WebRequest -Uri "http://localhost:8080/api/account/whoami" -UseBasicParsing | Out-Null
        Write-Host "$i -> 200"
    } catch {
        Write-Host "$i -> $($_.Exception.Response.StatusCode.value__)"
    }
}
```

當超過 `burstCapacity` 限制後，應會收到 `429`。

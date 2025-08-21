package wallet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class WalletApiTest {

    @Autowired
    WebTestClient webClient;

    @Test
    void balancesEndpointShouldExist() {
    // Initialize wallet by depositing 1000 USDT so balances have a deterministic value
    webClient.post()
        .uri("/api/wallet/deposit")
        .bodyValue("{\"asset\":\"USDT\",\"amount\":\"1000.00000000\"}")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.balance.asset").isEqualTo("USDT")
        .jsonPath("$.balance.available").isEqualTo("1000.00000000")
        .jsonPath("$.balance.locked").isEqualTo("0.00000000");

    // Now query balances and assert the known value is present
    webClient.get()
        .uri("/api/wallet/balances?asset=USDT")
        .exchange()
        .expectStatus().isOk()
        // expected output: JSON array of balance objects per API definition
        .expectBody()
        .jsonPath("$[0].asset").isEqualTo("USDT")
        .jsonPath("$[0].available").isEqualTo("1000.00000000")
        .jsonPath("$[0].locked").isEqualTo("0.00000000")
        .jsonPath("$[0].total").isEqualTo("1000.00000000")
        .jsonPath("$[0].updatedAt").exists();
    }

    @Test
    void depositEndpointShouldExist() {
        webClient.post()
                .uri("/api/wallet/deposit")
                // example deposit input per API doc
                .bodyValue("{\"asset\":\"USDT\",\"amount\":\"500.00000000\"}")
                .exchange()
                .expectStatus().isOk()
                // expected output per API: { "balance": { "asset","available","locked","total" } }
                .expectBody()
                .jsonPath("$.asset").exists()
                .jsonPath("$.amount").exists()
                .jsonPath("$.balance.locked").exists()
                .jsonPath("$.balance.total").exists();
    }

    @Test
    void withdrawEndpointShouldExist() {
        webClient.post()
                .uri("/api/wallet/withdraw")
                // example withdraw input per API doc
                .bodyValue("{\"asset\":\"USDT\",\"amount\":\"200.00000000\"}")
                .exchange()
                .expectStatus().isOk()
                // expected output per API: { "balance": { ... } }
                .expectBody()
                .jsonPath("$.balance.asset").exists()
                .jsonPath("$.balance.available").exists()
                .jsonPath("$.balance.locked").exists()
                .jsonPath("$.balance.total").exists();
    }

    @Test
    void ledgerEndpointShouldExist() {
        webClient.get()
                // request with example query parameters
                .uri("/api/wallet/ledger?asset=USDT&limit=10")
                .exchange()
                .expectStatus().isOk()
                // expected output: JSON array (ledger entries)
                .expectBody()
                .jsonPath("$.items[0].entryId").exists()
                .jsonPath("$.items[0].asset").exists()
                .jsonPath("$.items[0].amount").exists()
                .jsonPath("$.items[0].balanceAfter").exists()
                .jsonPath("$.items[0].createdAt").exists();
    }

    @Test
    void lockEndpointShouldExist() {
        webClient.post()
                .uri("/api/wallet/lock")
                // example lock request
                .bodyValue("{\"asset\":\"USDT\",\"amount\":\"100.00000000\",\"referenceId\":\"order-uuid-123\",\"reason\":\"ORDER_LOCK\"}")
                .exchange()
                .expectStatus().isOk()
                // expected output per API: { "locked": true, "balance": { ... } }
                .expectBody()
                .jsonPath("$.locked").isEqualTo(true)
                .jsonPath("$.balance.asset").exists()
                .jsonPath("$.balance.available").exists()
                .jsonPath("$.balance.locked").exists()
                .jsonPath("$.balance.total").exists();
    }

    @Test
    void unlockEndpointShouldExist() {
        webClient.post()
                .uri("/api/wallet/unlock")
                // example unlock request
                .bodyValue("{\"asset\":\"USDT\",\"amount\":\"100.00000000\",\"referenceId\":\"order-uuid-123\",\"reason\":\"ORDER_CANCEL\"}")
                .exchange()
                .expectStatus().isOk()
                // expected output per API: { "unlocked": true, "balance": { ... } }
                .expectBody()
                .jsonPath("$.unlocked").isEqualTo(true)
                .jsonPath("$.balance.asset").exists()
                .jsonPath("$.balance.available").exists()
                .jsonPath("$.balance.locked").exists()
                .jsonPath("$.balance.total").exists();
    }

    @Test
    void settleEndpointShouldExist() {
        webClient.post()
                .uri("/api/wallet/settle")
                // example settlement payload
                .bodyValue("{\"settlementId\":\"settle-uuid-123\",\"trades\":[{\"buyerId\":\"user-uuid-buyer\",\"sellerId\":\"user-uuid-seller\",\"baseAsset\":\"BTC\",\"quoteAsset\":\"USDT\",\"price\":\"30000.00\",\"quantity\":\"0.1\",\"fee\":\"3.00\",\"feeAsset\":\"USDT\"}]}")
                .exchange()
                .expectStatus().isOk()
                // expected output per API: { "settled": true, "processedTrades": <int> }
                .expectBody()
                .jsonPath("$.settled").isEqualTo(true)
                .jsonPath("$.processedTrades").exists();
    }
}

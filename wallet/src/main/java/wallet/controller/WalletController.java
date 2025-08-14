package wallet.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/wallet")
public class WalletController {

  @GetMapping("/ping")
  public Mono<String> ping() {
    return Mono.just("wallet service is running");
  }
}
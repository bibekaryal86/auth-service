package user.management.system.app.util;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Slf4j
@NoArgsConstructor
public class InterceptorLoggingUtilsOutgoing implements ExchangeFilterFunction {

  @NotNull
  @Override
  public Mono<ClientResponse> filter(
      @NotNull final ClientRequest request, @NotNull final ExchangeFunction next) {
    log.info("Request to [{}] [{}]", request.method(), request.url());
    return next.exchange(request)
        .doOnNext(response -> log.info("Response from {}", response.statusCode()));
  }
}

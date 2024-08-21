package user.management.system.app.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;
import user.management.system.app.util.InterceptorLoggingUtilsOutgoing;

@Configuration
public class WebClientConfig {

  @Bean("webClient")
  public WebClient webClient() {
    TcpClient tcpClient =
        TcpClient.create(ConnectionProvider.create("fixed"))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .doOnConnected(
                connection ->
                    connection
                        .addHandlerLast(new ReadTimeoutHandler(15))
                        .addHandlerLast(new WriteTimeoutHandler(15)));

    HttpClient httpClient = HttpClient.from(tcpClient);

    ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

    return WebClient.builder()
        .clientConnector(connector)
        .exchangeStrategies(
            ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build())
        .filter(new InterceptorLoggingUtilsOutgoing())
        .build();
  }
}
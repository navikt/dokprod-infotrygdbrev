package no.nav.dokprod_infotrygdbrev.bdok100.config;

import no.nav.dokprod_infotrygdbrev.consumer.SakRestConsumer;
import no.nav.dokprod_infotrygdbrev.consumer.SakService;
import no.nav.dokprod_infotrygdbrev.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokprod_infotrygdbrev.consumer.texas.NaisTexasConsumer;
import no.nav.dokprod_infotrygdbrev.consumer.texas.NaisTexasRequestInterceptor;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import static org.apache.hc.core5.util.Timeout.ofSeconds;

/**
 * Spring configuration for the consumer layer
 */
@Configuration
@Import({LokalCacheConfig.class,
		NaisTexasConsumer.class,
        PdlGraphQLConsumer.class,
        SakRestConsumer.class,
        SakService.class})
@EnableConfigurationProperties({DokprodInfotrygdbrevProperties.class, NaisProperties.class})
public class ConsumerConfig {

	@Bean
	RestClient texasAuthorizedRestClient(ClientHttpRequestFactory clientHttpRequestFactory, NaisTexasConsumer naisTexasConsumer) {
		return RestClient.builder()
			.requestFactory(clientHttpRequestFactory)
			.requestInterceptor(new NaisTexasRequestInterceptor(naisTexasConsumer))
			.build();
	}

	/**
	 * Explicit {@link RestClient.Builder} bean: not provided by Spring Boot's
	 * {@code RestClientAutoConfiguration} here, since {@code BatchTestConfig} bypasses
	 * auto-configuration (explicit {@code classes} in {@code @SpringBootTest}).
	 */
	@Bean
	RestClient.Builder restClientBuilder(ClientHttpRequestFactory clientHttpRequestFactory) {
		return RestClient.builder().requestFactory(clientHttpRequestFactory);
	}

	@Bean
	ClientHttpRequestFactory clientHttpRequestFactory(HttpClient httpClient) {
		return new HttpComponentsClientHttpRequestFactory(httpClient);
	}

	@Bean
	HttpClient httpClient() {
		var socketConfig = SocketConfig.custom().setSoTimeout(ofSeconds(20)).build();
		var connectionConfig = ConnectionConfig.custom().setConnectTimeout(ofSeconds(5)).build();
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(400);
		connectionManager.setDefaultMaxPerRoute(100);
		connectionManager.setDefaultSocketConfig(socketConfig);
		connectionManager.setDefaultConnectionConfig(connectionConfig);

		return HttpClients.custom()
			.setConnectionManager(connectionManager)
			.build();
	}
}

package no.nav.dokprod_infotrygdbrev.consumer;

import no.nav.dokprod_infotrygdbrev.bdok100.config.DokprodInfotrygdbrevProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

import static no.nav.dokprod_infotrygdbrev.consumer.texas.NaisTexasRequestInterceptor.TARGET_SCOPE;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Sak REST implementasjon.
 *
 */
@Component
public class SakRestConsumer implements SakConsumer {
    private static final String HEADER_SAK_CORRELATION_ID = "X-Correlation-ID";

    private final RestClient restClient;
    private final FinnArkivSakerQueryParamMapper finnArkivSakerQueryParamMapper;
	private final String sakTargetScope;

    public SakRestConsumer(RestClient texasAuthorizedRestClient,
						   DokprodInfotrygdbrevProperties dokprodInfotrygdbrevProperties) {
		this.restClient = texasAuthorizedRestClient.mutate()
			.baseUrl(dokprodInfotrygdbrevProperties.endpoints().sak().url())
			.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
			.defaultHeader(ACCEPT, APPLICATION_JSON_VALUE)
			.build();
		this.sakTargetScope = dokprodInfotrygdbrevProperties.endpoints().sak().scope();
        this.finnArkivSakerQueryParamMapper = new FinnArkivSakerQueryParamMapper();
    }

    @Retryable(include = HttpServerErrorException.class)
    @Override
    public String finnArkivsakId(FinnArkivsakIdTo to) {
        try {
			final MultiValueMap<String, String> queryParams = finnArkivSakerQueryParamMapper.map(to);
			List<SakDto> sakerDto = restClient.get()
				.uri(uriBuilder ->
					uriBuilder.queryParams(queryParams).build())
				.header(HEADER_SAK_CORRELATION_ID, UUID.randomUUID().toString())
				.attribute(TARGET_SCOPE, sakTargetScope)
				.retrieve()
				// errrorhandling?
				.body(new ParameterizedTypeReference<>() {
				});
            if (sakerDto == null || sakerDto.isEmpty()) {
                return null;
            } else {
                return sakerDto.getFirst().getId();
            }
        } catch (HttpClientErrorException e) {
            throw new SakFunctionalException("Kunne ikke finne sak=" + to + ", status=" + e.getStatusCode(), e);
        }
    }

    @Override
    public String opprettArkivsak(OpprettArkivsakTo to) {
        try {
            final OpprettResponseTo opprettResponseTo = restClient.post()
				.header(HEADER_SAK_CORRELATION_ID, UUID.randomUUID().toString())
				.attribute(TARGET_SCOPE, sakTargetScope)
				.body(to)
				.exchange((request, response) -> {
					try (response) {
						if (OK == response.getStatusCode()) {
							return response.bodyTo(OpprettResponseTo.class);
						} else if (response.getStatusCode().is4xxClientError() && !NOT_FOUND.equals(response.getStatusCode())) {
							throw new RuntimeException("Kall mot sak feilet med status " +
								response.getStatusCode() + " " + response.getStatusText());
						} else {
							throw new RuntimeException("kall mot sak fikk uventet status " +
								response.getStatusCode() + " " + response.getStatusText());
						}
					}
				});
            return opprettResponseTo.getId();
        } catch (HttpClientErrorException e) {
            throw new SakFunctionalException("Kunne ikke finne sak=" + to + ", status=" + e.getStatusCode(), e);
        }
    }
}

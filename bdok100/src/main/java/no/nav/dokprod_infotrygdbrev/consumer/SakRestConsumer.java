package no.nav.dokprod_infotrygdbrev.consumer;

import no.nav.dokprod_infotrygdbrev.consumer.sts.StsConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

/**
 * Sak REST implementasjon.
 *
 */
@Component
public class SakRestConsumer implements SakConsumer {
    private static final String HEADER_SAK_CORRELATION_ID = "X-Correlation-ID";

    private final RestTemplate restTemplate;
    private final StsConsumer stsConsumer;
    private final String sakUrl;
    private final FinnArkivSakerQueryParamMapper finnArkivSakerQueryParamMapper;

    public SakRestConsumer(RestTemplate restTemplate,
						   StsConsumer stsConsumer,
						   @Value("${sak.url}") String sakUrl) {
        this.restTemplate = restTemplate;
        this.stsConsumer = stsConsumer;
        this.sakUrl = sakUrl;
        this.finnArkivSakerQueryParamMapper = new FinnArkivSakerQueryParamMapper();
    }

    @Retryable(include = HttpServerErrorException.class)
    @Override
    public String finnArkivsakId(FinnArkivsakIdTo to) {
        try {
            final MultiValueMap<String, String> queryParams = finnArkivSakerQueryParamMapper.map(to);
            final UriComponents uri = UriComponentsBuilder.fromUriString(sakUrl)
                    .queryParams(queryParams)
                    .build();

            final RequestEntity<Void> requestEntity = RequestEntity.get(uri.toUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, getToken())
                    .header(HEADER_SAK_CORRELATION_ID, UUID.randomUUID().toString())
                    .build();
            final List<SakDto> sakerDto = requireNonNull(restTemplate.exchange(requestEntity, new ParameterizedTypeReference<List<SakDto>>() {
            }).getBody());
            if (sakerDto.isEmpty()) {
                return null;
            } else {
                return sakerDto.get(0).getId();
            }
        } catch (HttpClientErrorException e) {
            throw new SakFunctionalException("Kunne ikke finne sak=" + to + ", status=" + e.getStatusCode(), e);
        }
    }

    @Override
    public String opprettArkivsak(OpprettArkivsakTo to) {
        try {
            final UriComponents uri = UriComponentsBuilder.fromUriString(sakUrl).build();
            final RequestEntity<OpprettArkivsakTo> requestEntity = RequestEntity.post(uri.toUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, getToken())
                    .header(HEADER_SAK_CORRELATION_ID, UUID.randomUUID().toString())
                    .body(to);
            final OpprettResponseTo sakerDto = requireNonNull(restTemplate.exchange(requestEntity, OpprettResponseTo.class).getBody());
            return sakerDto.getId();
        } catch (HttpClientErrorException e) {
            throw new SakFunctionalException("Kunne ikke finne sak=" + to + ", status=" + e.getStatusCode(), e);
        }
    }

    private String getToken() {
        return "Bearer " + stsConsumer.getStsToken().getAccess_token();
    }
}

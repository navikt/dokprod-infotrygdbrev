package no.nav.dokprod_infotrygdbrev.consumer.sts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collections;

import static no.nav.dokprod_infotrygdbrev.bdok100.config.LokalCacheConfig.STS_CACHE;


@Component
public class StsRestConsumer implements StsConsumer {
    private static final String URL_ENCODED_BODY = "grant_type=client_credentials&scope=openid";

    private final RestTemplate restTemplate;
    private final String stsTokenUrl;
    private final String username;
    private final String password;

    @Autowired
    public StsRestConsumer(RestTemplate restTemplate,
                           @Value("${securitytokenservice.url}") String stsTokenUrl,
                           @Value("${no.nav.modig.security.systemuser.username}") String username,
                           @Value("${no.nav.modig.security.systemuser.password}") String password) {
        this.restTemplate = restTemplate;
        this.stsTokenUrl = stsTokenUrl;
        this.username = username;
        this.password = password;
    }

    @Retryable(include = StsException.class)
    @Cacheable(STS_CACHE)
    @Override
    public StsResponse getStsToken() {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(URL_ENCODED_BODY, headers);

            return restTemplate.exchange(stsTokenUrl, HttpMethod.POST, requestEntity, StsResponse.class)
                    .getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new StsException(String.format("Klarte ikke hente token fra STS. Feilet med httpstatus=%s. Feilmelding=%s", e.getStatusCode(), e.getMessage()), e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(username, password);
        return headers;
    }
}
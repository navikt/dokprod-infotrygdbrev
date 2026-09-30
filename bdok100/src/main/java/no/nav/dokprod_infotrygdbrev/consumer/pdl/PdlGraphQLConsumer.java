package no.nav.dokprod_infotrygdbrev.consumer.pdl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * https://navikt.github.io/pdl
 */
@Component
public class PdlGraphQLConsumer implements PdlConsumer {
    private final RestTemplate restTemplate;
    private final String pdlUrl;

    @Autowired
    public PdlGraphQLConsumer(RestTemplate restTemplate,
							  @Value("${pdl.url}") String pdlUrl) {
        this.restTemplate = restTemplate;
		this.pdlUrl = pdlUrl;
    }

    @Retryable(include = HttpServerErrorException.class)
    @Override
    public String hentAktoerIdForIdent(final String ident) {
        try {
            final UriComponents uri = UriComponentsBuilder.fromUriString(pdlUrl).build();
            final String serviceuserToken = "Bearer " + "TODO FIXME"; // + dokAuraProxyConsumer.getEntraIdTokenViaDokAuraProxy().getAccess_token();
            final RequestEntity<PdlRequestTo> requestEntity = RequestEntity.post(uri.toUri())
                    .accept(APPLICATION_JSON)
                    .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, serviceuserToken)
                    .body(mapRequest(ident));
            final PdlResponseTo pdlResponseTo = requireNonNull(restTemplate.exchange(requestEntity, PdlResponseTo.class).getBody());

            if(pdlResponseTo.getErrors() == null || pdlResponseTo.getErrors().isEmpty()) {
                return pdlResponseTo.getData().getHentIdenter().getIdenter().get(0).getIdent();
            } else {
                throw new PdlFunctionalException("Kunne ikke hente identer for ident i pdl. " + pdlResponseTo.getErrors());
            }
        } catch (HttpClientErrorException e) {
            throw new PdlFunctionalException("Kunne ikke hente identer for ident i pdl.", e);
        }
    }

    private PdlRequestTo mapRequest(final String ident) {
        final HashMap<String, Object> variables = new HashMap<>();
        variables.put("ident", ident);
        return PdlRequestTo.builder()
                .query("query hentIdenter($ident: ID!) {hentIdenter(ident: $ident, grupper: AKTORID, historikk: false) {identer { ident gruppe historisk } } }")
                .variables(variables)
                .build();
    }
}

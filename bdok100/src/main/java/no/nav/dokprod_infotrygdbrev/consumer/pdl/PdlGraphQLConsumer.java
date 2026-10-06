package no.nav.dokprod_infotrygdbrev.consumer.pdl;

import no.nav.dokprod_infotrygdbrev.bdok100.config.DokprodInfotrygdbrevProperties;
import no.nav.dokprod_infotrygdbrev.consumer.texas.ExplicitTargetScopeNaisTexasRequestInterceptor;
import no.nav.dokprod_infotrygdbrev.consumer.texas.NaisTexasConsumer;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import org.springframework.beans.factory.annotation.Autowired;

import static no.nav.dokprod_infotrygdbrev.consumer.texas.NaisTexasRequestInterceptor.TARGET_SCOPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * https://navikt.github.io/pdl
 */
@Component
public class PdlGraphQLConsumer implements PdlConsumer {
	private static final String HENT_IDENTER_QUERY = """
		query hentIdenter($ident: ID!) {
			hentIdenter(ident: $ident, grupper: AKTORID, historikk: false) {
				identer {
					ident
					gruppe
					historisk
				}
			}
		}
		""";

	private final HttpSyncGraphQlClient graphQlClient;
	private final String pdlScope;

    @Autowired
    public PdlGraphQLConsumer(RestClient texasAuthorizedRestClient,
							  DokprodInfotrygdbrevProperties dokprodInfotrygdbrevProperties,
							  NaisTexasConsumer naisTexasConsumer) {
		this.pdlScope = dokprodInfotrygdbrevProperties.endpoints().pdl().scope();
		this.graphQlClient = HttpSyncGraphQlClient.builder(
				texasAuthorizedRestClient.mutate()
					.baseUrl(dokprodInfotrygdbrevProperties.endpoints().pdl().url())
					.defaultHeaders((headers) -> {
						headers.setContentType(APPLICATION_JSON);
					})
					// attribute-feltet er av en eller annen grunn ikke
					// tilgjengelig i HttpRequest når nais-texas-interceptoren
					// slår inn. Dette skyldes at attributes ikke blir sendt
					// videre til restclient når man bruker
					// HttpSyncGraphQlTransport. En fiks har blitt gjort i
					// spring-graphql nå, som går ut i release 2.0.6. Inntil
					// videre må interceptoren allerede vite hvilket scope som
					// skal brukes. Dette kan fjernes når spring-graphql er
					// oppdatert til versjon 2.0.6
					.requestInterceptor(new ExplicitTargetScopeNaisTexasRequestInterceptor(naisTexasConsumer,
						dokprodInfotrygdbrevProperties.endpoints().pdl().scope()))
					.build()
			)
			.build();
    }

    @Retryable(include = HttpServerErrorException.class)
    @Override
    public String hentAktoerIdForIdent(final String ident) {
        try {
	        ClientGraphQlResponse graphQlResponse = graphQlClient
				.document(HENT_IDENTER_QUERY)
				.variable("ident", ident)
				.attribute(TARGET_SCOPE, pdlScope)
				.executeSync();

            if(graphQlResponse.getErrors().isEmpty()) {
				PdlResponseTo pdlResponseTo = graphQlResponse.toEntity(PdlResponseTo.class);
                return pdlResponseTo.getData().getHentIdenter().getIdenter().get(0).getIdent();
            } else {
                throw new PdlFunctionalException("Kunne ikke hente identer for ident i pdl. " + graphQlResponse.getErrors());
            }
        } catch (HttpClientErrorException e) {
            throw new PdlFunctionalException("Kunne ikke hente identer for ident i pdl.", e);
        }
    }

}

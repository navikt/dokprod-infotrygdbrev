package no.nav.dokprod_infotrygdbrev.consumer.texas;

import com.fasterxml.jackson.annotation.JsonProperty;

record NaisTexasToken(@JsonProperty("access_token") String accessToken) {
}

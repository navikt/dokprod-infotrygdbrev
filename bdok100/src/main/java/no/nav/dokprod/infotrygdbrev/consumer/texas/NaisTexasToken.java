package no.nav.dokprod.infotrygdbrev.consumer.texas;

import com.fasterxml.jackson.annotation.JsonProperty;

record NaisTexasToken(@JsonProperty("access_token") String accessToken) {
}

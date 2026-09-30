package no.nav.dokprod_infotrygdbrev.consumer.sts;

import lombok.Getter;

@Getter
public class StsRequest {
    private final String grant_type = "client_credentials";
    private final String scope = "openid";
}
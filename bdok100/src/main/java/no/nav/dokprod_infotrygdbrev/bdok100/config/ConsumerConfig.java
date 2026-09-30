package no.nav.dokprod_infotrygdbrev.bdok100.config;

import no.nav.dokprod_infotrygdbrev.consumer.SakRestConsumer;
import no.nav.dokprod_infotrygdbrev.consumer.SakService;
import no.nav.dokprod_infotrygdbrev.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokprod_infotrygdbrev.consumer.sts.StsRestConsumer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Spring configuration for the consumer layer
 */
@Configuration
@Import({LokalCacheConfig.class,
        RestTemplateConfig.class,
        StsRestConsumer.class,
        PdlGraphQLConsumer.class,
        SakRestConsumer.class,
        SakService.class})
public class ConsumerConfig {
}

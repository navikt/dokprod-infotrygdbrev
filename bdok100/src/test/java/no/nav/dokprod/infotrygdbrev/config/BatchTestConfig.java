package no.nav.dokprod.infotrygdbrev.config;

import no.nav.dokprod.infotrygdbrev.bdok100.config.BatchCommonConfig;
import no.nav.dokprod.infotrygdbrev.bdok100.config.Bdok100Config;
import no.nav.dokprod.infotrygdbrev.bdok100.config.Bdok100BatchParameters;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({BatchCommonConfig.class,
		RepositoryTestConfig.class,
		Bdok100Config.class,
		JmsProviderTestConfig.class,
		JmsConsumerTestConfig.class,
		ConsumerTestConfig.class
})
@ComponentScan("no.nav.dokprod.infotrygdbrev.bdok100.support.to")
public class BatchTestConfig {

	@Bean(destroyMethod = "close")
	public Bdok100TestWorkspace bdok100TestWorkspace() throws Exception {
		return new Bdok100TestWorkspace();
	}

	@Bean
	@Primary
	public Bdok100BatchParameters testBdok100BatchParameters(Bdok100TestWorkspace workspace) {
		return new Bdok100BatchParameters(
				workspace.input(),
				workspace.staticInput(),
				workspace.output(),
				workspace.failed(),
				workspace.behandlet(),
				2,
				10);
	}
}

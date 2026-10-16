package no.nav.dokprod.infotrygdbrev.config;

import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.brevogarkiv.batch.common.validator.CommonJobParametersValidator;
import no.nav.dokprod.infotrygdbrev.bdok100.config.BatchCommonConfig;
import no.nav.dokprod.infotrygdbrev.bdok100.config.Bdok100Config;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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
	/**
	 * Override startTimeParameter to allow milliseconds, needed in tests.
	 */
	@Bean
	public CommonJobParametersValidator.StringDateJobParameter startTimeParameter() {
		return new CommonJobParametersValidator
				.StringDateJobParameter(CommonBatchInputParameters.START_TIME_KEY, "dd.MM.yyyy-HH:mm:ss:SS");
	}
}

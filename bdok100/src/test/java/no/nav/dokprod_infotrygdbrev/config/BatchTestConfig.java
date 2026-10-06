package no.nav.dokprod_infotrygdbrev.config;

import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.brevogarkiv.batch.common.validator.CommonJobParametersValidator;
import no.nav.dokprod_infotrygdbrev.bdok100.config.BatchCommonConfig;
import no.nav.dokprod_infotrygdbrev.bdok100.config.Bdok100Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({BatchCommonConfig.class,
		RepositoryTestConfig.class,
		Bdok100Config.class,
		JmsProviderTestConfig.class,
		JmsConsumerTestConfig.class,
		ConsumerTestConfig.class
})
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

package no.nav.dokprod_infotrygdbrev.bdok100.config;

import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.brevogarkiv.batch.common.validator.CommonJobParametersValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Production-only beans that have an itest counterpart ({@code BatchTestConfig}).
 * Discovered via component scan in the running application; never imported by test configuration,
 * so no {@code @Profile} separation is required.
 */
@Configuration
public class BatchProdConfig {

	@Bean
	public CommonJobParametersValidator.StringDateJobParameter startTimeParameter() {
		return new CommonJobParametersValidator
				.StringDateJobParameter(CommonBatchInputParameters.START_TIME_KEY, "dd.MM.yyyy-HH:mm:ss");
	}
}

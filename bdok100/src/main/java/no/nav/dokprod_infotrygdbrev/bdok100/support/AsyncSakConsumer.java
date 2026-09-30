package no.nav.dokprod_infotrygdbrev.bdok100.support;

import lombok.SneakyThrows;
import no.nav.dokprod_infotrygdbrev.consumer.FinnEllerOpprettSakTo;
import no.nav.dokprod_infotrygdbrev.consumer.SakService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.concurrent.Future;

/**
 * Async caller for {@link SakService#finnEllerOpprettArkivsak(FinnEllerOpprettSakTo)} with retries on failure
 *
 */
@Async(value = "gsakAsyncExecutor")
public class AsyncSakConsumer {

	private int retries = 1;
	private SakService sakService;
	private long retryDelay = 300L;

	public Future<String> finnEllerOpprettSakWithCache(FinnEllerOpprettSakTo to) {
		String sakId = null;
		int retriesLeft = retries;
		do {
			try {
				sakId = sakService.finnEllerOpprettArkivsak(to);
			} catch (Exception e) {
				if (retriesLeft-- <= 0) {
					throw e;
				}
				sleep();
			}
		} while (sakId == null);
		return new AsyncResult<>(sakId);
	}

	@SneakyThrows
	private void sleep() {
		Thread.sleep(retryDelay);
	}

	public void setSakService(SakService sakService) {
		this.sakService = sakService;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public void setRetryDelay(long retryDelay) {
		this.retryDelay = retryDelay;
	}
}

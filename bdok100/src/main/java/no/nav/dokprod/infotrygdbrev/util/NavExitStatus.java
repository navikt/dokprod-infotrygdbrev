package no.nav.dokprod.infotrygdbrev.util;

import org.springframework.batch.core.ExitStatus;

public final class NavExitStatus {
	private NavExitStatus() {
	}

	/** The batch executed, but with warnings. */
	public static final ExitStatus WARNING = new ExitStatus("WARNING");

	/** The batch failed. */
	public static final ExitStatus ERROR = new ExitStatus("ERROR");
}

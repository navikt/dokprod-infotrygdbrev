package no.nav.dokprod_infotrygdbrev.bdok100.support.to;

import lombok.Builder;
import lombok.Data;

/**
 * Object used for sql update
 */
@Data
@Builder
public class UpdateArbTbl {
	private String onDemandId;
	private String journalforingsfil;
	private String status;
	private String updateStatus;
	private String feilStatus;
}

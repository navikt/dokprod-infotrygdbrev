package no.nav.dokprod_infotrygdbrev.consumer;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import no.nav.dokprod_infotrygdbrev.bdok100.kodeverk.BestillendeFagsystemCode;
import no.nav.dokprod_infotrygdbrev.bdok100.kodeverk.FagomradeCode;

@Data
@Builder
public class FinnEllerOpprettSakTo {

	public enum BrukerType {PERSON, ORGANISASJON}

	@Builder.Default
	private String sakstype = "MFS";
	private FagomradeCode fagomraade;
	private BestillendeFagsystemCode fagsystem;
	private String fagsystemSakId;
	@ToString.Exclude
	private String brukerId;
	private BrukerType brukerType;
}

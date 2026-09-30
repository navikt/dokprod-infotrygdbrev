package no.nav.dokprod_infotrygdbrev.bdok100.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Vedleggsoversikt BDOK100
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VedleggsListe {

	@NotNull
	@Pattern(regexp = "^\\w{4}$")
	private String brevkode;

	@Valid
	private List<Vedlegg> vedleggs = new ArrayList<>();

	public VedleggsListe addVedlegg(String vedleggsKode) {
		vedleggs.add(new Vedlegg(vedleggsKode));
		return this;
	}

	public void removeAllVedlegg() {
		vedleggs.clear();
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Vedlegg {

		@Pattern(regexp = "^[ -~]{1,12}")
		private String vedleggsKode;
	}


}


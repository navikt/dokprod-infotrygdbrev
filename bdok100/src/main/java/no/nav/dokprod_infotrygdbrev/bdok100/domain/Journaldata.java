package no.nav.dokprod_infotrygdbrev.bdok100.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DistKanal;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.GjelderType;
import no.nav.dokprod_infotrygdbrev.bdok100.kodeverk.BestillendeFagsystemCode;
import no.nav.dokprod_infotrygdbrev.bdok100.kodeverk.FagomradeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

/**
 * Arbeidstabell fragment for journaldata
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Journaldata {

	@Column(name = "on_demand_instans")
	private String onDemandInstans;

	@Column(name = "saks_nummer")
	private String saksNummer;

	@Enumerated(EnumType.STRING)
	@Column(name = "bestillende_fagsystemkode")
	private BestillendeFagsystemCode bestillendeFagsystemkode;

	@Enumerated(EnumType.STRING)
	@Column(name = "dok_tilhorendefagomraadekode")
	private FagomradeCode dokumentTilhorendefagomraadekode;

	@Column(name = "journalf_enhet")
	private String journalfEnhet;

	@Column(name = "saksbehandler_id")
	private String saksbehandlerId;

	@Column(name = "saksbehand_navn")
	private String saksbehandNavn;

	@Column(name = "journal_status")
	private String journalStatus;

	@Column(name = "dato_ferdig")
	private LocalDateTime datoFerdig;

	@Column(name = "gjelder_id")
	private String gjelderID;

	@Enumerated(EnumType.STRING)
	@Column(name = "gjelder_type")
	private GjelderType gjelderType;

	@Column(name = "innhold")
	private String innhold;

	@Column(name = "avsend_mottak_id")
	private String avsendMottakID;

	@Column(name = "avsend_mottaker")
	private String avsendMottaker;

	@Column(name = "dato_dokument")
	private LocalDateTime datoDokument;

	@Column(name = "dokument_type")
	private String dokumentType;

	@Column(name = "kategori")
	private String kategori;

	@Column(name = "brevkode")
	private String brevKode;

	@Column(name = "brev_gruppe")
	private String brevGruppe;

	@Enumerated(EnumType.STRING)
	@Column(name = "fakt_distr_kanal")
	private DistKanal faktDistrKanal;

	@Column(name = "sensitivt")
	private Boolean sensitivt;

	@Column(name = "elektronisk_distr")
	private Boolean elektroniskDistr;

	@Column(name = "dato_journal")
	private LocalDateTime datoJournal;

	@Column(name = "dato_sendt_print")
	private LocalDateTime datoSendtPrint;
}

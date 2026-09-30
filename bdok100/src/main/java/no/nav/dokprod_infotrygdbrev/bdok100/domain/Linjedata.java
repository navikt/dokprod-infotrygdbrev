package no.nav.dokprod_infotrygdbrev.bdok100.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Adressetype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Brevtype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Spraak;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Arbeidstabell fragment for linjedata
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Linjedata {

	// Linje 1
	@Column(name = "tk_nr_1")
	private String tknr1;
	@Column(name = "fnr")
	private String fnr;

	@Column(name = "aar")
	private String aar;
	@Column(name = "maaned")
	private String maaned;
	@Column(name = "dag")
	private String dag;

	@Column(name = "lege_fnr")
	private String legeFnr;
	@Column(name = "postnr")
	private String postnr;
	@Enumerated(EnumType.STRING)
	@Column(name = "brevtype")
	private Brevtype brevtype;
	@Column(name = "toppark_indikator")
	private String topparkIndikator;
	@Column(name = "toppark_postboks_mottat")
	private String topparkPostboksMottat;
	@Column(name = "toppark_kontrolltegn_1")
	private String topparkKontrolltegn1;
	@Column(name = "toppark_tema_fagomraade")
	private String topparkTemaFagomraade;
	@Column(name = "toppark_kontrolltegn_2")
	private String topparkKontrolltegn2;
	@Column(name = "brevnavn")
	private String brevnavn;
	@Enumerated(EnumType.STRING)
	@Column(name = "spraak")
	private Spraak spraak;
	@Column(name = "tk_nr_2")
	private String tknr2;
	@Column(name = "infotrygd_brevkode_page")
	private String infotrygdBrevkodePage;

	// Linje 2
	@Column(name = "adresselinj1")
	private String adresseLinje1;
	@Column(name = "adresselinj2")
	private String adresseLinje2;
	@Column(name = "adresselinj3")
	private String adresseLinje3;
	@Column(name = "adresselinj4")
	private String adresseLinje4;
	@Column(name = "poststed")
	private String poststed;
	@Column(name = "land")
	private String land;
	@Enumerated(EnumType.STRING)
	@Column(name = "adressetype")
	private Adressetype adressetype;

	@Column(name = "footer", length = 1000)
	private String footer;

}

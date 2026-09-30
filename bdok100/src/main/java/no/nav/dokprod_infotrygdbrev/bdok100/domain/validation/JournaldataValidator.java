package no.nav.dokprod_infotrygdbrev.bdok100.domain.validation;

import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Errors;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Journaldata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Linjedata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Adressetype;
import no.nav.dokprod_infotrygdbrev.common.exception.AvviksfilException;
import no.nav.dokprod_infotrygdbrev.common.exception.JournaldataException;
import no.nav.dokprod_infotrygdbrev.common.validation.AbstractValidator;
import no.nav.dokprod_infotrygdbrev.bdok100.kodeverk.BestillendeFagsystemCode;
import org.apache.commons.lang3.StringUtils;

import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DistKanal.L;

/**
 * Validator for Mapping to Journaldata
 * If validation fails for any of the fields, an JournaldataException is thrown.
 *
 */
public class JournaldataValidator extends AbstractValidator<Journaldata> {

	private static final String STATUS_AVBRUTT = "A";
	private static final String ALLOWED_DOKUMENTTYPE = "U";
	private static final String ALLOWED_KATEGORI = "B";

	public JournaldataValidator() {
		super(Journaldata.class);
	}

	public void validate(Bdok100ArbTbl record) {
		Journaldata journaldata = record.getJournaldata();

		validateFields(journaldata);

		checkFagsystem(journaldata);
		checkDokumentType(journaldata);
		checkKategori(journaldata);
		checkJournalstatus(journaldata);
		checkUtenlandskAdressType(record);
	}

	private void checkKategori(Journaldata journaldata) {
		if (!ALLOWED_KATEGORI.equals(journaldata.getKategori())) {
			throw new JournaldataException("Wrong Kategori: " + journaldata.getKategori());
		}
	}

	private boolean isLocalprint(Bdok100ArbTbl item) {
		return L.equals(item.getJournaldata().getFaktDistrKanal());

	}

	private void checkUtenlandskAdressType(Bdok100ArbTbl item){
		Linjedata linjedata = item.getLinjedata();
		if (linjedata.getAdressetype() == Adressetype.UTENLANDSK && StringUtils.isBlank(linjedata.getAdresseLinje2())) {
			if (!isLocalprint(item)) {
				throw new AvviksfilException("Utenlandsk adresse mangler adresselinje, må legges til manuelt");
			}
		}
	}

	private void checkDokumentType(Journaldata journaldata) {
		if (!ALLOWED_DOKUMENTTYPE.equals(journaldata.getDokumentType())) {
			throw new JournaldataException("Wrong DokumentType: " + journaldata.getDokumentType());
		}
	}

	private void checkFagsystem(Journaldata journaldata) {
		if (journaldata.getBestillendeFagsystemkode() != BestillendeFagsystemCode.IT01) {
			throw new JournaldataException("Wrong Bestillendefagsystemkode: " + journaldata.getBestillendeFagsystemkode());
		}
	}

	private void checkJournalstatus(Journaldata journaldata) {
		if (STATUS_AVBRUTT.equals(journaldata.getJournalStatus())) {
			throw new JournaldataException(Bdok100Errors.NOT_BREVBESTILLING);
		}
	}

	private void validateFields(Journaldata journaldata) {
		notNull(journaldata.getSaksNummer(), "SaksNummer");
		notNull(journaldata.getBestillendeFagsystemkode(), "BestillendeFagsystemkode");
		notNull(journaldata.getDokumentTilhorendefagomraadekode(), "DokumentTilhorendefagomraadekode");
		notNull(journaldata.getJournalfEnhet(), "JournalfEnhet");
		notNull(journaldata.getSaksbehandNavn(), "SaksbehandNavn");
		notNull(journaldata.getJournalStatus(), "JournalStatus");
		notNull(journaldata.getGjelderID(), "GjelderID");
		notNull(journaldata.getGjelderType(), "GjelderType");
		notNull(journaldata.getAvsendMottakID(), "AvsendMottakID");
		notNull(journaldata.getAvsendMottaker(), "AvsendMottaker");
		notNull(journaldata.getDokumentType(), "DokumentType");
		notNull(journaldata.getKategori(), "Kategori");
		notNull(journaldata.getFaktDistrKanal(), "FaktDistrKanal");
		hasText(journaldata.getInnhold(), "BrevTittel");
	}
}

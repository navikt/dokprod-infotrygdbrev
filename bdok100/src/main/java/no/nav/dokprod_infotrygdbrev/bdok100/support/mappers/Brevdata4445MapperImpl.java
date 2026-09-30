package no.nav.dokprod_infotrygdbrev.bdok100.support.mappers;

import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.BesoksadresseType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.Brevdata;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.BrevinnholdListeType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.BrevinnholdType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.FagType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.FellesType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.KontaktInformasjonType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.MottakerAdresseType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.MottakerType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.MottakerTypeKode;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.NedreBarkodeType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.OvreBarkodeType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.PostadresseType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.ReturadresseType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.SakspartType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.SakspartTypeKode;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.SignerendeSaksbehandlerType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.SkanningType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.VedleggslisteType;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Brevtekst;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Journaldata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Linjedata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.NAVKontor;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Adressetype;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.NAVKontors;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.VedleggsLister;
import no.nav.dokprod_infotrygdbrev.common.exception.AvviksfilException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.LANDKODE_NORGE;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Mapper for brevdata 44/45
 *
 */
public class Brevdata4445MapperImpl implements BrevdataMapper {

	private static final String TKNR_SKANNING_EXCEMPT = "2103";
	private static final String COMMA_SPACE = ", ";
	private NAVKontors navKontors;
	private VedleggsLister vedleggsLister;

	@Override
	public Brevdata map(Bdok100ArbTbl bdok100ArbTbl) {
		Brevdata brevdata = new Brevdata();
		brevdata.setFelles(mapFelles(bdok100ArbTbl));
		brevdata.setFag(mapFag(bdok100ArbTbl));
		return brevdata;
	}

	/**
	 * candidate for reuse if more batches are created that create felles brevdata
	 */
	private FellesType mapFelles(Bdok100ArbTbl bdok100ArbTbl) {
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();
		Journaldata journaldata = bdok100ArbTbl.getJournaldata();

		FellesType felles = new FellesType();
		felles.setSpraakkode(linjedata.getSpraak().getSpraakKode());
		felles.setFagsaksnummer(journaldata.getSaksNummer());

		felles.setSignerendeSaksbehandler(new SignerendeSaksbehandlerType());
		felles.getSignerendeSaksbehandler().setSignerendeSaksbehandlerNavn(journaldata.getSaksbehandNavn());

		felles.setSakspart(new SakspartType());
		felles.getSakspart().setSakspartId(journaldata.getGjelderID());
		felles.getSakspart().setSakspartTypeKode(SakspartTypeKode.valueOf(journaldata.getGjelderType().name()));

		felles.setMottaker(mapMottaker(bdok100ArbTbl));

		NAVKontor navKontor = navKontors.getNavKontor(linjedata.getTknr2());
		if (navKontor == null) {
			throw new AvviksfilException("Fant ikke navkontor for " + linjedata.getTknr2());
		}
		felles.setNavnAvsenderEnhet(navKontor.getTKName());
		felles.setNummerAvsenderEnhet(linjedata.getTknr2());

		felles.setKontaktInformasjon(mapKontaktInfo(navKontor));

		felles.setBunntekst(linjedata.getFooter());

		return felles;
	}

	private KontaktInformasjonType mapKontaktInfo(NAVKontor kontor) {
		KontaktInformasjonType kontaktInfo = new KontaktInformasjonType();
		kontaktInfo.setKontaktTelefonnummer(kontor.getTelefon());
		kontaktInfo.setKontaktFaksnummer(kontor.getTelefaks());

		ReturadresseType returadresse = new ReturadresseType();
		PostadresseType postadresse = new PostadresseType();
		BesoksadresseType besoksadresse = new BesoksadresseType();
		kontaktInfo.setReturadresse(returadresse);
		kontaktInfo.setPostadresse(postadresse);
		kontaktInfo.setBesoksadresse(besoksadresse);

		String adresselinje = buildAdresse(kontor.getPostadresse1(), kontor.getPostadresse2());

		returadresse.setNavEnhetsNavn(kontor.getTKName());
		returadresse.setAdresselinje(adresselinje);
		returadresse.setPostNr(kontor.getPostNr());
		returadresse.setPoststed(kontor.getPoststed());

		postadresse.setNavEnhetsNavn(kontor.getTKName());
		postadresse.setAdresselinje(adresselinje);
		postadresse.setPostNr(kontor.getPostNr());
		postadresse.setPoststed(kontor.getPoststed());

		besoksadresse.setAdresselinje(buildAdresse(kontor.getBesoeksadresse1(), kontor.getBesoeksadresse2()));
		besoksadresse.setPostNr(kontor.getPostNrBesoeksadresse());
		besoksadresse.setPoststed(kontor.getPoststedBesoeksadresse());

		return kontaktInfo;
	}

	private String buildAdresse(String postadresse1, String postadresse2) {
		StringBuilder adresselinje = new StringBuilder();
		if (StringUtils.isNotBlank(postadresse1)) {
			adresselinje.append(postadresse1);
			if (StringUtils.isNotBlank(postadresse2)) {
				adresselinje.append(COMMA_SPACE);
			}
		}
		if (StringUtils.isNotBlank(postadresse2)) {
			adresselinje.append(postadresse2);
		}
		return adresselinje.length() > 0 ? adresselinje.toString() : null;
	}

	private MottakerType mapMottaker(Bdok100ArbTbl bdok100ArbTbl) {
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();

		MottakerTypeKode mottakerType;
		if (bdok100ArbTbl.getJournaldata().getAvsendMottakID().length() == 11) {
			mottakerType = MottakerTypeKode.PERSON;
		} else {
			mottakerType = MottakerTypeKode.ORGANISASJON;
		}
		MottakerType mottaker = new MottakerType();
		mottaker.setMottakerId(bdok100ArbTbl.getJournaldata().getAvsendMottakID());
		mottaker.setMottakerTypeKode(mottakerType);
		mottaker.setMottakerNavn(linjedata.getAdresseLinje1());
		MottakerAdresseType adresse = new MottakerAdresseType();
		adresse.setAdresselinje1(linjedata.getAdresseLinje2());
		adresse.setAdresselinje2(linjedata.getAdresseLinje3());
		adresse.setAdresselinje3(linjedata.getAdresseLinje4());
		if (linjedata.getAdressetype() == Adressetype.NORSK) {
			adresse.setPostNr(linjedata.getPostnr());
			adresse.setPoststed(linjedata.getPoststed());
			adresse.setLand(LANDKODE_NORGE);
		} else {
			adresse.setLand(linjedata.getLand());
		}
		mottaker.setMottakerAdresse(adresse);

		return mottaker;
	}

	private FagType mapFag(Bdok100ArbTbl bdok100ArbTbl) {
		FagType fag = new FagType();
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();
		Journaldata journaldata = bdok100ArbTbl.getJournaldata();
		fag.setBrevDato(linjedata.getDag() + linjedata.getMaaned() + linjedata.getAar());
		fag.setTittel(journaldata.getInnhold());
		fag.setBrevinnholdListe(new BrevinnholdListeType());

		int i = 1;
		for (Bdok100Brevtekst brevtekst : bdok100ArbTbl.getBrevtekster()) {
			BrevinnholdType brevinnhold = new BrevinnholdType();
			if(isBlank(brevtekst.getInnhold())) {
				throw new AvviksfilException("Brevtekst nr " + i + "/" + bdok100ArbTbl.getBrevtekster().size() + " for id=" + bdok100ArbTbl.getIdnr() + " var tom.");
			}
			brevinnhold.setBrevtekst(brevtekst.getInnhold());
			fag.getBrevinnholdListe().getBrevinnholds().add(brevinnhold);
			i++;
		}

		fag.setVedleggsliste(new VedleggslisteType());
		VedleggsListe vedleggsListe = vedleggsLister.getVedleggsListe(linjedata.getBrevnavn());
		if (vedleggsListe != null) {
			List<VedleggsListe.Vedlegg> vedlegg = vedleggsListe.getVedleggs();
			fag.getVedleggsliste().setVedlegg1(!vedlegg.isEmpty() ? vedlegg.get(0).getVedleggsKode() : null);
			fag.getVedleggsliste().setVedlegg2(vedlegg.size() > 1 ? vedlegg.get(1).getVedleggsKode() : null);
			fag.getVedleggsliste().setVedlegg3(vedlegg.size() > 2 ? vedlegg.get(2).getVedleggsKode() : null);
		}

		if (linjedata.getTopparkIndikator() != null && !TKNR_SKANNING_EXCEMPT.equals(linjedata.getTknr1())) {
			fag.setSkanning(new SkanningType());
			fag.getSkanning().setOvreBarkode(new OvreBarkodeType());
			fag.getSkanning().getOvreBarkode().setKontrollsiffer(linjedata.getTopparkKontrolltegn1());
			fag.getSkanning().setNedreBarkode(new NedreBarkodeType());
			fag.getSkanning().getNedreBarkode().setTemaKode(linjedata.getTopparkTemaFagomraade());
			fag.getSkanning().getNedreBarkode().setEnhetId(linjedata.getTknr1());
			fag.getSkanning().getNedreBarkode().setKontrollsiffer(linjedata.getTopparkKontrolltegn2());
		}

		return fag;
	}

	public void setNavKontors(NAVKontors navKontors) {
		this.navKontors = navKontors;
	}

	public void setVedleggs(VedleggsLister vedleggsLister) {
		this.vedleggsLister = vedleggsLister;
	}
}

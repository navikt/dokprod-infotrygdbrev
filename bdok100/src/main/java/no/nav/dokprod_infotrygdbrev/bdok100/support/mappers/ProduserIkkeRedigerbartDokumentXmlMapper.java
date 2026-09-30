package no.nav.dokprod_infotrygdbrev.bdok100.support.mappers;


import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.LANDKODE_NORGE;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.UKJENT_LANDKODE_UTLAND;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DokumenttypeId._000044;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DokumenttypeId._000045;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DokumenttypeId._000046;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DokumenttypeId._000249;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.GjelderType.ORGANISASJON;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.GjelderType.PERSON;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Journaldata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Linjedata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Adressetype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DistKanal;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.Adresse;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.Aktoer;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.ArkivSak;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.Dokumentbestillingsinformasjon;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.NorskPostadresse;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.Organisasjon;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.Person;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.ProduserIkkeRedigerbartDokument;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.UtenlandskPostadresse;
import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
public class ProduserIkkeRedigerbartDokumentXmlMapper {

	private static final List<String> DOKTYPE_000044 = Lists.newArrayList(
			"AA08", "AG60", "AG61", "HT20", "HT22", "HT24", "HT25", "HT44"
	);

	private static final List<String> DOKTYPE_000249 = Lists.newArrayList(
			"SP02", "SP03", "SP04", "SP05", "SP06", "SP07", "SP08", "SP12", "SP22", "SP23", "SP24", "SP25",
			"SP50", "SP51", "SP98", "SP99", "S313", "S322", "S334", "S338", "S340", "S341", "S342", "S352",
			"S354", "OTA6", "QTKA", "QTK8"
	);

	private static final String UNKNOWN = "Unknown";
	private static final String SAKSTILHORENDE_FAGSYSTEMKODE = "FS22";
	public static final String BDOK100_PREFIX = "BDOK100_";

	private BrevdataMapper brevdataMapper;
	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

	public ProduserIkkeRedigerbartDokument map(Bdok100ArbTbl bdok100ArbTbl) {
		log.info("Behandler brevbestilling fra arbeidstabell med id={}, brevkode={}. Brevtekst lengder={}",
				bdok100ArbTbl.getIdnr(), bdok100ArbTbl.getJournaldata().getBrevKode(), bdok100ArbTbl.getBrevtekster().stream().mapToInt(brevtekst -> {
					if(isBlank(brevtekst.getInnhold())) {
						return 0;
					} else {
						return brevtekst.getInnhold().length();
					}
				}).boxed().collect(Collectors.toList()));
		Journaldata journaldata = bdok100ArbTbl.getJournaldata();

		Dokumentbestillingsinformasjon dokumentbestillingsinformasjon = new Dokumentbestillingsinformasjon();
		dokumentbestillingsinformasjon.setUtledRegisterInfo(false); //Qdok001 liker ikke null verdi
		dokumentbestillingsinformasjon.setBatchId(BDOK100_PREFIX + simpleDateFormat.format(LocalDate.now().toDate()));
		dokumentbestillingsinformasjon.setBestillingsId(BDOK100_PREFIX + bdok100ArbTbl.getIdnr());
		dokumentbestillingsinformasjon.setUstrukturertTittel(journaldata.getInnhold());
		dokumentbestillingsinformasjon.setDokumenttypeId(mapDokumenttypeId(bdok100ArbTbl));
		dokumentbestillingsinformasjon.setBestillendeFagsystemkode(mapEnumToString(journaldata.getBestillendeFagsystemkode()));
		dokumentbestillingsinformasjon.setBruker(mapBruker(bdok100ArbTbl));
		dokumentbestillingsinformasjon.setMottaker(mapMottaker(bdok100ArbTbl));
		dokumentbestillingsinformasjon.setArkivsak(mapArkivSak(bdok100ArbTbl));
		dokumentbestillingsinformasjon.setDokumenttilhoerendeFagomraadekode(mapEnumToString(journaldata.getDokumentTilhorendefagomraadekode()));
		dokumentbestillingsinformasjon.setJournalfoerendeEnhet(journaldata.getJournalfEnhet());
		dokumentbestillingsinformasjon.setSaksbehandlernavn(journaldata.getSaksbehandNavn());
		dokumentbestillingsinformasjon.setAdresse(mapAdresse(bdok100ArbTbl));

		ProduserIkkeRedigerbartDokument dokumentbestilling = new ProduserIkkeRedigerbartDokument();
		dokumentbestilling.setDokumentbestillingsinformasjon(dokumentbestillingsinformasjon);

		Object brevdata = brevdataMapper.map(bdok100ArbTbl);
		dokumentbestilling.setBrevdata(brevdata);
		if (bdok100ArbTbl.getJournaldata().getBrevKode() != null) {
			log.info(String.format("Brevbestilling med id %s og brevkode %s har blitt produsert",
					bdok100ArbTbl.getIdnr(),
					bdok100ArbTbl.getJournaldata().getBrevKode()));
		}

		return dokumentbestilling;
	}

	private String mapEnumToString(Enum e) {
		return e == null ? null : e.name();
	}

	private ArkivSak mapArkivSak(Bdok100ArbTbl bdok100ArbTbl) {
		ArkivSak arkivSak = new ArkivSak();
		arkivSak.setJournalsakId(bdok100ArbTbl.getSaksID());
		arkivSak.setSakstilhoerendeFagsystemkode(SAKSTILHORENDE_FAGSYSTEMKODE);
		return arkivSak;
	}

	private Adresse mapAdresse(Bdok100ArbTbl bdok100ArbTbl) {
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();
		Adresse adresse;
		if (linjedata.getAdressetype() == Adressetype.UTENLANDSK) {
			UtenlandskPostadresse utenlandskPostadresse = new UtenlandskPostadresse();
			utenlandskPostadresse.setAdresselinje1(linjedata.getAdresseLinje2());
			utenlandskPostadresse.setAdresselinje2(linjedata.getAdresseLinje3());
			utenlandskPostadresse.setAdresselinje3(linjedata.getAdresseLinje4());
			final String land = linjedata.getLand();
			final String iso3166Alpha2kode = Iso3611Alpha2.fromLand(land).orElse(UKJENT_LANDKODE_UTLAND);
			if(iso3166Alpha2kode.equals(UKJENT_LANDKODE_UTLAND) && bdok100ArbTbl.getJournaldata().getFaktDistrKanal() == DistKanal.S) {
				// Funksjonell feil med error logging. Grunnen til det er at man har rutine på å oppdage denne typen feil i loggene.
				// Denne typen feil burde håndteres ved at man legger inn oppslag på navn til riktig kode i Iso3611Alpha2.java.
				// Hvis land ikke er et land men heller "Attn: regnskapsfører", "v/ Bjarne Betjent" osv så kan det hende dette er et
				// norsk brev som er klassifisert som utenlandsk.
				log.error("Oppdaget ukjent land=\"{}\" for idnr={}. Dette må håndteres ved å legge til landet i oppslagslisten til Iso3611Alpha2.", land, bdok100ArbTbl.getIdnr());
			}
			utenlandskPostadresse.setLand(iso3166Alpha2kode);
			adresse = utenlandskPostadresse;
		} else {
			NorskPostadresse norskPostadresse = new NorskPostadresse();
			norskPostadresse.setAdresselinje1(linjedata.getAdresseLinje2());
			norskPostadresse.setAdresselinje2(linjedata.getAdresseLinje3());
			norskPostadresse.setAdresselinje3(linjedata.getAdresseLinje4());
			norskPostadresse.setPoststed(linjedata.getPoststed());
			norskPostadresse.setPostnummer(linjedata.getPostnr());
			norskPostadresse.setLand(LANDKODE_NORGE);
			adresse = norskPostadresse;
		}
		return adresse;
	}

	private Aktoer mapBruker(Bdok100ArbTbl bdok100ArbTbl) {
		String gjelderID = bdok100ArbTbl.getJournaldata().getGjelderID();
		Aktoer aktoer = null;
		if (bdok100ArbTbl.getJournaldata().getGjelderType() == ORGANISASJON) {
			Organisasjon bruker = new Organisasjon();
			bruker.setNavn(UNKNOWN);
			bruker.setOrgnummer(gjelderID);

			aktoer = bruker;
		} else if (bdok100ArbTbl.getJournaldata().getGjelderType() == PERSON) {
			Person bruker = new Person();
			bruker.setNavn(UNKNOWN);
			bruker.setPersonidentifikator(gjelderID);

			aktoer = bruker;
		}

		if (!gjelderID.equals(bdok100ArbTbl.getLinjedata().getFnr())) {
			log.warn("ArkivID=" + bdok100ArbTbl.getIdnr() +
					" gjelderID <> Infotrygd_Fnr gjelderID=" + gjelderID +
					" infotrygd_fnr=" + bdok100ArbTbl.getLinjedata().getFnr());
		}

		return aktoer;

	}

	private Aktoer mapMottaker(Bdok100ArbTbl bdok100ArbTbl) {
		Aktoer aktoer;
		if (bdok100ArbTbl.getJournaldata().getAvsendMottakID().length() == 11) {
			Person mottaker = new Person();
			mottaker.setNavn(bdok100ArbTbl.getJournaldata().getAvsendMottaker());
			mottaker.setPersonidentifikator(bdok100ArbTbl.getJournaldata().getAvsendMottakID());
			aktoer = mottaker;
		} else {
			Organisasjon mottaker = new Organisasjon();
			mottaker.setNavn(bdok100ArbTbl.getJournaldata().getAvsendMottaker());
			mottaker.setOrgnummer(bdok100ArbTbl.getJournaldata().getAvsendMottakID());
			aktoer = mottaker;
		}
		return aktoer;
	}

	private String mapDokumenttypeId(Bdok100ArbTbl bdok100ArbTbl) {
		String dokumenttypeId = null;
		if (DistKanal.L == bdok100ArbTbl.getJournaldata().getFaktDistrKanal()) {
			dokumenttypeId = _000046.toString();
		} else if (DistKanal.S == bdok100ArbTbl.getJournaldata().getFaktDistrKanal()) {
			if (DOKTYPE_000249.contains(bdok100ArbTbl.getLinjedata().getBrevnavn())) {
				dokumenttypeId = _000249.toString();
			} else if (DOKTYPE_000044.contains(bdok100ArbTbl.getLinjedata().getBrevnavn())) {
				dokumenttypeId = _000044.toString();
			} else {
				dokumenttypeId = _000045.toString();
			}
		}
		return dokumenttypeId;
	}

	public void setBrevdataMapper(BrevdataMapper brevdataMapper) {
		this.brevdataMapper = brevdataMapper;
	}
}

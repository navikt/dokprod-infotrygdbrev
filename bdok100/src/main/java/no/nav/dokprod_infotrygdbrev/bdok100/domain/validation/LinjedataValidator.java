package no.nav.dokprod_infotrygdbrev.bdok100.domain.validation;

import com.google.common.base.Joiner;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Brevtekst;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Linjedata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Adressetype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Brevtype;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.NAVKontors;
import no.nav.dokprod_infotrygdbrev.common.exception.AvviksfilException;
import no.nav.dokprod_infotrygdbrev.common.exception.KontrollRapportException;
import no.nav.dokprod_infotrygdbrev.common.validation.AbstractValidator;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableList;
import static no.nav.dokprod_infotrygdbrev.Regexes.NOT_CONTROL_CHAR_PATTERN;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.INFOTRYGD_BREVKODE_PAGE;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Errors.NAVKONTOR_NOT_FOUND;

/**
 * Validator for Mapping of Linjedata
 *
 */
public class LinjedataValidator extends AbstractValidator<Linjedata> {

	private static final Pattern INFOTRYGD_BREVKODEPAGE_PATTERN = Pattern.compile("\\w{5}_\\w{2}");

	private static final String AGNAVNADR = "<AGNAVNADR>";
	private static final String MOTT = "<MOTT>";

	public static final int TKNR_INVALID_GROUP_1_START = 2399;
	public static final int TKNR_INVALID_GROUP_1_END = 2820;

	public static final int TKNR_INVALID_GROUP_2_START = 2822;
	public static final int TKNR_INVALID_GROUP_2_END = 2999;

	public static final int TKNR_INVALID_GROUP_3_START = 3100;
	public static final int TKNR_INVALID_GROUP_3_END = 3399;

	public static final int TKNR_INVALID_GROUP_4_START = 3500;
	public static final int TKNR_INVALID_GROUP_4_END = 3799;

	public static final int TKNR_INVALID_GROUP_5_START = 3900;
	public static final int TKNR_INVALID_GROUP_5_END = 4199;

	public static final int TKNR_INVALID_GROUP_6_START = 4300;
	public static final int TKNR_INVALID_GROUP_6_END = 4400;

	public static final int TKNR_INVALID_GROUP_7_START = 4500;
	public static final int TKNR_INVALID_GROUP_7_END = 4529;

	public static final int TKNR_INVALID_GROUP_8_START = 4531;
	public static final int TKNR_INVALID_GROUP_8_END = 4600;

	public static final int TKNR_INVALID_GROUP_9_START = 5999;


	private static final List<String> FJERNEDE_BREVKODER =
			unmodifiableList(newArrayList("ZH2", "ZE2", "Z10", "Z20", "Z30", "Z40", "ZH3", "ZE3"));

	private NAVKontors navKontors;

	public LinjedataValidator() {
		super(Linjedata.class);
	}

	public void validate(Bdok100ArbTbl bdok100ArbTbl) {
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();

		validateForNoErrorHandling(linjedata);
		validateForAvviksfil(linjedata, bdok100ArbTbl.getBrevtekster());
		validateFields(linjedata);
		validateToppark(linjedata);
		validateNavKontors(linjedata);
	}

	private void validateNavKontors(Linjedata linjedata) {
		if (navKontors.getNavKontor(linjedata.getTknr2()) == null) {
			throw new AvviksfilException(String.format("%s %s", NAVKONTOR_NOT_FOUND, linjedata.getTknr2()));
		}
	}

	private void validateToppark(Linjedata linjedata) {
		if (StringUtils.isNotBlank(linjedata.getTopparkIndikator())) {
			boolean hasKontrolltegn2 = StringUtils.isNotBlank(linjedata.getTopparkKontrolltegn2());
			boolean hasTemaFagomraade = StringUtils.isNotBlank(linjedata.getTopparkTemaFagomraade());
			if (!hasKontrolltegn2 || !hasTemaFagomraade) {
				throw new AvviksfilException("TopparkIndikator er tilstede uten" +
						(hasKontrolltegn2 ? "" : " TopparkKontrolltegn2") +
						(hasTemaFagomraade ? "" : " TopparkTemaFagomraade"));
			}
		}
	}

	private void validateForAvviksfil(Linjedata linjedata, List<Bdok100Brevtekst> brevtekster) {
		String brevtekst = Joiner.on("").join(brevtekster.stream().map(Bdok100Brevtekst::getInnhold).collect(Collectors.toList()));
		if (brevtekst.contains(AGNAVNADR)) {
			throw new AvviksfilException("Forekomst av <AGNAVNADR> i brevtekst");
		}
		if (brevtekst.contains(MOTT)) {
			throw new AvviksfilException("Forekomst av <MOTT> i brevtekst");
		}

		if (StringUtils.isBlank(linjedata.getAdresseLinje1())) {
			throw new AvviksfilException("Adresse mangler adresselinje, må legges til manuelt");
		}

		if (linjedata.getAdressetype() == Adressetype.NORSK) {
			if (StringUtils.isBlank(linjedata.getPoststed())) {
				throw new AvviksfilException("Norsk adresse mangler poststed, må legges til manuelt");
			}
		}

		Integer tknr1 = Integer.valueOf(linjedata.getTknr1());
		if(!tKNrInValidRange(tknr1)) {
			throw new AvviksfilException("Ugyldig TKNR " + tknr1);
		}

	}

	private boolean tKNrInValidRange(Integer tknr) {
		if ((tknr >= TKNR_INVALID_GROUP_1_START && tknr <= TKNR_INVALID_GROUP_1_END) ||
				(tknr >= TKNR_INVALID_GROUP_2_START && tknr <= TKNR_INVALID_GROUP_2_END) ||
				(tknr >= TKNR_INVALID_GROUP_3_START && tknr <= TKNR_INVALID_GROUP_3_END) ||
				(tknr >= TKNR_INVALID_GROUP_4_START && tknr <= TKNR_INVALID_GROUP_4_END) ||
				(tknr >= TKNR_INVALID_GROUP_5_START && tknr <= TKNR_INVALID_GROUP_5_END) ||
				(tknr >= TKNR_INVALID_GROUP_6_START && tknr <= TKNR_INVALID_GROUP_6_END) ||
				(tknr >= TKNR_INVALID_GROUP_7_START && tknr <= TKNR_INVALID_GROUP_7_END) ||
				(tknr >= TKNR_INVALID_GROUP_8_START && tknr <= TKNR_INVALID_GROUP_8_END) ||
				tknr >= TKNR_INVALID_GROUP_9_START) {
			return false;
		}
		return true;
	}

	private void validateForNoErrorHandling(Linjedata linjedata) {
		for (String brevkodePrefix : FJERNEDE_BREVKODER) {
			if(StringUtils.isBlank(linjedata.getBrevnavn())) {
				throw new KontrollRapportException("Brevnavn som er tomme fjernes fra dokumentbestillingen");
			} else if (linjedata.getBrevnavn().startsWith(brevkodePrefix)) {
				throw new KontrollRapportException("Brevnavn som begynner med " + brevkodePrefix + " fjernes fra dokumentbestillingen");
			}
		}

		if (!linjedata.getInfotrygdBrevkodePage().equals(INFOTRYGD_BREVKODE_PAGE)) {
			throw new KontrollRapportException("Ugyldig Infotrygd_BrevkodePage " + linjedata.getInfotrygdBrevkodePage());
		}
		if (linjedata.getBrevtype() != Brevtype.OK) {
			throw new KontrollRapportException("Ugyldig Brevtype " + linjedata.getBrevtype());
		}
	}

	private void validateFields(Linjedata linjedata) {
		notNullpattern(linjedata.getTknr1(), DIG_4, "Tknr1");
		notNullpattern(linjedata.getFnr(), DIG_11, "Fnr");
		notNullpattern(linjedata.getAar(), DIG_2, "Aar");
		notNullpattern(linjedata.getMaaned(), DIG_2, "Maaned");
		notNullpattern(linjedata.getDag(), DIG_2, "Dag");

		pattern(linjedata.getLegeFnr(), DIG_11, "LegeFnr");
		notNullpattern(linjedata.getPostnr(), DIG_4, "Postnr");
		notNull(linjedata.getBrevtype(), "Brevtype");

		pattern(linjedata.getTopparkIndikator(), NOT_CONTROL_CHAR_PATTERN, "TopparkIndikator");
		pattern(linjedata.getTopparkPostboksMottat(), WORD_4, "TopparkPostboksMottat");
		pattern(linjedata.getTopparkTemaFagomraade(), WORD_2, "TopparkTemaFagomraade");
		pattern(linjedata.getTopparkKontrolltegn2(), NOT_CONTROL_CHAR_PATTERN, "TopparkKontrolltegn2");

		notNullpattern(linjedata.getBrevnavn(), WORD_4, "Brevnavn");
		notNull(linjedata.getSpraak(), "Spraak");
		notNullpattern(linjedata.getTknr2(), DIG_4, "Tknr2");
		notNullpattern(linjedata.getInfotrygdBrevkodePage(), INFOTRYGD_BREVKODEPAGE_PATTERN, "InfotrygdBrevkodePage");
	}

	public void setNavKontors(NAVKontors navKontors) {
		this.navKontors = navKontors;
	}
}

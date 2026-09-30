package no.nav.dokprod_infotrygdbrev.bdok100.support.mappers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Mapper fra navn på land til ISO3166 Alpha-2 koder.
 * Eks
 * Polen - PL
 * Sverige - SE
 * <p>
 * Oversikt hentet fra https://no.wikipedia.org/wiki/ISO_3166-1_alfa-2
 * <p>
 * Kompatibel med K_LANDKODE_POST_DEST
 *
 * @see no.nav.dokprod.domain.kodeverk.LandkodePostDest
 */
class Iso3611Alpha2 {
	private static final Pattern FOERSTE_ORD = Pattern.compile("^(\\w+)");
	private static final Pattern SISTE_ORD = Pattern.compile("(\\w+)[.!?,]?$");
	private static final Map<String, String> iso3166Alpha2 = new HashMap<>();

	static {
		// norsk
		iso3166Alpha2.put("andorra", "AD");
		iso3166Alpha2.put("de forente arabiske emirater", "AE");
		iso3166Alpha2.put("de arabiske emirater", "AE"); // NAV manuell
		iso3166Alpha2.put("afghanistan", "AF");
		iso3166Alpha2.put("antigua og barbuda", "AG");
		iso3166Alpha2.put("anguilla", "AI");
		iso3166Alpha2.put("albania", "AL");
		iso3166Alpha2.put("armenia", "AM");
		iso3166Alpha2.put("de nederlandske antiller", "AN"); // Midlertidig reservert
		iso3166Alpha2.put("angola", "AO");
		iso3166Alpha2.put("antarktis", "AQ");
		iso3166Alpha2.put("argentina", "AR");
		iso3166Alpha2.put("amerikansk samoa", "AS");
		iso3166Alpha2.put("østerrike", "AT");
		iso3166Alpha2.put("australia", "AU");
		iso3166Alpha2.put("aruba", "AW");
		iso3166Alpha2.put("åland", "AX");
		iso3166Alpha2.put("aserbajdsjan", "AZ");
		iso3166Alpha2.put("bosnia-hercegovina", "BA");
		iso3166Alpha2.put("barbados", "BB");
		iso3166Alpha2.put("bangladesh", "BD");
		iso3166Alpha2.put("belgia", "BE");
		iso3166Alpha2.put("burkina faso", "BF");
		iso3166Alpha2.put("bulgaria", "BG");
		iso3166Alpha2.put("bahrain", "BH");
		iso3166Alpha2.put("burundi", "BI");
		iso3166Alpha2.put("benin", "BJ");
		iso3166Alpha2.put("saint-barthélemy", "BL");
		iso3166Alpha2.put("bermuda", "BM");
		iso3166Alpha2.put("brunei", "BN");
		iso3166Alpha2.put("bolivia", "BO");
		iso3166Alpha2.put("bonaire, sint eustatius og saba", "BQ");
		iso3166Alpha2.put("brasil", "BR");
		iso3166Alpha2.put("bahamas", "BS");
		iso3166Alpha2.put("bhutan", "BT");
		iso3166Alpha2.put("bouvetøya", "BV");
		iso3166Alpha2.put("botswana", "BW");
		iso3166Alpha2.put("hviterussland", "BY");
		iso3166Alpha2.put("belize", "BZ");
		iso3166Alpha2.put("canada", "CA");
		iso3166Alpha2.put("kokosøyene", "CC");
		iso3166Alpha2.put("den demokratiske republikken kongo", "CD");
		iso3166Alpha2.put("den sentralafrikanske republikk", "CF");
		iso3166Alpha2.put("republikken kongo", "CG");
		iso3166Alpha2.put("sveits", "CH");
		iso3166Alpha2.put("elfenbenskysten", "CI");
		iso3166Alpha2.put("cookøyene", "CK");
		iso3166Alpha2.put("chile", "CL");
		iso3166Alpha2.put("kamerun", "CM");
		iso3166Alpha2.put("kina", "CN");
		iso3166Alpha2.put("colombia", "CO");
		iso3166Alpha2.put("costa rica", "CR");
		iso3166Alpha2.put("serbia og montenegro", "CS"); // Midlertidig reservert
		iso3166Alpha2.put("cuba", "CU");
		iso3166Alpha2.put("kapp verde", "CV");
		iso3166Alpha2.put("curaçao", "CW");
		iso3166Alpha2.put("christmasøya", "CX");
		iso3166Alpha2.put("kypros", "CY");
		iso3166Alpha2.put("tsjekkia", "CZ");
		iso3166Alpha2.put("den tsjekkiske rep.", "CZ"); // NAV manuell
		iso3166Alpha2.put("tyskland", "DE");
		iso3166Alpha2.put("djibouti", "DJ");
		iso3166Alpha2.put("danmark", "DK");
		iso3166Alpha2.put("dominica", "DM");
		iso3166Alpha2.put("den dominikanske republikk", "DO");
		iso3166Alpha2.put("algerie", "DZ");
		iso3166Alpha2.put("ecuador", "EC");
		iso3166Alpha2.put("estland", "EE");
		iso3166Alpha2.put("egypt", "EG");
		iso3166Alpha2.put("vest-sahara", "EH");
		iso3166Alpha2.put("eritrea", "ER");
		iso3166Alpha2.put("spania", "ES");
		iso3166Alpha2.put("etiopia", "ET");
		iso3166Alpha2.put("finland", "FI");
		iso3166Alpha2.put("fiji", "FJ");
		iso3166Alpha2.put("falklandsøyene", "FK");
		iso3166Alpha2.put("mikronesiaføderasjonen", "FM");
		iso3166Alpha2.put("færøyene", "FO");
		iso3166Alpha2.put("frankrike", "FR");
		iso3166Alpha2.put("gabon", "GA");
		iso3166Alpha2.put("storbritannia", "GB");
		iso3166Alpha2.put("grenada", "GD");
		iso3166Alpha2.put("georgia", "GE");
		iso3166Alpha2.put("fransk guyana", "GF");
		iso3166Alpha2.put("guernsey", "GG");
		iso3166Alpha2.put("ghana", "GH");
		iso3166Alpha2.put("gibraltar", "GI");
		iso3166Alpha2.put("grønland", "GL");
		iso3166Alpha2.put("gambia", "GM");
		iso3166Alpha2.put("guinea", "GN");
		iso3166Alpha2.put("guadeloupe", "GP");
		iso3166Alpha2.put("ekvatorial-guinea", "GQ");
		iso3166Alpha2.put("hellas", "GR");
		iso3166Alpha2.put("sør-georgia og sør-sandwichøyene", "GS");
		iso3166Alpha2.put("guatemala", "GT");
		iso3166Alpha2.put("guam", "GU");
		iso3166Alpha2.put("guinea-bissau", "GW");
		iso3166Alpha2.put("guyana", "GY");
		iso3166Alpha2.put("hongkong", "HK");
		iso3166Alpha2.put("heard- og mcdonaldøyene", "HM");
		iso3166Alpha2.put("honduras", "HN");
		iso3166Alpha2.put("kroatia", "HR");
		iso3166Alpha2.put("haiti", "HT");
		iso3166Alpha2.put("ungarn", "HU");
		iso3166Alpha2.put("indonesia", "ID");
		iso3166Alpha2.put("irland", "IE");
		iso3166Alpha2.put("israel", "IL");
		iso3166Alpha2.put("man", "IM");
		iso3166Alpha2.put("india", "IN");
		iso3166Alpha2.put("det britiske territoriet i indiahavet", "IO");
		iso3166Alpha2.put("irak", "IQ");
		iso3166Alpha2.put("iran", "IR");
		iso3166Alpha2.put("island", "IS");
		iso3166Alpha2.put("italia", "IT");
		iso3166Alpha2.put("jersey", "JE");
		iso3166Alpha2.put("jamaica", "JM");
		iso3166Alpha2.put("jordan", "JO");
		iso3166Alpha2.put("japan", "JP");
		iso3166Alpha2.put("kenya", "KE");
		iso3166Alpha2.put("kirgisistan", "KG");
		iso3166Alpha2.put("kambodsja", "KH");
		iso3166Alpha2.put("kiribati", "KI");
		iso3166Alpha2.put("komorene", "KM");
		iso3166Alpha2.put("saint kitts og nevis", "KN");
		iso3166Alpha2.put("nord-korea", "KP");
		iso3166Alpha2.put("sør-korea", "KR");
		iso3166Alpha2.put("kuwait", "KW");
		iso3166Alpha2.put("caymanøyene", "KY");
		iso3166Alpha2.put("kasakhstan", "KZ");
		iso3166Alpha2.put("laos", "LA");
		iso3166Alpha2.put("libanon", "LB");
		iso3166Alpha2.put("saint lucia", "LC");
		iso3166Alpha2.put("liechtenstein", "LI");
		iso3166Alpha2.put("sri lanka", "LK");
		iso3166Alpha2.put("liberia", "LR");
		iso3166Alpha2.put("lesotho", "LS");
		iso3166Alpha2.put("litauen", "LT");
		iso3166Alpha2.put("luxembourg", "LU");
		iso3166Alpha2.put("latvia", "LV");
		iso3166Alpha2.put("libya", "LY");
		iso3166Alpha2.put("marokko", "MA");
		iso3166Alpha2.put("monaco", "MC");
		iso3166Alpha2.put("moldova", "MD");
		iso3166Alpha2.put("montenegro", "ME");
		iso3166Alpha2.put("saint-martin", "MF");
		iso3166Alpha2.put("madagaskar", "MG");
		iso3166Alpha2.put("marshalløyene", "MH");
		iso3166Alpha2.put("makedonia", "MK");
		iso3166Alpha2.put("mali", "ML");
		iso3166Alpha2.put("myanmar", "MM");
		iso3166Alpha2.put("mongolia", "MN");
		iso3166Alpha2.put("macao", "MO");
		iso3166Alpha2.put("nord-marianene", "MP");
		iso3166Alpha2.put("martinique", "MQ");
		iso3166Alpha2.put("mauritania", "MR");
		iso3166Alpha2.put("montserrat", "MS");
		iso3166Alpha2.put("malta", "MT");
		iso3166Alpha2.put("mauritius", "MU");
		iso3166Alpha2.put("maldivene", "MV");
		iso3166Alpha2.put("malawi", "MW");
		iso3166Alpha2.put("mexico", "MX");
		iso3166Alpha2.put("malaysia", "MY");
		iso3166Alpha2.put("mosambik", "MZ");
		iso3166Alpha2.put("namibia", "NA");
		iso3166Alpha2.put("ny-caledonia", "NC");
		iso3166Alpha2.put("niger", "NE");
		iso3166Alpha2.put("norfolkøya", "NF");
		iso3166Alpha2.put("nigeria", "NG");
		iso3166Alpha2.put("nicaragua", "NI");
		iso3166Alpha2.put("nederland", "NL");
		iso3166Alpha2.put("norge", "NO");
		iso3166Alpha2.put("nepal", "NP");
		iso3166Alpha2.put("nauru", "NR");
		iso3166Alpha2.put("niue", "NU");
		iso3166Alpha2.put("new zealand", "NZ");
		iso3166Alpha2.put("oman", "OM");
		iso3166Alpha2.put("panama", "PA");
		iso3166Alpha2.put("peru", "PE");
		iso3166Alpha2.put("fransk polynesia", "PF");
		iso3166Alpha2.put("papua ny-guinea", "PG");
		iso3166Alpha2.put("filippinene", "PH");
		iso3166Alpha2.put("pakistan", "PK");
		iso3166Alpha2.put("polen", "PL");
		iso3166Alpha2.put("saint-pierre og miquelon", "PM");
		iso3166Alpha2.put("pitcairnøyene", "PN");
		iso3166Alpha2.put("puerto rico", "PR");
		iso3166Alpha2.put("palestina", "PS");
		iso3166Alpha2.put("portugal", "PT");
		iso3166Alpha2.put("palau", "PW");
		iso3166Alpha2.put("paraguay", "PY");
		iso3166Alpha2.put("qatar", "QA");
		iso3166Alpha2.put("réunion", "RE");
		iso3166Alpha2.put("romania", "RO");
		iso3166Alpha2.put("serbia", "RS");
		iso3166Alpha2.put("russland", "RU");
		iso3166Alpha2.put("rwanda", "RW");
		iso3166Alpha2.put("saudi-arabia", "SA");
		iso3166Alpha2.put("salomonøyene", "SB");
		iso3166Alpha2.put("seychellene", "SC");
		iso3166Alpha2.put("sudan", "SD");
		iso3166Alpha2.put("sverige", "SE");
		iso3166Alpha2.put("singapore", "SG");
		iso3166Alpha2.put("st. helena, ascension og tristan da cunha", "SH");
		iso3166Alpha2.put("slovenia", "SI");
		iso3166Alpha2.put("svalbard og jan mayen", "SJ");
		iso3166Alpha2.put("slovakia", "SK");
		iso3166Alpha2.put("sierra leone", "SL");
		iso3166Alpha2.put("san marino", "SM");
		iso3166Alpha2.put("senegal", "SN");
		iso3166Alpha2.put("somalia", "SO");
		iso3166Alpha2.put("surinam", "SR");
		iso3166Alpha2.put("sør-sudan", "SS");
		iso3166Alpha2.put("são tomé og príncipe", "ST");
		iso3166Alpha2.put("el salvador", "SV");
		iso3166Alpha2.put("sint maarten", "SX");
		iso3166Alpha2.put("syria", "SY");
		iso3166Alpha2.put("swaziland", "SZ");
		iso3166Alpha2.put("turks- og caicosøyene", "TC");
		iso3166Alpha2.put("tsjad", "TD");
		iso3166Alpha2.put("de franske sørterritorier", "TF");
		iso3166Alpha2.put("togo", "TG");
		iso3166Alpha2.put("thailand", "TH");
		iso3166Alpha2.put("tadsjikistan", "TJ");
		iso3166Alpha2.put("tokelau", "TK");
		iso3166Alpha2.put("øst-timor", "TL");
		iso3166Alpha2.put("turkmenistan", "TM");
		iso3166Alpha2.put("tunisia", "TN");
		iso3166Alpha2.put("tonga", "TO");
		iso3166Alpha2.put("tyrkia", "TR");
		iso3166Alpha2.put("trinidad og tobago", "TT");
		iso3166Alpha2.put("tuvalu", "TV");
		iso3166Alpha2.put("taiwan", "TW");
		iso3166Alpha2.put("tanzania", "TZ");
		iso3166Alpha2.put("ukraina", "UA");
		iso3166Alpha2.put("uganda", "UG");
		iso3166Alpha2.put("usas ytre småøyer", "UM");
		iso3166Alpha2.put("usa", "US");
		iso3166Alpha2.put("uruguay", "UY");
		iso3166Alpha2.put("usbekistan", "UZ");
		iso3166Alpha2.put("vatikanstaten", "VA");
		iso3166Alpha2.put("saint vincent og grenadinene", "VC");
		iso3166Alpha2.put("venezuela", "VE");
		iso3166Alpha2.put("de britiske jomfruøyer", "VG");
		iso3166Alpha2.put("de amerikanske jomfruøyer", "VI");
		iso3166Alpha2.put("vietnam", "VN");
		iso3166Alpha2.put("vanuatu", "VU");
		iso3166Alpha2.put("wallis og futuna", "WF");
		iso3166Alpha2.put("samoa", "WS");
		iso3166Alpha2.put("jemen", "YE");
		iso3166Alpha2.put("mayotte", "YT");
		iso3166Alpha2.put("jugoslavia", "YU"); // Midlertidig reservert
		iso3166Alpha2.put("sør-afrika", "ZA");
		iso3166Alpha2.put("zambia", "ZM");
		iso3166Alpha2.put("zimbabwe", "ZW");
		// kun engelsk, ingen duplikat
		iso3166Alpha2.put("united arab emirates", "AE");
		iso3166Alpha2.put("antigua and barbuda", "AG");
		iso3166Alpha2.put("antarctica", "AQ");
		iso3166Alpha2.put("american samoa", "AS");
		iso3166Alpha2.put("austria", "AT");
		iso3166Alpha2.put("åland islands", "AX");
		iso3166Alpha2.put("azerbaijan", "AZ");
		iso3166Alpha2.put("bosnia and herzegovina", "BA");
		iso3166Alpha2.put("belgium", "BE");
		iso3166Alpha2.put("saint barthélemy", "BL");
		iso3166Alpha2.put("brunei darussalam", "BN");
		iso3166Alpha2.put("bolivia (plurinational state of)", "BO");
		iso3166Alpha2.put("bonaire, sint eustatius and saba", "BQ");
		iso3166Alpha2.put("brazil", "BR");
		iso3166Alpha2.put("bouvet island", "BV");
		iso3166Alpha2.put("belarus", "BY");
		iso3166Alpha2.put("cocos (keeling) islands", "CC");
		iso3166Alpha2.put("congo, democratic republic of the", "CD");
		iso3166Alpha2.put("central african republic", "CF");
		iso3166Alpha2.put("congo", "CG");
		iso3166Alpha2.put("switzerland", "CH");
		iso3166Alpha2.put("côte d'ivoire", "CI");
		iso3166Alpha2.put("cook islands", "CK");
		iso3166Alpha2.put("cameroon", "CM");
		iso3166Alpha2.put("china", "CN");
		iso3166Alpha2.put("cabo verde", "CV");
		iso3166Alpha2.put("christmas island", "CX");
		iso3166Alpha2.put("cyprus", "CY");
		iso3166Alpha2.put("czechia", "CZ");
		iso3166Alpha2.put("germany", "DE");
		iso3166Alpha2.put("denmark", "DK");
		iso3166Alpha2.put("dominican republic", "DO");
		iso3166Alpha2.put("algeria", "DZ");
		iso3166Alpha2.put("estonia", "EE");
		iso3166Alpha2.put("western sahara", "EH");
		iso3166Alpha2.put("spain", "ES");
		iso3166Alpha2.put("ethiopia", "ET");
		iso3166Alpha2.put("falkland islands (malvinas)", "FK");
		iso3166Alpha2.put("micronesia (federated states of)", "FM");
		iso3166Alpha2.put("faroe islands", "FO");
		iso3166Alpha2.put("france", "FR");
		iso3166Alpha2.put("united kingdom of great britain and northern ireland", "GB");
		iso3166Alpha2.put("united kingdom", "GB"); // NAV manuell
		iso3166Alpha2.put("french guiana", "GF");
		iso3166Alpha2.put("greenland", "GL");
		iso3166Alpha2.put("equatorial guinea", "GQ");
		iso3166Alpha2.put("greece", "GR");
		iso3166Alpha2.put("south georgia and the south sandwich islands", "GS");
		iso3166Alpha2.put("hong kong", "HK");
		iso3166Alpha2.put("heard island and mcdonald islands", "HM");
		iso3166Alpha2.put("croatia", "HR");
		iso3166Alpha2.put("hungary", "HU");
		iso3166Alpha2.put("ireland", "IE");
		iso3166Alpha2.put("isle of man", "IM");
		iso3166Alpha2.put("british indian ocean territory", "IO");
		iso3166Alpha2.put("iraq", "IQ");
		iso3166Alpha2.put("iran (islamic republic of)", "IR");
		iso3166Alpha2.put("iceland", "IS");
		iso3166Alpha2.put("italy", "IT");
		iso3166Alpha2.put("kyrgyzstan", "KG");
		iso3166Alpha2.put("cambodia", "KH");
		iso3166Alpha2.put("comoros", "KM");
		iso3166Alpha2.put("saint kitts and nevis", "KN");
		iso3166Alpha2.put("korea (democratic people's republic of)", "KP");
		iso3166Alpha2.put("korea, republic of", "KR");
		iso3166Alpha2.put("cayman islands", "KY");
		iso3166Alpha2.put("kazakhstan", "KZ");
		iso3166Alpha2.put("lao people's democratic republic", "LA");
		iso3166Alpha2.put("lebanon", "LB");
		iso3166Alpha2.put("lithuania", "LT");
		iso3166Alpha2.put("morocco", "MA");
		iso3166Alpha2.put("moldova, republic of", "MD");
		iso3166Alpha2.put("saint martin (french part)", "MF");
		iso3166Alpha2.put("madagascar", "MG");
		iso3166Alpha2.put("marshall islands", "MH");
		iso3166Alpha2.put("north macedonia", "MK");
		iso3166Alpha2.put("northern mariana islands", "MP");
		iso3166Alpha2.put("maldives", "MV");
		iso3166Alpha2.put("mozambique", "MZ");
		iso3166Alpha2.put("new caledonia", "NC");
		iso3166Alpha2.put("norfolk island", "NF");
		iso3166Alpha2.put("netherlands", "NL");
		iso3166Alpha2.put("norway", "NO");
		iso3166Alpha2.put("french polynesia", "PF");
		iso3166Alpha2.put("papua new guinea", "PG");
		iso3166Alpha2.put("philippines", "PH");
		iso3166Alpha2.put("poland", "PL");
		iso3166Alpha2.put("saint pierre and miquelon", "PM");
		iso3166Alpha2.put("pitcairn", "PN");
		iso3166Alpha2.put("palestine, state of", "PS");
		iso3166Alpha2.put("russian federation", "RU");
		iso3166Alpha2.put("saudi arabia", "SA");
		iso3166Alpha2.put("solomon islands", "SB");
		iso3166Alpha2.put("seychelles", "SC");
		iso3166Alpha2.put("sweden", "SE");
		iso3166Alpha2.put("saint helena, ascension and tristan da cunha", "SH");
		iso3166Alpha2.put("svalbard and jan mayen", "SJ");
		iso3166Alpha2.put("suriname", "SR");
		iso3166Alpha2.put("south sudan", "SS");
		iso3166Alpha2.put("sao tome and principe", "ST");
		iso3166Alpha2.put("sint maarten (dutch part)", "SX");
		iso3166Alpha2.put("syrian arab republic", "SY");
		iso3166Alpha2.put("eswatini", "SZ");
		iso3166Alpha2.put("turks and caicos islands", "TC");
		iso3166Alpha2.put("chad", "TD");
		iso3166Alpha2.put("french southern territories", "TF");
		iso3166Alpha2.put("tajikistan", "TJ");
		iso3166Alpha2.put("timor-leste", "TL");
		iso3166Alpha2.put("turkey", "TR");
		iso3166Alpha2.put("trinidad and tobago", "TT");
		iso3166Alpha2.put("taiwan, province of china", "TW");
		iso3166Alpha2.put("tanzania, united republic of", "TZ");
		iso3166Alpha2.put("ukraine", "UA");
		iso3166Alpha2.put("united states minor outlying islands", "UM");
		iso3166Alpha2.put("united states of america", "US");
		iso3166Alpha2.put("uzbekistan", "UZ");
		iso3166Alpha2.put("holy see", "VA");
		iso3166Alpha2.put("saint vincent and the grenadines", "VC");
		iso3166Alpha2.put("venezuela (bolivarian republic of)", "VE");
		iso3166Alpha2.put("virgin islands (british)", "VG");
		iso3166Alpha2.put("virgin islands (u.s.)", "VI");
		iso3166Alpha2.put("viet nam", "VN");
		iso3166Alpha2.put("wallis and futuna", "WF");
		iso3166Alpha2.put("yemen", "YE");
		iso3166Alpha2.put("south africa", "ZA");
		// andre språk, forekommer ofte
		iso3166Alpha2.put("polska", "PL");
		iso3166Alpha2.put("polend", "PL");
	}

	/**
	 * Mapper fra navn på land til ISO3166 Alpha 2 kode.
	 *
	 * @param land Navn på land. Kan være siste ordet i en setning.
	 *             Eksempel: Sverige
	 *             Eksempel: 1234 Stockholm, Sverige.
	 * @return Optional med ISO3166 Alpha2 kode eller Optional.empty hvis ingen treff. Eksempel: SE
	 */
	static Optional<String> fromLand(final String land) {
		return Stream.of(fromExactMatch(land), fromSisteOrd(land), fromFoersteOrd(land))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}

	/**
	 * Mapper fra navn på land til ISO3166 Alpha 2 kode.
	 *
	 * @param land Fullt navn på land. Eksempel: Sverige
	 * @return Optional med ISO3166 Alpha2 kode eller Optional.empty hvis ingen treff. Eksempel: SE
	 */
	private static Optional<String> fromExactMatch(final String land) {
		if (isBlank(land)) {
			return Optional.empty();
		}

		final String landKey = land.toLowerCase();
		if (iso3166Alpha2.containsKey(landKey)) {
			return Optional.of(iso3166Alpha2.get(landKey));
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Mapper fra land som første enkeltord i en String til ISO3166 Alpha 2 kode.
	 *
	 * @param land Setning med land i siste ord. Eksempel: 1234 Stockholm, Sverige.
	 * @return Optional med ISO3166 Alpha2 kode eller Optional.empty hvis ingen treff. Eksempel: SE
	 */
	private static Optional<String> fromFoersteOrd(final String land) {
		if (isBlank(land)) {
			return Optional.empty();
		}

		final Matcher matcher = FOERSTE_ORD.matcher(land);
		if (matcher.find()) {
			return fromExactMatch(matcher.group(1));
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Mapper fra land som siste enkeltord i en String til ISO3166 Alpha 2 kode.
	 *
	 * @param land Setning med land i siste ord. Eksempel: Sverige, 1234 Stockholm.
	 * @return Optional med ISO3166 Alpha2 kode eller Optional.empty hvis ingen treff. Eksempel: SE
	 */
	private static Optional<String> fromSisteOrd(final String land) {
		if (isBlank(land)) {
			return Optional.empty();
		}

		final Matcher matcher = SISTE_ORD.matcher(land);
		if (matcher.find()) {
			return fromExactMatch(matcher.group(1));
		} else {
			return Optional.empty();
		}
	}
}

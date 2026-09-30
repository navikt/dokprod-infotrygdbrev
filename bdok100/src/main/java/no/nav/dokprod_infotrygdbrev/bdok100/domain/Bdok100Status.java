package no.nav.dokprod_infotrygdbrev.bdok100.domain;

/**
 * Statuses in BDOK100
 *
 */
public enum Bdok100Status {
	UNDER_INNLESNING_LPF,
	UNDER_INNLESNING_JFF,
	MAPPING_LPF,
	MAPPING_JFF,
	TIL_BEHANDLING,
	BEHANDLET,
	FEILET_GSAK,
	KAN_IKKE_BEHANDLES,
	TIL_RAPPORT,
}

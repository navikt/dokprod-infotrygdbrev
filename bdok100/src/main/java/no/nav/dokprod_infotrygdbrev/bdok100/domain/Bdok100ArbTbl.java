package no.nav.dokprod_infotrygdbrev.bdok100.domain;

import io.hypersistence.utils.hibernate.type.json.JsonBlobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status.TIL_RAPPORT;

/**
 * Main object for BDOK100 worktable
 *
 */
@Slf4j
@Data
@Builder
@EqualsAndHashCode(of = "idnr")
@ToString(exclude = "brevtekster")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ARBTB_BDOK100")
public class Bdok100ArbTbl {

	private static int maxFeiletLength = getFeilstatusLength();
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Id
	@NotNull
	@Pattern(regexp = "\\w{16}")
	@Column(name = "id_nr", updatable = false)
	private String idnr;

	// raw linjedata from file
	@Lob
	@Column(name = "linjeprint_brev", length = 50_000)
	private String lineprintBrev;
	// raw journaldata from file
	@Column(name = "journalforingsfil", length = 1000)
	private String journalforingsfil;

	// Gsak
	@Pattern(regexp = "\\d+")
	@Column(name = "saks_id")
	private String saksID;
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private Bdok100Status status;
	@Column(name = "feilstatus", length = 1000)
	private String feilstatus;
	@Column(name = "vedlegg1")
	private String vedlegg1;
	@Column(name = "vedlegg2")
	private String vedlegg2;
	@Column(name = "vedlegg3")
	private String vedlegg3;

	// mapped classes
	@Embedded
	@Valid
	private Linjedata linjedata;
	@Embedded
	@Valid
	private Journaldata journaldata;

	@Singular("brevtekst")
	@Type(JsonBlobType.class)
	@Column(name = "brevtekster")
	private List<Bdok100Brevtekst> brevtekster;

	public void failAvvik(String message) {
		setStatus(Bdok100Status.KAN_IKKE_BEHANDLES);
		setFeilstatus(message);
		log.warn(String.format("Brevbestilling med id %s kan ikke behandles og er sendt til avviksfil: %s", idnr, message));
	}

	public void failRapport(String message) {
		setStatus(TIL_RAPPORT);
		setFeilstatus(message);
		log.info(String.format("Brevbestilling med id %s ignoreres: %s", idnr, message));
	}

	public void setFeilstatus(String feilstatus) {
		this.feilstatus = StringUtils.abbreviate(feilstatus, maxFeiletLength);
	}

	// Default values for builder
	public static class Bdok100ArbTblBuilder {
		private Bdok100Status status = Bdok100Status.UNDER_INNLESNING_LPF;
	}

	@SneakyThrows
	private static int getFeilstatusLength() {
		return Bdok100ArbTbl.class.getDeclaredField("feilstatus").getAnnotation(Column.class).length();
	}

	public List<Bdok100Brevtekst> getBrevtekster() {
		if (brevtekster == null) {
			return new ArrayList<>();
		}
		return Collections.unmodifiableList(brevtekster.stream().sorted(Comparator.comparingInt(Bdok100Brevtekst::getRekkefolge)).collect(Collectors.toList()));
	}
}

package no.nav.dokprod.infotrygdbrev.bdok100.config;

import java.nio.file.Path;

public record Bdok100BatchParameters(
	Path inputFileLocation,
	Path staticInputFileLocation,
	Path outputFileLocation,
	Path failedFileLocation,
	Path behandletFileLocation,
	int workUnit,
	int maxFailures
) {

	// // batchens parametre (fra confluence)                                          // fremover
	// startTime               // standard // tidspunkt start batch       // påkrevd   // LocalDateTime.now() e.l.
	// workUnit                // standard // chunksize for batch         // påkrevd   // fra config
	// progressInterval        // standard // hvor ofte logges progress   // optional  // droppes
	// inputFileLocation       // bdok100  // inputfiler                  // påkrevd   // statisk / fra sftp
	// staticInputFileLocation // bdok100  // statiske inputfiler         // påkrevd   // statisk / fra sftp
	// outputFileLocation      // bdok100  // rapporter output            // påkrevd   // statisk
	// maxFailures             // bdok100  // max feil før jobb avbrytes  // påkrevd   // fra config
	// failedFileLocation      // bdok100  // Metabatch BMF001 fail       // påkrevd   // statisk
	// behandletFileLocation   // bdok100  // Metabatch BMF001 success    // påkrevd   // statisk
	// skipClean               // bdok100  // ikke rydd etter kjøring     // optional  // fra config?
	// clean                   // bdok100  // rydd arbeidstab., ikke kjør // optional  // fra config?

}

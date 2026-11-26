package no.nav.dokprod.infotrygdbrev.bdok100.support;

public interface RemoteReportWriter {

	void writeFile(String targetRelativePath, byte[] content);
}

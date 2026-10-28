package no.nav.dokprod.infotrygdbrev.sftp;

import no.nav.dokprod.infotrygdbrev.bdok100.config.DokprodInfotrygdbrevProperties;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SftpFileService {

	private final SftpRemoteFileTemplate remoteFileTemplate;
	private final DokprodInfotrygdbrevProperties.SftpProperties sftpProperties;

	public SftpFileService(
			SftpRemoteFileTemplate remoteFileTemplate,
			DokprodInfotrygdbrevProperties dokprodInfotrygdbrevProperties
	) {
		this.remoteFileTemplate = remoteFileTemplate;
		this.sftpProperties = dokprodInfotrygdbrevProperties.sftp();
	}

	public Set<String> listFiles() {
		return Arrays.stream(remoteFileTemplate.list(sftpProperties.remoteDirectory()))
			.filter(entry -> !entry.getAttributes().isDirectory())
			.map(SftpClient.DirEntry::getFilename)
			.collect(Collectors.toUnmodifiableSet());
	}

	public Set<String> list() {
		return Arrays.stream(remoteFileTemplate.list(sftpProperties.remoteDirectory()))
				.map(SftpClient.DirEntry::getFilename)
				.collect(Collectors.toUnmodifiableSet());
	}

	public void readFile(String fileName, OutputStream outputStream) {
		boolean read = remoteFileTemplate.get(remotePath(fileName), inputStream -> inputStream.transferTo(outputStream));
		if (!read) {
			throw new IllegalStateException("SFTP file not found: " + fileName);
		}
	}

	public void createEmptyFile(String fileName) {
		remoteFileTemplate.execute(session -> {
			session.write(InputStream.nullInputStream(), remotePath(fileName));
			return null;
		});
	}

	public void deleteFile(String fileName) {
		if (!remoteFileTemplate.remove(remotePath(fileName))) {
			throw new IllegalStateException("SFTP file could not be deleted: " + fileName);
		}
	}

	private String remotePath(String fileName) {
		String remoteDirectory = sftpProperties.remoteDirectory();
		return remoteDirectory.endsWith("/") ? remoteDirectory + fileName : remoteDirectory + "/" + fileName;
	}
}

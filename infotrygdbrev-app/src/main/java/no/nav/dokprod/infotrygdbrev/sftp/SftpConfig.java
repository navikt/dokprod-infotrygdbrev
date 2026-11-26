package no.nav.dokprod.infotrygdbrev.sftp;

import no.nav.dokprod.infotrygdbrev.bdok100.config.DokprodInfotrygdbrevProperties;
import no.nav.dokprod.infotrygdbrev.bdok100.support.LocalFileService;
import no.nav.dokprod.infotrygdbrev.bdok100.support.DatabaseOutputService;
import no.nav.dokprod.infotrygdbrev.common.support.JobCompletionFileHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

@Configuration
public class SftpConfig {

	@Bean
	JobCompletionFileHandler jobCompletionFileHandler(SftpFileService sftpFileService,
													  LocalFileService localFileService,
													  DatabaseOutputService databaseOutputService) {
		return new JobCompletionFileHandler(sftpFileService, localFileService, databaseOutputService);
	}

	@Bean
	DefaultSftpSessionFactory sftpSessionFactory(DokprodInfotrygdbrevProperties dokprodInfotrygdbrevProperties) {
		var sftp = dokprodInfotrygdbrevProperties.sftp();
		DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory();
		factory.setHost(sftp.host());
		factory.setPort(sftp.port());
		factory.setUser(sftp.username());
		factory.setPrivateKey(new FileSystemResource(sftp.privateKeyFile()));
		factory.setKnownHostsResource(new FileSystemResource(sftp.knownHostsFile()));
		factory.setAllowUnknownKeys(false);
		factory.setTimeout(Math.toIntExact(sftp.timeout().toMillis()));
		factory.setPrivateKeyPassphrase(sftp.privateKeyPassphrase());
		return factory;
	}

	@Bean
	SftpRemoteFileTemplate sftpRemoteFileTemplate(DefaultSftpSessionFactory sessionFactory) {
		return new SftpRemoteFileTemplate(sessionFactory);
	}

}

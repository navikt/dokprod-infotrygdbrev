package no.nav.dokprod_infotrygdbrev.sftp;

import no.nav.dokprod_infotrygdbrev.bdok100.config.DokprodInfotrygdbrevProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

@Configuration
public class SftpConfig {

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

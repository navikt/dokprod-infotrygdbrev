package no.nav.dokprod_infotrygdbrev.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Test replacement for the production {@code DataSource}/{@code EntityManagerFactory}/
 * {@code PlatformTransactionManager}, which in production are auto-configured by Spring Boot
 * (via {@code spring.datasource.*} properties) in the {@code app} module - unavailable here since
 * the batch tests boot a plain Spring {@code AnnotationConfigApplicationContext},
 * not a Spring Boot application context.
 * <p>
 * Uses an in-memory HSQLDB database built with Spring Boot's {@link DataSourceBuilder} and
 * {@link EntityManagerFactoryBuilder} - the same building blocks Spring Boot's own
 * autoconfiguration uses internally - for minimal manual configuration.
 */
@Configuration
public class RepositoryTestConfig {

	@Value("classpath:org/springframework/batch/core/schema-hsqldb.sql")
	private Resource batchSchema;

	@Bean
	public DataSource dataSource() {
		return DataSourceBuilder.create()
				.driverClassName("org.hsqldb.jdbc.JDBCDriver")
				.url("jdbc:hsqldb:mem:bdok100test;DB_CLOSE_DELAY=-1")
				.username("sa")
				.password("")
				.build();
	}

	@Bean
	public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
		DataSourceInitializer initializer = new DataSourceInitializer();
		initializer.setDataSource(dataSource);
		initializer.setDatabasePopulator(new ResourceDatabasePopulator(batchSchema));
		return initializer;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
		Map<String, Object> jpaProperties = new HashMap<>();
		// in-memory test database: let Hibernate derive the domain schema from the @Entity classes
		jpaProperties.put("hibernate.hbm2ddl.auto", "create-drop");

		return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), ds -> jpaProperties, null)
				.dataSource(dataSource)
				.packages("no.nav.dokprod_infotrygdbrev.bdok100.domain")
				.build();
	}

	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}
}

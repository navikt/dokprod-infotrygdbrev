package no.nav.dokprod_infotrygdbrev.bdok100.config.config.bdok100;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.ProduserIkkeRedigerbartDokument;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.Brevdata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100JmsWriter;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.Brevdata4445MapperImpl;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.BrevdataMapper;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.ProduserIkkeRedigerbartDokumentXmlMapper;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.NAVKontors;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.VedleggsLister;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.database.ItemSqlParameterSourceProvider;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaCursorItemReader;
import org.springframework.batch.infrastructure.item.jms.JmsItemWriter;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import java.util.Map;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.WORK_UNIT_KEY;
import static no.nav.dokprod_infotrygdbrev.bdok100.config.config.Bdok100Config.createArbTblReaderWithoutQuery;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status.BEHANDLET;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status.TIL_BEHANDLING;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.ProduserIkkeRedigerbartDokumentXmlMapper.BDOK100_PREFIX;

/**
 * Step for writing XML output
 *
 */
@Slf4j
@Configuration
public class XmlWriterStepConfiguration extends AbstractBdok100StepConfig {

	@Bean
	public Step xmlDokumentbestillingWriterStep(
		ItemReader<Bdok100ArbTbl> tilXmlArbtblReader,
		ItemProcessor<Bdok100ArbTbl, ProduserIkkeRedigerbartDokument> dokumentbestillingItemProcessor,
		CompositeItemWriter<ProduserIkkeRedigerbartDokument> xmlDokumentbestillingWriter,
		JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
		return new StepBuilder("xmlDokumentbestillingWriterStep", jobRepository)
				.<Bdok100ArbTbl, ProduserIkkeRedigerbartDokument>chunk(workUnitCompletionPolicy, platformTransactionManager)
				.reader(tilXmlArbtblReader)
				.processor(dokumentbestillingItemProcessor)
				.writer(xmlDokumentbestillingWriter)

				.exceptionHandler(bdok100ExceptionHandler)
				.listener(logContextListener)
				.listener(bdok100BatchCounterLogger)
				.listener(workUnitCompletionPolicy)
				.allowStartIfComplete(true)
				.build();
	}

	@Bean
	@StepScope
	public JpaCursorItemReader<Bdok100ArbTbl> tilXmlArbtblReader(
			EntityManagerFactory entityManager,
			@Value("#{jobParameters[" + WORK_UNIT_KEY + "]}") Integer workUnit) {
		JpaCursorItemReader<Bdok100ArbTbl> reader = createArbTblReaderWithoutQuery(entityManager);
		reader.setQueryString("select distinct arbtbl from Bdok100ArbTbl arbtbl "
				+ "where arbtbl.status = '" + TIL_BEHANDLING + "'");
		// denne kjørte egentlig med en statelesssession som har noen fordeler. man kan vurdere om
		// man vil fikle mer med denne for å eventuelt få dette til å bli mer smartlike / smartwise
		// Hintet under skal i hvertfall (?) sørge for at det ikke blir gjort noen writes men YMMV
		reader.setHintValues(Map.of("org.hibernate.readOnly", true));
		return reader;
	}

	@Bean
	public ItemProcessor<Bdok100ArbTbl, ProduserIkkeRedigerbartDokument> dokumentbestillingItemProcessor(
			final ProduserIkkeRedigerbartDokumentXmlMapper produserIkkeRedigerbartDokumentXmlMapper) {
		return new ItemProcessor<Bdok100ArbTbl, ProduserIkkeRedigerbartDokument>() {
			@Override
			public ProduserIkkeRedigerbartDokument process(Bdok100ArbTbl item) {
				return produserIkkeRedigerbartDokumentXmlMapper.map(item);
			}
		};
	}

	@Bean
	public ProduserIkkeRedigerbartDokumentXmlMapper produserIkkeRedigerbartDokumentXmlMapper(BrevdataMapper brevdata4445Mapper) {
		ProduserIkkeRedigerbartDokumentXmlMapper xmlMapper = new ProduserIkkeRedigerbartDokumentXmlMapper();
		xmlMapper.setBrevdataMapper(brevdata4445Mapper);
		return xmlMapper;
	}

	@Bean
	@StepScope
	public BrevdataMapper brevdata4445Mapper(
			NAVKontors navKontors,
			VedleggsLister vedleggsLister) {
		Brevdata4445MapperImpl mapper = new Brevdata4445MapperImpl();
		mapper.setNavKontors(navKontors);
		mapper.setVedleggs(vedleggsLister);
		return mapper;
	}

	@StepScope
	@SuppressWarnings("unchecked")
	@Bean(destroyMethod = "")
	public CompositeItemWriter<ProduserIkkeRedigerbartDokument> xmlDokumentbestillingWriter(
			JdbcBatchItemWriter<ProduserIkkeRedigerbartDokument> tilBehandletWriter,
			JmsItemWriter<ProduserIkkeRedigerbartDokument> bdok100XmlWriter
	) {
		CompositeItemWriter<ProduserIkkeRedigerbartDokument> writer = new CompositeItemWriter<>();
		writer.setDelegates(Lists.newArrayList(bdok100XmlWriter, tilBehandletWriter));
		return writer;
	}

	@StepScope
	@Bean(destroyMethod = "")
	@SuppressWarnings("unchecked")
	public JmsItemWriter<ProduserIkkeRedigerbartDokument> bdok100XmlWriter(final JmsTemplate jmsBdok100Template){
		JmsItemWriter<ProduserIkkeRedigerbartDokument> jmsWriter = new Bdok100JmsWriter(jmsBdok100Template);
		return jmsWriter;
	}

	@Bean
	public Marshaller dokumentbestillingMarshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(Brevdata.class);
		return marshaller;
	}

	@Bean(destroyMethod = "")
	public JdbcBatchItemWriter<ProduserIkkeRedigerbartDokument> tilBehandletWriter(DataSource dataSource) {
		JdbcBatchItemWriter<ProduserIkkeRedigerbartDokument> writer = new JdbcBatchItemWriter<>();
		writer.setDataSource(dataSource);
		writer.setAssertUpdates(false);
		writer.setSql("UPDATE ARBTB_BDOK100 SET "
				+ "status = :status "
				+ "WHERE id_nr = :id");
		writer.setItemSqlParameterSourceProvider(new ItemSqlParameterSourceProvider<ProduserIkkeRedigerbartDokument>() {
			@Override
			public SqlParameterSource createSqlParameterSource(ProduserIkkeRedigerbartDokument item) {
				MapSqlParameterSource source = new MapSqlParameterSource();
				source.addValue("id", getBdok100ArbtbIdNr(item.getDokumentbestillingsinformasjon().getBestillingsId()));
				source.addValue("status", BEHANDLET.name());
				return source;
			}
		});
		return writer;
	}

	private String getBdok100ArbtbIdNr(String bestillingsId) {
		return bestillingsId.substring(BDOK100_PREFIX.length());
	}


}

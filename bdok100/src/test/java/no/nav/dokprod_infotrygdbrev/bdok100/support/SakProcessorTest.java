package no.nav.dokprod_infotrygdbrev.bdok100.support;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;
import no.nav.brevogarkiv.batch.common.BatchCounter;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Journaldata;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.Bdok100GsakMapper;
import no.nav.dokprod_infotrygdbrev.consumer.FinnEllerOpprettSakTo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.scheduling.annotation.AsyncResult;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link SakProcessor}
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SakProcessorTest {

	public static final String SAKID = "sakid";
	@Mock
	private AsyncSakConsumer gsakConsumerMock;
	@Mock
	private BatchCounter batchCounterMock;
	@Mock
	private Bdok100GsakMapper bdok100GsakMapper;
	@Mock
	private ExecutionContext jobExecutionContext;

	@InjectMocks
	private SakProcessor sakProcessor;

	private Bdok100ArbTbl arbTbl = Bdok100ArbTbl.builder()
			.status(Bdok100Status.MAPPING_JFF)
			.journaldata(Journaldata.builder().avsendMottakID("1").build())
			.build();
	private Bdok100ArbTbl arbTblFailMap = Bdok100ArbTbl.builder()
			.status(Bdok100Status.MAPPING_JFF)
			.journaldata(Journaldata.builder().avsendMottakID("2").build())
			.build();
	private Bdok100ArbTbl arbTblFailGsak = Bdok100ArbTbl.builder()
			.status(Bdok100Status.MAPPING_JFF)
			.journaldata(Journaldata.builder().avsendMottakID("3").build())
			.build();

	@Before
	public void setUp() throws Exception {
		FinnEllerOpprettSakTo requestTo = FinnEllerOpprettSakTo.builder().brukerId("1").build();
		FinnEllerOpprettSakTo requestToFail = FinnEllerOpprettSakTo.builder().brukerId("2").build();

		when(bdok100GsakMapper.map(arbTbl.getJournaldata())).thenReturn(requestTo);
		when(bdok100GsakMapper.map(arbTblFailGsak.getJournaldata())).thenReturn(requestToFail);
		when(bdok100GsakMapper.map(arbTblFailMap.getJournaldata())).thenThrow(new RuntimeException("map"));
		when(gsakConsumerMock.finnEllerOpprettSakWithCache(requestTo)).thenReturn(new AsyncResult<>(SAKID));
		SettableFuture<String> future = SettableFuture.create();
		future.setException(new RuntimeException("sak"));
		when(gsakConsumerMock.finnEllerOpprettSakWithCache(requestToFail)).thenReturn(future);
	}

	@Test
	public void shouldCallGsak() throws Exception {
		sakProcessor.write(new Chunk<>(Lists.newArrayList(arbTbl)));

		assertThat(arbTbl.getStatus(), is(Bdok100Status.TIL_BEHANDLING));
		assertThat(arbTbl.getSaksID(), is(SAKID));
	}

	@Test
	public void shouldSetFailedIfFailMap() throws Exception {
		sakProcessor.write(new Chunk<>(Lists.newArrayList(arbTblFailMap)));

		assertThat(arbTblFailMap.getStatus(), is(Bdok100Status.KAN_IKKE_BEHANDLES));
		assertThat(arbTblFailMap.getFeilstatus(), is("Sak feilet mapping: map"));
		assertThat(arbTblFailMap.getSaksID(), nullValue());
	}

	@Test
	public void shouldSetFailedIfFailGsak() throws Exception {
		sakProcessor.write(new Chunk<>(Lists.newArrayList(arbTblFailGsak)));

		assertThat(arbTblFailGsak.getStatus(), is(Bdok100Status.FEILET_GSAK));
		assertThat(arbTblFailGsak.getFeilstatus(), is("Sak feilet sak"));
		assertThat(arbTblFailGsak.getSaksID(), nullValue());
	}
}
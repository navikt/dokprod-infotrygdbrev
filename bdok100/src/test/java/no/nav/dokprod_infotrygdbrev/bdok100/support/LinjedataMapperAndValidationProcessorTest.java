package no.nav.dokprod_infotrygdbrev.bdok100.support;

import static no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status.MAPPING_LPF;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.brevogarkiv.batch.common.BatchCounter;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.validation.LinjedataValidator;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.LinjedataMapper;
import no.nav.dokprod_infotrygdbrev.bdok100.support.processors.LinjedataMapperAndValidationProcessor;
import no.nav.dokprod_infotrygdbrev.common.exception.AvviksfilException;
import no.nav.dokprod_infotrygdbrev.common.exception.KontrollRapportException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit test for {@link LinjedataMapperAndValidationProcessor}
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class LinjedataMapperAndValidationProcessorTest {

	@Mock
	private LinjedataMapper linjedataMapperMock;
	@Mock
	private LinjedataValidator linjedataValidatorMock;
	@Mock
	private BatchCounter batchCounterMock;

	@InjectMocks
	private LinjedataMapperAndValidationProcessor processor;

	private Bdok100ArbTbl bdok100ArbTbl = Bdok100ArbTbl.builder()
			.status(MAPPING_LPF)
			.lineprintBrev("linjedata")
			.build();

	@Test
	public void shouldMapAndValidate() throws Exception {
		processor.process(bdok100ArbTbl);

		verify(linjedataMapperMock).map(bdok100ArbTbl);
		verify(linjedataValidatorMock).validate(bdok100ArbTbl);
		assertThat(bdok100ArbTbl.getStatus(), is(MAPPING_LPF));
	}

	@Test
	public void shouldFailRowsWithoutLinjedata() throws Exception {
		bdok100ArbTbl.setLineprintBrev(null);
		processor.process(bdok100ArbTbl);

		assertThat(bdok100ArbTbl.getStatus(), is(Bdok100Status.KAN_IKKE_BEHANDLES));
		assertThat(bdok100ArbTbl.getFeilstatus(), containsString("mangler linjedata"));
	}

	@Test
	public void shouldCatchExceptionsFromMapper() throws Exception {
		when(linjedataMapperMock.map(bdok100ArbTbl)).thenThrow(new IllegalStateException("something went wrong"));

		processor.process(bdok100ArbTbl);
		assertThat(bdok100ArbTbl.getStatus(), is(Bdok100Status.KAN_IKKE_BEHANDLES));
		assertThat(bdok100ArbTbl.getFeilstatus(), containsString("mapping error"));
		assertThat(bdok100ArbTbl.getFeilstatus(), containsString("something went wrong"));
	}

	@Test
	public void shouldCatchManAvvikExceptions() throws Exception {
		doThrow(new AvviksfilException("fatal validation error")).when(linjedataValidatorMock).validate(bdok100ArbTbl);

		processor.process(bdok100ArbTbl);
		assertThat(bdok100ArbTbl.getStatus(), is(Bdok100Status.KAN_IKKE_BEHANDLES));
		assertThat(bdok100ArbTbl.getFeilstatus(), containsString("fatal validation error"));
	}

	@Test
	public void shouldCatchKontrollRapportExceptions() throws Exception {
		doThrow(new KontrollRapportException("rapport validation error"))
				.when(linjedataValidatorMock).validate(bdok100ArbTbl);

		processor.process(bdok100ArbTbl);
		assertThat(bdok100ArbTbl.getStatus(), is(Bdok100Status.TIL_RAPPORT));
		assertThat(bdok100ArbTbl.getFeilstatus(), containsString("rapport validation error"));
	}
}
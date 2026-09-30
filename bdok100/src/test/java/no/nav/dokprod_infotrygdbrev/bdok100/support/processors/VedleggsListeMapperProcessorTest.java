package no.nav.dokprod_infotrygdbrev.bdok100.support.processors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Lists;

import no.nav.brevogarkiv.batch.common.BatchCounter;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Events;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe.Vedlegg;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.validation.VedleggsListeValidator;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.VedleggsLister;

/**
 * Unit test for {@link VedleggsListeMapperProcessor}
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class VedleggsListeMapperProcessorTest {
	private static final String BREVKODE = "brevkode";
	private static final Vedlegg VEDLEGG_1 = new Vedlegg("vedlegg1");
	private static final Vedlegg VEDLEGG_2 = new Vedlegg("vedlegg2");

	@Mock private BatchCounter bdok100BatchCounter;
	@Mock private VedleggsLister vedleggsLister;
	@Mock private VedleggsListeValidator validator;
	
	@InjectMocks
	private VedleggsListeMapperProcessor processor;

	@Test
	public void shouldRunProcess() throws Exception {
		VedleggsListe processedVedleggsListe = processor.process(BREVKODE + "," + VEDLEGG_1.getVedleggsKode() + "," + VEDLEGG_2.getVedleggsKode());
		
		VedleggsListe expectedVedleggsListe = new VedleggsListe(BREVKODE, Lists.newArrayList(VEDLEGG_1, VEDLEGG_2));
		verify(validator).validate(expectedVedleggsListe);
		verify(vedleggsLister).add(expectedVedleggsListe);
		verify(bdok100BatchCounter).incrementEvent(Bdok100Events.VEDLEGGSLISTE_LOADED);
		assertThat(processedVedleggsListe, is(expectedVedleggsListe));
	}

	
}
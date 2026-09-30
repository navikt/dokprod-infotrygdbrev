package no.nav.dokprod_infotrygdbrev.bdok100.support;

import no.nav.dokprod_infotrygdbrev.consumer.FinnEllerOpprettSakTo;
import no.nav.dokprod_infotrygdbrev.consumer.SakService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.Future;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit test for AsyncSakConsumer
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AsyncSakConsumerTest {

    public static final String ID = "id";

    @Mock
    private SakService sakServiceMock;
    @InjectMocks
    private AsyncSakConsumer consumer;

    @Before
    public void setUp() throws Exception {
        consumer.setRetries(1);
        consumer.setRetryDelay(5);
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldCallConsumer() throws Exception {
        FinnEllerOpprettSakTo to = FinnEllerOpprettSakTo.builder().build();
        when(sakServiceMock.finnEllerOpprettArkivsak(to)).thenReturn(ID);

        Future<String> future = consumer.finnEllerOpprettSakWithCache(to);
        assertThat(future.get(), is(ID));
    }

    @Test
    public void shouldRetry() throws Exception {
        FinnEllerOpprettSakTo to = FinnEllerOpprettSakTo.builder().build();
        when(sakServiceMock.finnEllerOpprettArkivsak(to))
                .thenThrow(new RuntimeException("e")).thenReturn(ID);

        Future<String> future = consumer.finnEllerOpprettSakWithCache(to);
        assertThat(future.get(), is(ID));
    }

    @Test
    public void shouldFailAfterRetries() throws Exception {
        expectedException.expectMessage("gsak failed2");
        FinnEllerOpprettSakTo to = FinnEllerOpprettSakTo.builder().build();
        when(sakServiceMock.finnEllerOpprettArkivsak(to))
                .thenThrow(new RuntimeException("gsak failed")).thenThrow(new RuntimeException("gsak failed2")).thenReturn(ID);

        consumer.finnEllerOpprettSakWithCache(to);
    }
}
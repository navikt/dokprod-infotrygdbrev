package no.nav.dokprod_infotrygdbrev.bdok100.support.readers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.batch.infrastructure.item.support.SingleItemPeekableItemReader;

/**
 * Unit test for {@link MultiLineItemReader}
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiLineItemReaderTest {

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private SingleItemPeekableItemReader<String> delegateMock;
	@InjectMocks
	private MultiLineItemReader multiLineItemReader;

	@Before
	public void setUp() throws Exception {
		multiLineItemReader.setNewRecordPrefix("1");
		delegateMock.setDelegate(new ListItemReader<>(Lists.newArrayList("1x", "2y", "1y", "3x", "1")));
	}

	@Test
	public void shouldReadRecords() throws Exception {
		Assert.assertThat(multiLineItemReader.read(), is("1x\r\n2y"));
		Assert.assertThat(multiLineItemReader.read(), is("1y\r\n3x"));
		Assert.assertThat(multiLineItemReader.read(), is("1"));
		Assert.assertThat(multiLineItemReader.read(), nullValue());
	}

	@Test
	public void shouldDelegateOpen() throws Exception {
		ExecutionContext executionContext = new ExecutionContext();
		multiLineItemReader.open(executionContext);
		verify(delegateMock).open(executionContext);
	}

	@Test
	public void shouldDelegateClose() throws Exception {
		multiLineItemReader.close();
		verify(delegateMock).close();
	}
}
package no.nav.dokprod_infotrygdbrev.bdok100.support.readers;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.CRLF;

import com.google.common.base.Joiner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemStream;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.support.SingleItemPeekableItemReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Reader that uses a peekable FlatFileReader to read multiline records
 *
 */
public class MultiLineItemReader implements ItemReader<String>, ItemStream {
	private SingleItemPeekableItemReader<String> delegate;
	private String newRecordPrefix;

	private List<String> current = new ArrayList<>();

	@Override
	public String read() throws Exception {
		String peek;
		while ((peek = delegate.peek()) != null) {
			if (peek.startsWith(newRecordPrefix) && !current.isEmpty()) {
				return readOut();
			} else {
				current.add(delegate.read());
			}
		}
		if (!current.isEmpty()) {
			return readOut();
		} else {
			return null;
		}
	}

	private String readOut() {
		String read = Joiner.on(CRLF).join(current);
		current = new ArrayList<>();
		return read;
	}

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		delegate.open(executionContext);
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
	}

	@Override
	public void close() throws ItemStreamException {
		delegate.close();
	}

	public void setNewRecordPrefix(String newRecordPrefix) {
		this.newRecordPrefix = newRecordPrefix;
	}

	public void setDelegate(SingleItemPeekableItemReader<String> delegate) {
		this.delegate = delegate;
	}

}

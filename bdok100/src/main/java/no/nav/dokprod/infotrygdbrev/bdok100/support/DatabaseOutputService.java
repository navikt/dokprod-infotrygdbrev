package no.nav.dokprod.infotrygdbrev.bdok100.support;

import no.nav.dokprod.infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod.infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamWriter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import static no.nav.dokprod.infotrygdbrev.bdok100.Bdok100Constants.CRLF;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.getAvviksrapport;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.getJFFAvvikFilename;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.getLPFAvvikFilename;

public class DatabaseOutputService {

	private static final String INSERT = "INSERT INTO BDOK100_AVVIKSRAPPORT_LINE (JOB_INSTANCE_ID, FILE_TYPE, LINE_CONTENT) VALUES (?, ?, ?)";
	private final JdbcTemplate jdbcTemplate;

	public DatabaseOutputService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public enum AvvikType {
		LINJEPRINTFIL(Bdok100Constants.BDOK100_INPUT_CHARSET, CRLF),
		JOURNALFORINGSFIL(Bdok100Constants.BDOK100_INPUT_CHARSET, CRLF),
		REPORT(Bdok100Constants.BDOK100_OUTPUT_CHARSET, System.lineSeparator());

		private final Charset charset;
		private final String lineSeparator;

		AvvikType(Charset charset, String lineSeparator) {
			this.charset = charset;
			this.lineSeparator = lineSeparator;
		}

		public String filename(Date startTime) {
			return switch (this) {
				case LINJEPRINTFIL -> getLPFAvvikFilename(startTime);
				case JOURNALFORINGSFIL -> getJFFAvvikFilename(startTime);
				case REPORT -> getAvviksrapport(startTime);
			};
		}
	}

	public ItemStreamWriter<Bdok100ArbTbl> writer(long jobInstanceId, AvvikType type,
												 Function<Bdok100ArbTbl, String> line) {
		return new ItemStreamWriter<>() {
			@Override
			public void open(ExecutionContext executionContext) {
				jdbcTemplate.update("DELETE FROM BDOK100_AVVIKSRAPPORT_LINE WHERE JOB_INSTANCE_ID = ? AND FILE_TYPE = ?",
					jobInstanceId, type.name());
				jdbcTemplate.update(INSERT, jobInstanceId, type.name(), null);
			}

			@Override
			public void write(Chunk<? extends Bdok100ArbTbl> items) {
				List<Object[]> rows = new ArrayList<>();
				for (Bdok100ArbTbl item : items) {
					String value = line.apply(item);
					if (value != null) {
						rows.add(new Object[]{jobInstanceId, type.name(), value});
					}
				}
				if (!rows.isEmpty()) {
					jdbcTemplate.batchUpdate(INSERT, rows);
				}
			}

			@Override
			public void update(ExecutionContext executionContext) {
			}

			@Override
			public void close() {
			}
		};
	}

	public List<AvvikType> filesForJob(long jobInstanceId) {
		return jdbcTemplate.query(
			"SELECT DISTINCT FILE_TYPE FROM BDOK100_AVVIKSRAPPORT_LINE WHERE JOB_INSTANCE_ID = ? ORDER BY FILE_TYPE",
			(rs, _) -> AvvikType.valueOf(rs.getString(1)), jobInstanceId);
	}

	public InputStream openStream(long jobInstanceId, AvvikType type) {
		return new InputStream() {
			private final ArrayDeque<byte[]> pending = new ArrayDeque<>();
			private long lastId;
			private byte[] current = new byte[0];
			private int offset;
			private boolean exhausted;

			@Override
			public int read() throws IOException {
				byte[] single = new byte[1];
				return read(single, 0, 1) < 0 ? -1 : single[0] & 0xff;
			}

			@Override
			public int read(byte[] buffer, int start, int length) throws IOException {
				java.util.Objects.checkFromIndexSize(start, length, buffer.length);
				if (length == 0) {
					return 0;
				}
				while (offset == current.length) {
					if (pending.isEmpty() && !exhausted) {
						var page = jdbcTemplate.query(
							"SELECT ID, LINE_CONTENT FROM BDOK100_AVVIKSRAPPORT_LINE "
								+ "WHERE JOB_INSTANCE_ID = ? AND FILE_TYPE = ? AND ID > ? ORDER BY ID LIMIT 100",
							(rs, _) -> new OutputLine(rs.getLong(1), rs.getString(2)),
							jobInstanceId, type.name(), lastId);
						exhausted = page.size() < 100;
						for (OutputLine row : page) {
							lastId = row.id();
							if (row.content() != null) {
								pending.add((row.content() + type.lineSeparator).getBytes(type.charset));
							}
						}
					}
					if (pending.isEmpty()) {
						return -1;
					}
					current = pending.remove();
					offset = 0;
				}
				int count = Math.min(length, current.length - offset);
				System.arraycopy(current, offset, buffer, start, count);
				offset += count;
				return count;
			}
		};
	}

	public void deleteForJob(long jobInstanceId) {
		jdbcTemplate.update("DELETE FROM BDOK100_AVVIKSRAPPORT_LINE WHERE JOB_INSTANCE_ID = ?", jobInstanceId);
	}

	public void deleteAll() {
		jdbcTemplate.update("DELETE FROM BDOK100_AVVIKSRAPPORT_LINE");
	}

	private record OutputLine(long id, String content) {
	}
}

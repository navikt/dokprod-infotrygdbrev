package no.nav.dokprod_infotrygdbrev.serializer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for ConverterCapableXStreamExecutionContextStringSerializer
 *
 */
public class ConverterCapableXStreamExecutionContextStringSerializerTest {

	public static final String DATE = "2015-04-21";
	public static final String JSON = "{\"map\":[{\"entry\":{\"string\":\"startDate\",\"org.joda.time.LocalDate\":\"2015-04-21\"}}]}";

	private ConverterCapableXStreamExecutionContextStringSerializer serializer =
			new ConverterCapableXStreamExecutionContextStringSerializer();

	@Before
	public void setUp() throws Exception {
		serializer.addConverter(new LocalDateConverter());
		serializer.afterPropertiesSet();
	}

	@Test
	public void shouldSerialize() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		LocalDate parse = LocalDate.parse(DATE);

		HashMap<String, Object> stringObjectMap = new HashMap<>();
		stringObjectMap.put("startDate", parse);
		serializer.serialize(stringObjectMap, out);

		assertThat(out.toString(), is(JSON));
	}

	@Test
	public void shouldDeserialize() throws Exception {
		Map<String, Object> deserialize = serializer.deserialize(new ByteArrayInputStream(JSON.getBytes()));

		assertThat(deserialize.get("startDate"), instanceOf(LocalDate.class));
		assertThat(deserialize.get("startDate").toString(), is(DATE));
	}
}

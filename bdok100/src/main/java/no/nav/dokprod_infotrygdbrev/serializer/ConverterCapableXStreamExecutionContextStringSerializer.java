package no.nav.dokprod_infotrygdbrev.serializer;

/*
 * Copyright 2006-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
// import org.springframework.batch.core.repository.dao.XStreamExecutionContextStringSerializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.serializer.Deserializer;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation that uses XStream and Jettison to provide serialization.
 * Based on XStreamExecutionContextStringSerializer
 *
 * @see ExecutionContextSerializer
 * @see XStreamExecutionContextStringSerializer
 * @since 2.0
 */
public class ConverterCapableXStreamExecutionContextStringSerializer
		implements ExecutionContextSerializer, InitializingBean {

	private XStream xstream;
	private List<SingleValueConverter> singleValueConverters = new ArrayList<>();
	private List<Converter> converters = new ArrayList<>();

	public void addConverter(SingleValueConverter singleValueConverter) {
		this.singleValueConverters.add(singleValueConverter);
	}

	public void addConverter(Converter converter) {
		this.converters.add(converter);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}

	public synchronized void init() throws Exception {
		xstream = new XStream(new JettisonMappedXmlDriver());
		xstream.allowTypesByWildcard(new String[] {
				"no.nav.dokprod.**",
				"no.nav.dokdist.**",
				"org.joda.time.**"
		});
		for (SingleValueConverter converter : singleValueConverters) {
			xstream.registerConverter(converter);
		}
		for (Converter converter : converters) {
			xstream.registerConverter(converter);
		}
	}

	@Override
	public void serialize(Map<String, Object> stringObjectMap, OutputStream outputStream) throws IOException {
		Assert.notNull(stringObjectMap, "stringObjectMap cannot be null");
		Assert.notNull(outputStream, "outputStream cannot be null");
		outputStream.write(xstream.toXML(stringObjectMap).getBytes());
	}

	/**
	 * Deserializes the supplied input stream into a new execution context.
	 *
	 * @param in
	 * @return a reconstructed execution context
	 * @see Deserializer#deserialize(InputStream)
	 */
	@Override
	public Map<String, Object> deserialize(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		StringBuilder sb = new StringBuilder();

		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}

		return (Map<String, Object>) xstream.fromXML(sb.toString());
	}
}

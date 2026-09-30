package no.nav.dokprod_infotrygdbrev.common.support;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import lombok.AllArgsConstructor;
import lombok.Data;

public class CSVWrapperFieldExtractorTest {
	
	
	@Test
	public void extractsAnObjectToArrayEncloseStringWithEnclosingChar() throws Exception {
		CSVWrapperFieldExtractor<TestObject> extractor = new CSVWrapperFieldExtractor<>();
		String[] names = {"aString" , "aDate", "aLong"};
		extractor.setNames(names);		
		LocalDateTime now = LocalDateTime.now();
		
		Object[] extract = extractor.extract(new TestObject("stringTest", 1L, now , null));
		
		assertThat(extract.length, is(3));
		assertThat((String)extract[0], is("\"stringTest\""));
		assertThat((Long)extract[2], is(1L));
		assertThat((LocalDateTime)extract[1], is(now));
		
	}
	
	@Data
	@AllArgsConstructor
	private class TestObject {
		private String aString;
		private Long aLong;
		private LocalDateTime aDate;
		private Integer aInt;
	}

}

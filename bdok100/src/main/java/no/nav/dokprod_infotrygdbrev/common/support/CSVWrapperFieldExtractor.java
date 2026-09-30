package no.nav.dokprod_infotrygdbrev.common.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.batch.infrastructure.item.file.transform.FieldExtractor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;

/**
 * FieldExtractorWrapper, puts enclosing character around strings for use in CSV files
 *
 */
public class CSVWrapperFieldExtractor<T> implements FieldExtractor<T> {
	private String[] names;
	private String enclosingChar = "\"";

	public void setNames(String[] names) {
		Assert.notNull(names, "Names must be non-null");
		this.names = Arrays.asList(names).toArray(new String[names.length]);
	}

	public void setEnclosingChar(String enclosingChar) {
		Assert.notNull(enclosingChar, "Enclosing characted must be non-null");
		this.enclosingChar = enclosingChar;
	}

	@Override
	public Object[] extract(T item) {
		List<Object> values = new ArrayList<Object>();

		BeanWrapper bw = new BeanWrapperImpl(item);
		for (String propertyName : this.names) {
			if(bw.getPropertyValue(propertyName) instanceof String) {
				values.add(enclosingChar + bw.getPropertyValue(propertyName) + enclosingChar);
			} else {
				values.add(bw.getPropertyValue(propertyName));
			}

		}
		return values.toArray();
	}
}

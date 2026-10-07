package no.nav.brevogarkiv.batch.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Copied from Stelvio.
 * 
 * Formats batch counting info, input data is {@link BatchCounter#getEventReport()}.
 * @author Ole Hjalmar Herje, BEKK
 */
public class TableFormatter implements EventReportFormatter {

	/**
	 * Formats a report into tabular form
	 * @param eventReport Report to format
	 * @return Tabular string representation
	 */
	public String format(Map<CounterEvent, ? extends EventCounter> eventReport) {
		List<Entry<CounterEvent, ? extends EventCounter>> events = sortEvents(eventReport);
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append("================================ BATCH COUNTERS ================================================================================\n");		
		sb.append("|Name                          |Count     |Time(ms)  |Avg(ms)   |Type           |Description                                    |\n");
		sb.append("|-------------------------------------------------------------------------------------------------------------------------------|\n");		
		for (Entry<CounterEvent, ? extends EventCounter> event : events) {
			String format = "|%1$-30s|%2$10s|%3$10s|%4$10s|%5$-15s|%6$-47s|\n";
			sb.append(String.format(format, 
					event.getKey().getName(),
					event.getValue().getCount(),
					event.getValue().getTime(),
					event.getValue().getAvg(), 
					event.getKey().getType(),
					event.getKey().getDescription()));
		}
		sb.append("================================================================================================================================\n");
		return sb.toString();
	}

	private List<Entry<CounterEvent, ? extends EventCounter>> sortEvents(Map<CounterEvent, ? extends EventCounter> eventReport) {
		List<Entry<CounterEvent, ? extends EventCounter>> events = 
			new ArrayList<Entry<CounterEvent, ? extends EventCounter>>(eventReport.entrySet());
		Collections.sort(events, new Comparator<Entry<CounterEvent, ? extends EventCounter>>() {
					public int compare(
							Entry<CounterEvent, ? extends EventCounter> o1,
							Entry<CounterEvent, ? extends EventCounter> o2) {
						return o1.getKey().getName().compareToIgnoreCase(o2.getKey().getName());
					}
				});
		return events;
	}

}

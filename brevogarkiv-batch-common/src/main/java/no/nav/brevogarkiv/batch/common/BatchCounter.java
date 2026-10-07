package no.nav.brevogarkiv.batch.common;

import java.util.Map;

/**
 * Copied from Stelvio.
 * 
 * Defines counting operations.
 * 
 * @author Ole Hjalmar Herje, BEKK
 */
public interface BatchCounter {
	
	/**
	 * Starts an event.
	 * @param event Event to start.
	 */
	void start(CounterEvent event);
	
	
	/**
	 * Stops an event and sets time. The event counter will be incremented.
	 * @param event Event to stop.
	 */
	void stop(CounterEvent event);
	
	/**
	 * Stops an event and sets time. The event counter will be increased by given value.
	 * @param event Event to stop.
	 * @param count Value to add to event counter.
	 */
	void stop(CounterEvent event, int count);	
	
	/**
	 * Increments the event counter by 1.
	 * @param event Event to increment.
	 */
	void incrementEvent(CounterEvent event);
	
	/**
	 * Increments the event counter by 1 and adds given value to time.
	 * @param event Event to modify.
	 * @param ms Milliseconds to add to event time.
	 */
	void incrementEvent(CounterEvent event, long ms);
	
	/**
	 * Increases the event counter by given value.
	 * @param event Event to increase.
	 * @param count Value to add to event counter.
	 */
	void addEvents(CounterEvent event, long count);
	

	/**
	 * Increases the event counter by given value and adds given value to time.
	 * @param event Event to increase.
	 * @param count Value to add to event counter.
	 * @param ms Milliseconds to add to event time.
	 */
	void addEvents(CounterEvent event, long count, long ms);
	

	/**
	 * Gets the events and counters for the events.
	 * @return A map containing events as key and counters as values.
	 */
	Map<CounterEvent, ? extends EventCounter> getEventReport();

	/**
	 * Resets all event-counters. 
	 */
	void resetCounter();
}


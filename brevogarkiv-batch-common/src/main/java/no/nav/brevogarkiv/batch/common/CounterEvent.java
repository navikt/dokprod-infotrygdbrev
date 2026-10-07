package no.nav.brevogarkiv.batch.common;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copied from Stelvio.
 * <p>
 * A batch event that can be counted by a {@link BatchCounter}.
 *
 * @author Ole Hjalmar Herje, BEKK
 */
public final class CounterEvent {
	private static ConcurrentHashMap<Class<?>, Set<CounterEvent>> registeredEvents =
			new ConcurrentHashMap<Class<?>, Set<CounterEvent>>();

	/**
	 * Defines different types of {@link CounterEvent}.
	 *
	 * @author Ole Hjalmar Herje, BEKK
	 */
	public enum EventType {
		/**
		 *
		 */
		FUNCTIONAL,
		/**
		 *
		 */
		TECHNICAL
	}

	private final String name;
	private final String description;
	private final EventType type;

	private CounterEvent(String name, String description, EventType type) {
		this.name = name;
		this.description = description;
		this.type = type;
	}

	/**
	 * Creates a new event and registrates it.
	 *
	 * @param eventClass  Class that defines the event
	 * @param name        Name of event
	 * @param description Description of event
	 * @param type        Type of event
	 * @return The newly created event
	 */
	public static CounterEvent createCounterEvent(Class<?> eventClass, String name, String description, EventType type) {
		CounterEvent event = new CounterEvent(name, description, type);
		Set<CounterEvent> events = registeredEvents.get(eventClass);
		if (events == null) {
			Set<CounterEvent> newEventSet = new HashSet<CounterEvent>();
			events = registeredEvents.putIfAbsent(eventClass, newEventSet);
			if (events == null) {
				events = newEventSet;
			}
		}
		events.add(event);
		return event;
	}

	/**
	 * @return the registeredEvents
	 */
	public static Map<Class<?>, Set<CounterEvent>> getRegisteredEvents() {
		return registeredEvents;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CounterEvent that = (CounterEvent) o;
		return Objects.equals(name, that.name) && Objects.equals(description, that.description) && type == that.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, type);
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the type
	 */
	public EventType getType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "name=" + name + ", description=" + description + ", type=" + type;
	}

}

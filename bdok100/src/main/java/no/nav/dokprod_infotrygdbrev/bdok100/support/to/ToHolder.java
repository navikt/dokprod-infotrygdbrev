package no.nav.dokprod_infotrygdbrev.bdok100.support.to;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.TreeSet;

/**
 * Parent class for TO holders
 *
 */
public abstract class ToHolder<K, V> {

	private final Map<K, V> keyToValueMap = Maps.newConcurrentMap();

	protected void put(K key, V value) {
		if (key != null) {
			keyToValueMap.put(key, value);
		}
	}

	V getValue(K key) {
		if (keyToValueMap.containsKey(key)) {
			return keyToValueMap.get(key);
		} else {
			return null;
		}
	}

	public int size() {
		return keyToValueMap.size();
	}

	public void clear() {
		keyToValueMap.clear();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (K key : new TreeSet<>(keyToValueMap.keySet())) {
			sb.append("[key=" + key + ",value=["+ keyToValueMap.get(key) +"]]," + '\n');
		}
		sb.append("]");
		return sb.toString();
	}
}

package se.wetterstrom.jfuncgen;

/**
 * A key-value pair
 * @param <K> the key type
 * @param <V> the value type
 */
public class Pair<K extends Comparable<K>,V> implements Comparable<K> {

	final K key;
	V value;

	/**
	 * Constructor
	 * @param key the key
	 * @param value the value
	 */
	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public String toString() {
		return String.valueOf(key) + " = " + value;
	}

	@Override
	public int compareTo(K o) {
		return o.compareTo(key);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Pair<?,?> p && key.equals(p.key);
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}
}
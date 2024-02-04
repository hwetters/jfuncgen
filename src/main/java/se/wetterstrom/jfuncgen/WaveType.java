package se.wetterstrom.jfuncgen;

/**
 * Wave type
 */
class WaveType implements Comparable<WaveType> {
	final int id;
	final String name;

	/**
	 * Constructor
	 * @param id the id
	 * @param name the name
	 */
	public WaveType(int id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof WaveType that && this.id == that.id;
	}

	@Override
	public int compareTo(WaveType other) {
		return this.name.compareTo(other.name);
	}

	@Override
	public String toString() {
		return name;
	}
}
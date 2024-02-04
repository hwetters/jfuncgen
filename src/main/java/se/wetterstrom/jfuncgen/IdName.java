package se.wetterstrom.jfuncgen;

import java.util.Optional;

/**
 * Id name
 */
public class IdName extends Pair<Integer,String> {

	/**
	 * Constructor
	 * @param key the id key.
	 * @param value the value
	 * @throws NullPointerException if key is null
	 */
	public IdName(Integer key, String value) {
		super(Optional.ofNullable(key).orElseThrow(NullPointerException::new), value);
	}
}
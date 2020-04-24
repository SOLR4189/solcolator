package solcolator.luwak;

import java.util.Arrays;

/**
 * Factories for matching docs:
 * 		SimpleMatcher (simple) - reports which queries matched the InputDocument
 * 		HighlightingMatcher (highlighting) - reports which queries matched, with the individual matches for each query
*/

public enum LuwakMatcherFactory {
	HIGHLIGHTING,
	SIMPLE;
	
	/**
	 * If given factory exists, function will return its enum value otherwise will throw exception.
	 * Function is case insensitive to value of kindName
	 * @param kindName
	 * @return enum value of kind name or exception
	 * @throws Exception 
	 */
	public static LuwakMatcherFactory get(String kindName) throws Exception {
		for (LuwakMatcherFactory kind : LuwakMatcherFactory.values()) {
			if (kind.name().equalsIgnoreCase(kindName)) {
				return kind;
			}
		}
		
		throw new Exception(String.format("Not existing luwak matcher factory - %s. Legal kinds: %s",
				kindName, Arrays.asList(LuwakMatcherFactory.values())));
	}
}

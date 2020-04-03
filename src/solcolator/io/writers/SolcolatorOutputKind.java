package solcolator.io.writers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enum of possible writers:
 * FILE - for test and integration
 * COLLECTION - for production
 * KAFKA - for production (still not cheked)
 * TAGGER - for production
 *
 */
public enum SolcolatorOutputKind {
	FILE,
	COLLECTION,
	KAFKA,
	TAGGER;
	
	/**
	 * If given kindName exists, function will return its enum value otherwise will throw exception.
	 * Function case insensitive to value of kindName
	 * @param kindNameStr - kind names string separated by comma
	 * @return list of enum value of kind name or exception
	 * @throws Exception 
	 */
	public static List<SolcolatorOutputKind> get(String kindNameStr) throws Exception {
		List<SolcolatorOutputKind> returnKinds = new ArrayList<>();
		
		for (String kindName : kindNameStr.split(",")) {
			try {
				SolcolatorOutputKind kindNameEnum = SolcolatorOutputKind.valueOf(kindName.toUpperCase());
				if (kindNameEnum != null) {
					returnKinds.add(kindNameEnum);
				} 
			} catch (IllegalArgumentException e) {
					throw new Exception(String.format("Not existing percolator output kind - %s. Legal kinds: %s",
							kindName, Arrays.asList(SolcolatorOutputKind.values())));
			}
		}
		
		return returnKinds;
	}
}

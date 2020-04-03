package solcolator.io.readers;

import java.util.Arrays;

public enum SolcolatorInputKind {
	DIRECTORY;
	
	/**
	 * If given kindName exists, function will return its enum value otherwise will throw exception.
	 * Function case insensitive to value of kindName
	 * @param kindName
	 * @return enum value of kind name or exception
	 * @throws Exception - Not existing solcolator input kind
	 */
	public static SolcolatorInputKind get(String kindName) throws Exception {
		for (SolcolatorInputKind kind : SolcolatorInputKind.values()) {
			if (kind.name().equalsIgnoreCase(kindName)) {
				return kind;
			}
		}
		
		throw new Exception(String.format("Not existing solcolator input kind - %s. Legal kinds: %s",
				kindName, Arrays.asList(SolcolatorInputKind.values())));
	}
}

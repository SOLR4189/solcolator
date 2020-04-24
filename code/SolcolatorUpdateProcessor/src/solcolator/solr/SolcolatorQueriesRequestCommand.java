package solcolator.solr;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum SolcolatorQueriesRequestCommand {
	UPDATE,		// add a new query OR update a existing query
	DELETE,		// delete query
	REFRESH,	// refresh queries in Luwak (it is used for dynamic values like 'NOW')
	REREAD;		// load all queries from the source (file/db/etc..)

	/**
	 * If given request command exists, function will return its enum value otherwise will return null.
	 * Function case insensitive to value of request command
	 * @param reqCommand
	 * @return enum value of request command or null
	 */
	public static SolcolatorQueriesRequestCommand getIfExists(String reqCommand) {
		for (SolcolatorQueriesRequestCommand kind : SolcolatorQueriesRequestCommand.values()) {
			if (kind.name().equalsIgnoreCase(reqCommand)) {
				return kind;
			}
		}
		
		return null;
	}
	
	public static String toPrint() {
		List<String> values = Arrays.asList(SolcolatorQueriesRequestCommand.values()).stream().map(x -> x.name()).collect(Collectors.toList());
		String str = String.format("Supported commands: %s", String.join(",", values));
		
		return str;
	}
}

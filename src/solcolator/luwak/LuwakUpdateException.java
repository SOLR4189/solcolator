package solcolator.luwak;

import java.util.List;

import uk.co.flax.luwak.QueryError;

public class LuwakUpdateException {
	public static String getPrintableErrorString(List<QueryError> errors) {
		StringBuilder printableString = new StringBuilder("\r\n");
		
		for (QueryError error : errors) {
			printableString.append("\r\nQuery: ");
			printableString.append(error.query);
			printableString.append("\r\nError: ");
			printableString.append(error.error);
			printableString.append("\r\n");
		}

		return printableString.toString();
	}
}

package solcolator.config;

/**
 * An exception thrown from the initialization of a SolrPluginConfiguration. It
 * derives from runtime exception as it can be thrown on a SolrPlugin's init
 * function, and therefore cannot be declared explicitly
 * 
 *
 */
public class SolrConfigurationInitializationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SolrConfigurationInitializationException(String message) {
		super(message);
	}
	
	public SolrConfigurationInitializationException(Exception innerException) {
		super(innerException);
	}
	
	public SolrConfigurationInitializationException(String message, Exception innerException) {
		super(message, innerException);
	}
}
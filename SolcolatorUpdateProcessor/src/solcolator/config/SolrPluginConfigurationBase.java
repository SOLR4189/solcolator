package solcolator.config;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.solr.common.util.NamedList;

/**
 * A base class for classes representing a configuration of a certain custom
 * Solr plug-in, providing functionality for initializing the configuration
 * class' fields. It should usually be derived from and used together with
 * fields having the ConfigField annotation. However, in case the configuration
 * contains only few fields it can be instantiated and used directly through the
 * variety of get...Field methods.
 * 
 *
 */
@SuppressWarnings("rawtypes")
public class SolrPluginConfigurationBase {

	/**
	 * The NamedList passed to the current instance when calling
	 * {@link com.solr.SolrPluginConfigurationBase#initializeConfiguration(NamedList)}
	 */
	protected NamedList args;

	/**
	 * Creates and initializes a new instance of a SolrPluginConfiguration,
	 * assigning values to all fields containing the ConfigField annotation in
	 * the current instance
	 * 
	 * @param args
	 *            The NamedList used to initialize the SolrPluginConfiguration
	 * @throws SolrConfigurationInitializationException
	 *             If one of the configuration's fields didn't meet requested
	 *             criteria
	 */
	public SolrPluginConfigurationBase(NamedList args) throws SolrConfigurationInitializationException {
		initializeConfiguration(args);
	}

	private void initializeConfiguration(NamedList args) throws SolrConfigurationInitializationException {
		this.args = args;
		for (Field field : this.getClass().getDeclaredFields()) {
			ConfigField[] configFieldAnnotations = field.getAnnotationsByType(ConfigField.class);
			if (configFieldAnnotations.length == 0) {
				continue;
			}

			ConfigField configFieldAnnotation = configFieldAnnotations[0]; // Non-repeatable
			field.setAccessible(true); // In order to modify private fields
			Object value = getValueForField(configFieldAnnotation);
			setValueForField(field, configFieldAnnotation, value);
		}
	}

	/**
	 * Gets an Integer parameter from the NamedList. If the parameter isn't
	 * mandatory and isn't in the NamedList, returns null. If it is mandatory
	 * and doesn't exist, throws an exception. Also throws an exception if the
	 * parameter exists exists but isn't an integer
	 * 
	 * @param parameterName
	 *            The name of the parameter to retrieve
	 * @param isMandatory
	 *            Whether the parameter must be specified in the configuration
	 * @return The value of the Integer parameter
	 */
	public Integer getIntParameter(String parameterName, boolean isMandatory) {
		return getParameter(parameterName, isMandatory, Integer.class);
	}

	/**
	 * Gets a Long parameter from the NamedList. If the parameter isn't
	 * mandatory and isn't in the NamedList, returns null. If it is mandatory
	 * and doesn't exist, throws an exception. Also throws an exception if the
	 * parameter exists exists but isn't a long
	 * 
	 * @param parameterName
	 *            The name of the parameter to retrieve
	 * @param isMandatory
	 *            Whether the parameter must be specified in the configuration
	 * @return The value of the Long parameter
	 */
	public Long getLongParameter(String parameterName, boolean isMandatory) {
		return getParameter(parameterName, isMandatory, Long.class);
	}

	/**
	 * Gets a Float parameter from the NamedList. If the parameter isn't
	 * mandatory and isn't in the NamedList, returns null. If it is mandatory
	 * and doesn't exist, throws an exception. Also throws an exception if the
	 * parameter exists exists but isn't a float
	 * 
	 * @param parameterName
	 *            The name of the parameter to retrieve
	 * @param isMandatory
	 *            Whether the parameter must be specified in the configuration
	 * @return The value of the Float parameter
	 */
	public Float getFloatParameter(String parameterName, boolean isMandatory) {
		return getParameter(parameterName, isMandatory, Float.class);
	}

	/**
	 * Gets a Double parameter from the NamedList. If the parameter isn't
	 * mandatory and isn't in the NamedList, returns null. If it is mandatory
	 * and doesn't exist, throws an exception. Also throws an exception if the
	 * parameter exists exists but isn't a double
	 * 
	 * @param parameterName
	 *            The name of the parameter to retrieve
	 * @param isMandatory
	 *            Whether the parameter must be specified in the configuration
	 * @return The value of the Double parameter
	 */
	public Double getDoubleParameter(String parameterName, boolean isMandatory) {
		return getParameter(parameterName, isMandatory, Double.class);
	}

	/**
	 * Gets a String parameter from the NamedList. If the parameter isn't
	 * mandatory and isn't in the NamedList, returns null. If it is mandatory
	 * and doesn't exist, throws an exception. Also throws an exception if the
	 * parameter exists exists but isn't a string
	 * 
	 * @param parameterName
	 *            The name of the parameter to retrieve
	 * @param isMandatory
	 *            Whether the parameter must be specified in the configuration
	 * @return The value of the String parameter
	 */
	public String getStringParameter(String parameterName, boolean isMandatory) {
		return getParameter(parameterName, isMandatory, String.class);
	}

	/**
	 * Gets a Boolean parameter from the NamedList. If the parameter isn't
	 * mandatory and isn't in the NamedList, returns null. If it is mandatory
	 * and doesn't exist, throws an exception. Also throws an exception if the
	 * parameter exists exists but isn't a boolean
	 * 
	 * @param parameterName
	 *            The name of the parameter to retrieve
	 * @param isMandatory
	 *            Whether the parameter must be specified in the configuration
	 * @return The value of the Boolean parameter
	 */
	public Boolean getBooleanParameter(String parameterName, boolean isMandatory) {
		return getParameter(parameterName, isMandatory, Boolean.class);
	}

	/**
	 * Gets an array parameter from the NamedList. If the parameter isn't
	 * mandatory and isn't in the NamedList, returns null. If it is mandatory
	 * and doesn't exist, throws an exception. Also throws an exception if the
	 * parameter exists exists but isn't an array
	 * 
	 * @param parameterName
	 *            The name of the parameter to retrieve
	 * @param isMandatory
	 *            Whether the parameter must be specified in the configuration
	 * @return The value of the array parameter
	 */
	public List<?> getArrParameter(String parameterName, boolean isMandatory) {
		return getParameter(parameterName, isMandatory, "regular array");
	}

	/**
	 * Gets a NamedList parameter from the NamedList. If the parameter isn't
	 * mandatory and isn't in the NamedList, returns null. If it is mandatory
	 * and doesn't exist, throws an exception. Also throws an exception if the
	 * parameter exists exists but isn't a NamedList
	 * 
	 * @param parameterName
	 *            The name of the parameter to retrieve
	 * @param isMandatory
	 *            Whether the parameter must be specified in the configuration
	 * @return The value of the NamedList parameter
	 */
	public NamedList<?> getNamedListParameter(String parameterName, boolean isMandatory) {
		return getParameter(parameterName, isMandatory, "named list");
	}

	/**
	 * Gets a NamedList parameter from the NamedList, as a separate
	 * SolrPluginConfigurationBase object. If the parameter isn't mandatory and
	 * isn't in the NamedList, returns null. If it is mandatory and doesn't
	 * exist, throws an exception. Also throws an exception if the parameter
	 * exists exists but isn't a NamedList
	 * 
	 * @param parameterName
	 *            The name of the parameter to retrieve
	 * @param isMandatory
	 *            Whether the parameter must be specified in the configuration
	 * @return A SolrPluginConfigurationBase instant based on the NamedList in
	 *         the configuration
	 */
	public SolrPluginConfigurationBase getNamedListAsConfig(String parameterName, boolean isMandatory) {
		return new SolrPluginConfigurationBase(getNamedListParameter(parameterName, isMandatory));
	}

	/**
	 * Throws an exception for an event that occurred during the initialization
	 * of the configuration object
	 * 
	 * @param message
	 *            The message of the exception to throw
	 * 
	 * @return Doesn't return a value - guaranteed to throw an exception. The
	 *         return type is used to simplify code calling this method
	 */
	protected <T> T throwInitializationException(String message) {
		throw new SolrConfigurationInitializationException(message);
	}

	/**
	 * 
	 * @param parameterName
	 * @param isMandatory
	 * @param className
	 * @return The value of the parameter. If mandatory then cannot be null
	 */
	@SuppressWarnings("unchecked")
	private <T> T getParameter(String parameterName, boolean isMandatory, String className) {
		Object rawValue = args.get(parameterName);
		if (rawValue == null) {
			if (isMandatory) {
				return throwInitializationException(String.format("No %s parameter was specified", parameterName));
			}
			return null;
		}
		try {
			return (T) rawValue;
		} catch (ClassCastException ex) {
			return throwInitializationException(String.format("Parameter %s must be a %s", parameterName, className));
		}
	}

	/**
	 * 
	 * @param parameterName
	 * @param isMandatory
	 * @param className
	 * @return The value of the parameter. If mandatory then cannot be null
	 */
	private <T> T getParameter(String parameterName, boolean isMandatory, Class<T> classObj) {
		return getParameter(parameterName, isMandatory, classObj.getName());
	}

	private Object getValueForField(ConfigField configFieldAnnotation) {
		String fieldName = configFieldAnnotation.fieldName();
		boolean isMandatory = configFieldAnnotation.isMandatory();
		switch (configFieldAnnotation.fieldType()) {
		case INT: {
			return getIntParameter(fieldName, isMandatory);
		}
		case STRING: {
			return getStringParameter(fieldName, isMandatory);
		}
		case BOOLEAN: {
			return getBooleanParameter(fieldName, isMandatory);
		}
		case LONG: {
			return getLongParameter(fieldName, isMandatory);
		}
		case FLOAT: {
			return getFloatParameter(fieldName, isMandatory);
		}
		case ARRAY: {
			return getArrParameter(fieldName, isMandatory);
		}
		case NAMED_LIST: {
			return getNamedListParameter(fieldName, isMandatory);
		}
		case NAMED_LIST_AS_CONFIG: {
			return getNamedListAsConfig(fieldName, isMandatory);
		}
		default: {
			return throwInitializationException("Unsupported field type: " + configFieldAnnotation.fieldType());
		}
		}
	}

	private void setValueForField(Field field, ConfigField configFieldAnnotation, Object value) {
		try {
			if (value != null) {
				field.set(this, value);
			}
		} catch (IllegalArgumentException ex) {
			throwInitializationException(String.format("Field %s is of the wrong type. Excepted: %s, Actual: %s",
					field.getName(), configFieldAnnotation.fieldType(), field.getDeclaringClass().getName()));
		} catch (IllegalAccessException ex) {
			throwInitializationException(String.format("Field %s cannot be modified with reflection", field.getName()));
		}
	}
}
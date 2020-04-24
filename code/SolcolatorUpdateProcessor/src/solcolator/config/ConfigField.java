package solcolator.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation placed on a field of a SolrPluginConfiguration class with
 * information about the field, and is automatically initialized when
 * initializeConfiguration() is called (usually when the configuration instance
 * is created)
 * 
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigField {

	/**
	 * 
	 * @return The name (in the configuration) of the field
	 */
	String fieldName();

	/**
	 * 
	 * @return The type of the field
	 */
	ConfigFieldType fieldType();

	/**
	 * 
	 * @return Whether a value for the field must be specified in the
	 *         configuration or not
	 */
	boolean isMandatory();
}
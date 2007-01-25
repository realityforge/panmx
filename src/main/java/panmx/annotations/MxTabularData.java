package panmx.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Annotation to define a type potentially as a tabular data type with specified keys.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface MxTabularData
{
   /**
    * The key names for data. Must be the names of MX attributes.
    */
   String[] keys();
}

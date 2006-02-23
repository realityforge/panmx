package panmx.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to describe metadata about an operation or constructor parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface MxParameter
{
    /** The name of parameter. */
    String name();

    /** The description for management parameter. */
    String description() default "";
}

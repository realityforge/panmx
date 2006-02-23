package panmx.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate method is a managed operation.
 */
@Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MxOperation
{
    /** The display name for managemed element. */
    String displayName() default "";

    /** The description for managemed element. */
    String description() default "";

    /** Descriptor fields for managemed element. */
    MxField[] fields() default {};

    /** The impact of managed operation. */
    Impact impact() default Impact.ACTION;
}

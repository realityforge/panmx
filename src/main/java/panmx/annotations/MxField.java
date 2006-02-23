package panmx.annotations;

/**
 * Annotation to describe descriptor field.
 */
public @interface MxField
{
    /** The field name. */
    String name();
    /** The field value. */
    String value();
}

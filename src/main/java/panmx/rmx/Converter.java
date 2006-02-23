package panmx.rmx;

import java.lang.reflect.Type;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;

/**
 * Classes to convert from an OpenType to a Java type should
 * implement this interface.
 */
interface Converter
{
    /**
     * @return the Class representing the Java type.
     */
    Type getJavaType();

    /**
     * @return the OpenType that the Java type is converted to.
     */
    OpenType getOpenType();

    /**
     * Convert the specified object from its Java type to its OpenType.
     *
     * @param object the object to convert.
     * @return the converted object.
     */
    Object toOpenType( Object object )
        throws OpenDataException;

    /**
     * Convert the specified object from its OpenType to its Java type.
     *
     * @param object the object to convert.
     * @return the converted object.
     */
    Object toJavaType( Object object )
        throws OpenDataException;
}

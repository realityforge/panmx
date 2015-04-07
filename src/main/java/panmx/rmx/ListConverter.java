package panmx.rmx;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;

/**
 * Converter to translate Lists to arrays and back again.
 */
class ListConverter
    implements Converter
{
    /** Java type. */
    private final Type m_javaType;
    /** Open type. */
    private final ArrayType m_openType;
    /** The convert for component type. */
    private final Converter m_componentConverter;
    /** Types of component when converting from OpenType to Java type. */
    private Class m_openJavaType;

    ListConverter( final Type javaType,
                   final Converter componentConverter )
        throws OpenDataException
    {
        m_javaType = javaType;
        //TODO: Ensure component is not an array type or if it is change next line
        m_openType = new ArrayType( 1, componentConverter.getOpenType() );
        m_componentConverter = componentConverter;
        final String component = m_openType.getElementOpenType().getClassName();
        try
        {
            m_openJavaType = Class.forName( component );
        }
        catch( final ClassNotFoundException cnfe )
        {
            final String message =
                "Component class (" + component + ") is not in system class path and " +
                "thus does not conform to OpenMBean specification.";
            final IllegalArgumentException exception = new IllegalArgumentException( message );
            exception.initCause( cnfe );
            throw exception;
        }
    }

    public Type getJavaType()
    {
        return m_javaType;
    }

    public OpenType getOpenType()
    {
        return m_openType;
    }

    public Object toOpenType( final Object object )
        throws OpenDataException
    {
        final List list = (List)object;
        final Object[] array =
            (Object[])Array.newInstance( m_openJavaType, list.size() );
        int i = 0;
        for( final Object element : list )
        {
            array[i++] = m_componentConverter.toOpenType( element );
        }

        return array;
    }

    public Object toJavaType( final Object object )
        throws OpenDataException
    {
        final Object[] array = (Object[])object;
        final ArrayList<Object> list = new ArrayList<Object>( array.length );
        for( final Object element : array )
        {
            list.add( m_componentConverter.toJavaType( element ) );
        }

        return list;
    }
}

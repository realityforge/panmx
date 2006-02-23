package panmx.rmx;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * Converter to translate non-SimpleType Arrays to OpenType arrays and back again.
 */
class ArrayConverter
    implements Converter
{
    /** Java type. */
    private final Class m_javaType;
    /** Open type. */
    private final ArrayType m_openType;
    /** The convert for component type. */
    private final Converter m_componentConverter;
    /** Types of each dimension when converting from Java type to OpenType. */
    private final Type[] m_javaTypes;
    /** Types of each dimension when converting from OpenType to Java type. */
    private final Class[] m_openJavaTypes;

    ArrayConverter( final Class javaType,
                    final int dimensions,
                    final Converter componentConverter )
        throws OpenDataException
    {
        m_javaType = javaType;
        m_openType = new ArrayType( dimensions, SimpleType.STRING );
        m_componentConverter = componentConverter;

        final int size = m_openType.getDimension();

        m_javaTypes = new Class[size];
        m_javaTypes[0] = m_componentConverter.getJavaType();
        for( int i = 1; i < size; i++ )
        {
            final Object value = Array.newInstance( (Class)m_javaTypes[i - 1], 0 );
            m_javaTypes[i] = value.getClass();
        }

        m_openJavaTypes = new Class[size];
        final String component = m_componentConverter.getOpenType().getClassName();
        try
        {
            m_openJavaTypes[0] = Class.forName( component );
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
        for( int i = 1; i < size; i++ )
        {
            final Object value = Array.newInstance( m_openJavaTypes[i - 1], 0 );
            m_openJavaTypes[i] = value.getClass();
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
        final int level = m_openType.getDimension() - 1;
        return toOpenType( object, level );
    }

    private Object toOpenType( final Object object, final int level )
        throws OpenDataException
    {
        if( null == object )
        {
            return null;
        }
        else
        {
            final Object[] array = (Object[])object;
            final Object[] newArray =
                (Object[])Array.newInstance( m_openJavaTypes[level], array.length );
            if( 0 == level )
            {
                for( int i = 0; i < array.length; i++ )
                {
                    newArray[i] = m_componentConverter.toOpenType( array[i] );
                }
            }
            else
            {
                final int nextLevel = level - 1;
                for( int i = 0; i < array.length; i++ )
                {
                    newArray[i] = toOpenType( array[i], nextLevel );
                }
            }

            return newArray;
        }
    }

    public Object toJavaType( final Object object )
        throws OpenDataException
    {
        return toJavaType( object, m_openType.getDimension() - 1 );
    }

    private Object toJavaType( final Object object, final int level )
        throws OpenDataException
    {
        if( null == object )
        {
            return null;
        }
        else
        {
            final Object[] array = (Object[])object;
            final Object[] newArray =
                (Object[])Array.newInstance( (Class)m_javaTypes[level], array.length );
            if( 0 == level )
            {
                for( int i = 0; i < array.length; i++ )
                {
                    newArray[i] = m_componentConverter.toJavaType( array[i] );
                }
            }
            else
            {
                final int nextLevel = level - 1;
                for( int i = 0; i < array.length; i++ )
                {
                    newArray[i] = toJavaType( array[i], nextLevel );
                }
            }
            return newArray;
        }
    }
}

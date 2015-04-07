package panmx.rmx;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;

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
        m_openType = new ArrayType( dimensions, componentConverter.getOpenType() );
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
        return convertArray( object, m_openType.getDimension() - 1, m_openJavaTypes, false );
    }

    public Object toJavaType( final Object object )
        throws OpenDataException
    {
       return convertArray( object, m_openType.getDimension() - 1, m_javaTypes, true );
    }

   private Object convertArray( final Object object,
                                final int level,
                                final Type[] javaTypes,
                                final boolean toJava )
      throws OpenDataException
   {
      if( null == object )
      {
          return null;
      }
      else
      {
         if( 0 == level )
          {
              final int length = Array.getLength( object );
              final Object newArray =
                  Array.newInstance( (Class) javaTypes[ level ], length );

              for( int i = 0; i < length; i++ )
              {
                 final Object v = Array.get( object, i );
                 final Object value =
                     toJava ?
                     m_componentConverter.toJavaType( v ) :
                     m_componentConverter.toOpenType( v );
                  Array.set( newArray, i, value );
              }
              return newArray;
          }
          else
          {
              final Object[] array = (Object[]) object;
              final Object[] newArray =
                  (Object[]) Array.newInstance( (Class) javaTypes[ level ], array.length );

              final int nextLevel = level - 1;
              for( int i = 0; i < array.length; i++ )
              {
                  newArray[i] = convertArray( array[i], nextLevel, javaTypes, toJava );
              }
              return newArray;
          }
      }
   }
}

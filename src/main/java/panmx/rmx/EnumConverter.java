package panmx.rmx;

import java.lang.reflect.Type;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * Converter to translate Enum types to strings and back again.
 */
class EnumConverter
    implements Converter
{
    /** Java type. */
    private final Class<? extends Enum> m_javaType;

    EnumConverter( final Class<? extends Enum> enumType )
    {
        m_javaType = enumType;
    }

    public Type getJavaType()
    {
        return m_javaType;
    }

    public OpenType getOpenType()
    {
        return SimpleType.STRING;
    }

    public Object toOpenType( final Object object )
    {
        if( null == object )
        {
            return null;
        }
        else
        {
            final Enum value = (Enum)object;
            return value.name();
        }
    }

    public Object toJavaType( final Object object )
        throws OpenDataException
    {
        if( null == object )
        {
            return null;
        }
        else
        {
            try
            {
                final @SuppressWarnings(value = {"unchecked"}) Class<? extends Enum> javaType = m_javaType;
                return Enum.valueOf( javaType, (String)object );
            }
            catch( final IllegalArgumentException iae )
            {
                final OpenDataException exception = new OpenDataException();
                exception.initCause( iae );
                throw exception;
            }
        }
    }
}

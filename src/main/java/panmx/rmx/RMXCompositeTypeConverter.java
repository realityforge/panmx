package panmx.rmx;

import java.lang.reflect.Type;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;

/**
 * Converter for RMXCompositeTypes.
 */
class RMXCompositeTypeConverter
    implements Converter
{
    /** The RMXCompositeType type. */
    private final RMXCompositeType m_type;

    RMXCompositeTypeConverter( final RMXCompositeType type )
    {
        m_type = type;
    }

    public Type getJavaType()
    {
        return m_type.getType();
    }

    public OpenType getOpenType()
    {
        return m_type.getCompositeType();
    }

    public Object toOpenType( final Object object )
        throws OpenDataException
    {
        return m_type.toCompositeData( object );
    }

    public Object toJavaType( final Object object )
        throws OpenDataException
    {
        return m_type.fromCompositeData( (CompositeData)object );
    }
}

package panmx.rmx;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import javax.management.ObjectName;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * Pass-through convert for simple types.
 */
class SimpleTypeConverter
    implements Converter
{
    static final Converter WRAPPER_BOOLEAN = new SimpleTypeConverter( Boolean.class, SimpleType.BOOLEAN );
    static final Converter BOOLEAN = new SimpleTypeConverter( Boolean.TYPE, SimpleType.BOOLEAN );
    static final Converter WRAPPER_CHARACTER = new SimpleTypeConverter( Character.class, SimpleType.CHARACTER );
    static final Converter CHARACTER = new SimpleTypeConverter( Character.TYPE, SimpleType.CHARACTER );
    static final Converter WRAPPER_BYTE = new SimpleTypeConverter( Byte.class, SimpleType.BYTE );
    static final Converter BYTE = new SimpleTypeConverter( Byte.TYPE, SimpleType.BYTE );
    static final Converter WRAPPER_SHORT = new SimpleTypeConverter( Short.class, SimpleType.SHORT );
    static final Converter SHORT = new SimpleTypeConverter( Short.TYPE, SimpleType.SHORT );
    static final Converter WRAPPER_INTEGER = new SimpleTypeConverter( Integer.class, SimpleType.INTEGER );
    static final Converter INTEGER = new SimpleTypeConverter( Integer.TYPE, SimpleType.INTEGER );
    static final Converter WRAPPER_LONG = new SimpleTypeConverter( Long.class, SimpleType.LONG );
    static final Converter LONG = new SimpleTypeConverter( Long.TYPE, SimpleType.LONG );
    static final Converter WRAPPER_FLOAT = new SimpleTypeConverter( Float.class, SimpleType.FLOAT );
    static final Converter FLOAT = new SimpleTypeConverter( Float.TYPE, SimpleType.FLOAT );
    static final Converter WRAPPER_DOUBLE = new SimpleTypeConverter( Double.class, SimpleType.DOUBLE );
    static final Converter DOUBLE = new SimpleTypeConverter( Double.TYPE, SimpleType.DOUBLE );
    static final Converter STRING = new SimpleTypeConverter( String.class, SimpleType.STRING );
    static final Converter BIGDECIMAL = new SimpleTypeConverter( BigDecimal.class, SimpleType.BIGDECIMAL );
    static final Converter BIGINTEGER = new SimpleTypeConverter( BigInteger.class, SimpleType.BIGINTEGER );
    static final Converter DATE = new SimpleTypeConverter( Date.class, SimpleType.DATE );
    static final Converter OBJECTNAME = new SimpleTypeConverter( ObjectName.class, SimpleType.OBJECTNAME );
    /** Java type. */
    private final Class m_javaType;
    /** Open type. */
    private final OpenType m_openType;

    SimpleTypeConverter( final Class javaType, final OpenType openType )
    {
        m_javaType = javaType;
        m_openType = openType;
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
    {
        return object;
    }

    public Object toJavaType( final Object object )
    {
        return object;
    }
}

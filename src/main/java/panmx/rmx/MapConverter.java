package panmx.rmx;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

/**
 * Converter to translate Lists to arrays and back again.
 */
class MapConverter
    implements Converter
{
    /** Name of key field in tabular data. */
    private static final String KEY = "key";
    /** Name of value field in tabular data. */
    private static final String VALUE = "value";
    /** Fields that appear in tabular data. */
    private static final String[] ITEMS = new String[]{KEY, VALUE};
    /** Fields that uniquely identify row in tabular data. */
    private static final String[] INDEXS = new String[]{KEY};
    /** Java type. */
    private final ParameterizedType m_javaType;
    /** Open type. */
    private final TabularType m_openType;
    /** Converter for key fields. */
    private final Converter m_keyConverter;
    /** Converter for value fields. */
    private final Converter m_valueConverter;

    MapConverter( final ParameterizedType javaType,
                  final Converter keyConverter,
                  final Converter valueConverter )
        throws OpenDataException
    {
        m_javaType = javaType;
        m_keyConverter = keyConverter;
        m_valueConverter = valueConverter;

        final String name = "Map<" +
                            m_keyConverter.getOpenType().getTypeName() + "," +
                            m_valueConverter.getOpenType().getTypeName() + ">";

        final OpenType[] types = new OpenType[2];
        types[0] = m_keyConverter.getOpenType();
        types[1] = m_valueConverter.getOpenType();
        final CompositeType compositeType = new CompositeType( name, name, ITEMS, ITEMS, types );
        m_openType = new TabularType( name, name, compositeType, INDEXS );
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
        if( null == object )
        {
            return null;
        }

        final @SuppressWarnings(value = {"unchecked"}) Map<Object, Object> map =
            (Map<Object, Object>)object;
        final TabularDataSupport support = new TabularDataSupport( m_openType );
        final CompositeType type = m_openType.getRowType();

        final Iterator<Map.Entry<Object, Object>> iterator = map.entrySet().iterator();
        while( iterator.hasNext() )
        {
            final Map.Entry entry = iterator.next();
            final Object key = m_keyConverter.toOpenType( entry.getKey() );
            final Object value = m_valueConverter.toOpenType( entry.getValue() );
            final CompositeDataSupport rowSupport =
                new CompositeDataSupport( type, ITEMS, new Object[]{key, value} );
            support.put( rowSupport );
        }

        return support;
    }

    public Object toJavaType( final Object object )
        throws OpenDataException
    {
        if( null == object )
        {
            return null;
        }
        final TabularData data = (TabularData)object;
        final HashMap<Object, Object> map = new HashMap<Object, Object>();
        final Iterator iterator = data.values().iterator();
        while( iterator.hasNext() )
        {
            final CompositeData compositeData = (CompositeData)iterator.next();
            final Object keyEntry = compositeData.get( "key" );
            final Object valueEntry = compositeData.get( "value" );
            final Object key = m_keyConverter.toJavaType( keyEntry );
            final Object value = m_valueConverter.toJavaType( valueEntry );
            map.put( key, value );
        }

        return map;
    }
}

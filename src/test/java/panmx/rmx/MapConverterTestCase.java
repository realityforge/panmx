package panmx.rmx;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import junit.framework.TestCase;

public class MapConverterTestCase
    extends TestCase
{
    //DO NOT DELETE !!!!!!!!!!!!!!
    public Map<String, Integer> myMapMethod()
    {
        return null;
    }

    public void testBasicOperation()
        throws Exception
    {
        final Method method =
            MapConverterTestCase.class.getMethod( "myMapMethod", new Class[0] );
        final ParameterizedType type = (ParameterizedType)method.getGenericReturnType();

        final MapConverter converter =
            new MapConverter( type, SimpleTypeConverter.STRING, SimpleTypeConverter.INTEGER );
        assertEquals( "getJavaType()", type, converter.getJavaType() );

        final String name = "Map<java.lang.String,java.lang.Integer>";
        final String[] items = new String[]{"key", "value"};
        final OpenType[] types = new OpenType[]{SimpleType.STRING, SimpleType.INTEGER};
        final CompositeType compositeType =
            new CompositeType( name, name, items, items, types );
        final TabularType tabularType = new TabularType( name, name, compositeType, new String[]{"key"} );

        assertEquals( "getOpenType()",
                      tabularType,
                      converter.getOpenType() );

        final TabularData openValue = new TabularDataSupport( tabularType );
        final CompositeDataSupport compositeData =
            new CompositeDataSupport( compositeType, items, new Object[]{"score", new Integer( 100 )} );
        openValue.put( compositeData );
        final Object convertedOpenValue = converter.toJavaType( openValue );
        assertEquals( "toJavaType().class", HashMap.class, convertedOpenValue.getClass() );
        assertEquals( "toJavaType().size()", 1, ( (Map)convertedOpenValue ).size() );
        assertEquals( "toJavaType().get(score)",
                      new Integer( 100 ),
                      ( (Map)convertedOpenValue ).get( "score" ) );

        final Map<String, Integer> javaValue = new HashMap<String, Integer>();
        javaValue.put( "anger", 50 );
        final Object convertedJavaValue = converter.toOpenType( javaValue );

        assertEquals( "toOpenType().class", TabularDataSupport.class, convertedJavaValue.getClass() );
        assertEquals( "toOpenType().size()", 1, ( (TabularData)convertedJavaValue ).size() );
        final CompositeData resultCompositeData =
            ( (TabularData)convertedJavaValue ).get( new Object[]{"anger"} );
        assertEquals( "toOpenType().get(anger).type", compositeType, resultCompositeData.getCompositeType() );
        assertEquals( "toOpenType().get(anger).key", "anger", resultCompositeData.get("key") );
        assertEquals( "toOpenType().get(anger).value", 50, resultCompositeData.get("value") );
    }
}

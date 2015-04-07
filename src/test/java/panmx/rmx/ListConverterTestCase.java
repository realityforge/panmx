package panmx.rmx;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.SimpleType;
import junit.framework.TestCase;

public class ListConverterTestCase
    extends TestCase
{
    //DO NOT DELETE !!!!!!!!!!!!!!
    public List<String> totestParametizedTypes()
    {
        return null;
    }

    public void testBasicOperation()
        throws Exception
    {
        final Method method =
            ListConverterTestCase.class.getMethod( "totestParametizedTypes", new Class[0] );
        final Type type = method.getGenericReturnType();

        final ListConverter converter =
            new ListConverter( type, SimpleTypeConverter.STRING );
        assertEquals( "getJavaType()", type, converter.getJavaType() );
        assertEquals( "getOpenType()",
                      new ArrayType( 1, SimpleType.STRING ),
                      converter.getOpenType() );

        final String[] openValue = new String[]{"MissString"};
        final Object convertedOpenValue = converter.toJavaType( openValue );
        assertEquals( "toJavaType().class", ArrayList.class, convertedOpenValue.getClass() );
        assertEquals( "toJavaType().size()", 1, ((List)convertedOpenValue).size() );
        assertEquals( "toJavaType().get(0)", "MissString", ((List)convertedOpenValue).get(0) );

        final List<String> javaValue = new ArrayList<String>();
        javaValue.add( "MrString" );
        final Object convertedJavaValue = converter.toOpenType( javaValue );
        assertEquals( "toOpenType().class", String[].class, convertedJavaValue.getClass() );
        assertEquals( "toOpenType().length", 1, ((String[])convertedJavaValue).length );
        assertEquals( "toOpenType()[0]", "MrString", ((String[])convertedJavaValue)[0] );
    }
}

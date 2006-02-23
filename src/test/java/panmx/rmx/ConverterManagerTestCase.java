package panmx.rmx;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularType;
import junit.framework.TestCase;

public class ConverterManagerTestCase
    extends TestCase
{
    public void testGetExistingConverter()
        throws Exception
    {
        final Converter converter = ConverterManager.getConverterFor( String.class );
        assertNotNull( "getConverterFor(String.class)", converter );
        assertTrue( "converter.class", converter instanceof SimpleTypeConverter );
        assertEquals( "converter == SimpleTypeConverter.STRING",
                      SimpleTypeConverter.STRING, converter );
        assertEquals( "converter.getJavaType()", String.class, converter.getJavaType() );
        assertEquals( "converter.getOpenType()", SimpleType.STRING, converter.getOpenType() );
    }

    static enum MyEnum
    {
        A, B, C;
    }

    public void testGetEnumConverter()
        throws Exception
    {
        final Converter converter = ConverterManager.getConverterFor( MyEnum.class );
        assertNotNull( "getConverterFor(MyEnum.class)", converter );
        assertTrue( "converter.class", converter instanceof EnumConverter );
        assertEquals( "converter.getJavaType()", MyEnum.class, converter.getJavaType() );
        assertEquals( "converter.getOpenType()", SimpleType.STRING, converter.getOpenType() );
    }

    static enum MyEnum2
    {
        A, B, C;
    }

    public void testReGetConverter()
        throws Exception
    {
        final Converter converter = ConverterManager.getConverterFor( MyEnum2.class );
        assertNotNull( "getConverterFor(MyEnum2.class)", converter );
        assertTrue( "converter.class", converter instanceof EnumConverter );

        final Converter converter2 = ConverterManager.getConverterFor( MyEnum2.class );
        assertNotNull( "re-getConverterFor(MyEnum2.class)", converter2 );
        assertTrue( "converter2.class", converter2 instanceof EnumConverter );

        assertEquals( "converter == converter2", converter, converter2 );
    }

    //DO NOT DELETE !!!!!!!!!!!!!!
    public List<String> myParametizedMethodWithListReturnValue()
    {
        return null;
    }

    public void testGetListConverter()
        throws Exception
    {
        final Method method =
            ConverterManagerTestCase.class.getMethod( "myParametizedMethodWithListReturnValue", new Class[0] );
        final Type type = method.getGenericReturnType();

        final Converter converter = ConverterManager.getConverterFor( type );
        assertNotNull( "getConverterFor(List<String>)", converter );
        assertTrue( "converter.class", converter instanceof ListConverter );
        assertEquals( "converter.getJavaType()", type, converter.getJavaType() );
        assertEquals( "converter.getOpenType()",
                      new ArrayType( 1, SimpleType.STRING ),
                      converter.getOpenType() );
    }

    //DO NOT DELETE !!!!!!!!!!!!!!
    public List<?> myNonParametizedListMethod()
    {
        return null;
    }

    public void testGetNonSpecifiedGenericListConverter()
        throws Exception
    {
        final Method method =
            ConverterManagerTestCase.class.getMethod( "myNonParametizedListMethod", new Class[0] );
        final Type type = method.getGenericReturnType();

        try
        {
            ConverterManager.getConverterFor( type );
            fail( "Expected to fail with unsupported type ?" );
        }
        catch( final OpenDataException ode )
        {
            assertEquals( "ode.getMessage()", "Unsupported type: ?", ode.getMessage() );
        }
    }

    //DO NOT DELETE !!!!!!!!!!!!!!
    public Map<String, Integer> myParametizedMapMethod()
    {
        return null;
    }

    public void testGetMapConverter()
        throws Exception
    {
        final Method method =
            ConverterManagerTestCase.class.getMethod( "myParametizedMapMethod", new Class[0] );
        final Type type = method.getGenericReturnType();

        final Converter converter = ConverterManager.getConverterFor( type );
        assertNotNull( "getConverterFor(Map<String,Integer>)", converter );
        assertTrue( "converter.class", converter instanceof MapConverter );
        assertEquals( "converter.getJavaType()", type, converter.getJavaType() );

        final String name = "Map<java.lang.String,java.lang.Integer>";
        final String[] items = new String[]{"key", "value"};
        final OpenType[] types = new OpenType[]{SimpleType.STRING, SimpleType.INTEGER};
        final CompositeType compositeType =
            new CompositeType( name, name, items, items, types );
        final TabularType tabularType = new TabularType( name, name, compositeType, new String[]{"key"} );
        assertEquals( "converter.getOpenType()", tabularType, converter.getOpenType() );
    }
}

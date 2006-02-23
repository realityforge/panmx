package panmx.rmx;

import javax.management.openmbean.SimpleType;
import junit.framework.TestCase;

public class SimpleTypeConverterTestCase
    extends TestCase
{
    public void testBasicOperation()
        throws Exception
    {
        final SimpleTypeConverter converter =
            new SimpleTypeConverter( String.class, SimpleType.STRING );
        assertEquals( "getJavaType()", String.class, converter.getJavaType() );
        assertEquals( "getOpenType()", SimpleType.STRING, converter.getOpenType() );
        assertEquals( "toJavaType(MrString)", "MrString", converter.toJavaType( "MrString" ) );
        assertEquals( "toOpenType(MissString)", "MissString", converter.toOpenType( "MissString" ) );
        assertEquals( "toJavaType(null)", null, converter.toJavaType( null ) );
        assertEquals( "toOpenType(null)", null, converter.toOpenType( null ) );
    }
}

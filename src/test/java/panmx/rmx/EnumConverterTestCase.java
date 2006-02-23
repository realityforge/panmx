package panmx.rmx;

import javax.management.openmbean.SimpleType;
import javax.management.openmbean.OpenDataException;
import junit.framework.TestCase;

public class EnumConverterTestCase
    extends TestCase
{
    static enum MyEnum
    {
        A, B, C;
    }

    public void testBasicOperation()
        throws Exception
    {
        final EnumConverter converter = new EnumConverter( MyEnum.class );
        assertEquals( "getJavaType()", MyEnum.class, converter.getJavaType() );
        assertEquals( "getOpenType()", SimpleType.STRING, converter.getOpenType() );
        assertEquals( "toJavaType(A)", MyEnum.A, converter.toJavaType( "A" ) );
        assertEquals( "toOpenType(A)", "A", converter.toOpenType( MyEnum.A ) );
        assertEquals( "toJavaType(null)", null, converter.toJavaType( null ) );
        assertEquals( "toOpenType(null)", null, converter.toOpenType( null ) );
    }

    public void testConvertingInvalidString_toJavaType()
        throws Exception
    {
        final EnumConverter converter = new EnumConverter( MyEnum.class );
        try
        {
            converter.toJavaType( "ACE" );
            fail( "Expected to fail with exception when converting bad string to enum" );
        }
        catch( final OpenDataException e )
        {
            return;
        }
    }
}

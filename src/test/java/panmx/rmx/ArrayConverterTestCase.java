package panmx.rmx;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.SimpleType;
import junit.framework.TestCase;

public class ArrayConverterTestCase
    extends TestCase
{
    public void testBasicOperation()
        throws Exception
    {
        final ArrayConverter converter =
            new ArrayConverter( String[].class,
                                1,
                                SimpleTypeConverter.STRING );
        assertEquals( "getJavaType()", String[].class, converter.getJavaType() );
        assertEquals( "getOpenType()",
                      new ArrayType( 1, SimpleType.STRING ),
                      converter.getOpenType() );

        final String[] openValue = new String[]{"MissString"};
        final Object convertedOpenValue = converter.toJavaType( openValue );
        assertEquals( "toJavaType(...) type", String[].class, convertedOpenValue.getClass() );
        assertTrue( "toJavaType(...) type", convertedOpenValue instanceof String[] );
        assertEquals( "toJavaType(...).length", 1, ( (String[])convertedOpenValue ).length );
        assertEquals( "toJavaType(...)[0]", openValue[0], ( (String[])convertedOpenValue )[0] );

        final String[] javaValue = new String[]{"MrString"};
        final Object converterJavaValue = converter.toOpenType( javaValue );
        assertTrue( "toOpenType(...) type", converterJavaValue instanceof String[] );
        assertEquals( "toOpenType(...).length", 1, ( (String[])converterJavaValue ).length );
        assertEquals( "toOpenType(...)[0]", javaValue[0], ( (String[])converterJavaValue )[0] );

        assertEquals( "toJavaType(null)", null, converter.toJavaType( null ) );
        assertEquals( "toOpenType(null)", null, converter.toOpenType( null ) );
    }

   public void testBasicOperationWithNonString()
      throws Exception
   {
      final ArrayConverter converter =
         new ArrayConverter( int[].class, 1, SimpleTypeConverter.INTEGER );
      assertEquals( "getJavaType()", int[].class, converter.getJavaType() );
      assertEquals( "getOpenType()", new ArrayType( 1, SimpleType.INTEGER ), converter.getOpenType() );

      final int[] openValue = new int[]{42};
      final Object convertedOpenValue = converter.toJavaType( openValue );
      assertEquals( "toJavaType(...) type", int[].class, convertedOpenValue.getClass() );
      assertTrue( "toJavaType(...) type", convertedOpenValue instanceof int[] );
      assertEquals( "toJavaType(...).length", 1, ((int[])convertedOpenValue ).length );
      assertEquals( "toJavaType(...)[0]", openValue[ 0 ], ((int[])convertedOpenValue )[ 0 ] );

      final int[] javaValue = new int[]{16};
      final Object converterJavaValue = converter.toOpenType( javaValue );
      assertTrue( "toOpenType(...) type", converterJavaValue instanceof Integer[] );
      assertEquals( "toOpenType(...).length", 1, ((Integer[])converterJavaValue ).length );
      assertEquals( "toOpenType(...)[0]", javaValue[ 0 ], ((Integer[])converterJavaValue )[ 0 ].intValue() );

      assertEquals( "toJavaType(null)", null, converter.toJavaType( null ) );
      assertEquals( "toOpenType(null)", null, converter.toOpenType( null ) );
   }

   public void testConvertingEmptyArray()
        throws Exception
    {
        final ArrayConverter converter =
            new ArrayConverter( String[].class,
                                1,
                                SimpleTypeConverter.STRING );
        assertEquals( "getJavaType()", String[].class, converter.getJavaType() );
        assertEquals( "getOpenType()",
                      new ArrayType( 1, SimpleType.STRING ),
                      converter.getOpenType() );

        final String[] openValue = new String[0];
        final Object convertedOpenValue = converter.toJavaType( openValue );
        assertTrue( "toJavaType(...) type", convertedOpenValue instanceof String[] );
        assertEquals( "toJavaType(...).length", 0, ( (String[])convertedOpenValue ).length );

        final String[] javaValue = new String[0];
        final Object converterJavaValue = converter.toOpenType( javaValue );
        assertTrue( "toOpenType(...) type", converterJavaValue instanceof String[] );
        assertEquals( "toOpenType(...).length", 0, ( (String[])converterJavaValue ).length );
    }

    public void testConvertingArrayWithNullElement()
        throws Exception
    {
        final ArrayConverter converter =
            new ArrayConverter( String[].class,
                                1,
                                SimpleTypeConverter.STRING );
        assertEquals( "getJavaType()", String[].class, converter.getJavaType() );
        assertEquals( "getOpenType()",
                      new ArrayType( 1, SimpleType.STRING ),
                      converter.getOpenType() );

        final String[] openValue = new String[]{null};
        final Object convertedOpenValue = converter.toJavaType( openValue );
        assertTrue( "toJavaType(...) type", convertedOpenValue instanceof String[] );
        assertEquals( "toJavaType(...).length", 1, ( (String[])convertedOpenValue ).length );
        assertEquals( "toJavaType(...)[0]", null, ( (String[])convertedOpenValue )[0] );

        final String[] javaValue = new String[]{null};
        final Object converterJavaValue = converter.toOpenType( javaValue );
        assertTrue( "toOpenType(...) type", converterJavaValue instanceof String[] );
        assertEquals( "toOpenType(...).length", 1, ( (String[])converterJavaValue ).length );
        assertEquals( "toOpenType(...)[0]", null, ( (String[])converterJavaValue )[0] );
    }

    public void testConvertingMultiDimensionalArray()
        throws Exception
    {
        final ArrayConverter converter =
            new ArrayConverter( String[][].class,
                                2,
                                SimpleTypeConverter.STRING );
        assertEquals( "getJavaType()", String[][].class, converter.getJavaType() );
        assertEquals( "getOpenType()",
                      new ArrayType( 2, SimpleType.STRING ),
                      converter.getOpenType() );

        final String[][] openValue = new String[][]{new String[]{"MrString"}};
        final Object convertedOpenValue = converter.toJavaType( openValue );
        assertTrue( "toJavaType(...) type", convertedOpenValue instanceof String[][] );
        assertEquals( "toJavaType(...).length", 1, ( (String[][])convertedOpenValue ).length );
        assertTrue( "toJavaType(...)[0] type", ( (String[][])convertedOpenValue )[0] instanceof String[] );
        assertEquals( "toJavaType(...)[0].length", 1, ( (String[][])convertedOpenValue )[0].length );
        assertEquals( "toJavaType(...)[0][0]", "MrString", ( (String[][])convertedOpenValue )[0][0] );

        final String[][] javaValue = new String[][]{new String[]{"MissString"}};
        final Object converterJavaValue = converter.toOpenType( javaValue );
        assertTrue( "toOpenType(...) type", converterJavaValue instanceof String[][] );
        assertEquals( "toOpenType(...).length", 1, ( (String[][])converterJavaValue ).length );
        assertTrue( "toOpenType(...)[0] type", ( (String[][])convertedOpenValue )[0] instanceof String[] );
        assertEquals( "toOpenType(...)[0].length", 1, ( (String[][])converterJavaValue )[0].length );
        assertEquals( "toOpenType(...)[0][0]", "MissString", ( (String[][])converterJavaValue )[0][0] );
    }

    static enum MyEnum
    {
        A, B, C;
    }

    public void testOperationWithNonSimpleType()
        throws Exception
    {
        final EnumConverter enumConverter = new EnumConverter( MyEnum.class );
        final ArrayConverter converter =
            new ArrayConverter( MyEnum[].class,
                                1,
                                enumConverter );
        assertEquals( "getJavaType()", MyEnum[].class, converter.getJavaType() );
        assertEquals( "getOpenType()",
                      new ArrayType( 1, enumConverter.getOpenType() ),
                      converter.getOpenType() );

        final String[] openValue = new String[]{"A"};
        final Object convertedOpenValue = converter.toJavaType( openValue );
        assertTrue( "toJavaType(...) type", convertedOpenValue instanceof MyEnum[] );
        assertEquals( "toJavaType(...).length", 1, ( (MyEnum[])convertedOpenValue ).length );
        assertEquals( "toJavaType(...)[0]", MyEnum.A, ( (MyEnum[])convertedOpenValue )[0] );

        final MyEnum[] javaValue = new MyEnum[]{MyEnum.A};
        final Object converterJavaValue = converter.toOpenType( javaValue );
        assertTrue( "toOpenType(...) type", converterJavaValue instanceof String[] );
        assertEquals( "toOpenType(...).length", 1, ( (String[])converterJavaValue ).length );
        assertEquals( "toOpenType(...)[0]", "A", ( (String[])converterJavaValue )[0] );

        assertEquals( "toJavaType(null)", null, converter.toJavaType( null ) );
        assertEquals( "toOpenType(null)", null, converter.toOpenType( null ) );
    }

}

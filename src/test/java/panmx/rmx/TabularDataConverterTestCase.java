/*
 * Copyright 2005, Stock Software Pty Ltd
 *
 * All rights reserved
 */
package panmx.rmx;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import junit.framework.TestCase;

public class TabularDataConverterTestCase
   extends TestCase
{
   public void testBasicOperation()
      throws Exception
   {
      final StandardRMXCompositeType t = new StandardRMXCompositeType( Toy.class );
      final Converter componentConverter = new RMXCompositeTypeConverter( t );
      final String[] keys = new String[]{"id"};
      final TabularDataConverter converter =
         new TabularDataConverter( Toy[].class, componentConverter, keys );
      assertEquals( "getJavaType()", Toy[].class, converter.getJavaType() );
      final String name = Toy[].class.getName();
      final CompositeType cType = (CompositeType) componentConverter.getOpenType();
      final TabularType type = new TabularType( name, name, cType, keys );
      assertEquals( "getOpenType()", type, converter.getOpenType() );

      final Toy toy = new Toy();
      toy.setId( 2 );
      toy.setName( "Darth Yike" );
      toy.setType( "Unicycle" );

      final Toy[] javaValue = new Toy[]{toy};
      final Object converterJavaValue = converter.toOpenType( javaValue );
      assertTrue( "toOpenType(...) type", converterJavaValue instanceof TabularData );
      assertEquals( "toOpenType(...).length", 1, ( (TabularData) converterJavaValue ).size() );
      final CompositeData convertedToy =
         (CompositeData) ( (TabularData) converterJavaValue ).values().iterator().next();
      assertEquals( "toOpenType(...)[0].id", toy.getId(), convertedToy.get( "id" ) );
      assertEquals( "toOpenType(...)[0].name", toy.getName(), convertedToy.get( "name" ) );
      assertEquals( "toOpenType(...)[0].type", toy.getType(), convertedToy.get( "type" ) );

      final Object[] values = new Object[]{1, "Lucky", "Cooking Knife"};
      final CompositeDataSupport cd =
         new CompositeDataSupport( cType, new String[]{"id", "name", "type"}, values );

      final TabularDataSupport openValue = new TabularDataSupport( type );
      openValue.put( cd );
      final Object convertedOpenValue = converter.toJavaType( openValue );
      assertEquals( "toJavaType(...) type", Toy[].class, convertedOpenValue.getClass() );
      assertTrue( "toJavaType(...) type", convertedOpenValue instanceof Toy[] );
      final Toy[] toys = (Toy[]) convertedOpenValue;
      assertEquals( "toJavaType(...).length", 1, toys.length );
      assertEquals( "toJavaType(...)[0].id", values[ 0 ], toys[ 0 ].getId() );
      assertEquals( "toJavaType(...)[0].name", values[ 1 ], toys[ 0 ].getName() );
      assertEquals( "toJavaType(...)[0].type", values[ 2 ], toys[ 0 ].getType() );

      assertEquals( "toJavaType(null)", null, converter.toJavaType( null ) );
      assertEquals( "toOpenType(null)", null, converter.toOpenType( null ) );
   }

   static class Toy
   {
      private int id;
      private String name;
      private String type;

      public int getId()
      {
         return id;
      }

      public void setId( final int id )
      {
         this.id = id;
      }

      public String getName()
      {
         return name;
      }

      public void setName( final String name )
      {
         this.name = name;
      }

      public String getType()
      {
         return type;
      }

      public void setType( final String type )
      {
         this.type = type;
      }
   }
}

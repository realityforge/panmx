/*
 * Copyright 2005, Stock Software Pty Ltd
 *
 * All rights reserved
 */
package panmx.rmx;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

/**
 * Converter to translate arrays of OpenType values to TabularType values and back again.
 */
class TabularDataConverter
   implements Converter
{
   /**
    * Java type.
    */
   private final Class m_javaType;
   /**
    * Open type.
    */
   private final TabularType m_openType;
   /**
    * The convert for component type.
    */
   private final Converter m_componentConverter;

   TabularDataConverter( final Class<?> javaType,
                         final Converter componentConverter,
                         final String[] keys )
      throws OpenDataException
   {
      m_javaType = javaType;
      final String name = javaType.getName();
      final CompositeType compositeType = (CompositeType) componentConverter.getOpenType();
      m_openType = new TabularType( name, name, compositeType, keys );
      m_componentConverter = componentConverter;
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
      if ( null == object )
      {
         return null;
      }
      else
      {
         final int length = Array.getLength( object );

         final TabularDataSupport td = new TabularDataSupport( m_openType );
         for ( int i = 0; i < length; i++ )
         {
            final Object v = Array.get( object, i );
            final CompositeData value =
               (CompositeData) m_componentConverter.toOpenType( v );
            td.put( value );
         }

         return td;
      }
   }

   public Object toJavaType( final Object object )
      throws OpenDataException
   {
      if ( null == object )
      {
         return null;
      }
      else
      {
         final TabularData td = (TabularData) object;
         final int length = td.size();
         final Object newArray =
            Array.newInstance( m_javaType.getComponentType(), length );
         @SuppressWarnings( value = {"unchecked"} ) final Collection<CompositeData> collection =
            td.values();
         int index = 0;
         for ( final CompositeData cd : collection )
         {
            final Object value = m_componentConverter.toJavaType( cd );
            Array.set( newArray, index, value );
            index++;
         }
         return newArray;
      }
   }
}

package panmx.rmx;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;

class ConverterManager
{
    /** Map between types and Converters. */
    private static final WeakHashMap<Type, Converter> c_converters =
        new WeakHashMap<Type, Converter>();

    static
    {
        addConverter( SimpleTypeConverter.WRAPPER_BOOLEAN );
        addConverter( SimpleTypeConverter.BOOLEAN );
        addConverter( SimpleTypeConverter.WRAPPER_CHARACTER );
        addConverter( SimpleTypeConverter.CHARACTER );
        addConverter( SimpleTypeConverter.WRAPPER_BYTE );
        addConverter( SimpleTypeConverter.BYTE );
        addConverter( SimpleTypeConverter.WRAPPER_SHORT );
        addConverter( SimpleTypeConverter.SHORT );
        addConverter( SimpleTypeConverter.WRAPPER_INTEGER );
        addConverter( SimpleTypeConverter.INTEGER );
        addConverter( SimpleTypeConverter.WRAPPER_LONG );
        addConverter( SimpleTypeConverter.LONG );
        addConverter( SimpleTypeConverter.WRAPPER_FLOAT );
        addConverter( SimpleTypeConverter.FLOAT );
        addConverter( SimpleTypeConverter.WRAPPER_DOUBLE );
        addConverter( SimpleTypeConverter.DOUBLE );
        addConverter( SimpleTypeConverter.STRING );
        addConverter( SimpleTypeConverter.BIGDECIMAL );
        addConverter( SimpleTypeConverter.BIGINTEGER );
        addConverter( SimpleTypeConverter.DATE );
        addConverter( SimpleTypeConverter.OBJECTNAME );
    }

    static Converter getConverterFor( final Type type )
        throws OpenDataException
    {
        synchronized( c_converters )
        {
            Converter converter = c_converters.get( type );
            if( null == converter )
            {
                if( type instanceof Class )
                {
                    converter = createConverterForClass( (Class<?>)type );
                }
                else if( type instanceof ParameterizedType )
                {
                    converter = createConverterForGenericType( (ParameterizedType)type );
                }
                else
                {
                    throw new OpenDataException( "Unsupported type: " + type );
                }
                addConverter( converter );
            }
            return converter;
        }
    }

    private static Converter createConverterForClass( final Class<?> type )
        throws OpenDataException
    {
        if( type.isArray() )
        {
            int dimension = 0;
            Class baseType = type;
            while( baseType.isArray() )
            {
                baseType = baseType.getComponentType();
                dimension++;
            }
            final Converter converter = getConverterFor( baseType );
            final OpenType baseOpenType = converter.getOpenType();
            if( converter instanceof SimpleTypeConverter )
            {
                final ArrayType openType = new ArrayType( dimension, baseOpenType );
                return new SimpleTypeConverter( type, openType );
            }
            else
            {
                return new ArrayConverter( type, dimension, converter );
            }
        }
        else if( type.isEnum() )
        {
            @SuppressWarnings( value = {"unchecked"} ) final Class<? extends Enum> enumType =
                (Class<? extends Enum>)type;
            return new EnumConverter( enumType );
        }
        else
        {
            try
            {
                return new CompositeConverter( type );
            }
            catch( final OpenDataException ode )
            {
            }

            final StandardRMXCompositeType compositeType = new StandardRMXCompositeType( type );
            return new RMXCompositeTypeConverter( compositeType );
            //throw new RuntimeException( "The Blue Meanies are coming!!!" );
        }
    }

    private static Converter createConverterForGenericType( final ParameterizedType type )
        throws OpenDataException
    {
        final Type clazz = type.getRawType();
        if( clazz == List.class )
        {
            final Type[] types = type.getActualTypeArguments();
            if( 1 != types.length )
            {
                throw new OpenDataException( "List type must be parameterized." );
            }
            final Converter converter = getConverterFor( types[0] );
            return new ListConverter( type, converter );
        }
        else if( clazz == Map.class )
        {
            final Type[] types = type.getActualTypeArguments();
            if( 2 != types.length )
            {
                throw new OpenDataException( "Map type must be parameterized." );
            }
            final Converter keyConverter = getConverterFor( types[0] );
            final Converter valueConverter = getConverterFor( types[1] );
            return new MapConverter( type, keyConverter, valueConverter );
        }
        else
        {
            return getConverterFor( clazz );
        }
    }

    /**
     * Add converter into registry.
     *
     * @param converter the converter.
     */
    private static void addConverter( final Converter converter )
    {
        c_converters.put( converter.getJavaType(), converter );
    }
}

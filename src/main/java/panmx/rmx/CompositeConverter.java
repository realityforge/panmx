package panmx.rmx;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;

/**
 * Converter to translate between "RMX" composite objects and back again.
 */
class CompositeConverter
    implements Converter
{
    /** name of "from" method. */
    private static final String FROM_METHOD_NAME = "fromCompositeData";
    /** name of "to" method. */
    private static final String TO_METHOD_NAME = "toCompositeData";
    /** name of method to get type details. */
    private static final String GET_TYPE_METHOD_NAME = "getCompositeType";
    /** Cached type class. */
    private static final Class<CompositeType> COMPOSITE_TYPE_CLASS_TYPE = CompositeType.class;
    /** Cached data class. */
    private static final Class<CompositeData> COMPOSITE_CLASS_TYPE = CompositeData.class;
    /** Java type. */
    private final Class m_javaType;
    /** Open type. */
    private final CompositeType m_openType;
    /** Method to convert CompositeData to Java type. */
    private final Method m_from;
    /** Method to convert Java type to CompositeData. */
    private final Method m_to;

    CompositeConverter( final Class javaType )
        throws OpenDataException
    {
        m_javaType = javaType;
        final Method getType;
        try
        {
            getType = m_javaType.getMethod( GET_TYPE_METHOD_NAME, new Class[0] );
            m_to = m_javaType.getMethod( TO_METHOD_NAME, new Class[]{m_javaType} );
            m_from = m_javaType.getMethod( FROM_METHOD_NAME, new Class[]{COMPOSITE_CLASS_TYPE} );

            validateReturnType( getType, COMPOSITE_TYPE_CLASS_TYPE );
            validateReturnType( m_to, COMPOSITE_CLASS_TYPE );
            validateReturnType( m_from, m_javaType );
            validatePublicStatic( getType );
            validatePublicStatic( m_to );
            validatePublicStatic( m_from );
        }
        catch( final NoSuchMethodException nsme )
        {
            final String message = "Missing required method (" + nsme.getMessage() +
                                   ") for class " + m_javaType.getName();
            final OpenDataException exception = new OpenDataException( message );
            exception.initCause( nsme );
            throw exception;
        }

        m_openType = (CompositeType)invoke( getType, new Object[0] );
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
        return invoke( m_to, new Object[]{object} );
    }

    public Object toJavaType( final Object object )
        throws OpenDataException
    {
        return invoke( m_from, new Object[]{object} );
    }

    private Object invoke( final Method method, final Object[] params )
        throws OpenDataException
    {
        try
        {
            return method.invoke( null, params );
        }
        catch( final Exception e )
        {
            final String message = "Error invoking method " + method.getName() +
                                   ") for class " + m_javaType.getName();
            final OpenDataException exception = new OpenDataException( message );
            exception.initCause( e );
            throw exception;
        }
    }

    private void validatePublicStatic( final Method method )
        throws OpenDataException
    {
        final int modifiers = method.getModifiers();
        if( !Modifier.isPublic( modifiers ) || !Modifier.isStatic( modifiers ) )
        {
            final String message = "Method " + method.getName() +
                                   " is not a public static method.";
            throw new OpenDataException( message );
        }
    }

    private void validateReturnType( final Method method, final Class returnType )
        throws OpenDataException
    {
        if( returnType != method.getReturnType() )
        {
            final String message = "Invalid return type (" +
                                   method.getReturnType().getName() +
                                   ") for method " + method.getName() +
                                   " in class " + m_javaType.getName();
            throw new OpenDataException( message );
        }
    }
}

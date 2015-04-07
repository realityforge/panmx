package panmx.rmx;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.OpenDataException;
import panmx.util.BeanUtil;

class RMXBeanType
{
    /** Empty args constant used in getters. */
    private static final Object[] EMPTY_ARGS = new Object[0];
    /** The MBeanInfo for bean. */
    private MBeanInfo m_mBeanInfo;
    /** The Java type for bean. */
    private final Class m_type;
    /** The map of attribute reader invocation targets. */
    private final Map<String, InvocationTarget> m_accessors =
        new HashMap<String, InvocationTarget>();
    /** The map of attribute writer invocation targets. */
    private final Map<String, InvocationTarget> m_mutators =
        new HashMap<String, InvocationTarget>();
    /** The map of operation invocation targets. */
    private final Map<String, InvocationTarget> m_operations =
        new HashMap<String, InvocationTarget>();
    /** Flag indicating whether the type is "frozen". */
    private boolean m_frozen;

    RMXBeanType( final Class type )
    {
        if( null == type )
        {
            throw new NullPointerException( "type" );
        }
        m_type = type;
    }

    MBeanInfo getMBeanInfo()
    {
        return m_mBeanInfo;
    }

    Class getType()
    {
        return m_type;
    }

    Object getAttribute( final MBeanServerConnection connection,
                         final ObjectName objectName,
                         final String name )
        throws MBeanException, AttributeNotFoundException,
               InstanceNotFoundException, ReflectionException,
               IOException
    {
        final InvocationTarget invocationTarget = getAccessor( name );
        final Object value = connection.getAttribute( objectName, name );
        return convertToJavaReturnValue( invocationTarget, value );
    }

    void setAttribute( final MBeanServerConnection connection,
                       final ObjectName objectName,
                       final Attribute attribute )
        throws InstanceNotFoundException, AttributeNotFoundException,
               InvalidAttributeValueException, MBeanException,
               ReflectionException, IOException
    {
        final String name = attribute.getName();
        final InvocationTarget invocationTarget = getMutator( name );
        final Object value = convertAttributeValueToOpenType( invocationTarget, attribute );

        connection.setAttribute( objectName, new Attribute( name, value ) );
    }

    Object invoke( final MBeanServerConnection connection,
                   final ObjectName objectName,
                   final String actionName,
                   final Object[] params,
                   final String[] signature )
        throws InstanceNotFoundException, MBeanException,
               ReflectionException, IOException
    {
        final String name = BeanUtil.makeFullyQualifiedName( actionName, signature );
        final InvocationTarget invocationTarget = getOperation( name );
        final Object[] args = convertParametersToOpenType( invocationTarget, params );
        final Object returnValue = connection.invoke( objectName, actionName, args, signature );
        return convertToJavaReturnValue( invocationTarget, returnValue );
    }

    Object getAttribute( final Object target, final String name )
        throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        final InvocationTarget invocationTarget = getAccessor( name );
        final Object value = performInvocation( invocationTarget, target, EMPTY_ARGS );
        return convertToOpenTypeReturnValue( invocationTarget, value );
    }

    void setAttribute( final Object target, final Attribute attribute )
        throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        final InvocationTarget invocationTarget = getMutator( attribute.getName() );
        final Object value = convertAttributeValueToJava( invocationTarget, attribute );
        performInvocation( invocationTarget, target, new Object[]{value} );
    }

    Object invoke( final Object target,
                   final String actionName,
                   final Object[] params,
                   final String[] signature )
        throws MBeanException, ReflectionException
    {
        final String name = BeanUtil.makeFullyQualifiedName( actionName, signature );

        final InvocationTarget invocationTarget = getOperation( name );

        final Object[] args = convertParametersToJava( invocationTarget, params );
        final Object returnValue = performInvocation( invocationTarget, target, args );
        return convertToOpenTypeReturnValue( invocationTarget, returnValue );
    }

    final synchronized void freeze()
        throws OpenDataException
    {
        if( m_frozen )
        {
            return;
        }
        m_frozen = true;
        m_mBeanInfo = OpenMBeanInfoCreator.createMBeanInfo( m_type,
                                                            m_accessors,
                                                            m_mutators,
                                                            m_operations );
    }

    final synchronized void defineAttribute( final Method method )
        throws OpenDataException
    {
        if( m_frozen )
        {
            final String message =
                "Type has been frozen and no more attributes can be defined.";
            throw new OpenDataException( message );
        }
        if( BeanUtil.isMutator( method ) )
        {
            final Type[] types = method.getGenericParameterTypes();
            final Converter converter = ConverterManager.getConverterFor( types[0] );
            final Converter[] converters = new Converter[]{converter};
            final InvocationTarget invocationTarget =
                new InvocationTarget( method, converters, null );
            final String name = BeanUtil.getAttributeName( method );
            m_mutators.put( name, invocationTarget );
        }
        else if( BeanUtil.isAccessor( method ) )
        {
            final Type type = method.getGenericReturnType();
            final Converter converter = ConverterManager.getConverterFor( type );
            final InvocationTarget invocationTarget =
                new InvocationTarget( method, null, converter );
            final String name = BeanUtil.getAttributeName( method );
            m_accessors.put( name, invocationTarget );
        }
        else
        {
            final String message =
                "Method (" + method.getName() + ") is not an attribute method.";
            throw new OpenDataException( message );
        }
    }

    final synchronized void defineOperation( final Method method )
        throws OpenDataException
    {
        if( m_frozen )
        {
            final String message =
                "Type has been frozen and no more attributes can be defined.";
            throw new OpenDataException( message );
        }
        final Converter[] converters = getConverters( method.getGenericParameterTypes() );
        final Converter rvConverter = getConverter( method.getGenericReturnType() );

        final InvocationTarget invocationTarget =
            new InvocationTarget( method, converters, rvConverter );

        final String name = BeanUtil.makeFullyQualifiedName( method );
        m_operations.put( name, invocationTarget );
    }

    /**
     * Return converter for specified type if type is not a SimpleType.
     * This method will return null if type is void, null or a SimpleType.
     *
     * @param type the type.
     * @return the Converter.
     * @throws OpenDataException if unable to get converter for type.
     */
    private Converter getConverter( final Type type )
        throws OpenDataException
    {
        if( null == type || Void.TYPE == type )
        {
            return null;
        }
        final Converter converter = ConverterManager.getConverterFor( type );
        if( converter instanceof SimpleTypeConverter )
        {
            return null;
        }
        else
        {
            return converter;
        }
    }

    /**
     * Return an array of converters for specified types.
     * This method will return null if all the types are SimpleTypes.
     *
     * @param types the types.
     * @return the Converters.
     * @throws OpenDataException if unable to get converter for an element.
     */
    private Converter[] getConverters( final Type[] types )
        throws OpenDataException
    {
        if( 0 == types.length )
        {
            return null;
        }
        else
        {
            final Converter[] converters = new Converter[types.length];
            boolean nonSimpleTypes = false;

            for( int i = 0; i < converters.length; i++ )
            {
                converters[i] = ConverterManager.getConverterFor( types[i] );
                nonSimpleTypes = nonSimpleTypes ||
                                 !( converters[i] instanceof SimpleTypeConverter );
            }

            if( nonSimpleTypes )
            {
                return converters;
            }
            else
            {
                return null;
            }
        }
    }

    private Object[] convertParametersToJava( final InvocationTarget invocationTarget, final Object[] params )
        throws ReflectionException, MBeanException
    {
        final Converter[] converters = invocationTarget.getParameterConverters();
        final Object[] args;
        if( null == converters )
        {
            args = params;
        }
        else
        {
            if( null == params || params.length != converters.length )
            {
                final String message = "Expected " + converters.length + " parameters.";
                throw new ReflectionException( new IllegalArgumentException( message ) );
            }
            args = new Object[converters.length];
            for( int i = 0; i < args.length; i++ )
            {
                try
                {
                    args[i] = converters[i].toJavaType( params[i] );
                }
                catch( final OpenDataException ode )
                {
                    final MBeanException exception = new MBeanException( ode );
                    exception.initCause( ode );
                    throw exception;
                }
            }
        }
        return args;
    }

    private Object[] convertParametersToOpenType( final InvocationTarget invocationTarget, final Object[] params )
        throws ReflectionException, MBeanException
    {
        final Converter[] converters = invocationTarget.getParameterConverters();
        final Object[] args;
        if( null == converters )
        {
            args = params;
        }
        else
        {
            if( null == params || params.length != converters.length )
            {
                final String message = "Expected " + converters.length + " parameters.";
                throw new ReflectionException( new IllegalArgumentException( message ) );
            }
            args = new Object[converters.length];
            for( int i = 0; i < args.length; i++ )
            {
                try
                {
                    args[i] = converters[i].toOpenType( params[i] );
                }
                catch( final OpenDataException ode )
                {
                    final MBeanException exception = new MBeanException( ode );
                    exception.initCause( ode );
                    throw exception;
                }
            }
        }
        return args;
    }

    private Object performInvocation( final InvocationTarget invocationTarget,
                                      final Object target,
                                      final Object[] args )
        throws ReflectionException, MBeanException
    {
        try
        {
            return invocationTarget.getMethod().invoke( target, args );
        }
        catch( final IllegalAccessException iae )
        {
            final ReflectionException exception = new ReflectionException( iae );
            exception.initCause( iae );
            throw exception;
        }
        catch( final InvocationTargetException ite )
        {
            final Throwable cause = ite.getTargetException();
            if( cause instanceof Exception )
            {
                final MBeanException exception = new MBeanException( (Exception)cause );
                exception.initCause( ite );
                throw exception;
            }
            else
            {
                final MBeanException exception = new MBeanException( ite );
                exception.initCause( ite );
                throw exception;
            }
        }
        catch( final Exception e )
        {
            final MBeanException exception = new MBeanException( e );
            exception.initCause( e );
            throw exception;
        }
    }

    private Object convertAttributeValueToJava( final InvocationTarget invocationTarget,
                                                final Attribute attribute )
        throws InvalidAttributeValueException
    {
        final Converter[] converters = invocationTarget.getParameterConverters();
        if( null != converters )
        {
            try
            {
                return converters[0].toJavaType( attribute.getValue() );
            }
            catch( final OpenDataException ode )
            {
                final InvalidAttributeValueException e =
                    new InvalidAttributeValueException( attribute.getName() );
                e.initCause( ode );
                throw e;
            }
        }
        else
        {
            return attribute.getValue();
        }
    }

    private Object convertAttributeValueToOpenType( final InvocationTarget invocationTarget,
                                                    final Attribute attribute )
        throws InvalidAttributeValueException
    {
        final Converter[] converters = invocationTarget.getParameterConverters();
        if( null != converters )
        {
            try
            {
                return converters[0].toOpenType( attribute.getValue() );
            }
            catch( final OpenDataException ode )
            {
                final InvalidAttributeValueException e =
                    new InvalidAttributeValueException( attribute.getName() );
                e.initCause( ode );
                throw e;
            }
        }
        else
        {
            return attribute.getValue();
        }
    }

    private Object convertToJavaReturnValue( final InvocationTarget invocationTarget, final Object value )
        throws MBeanException
    {
        final Converter converter = invocationTarget.getReturnValueConverter();
        if( null != converter )
        {
            try
            {
                return converter.toJavaType( value );
            }
            catch( final OpenDataException ode )
            {
                final MBeanException exception = new MBeanException( ode );
                exception.initCause( ode );
                throw exception;
            }
        }
        else
        {
            return value;
        }
    }

    private Object convertToOpenTypeReturnValue( final InvocationTarget invocationTarget, final Object value )
        throws MBeanException
    {
        final Converter converter = invocationTarget.getReturnValueConverter();
        if( null != converter )
        {
            try
            {
                return converter.toOpenType( value );
            }
            catch( final OpenDataException ode )
            {
                final MBeanException exception = new MBeanException( ode );
                exception.initCause( ode );
                throw exception;
            }
        }
        else
        {
            return value;
        }
    }

    private InvocationTarget getAccessor( final String name )
        throws AttributeNotFoundException
    {
        final InvocationTarget invocationTarget = m_accessors.get( name );
        if( null == invocationTarget )
        {
            throw new AttributeNotFoundException( name );
        }
        return invocationTarget;
    }

    private InvocationTarget getMutator( final String name )
        throws AttributeNotFoundException
    {
        final InvocationTarget invocationTarget = m_mutators.get( name );
        if( null == invocationTarget )
        {
            throw new AttributeNotFoundException( name );
        }
        return invocationTarget;
    }

    private InvocationTarget getOperation( final String name )
        throws ReflectionException
    {
        final InvocationTarget invocationTarget = m_operations.get( name );
        if( null == invocationTarget )
        {
            throw new ReflectionException( new NoSuchMethodException( name ) );
        }
        return invocationTarget;
    }
}

package panmx.rmx;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfo;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfo;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import panmx.annotations.MBean;
import panmx.annotations.MxAttribute;
import panmx.annotations.MxOperation;
import panmx.annotations.MxParameter;

/**
 * Utility class to create OpenMBeanInfo from a management specification.
 */
final class OpenMBeanInfoCreator
{
    /** Constant for empty string to avoid gratuitous string creation. */
    private static final String EMPTY_STRING = "";
    /** Constant used to prefix unamed parameters. */
    private static final String PARAM_NAME_PREFIX = "param";

    /**
     * Create a MBeanInfo for supplied management specification.
     *
     * @param type the managed type.
     * @param accessors the attribute accessors.
     * @param mutators the attribute mutators.
     * @param operations the operations.
     * @return the MBeanInfo.
     * @throws OpenDataException if managed resource does not conform to
     *                           OpenMBean specification.
     */
    static MBeanInfo createMBeanInfo( final Class<?> type,
                                      final Map<String, InvocationTarget> accessors,
                                      final Map<String, InvocationTarget> mutators,
                                      final Map<String, InvocationTarget> operations )
        throws OpenDataException
    {
        final OpenMBeanAttributeInfo[] attributeInfos = collectAttributes( accessors, mutators );
        final OpenMBeanOperationInfo[] operationInfos = collectOperations( operations );

        final String name = type.getName();
        final MBean annotation = type.getAnnotation( MBean.class );
        final String description = ( null != annotation ) ? annotation.description() : EMPTY_STRING;
        return new OpenMBeanInfoSupport( name,
                                         fixDescription( description, name ),
                                         attributeInfos,
                                         new OpenMBeanConstructorInfo[0],
                                         operationInfos,
                                         new MBeanNotificationInfo[0] );
    }

    /**
     * Collect operation definitions from specified operations.
     *
     * @param operations the operations.
     * @return the infos representing operation.
     */
    private static OpenMBeanOperationInfo[] collectOperations( final Map<String, InvocationTarget> operations )
        throws OpenDataException
    {
        final OpenMBeanOperationInfo[] infos =
            new OpenMBeanOperationInfo[operations.size()];
        int index = 0;
        for( final InvocationTarget target : operations.values() )
        {
            final Method method = target.getMethod();
            final OpenMBeanParameterInfo[] params = getParameters( method );
            final OpenType returnType = getReturnType( method );

            final MxOperation annotation = method.getAnnotation( MxOperation.class );
            final String description = ( null != annotation ) ? annotation.description() : EMPTY_STRING;

            final OpenMBeanOperationInfoSupport operation =
                new OpenMBeanOperationInfoSupport( method.getName(),
                                                   fixDescription( description, method.getName() ),
                                                   params,
                                                   returnType,
                                                   MBeanOperationInfo.ACTION );
            infos[index++] = operation;
        }
        return infos;
    }

    /**
     * Return the parameter infos for specified method.
     *
     * @param method the method.
     * @return the parameter infos.
     * @throws OpenDataException if a parameter has an invalid type.
     */
    private static OpenMBeanParameterInfo[] getParameters( final Method method )
        throws OpenDataException
    {
        final Type[] types = method.getGenericParameterTypes();
        final OpenMBeanParameterInfo[] params = new OpenMBeanParameterInfo[types.length];
        final Annotation[][] annotations = method.getParameterAnnotations();
        for( int i = 0; i < types.length; i++ )
        {
            final Type type = types[i];
            final Converter converter = ConverterManager.getConverterFor( type );

            final MxParameter parameter = getParameter( annotations[i] );
            final String name;
            final String description;
            if( null == parameter )
            {
                name = PARAM_NAME_PREFIX + i;
                description = EMPTY_STRING;
            }
            else
            {
                name = parameter.name();
                description = parameter.description();
            }

            final OpenMBeanParameterInfoSupport info =
                new OpenMBeanParameterInfoSupport( name,
                                                   fixDescription( description, name ),
                                                   converter.getOpenType() );
            params[i] = info;
        }

        return params;
    }

    /**
     * Return the OpenType for methods return value.
     *
     * @param method the method.
     * @return the OpenType for methods return value.
     */
    private static OpenType getReturnType( final Method method )
        throws OpenDataException
    {
        final Type returnType = method.getGenericReturnType();
        if( Void.TYPE == returnType )
        {
            return SimpleType.VOID;
        }
        else
        {
            final Converter converter = ConverterManager.getConverterFor( returnType );
            return converter.getOpenType();
        }
    }

    /**
     * Collect attribute definitions from specified accessors and mutators.
     *
     * @param accessors the accessors.
     * @param mutators the mutators.
     * @return the infos representing attributes.
     */
    private static OpenMBeanAttributeInfo[] collectAttributes( final Map<String, InvocationTarget> accessors,
                                                               final Map<String, InvocationTarget> mutators )
    {
        final HashSet<String> names = new HashSet<String>();
        names.addAll( accessors.keySet() );
        names.addAll( mutators.keySet() );

        final OpenMBeanAttributeInfo[] attributes = new OpenMBeanAttributeInfo[names.size()];

        int index = 0;
        for( final String name : names )
        {
            final InvocationTarget reader = accessors.get( name );
            final InvocationTarget writer = mutators.get( name );
            final OpenType type =
                ( null == reader ) ?
                writer.getParameterConverters()[0].getOpenType() :
                reader.getReturnValueConverter().getOpenType();
            final boolean isIs = null != reader &&
                                 reader.getMethod().getName().startsWith( "is" );

            String description = EMPTY_STRING;
            if( null != writer )
            {
                final MxAttribute annotation =
                    writer.getMethod().getAnnotation( MxAttribute.class );
                if( null != annotation )
                {
                    description = annotation.description();
                }
            }
            if( null != reader && EMPTY_STRING.equals( description ) )
            {
                final MxAttribute annotation =
                    reader.getMethod().getAnnotation( MxAttribute.class );
                if( null != annotation )
                {
                    description = annotation.description();
                }
            }

            final OpenMBeanAttributeInfoSupport attribute =
                new OpenMBeanAttributeInfoSupport( name,
                                                   fixDescription( description, name ),
                                                   type,
                                                   null != reader,
                                                   null != writer,
                                                   isIs );
            attributes[index++] = attribute;
        }

        return attributes;
    }

    /**
     * Return parameter annotation for specified index.
     *
     * @param annotations the parameter annotations.
     * @return parameter annotation if any, else null.
     */
    private static MxParameter getParameter( final Annotation[] annotations )
    {
        for( final Annotation annotation : annotations )
        {
            if( annotation instanceof MxParameter )
            {
                return (MxParameter)annotation;
            }
        }
        return null;
    }

    /**
     * Ensure that description is not empty string.
     *
     * @param description the description.
     * @param defaultValue the default value.
     * @return the description if not empty else defaultValue.
     */
    private static String fixDescription( final String description,
                                          final String defaultValue )
    {
        if( EMPTY_STRING.equals( description ) )
        {
            return defaultValue;
        }
        else
        {
            return description;
        }
    }
}

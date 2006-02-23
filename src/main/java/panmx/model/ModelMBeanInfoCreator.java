package panmx.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.openmbean.OpenDataException;
import panmx.annotations.MBean;
import panmx.annotations.MxAttribute;
import panmx.annotations.MxField;
import panmx.annotations.MxOperation;
import panmx.annotations.MxParameter;
import panmx.util.BeanUtil;

/**
 * Utility class to create ModelMBeanInfo from a management specification.
 */
final class ModelMBeanInfoCreator
{
    /** Field name for accessor in attribute descriptor. */
    private static final String GET_METHOD_FIELD = "getMethod";
    /** Field name for mutator in attribute descriptor. */
    private static final String SET_METHOD_FIELD = "setMethod";
    /** Constant for empty string to avoid gratuitous string creation. */
    private static final String EMPTY_STRING = "";
    /** Constant used to prefix unamed parameters. */
    private static final String PARAM_NAME_PREFIX = "param";
    /** Constant for set of empty fields. */
    private static final MxField[] EMPTY_FIELDS = new MxField[0];
    /**
     * Constant for field that contains currency Time Limit.
     * i.e. How often to refresh attribute value.
     */
    private static final String CURRENCY_TIME_LIMIT_FIELD = "currencyTimeLimit";
    private static final String NAME_FIELD = "name";
    private static final String ROLE_FIELD = "role";
    private static final String DESCRIPTOR_TYPE_FIELD = "descriptorType";
    private static final String DISPLAY_NAME_FIELD = "displayname";
    private static final String OPERATION_FIELD_VALUE = "operation";
    private static final String ATTRIBUTE_FIELD_VALUE = "attribute";

    /**
     * Create a ModelMBeanInfo for type.
     *
     * @param type the managed type.
     * @return the MBeanInfo.
     */
    static ModelMBeanInfo createModelMBeanInfo( final Class<?> type )
        throws NotCompliantMBeanException
    {
        final MBean mBean = type.getAnnotation( MBean.class );
        if( null == mBean )
        {
            final String message =
                "Class " + type.getName() + " is missing the MBean annotation.";
            throw new NotCompliantMBeanException( message );
        }
        final HashMap<String, ModelMBeanAttributeInfo> attributes = new HashMap<String, ModelMBeanAttributeInfo>();
        final HashMap<String, ModelMBeanOperationInfo> operations = new HashMap<String, ModelMBeanOperationInfo>();

        defineManagementElements( type, type.getMethods(), attributes, operations, false );
        final Class<?>[] mxInterfaces = mBean.interfaces();
        for( final Class<?> mx : mxInterfaces )
        {
            if( !mx.isInterface() )
            {
                final String message =
                    "Management interface " + mx.getName() + " of class " +
                    type.getName() + " is not a java interface.";
                throw new NotCompliantMBeanException( message );
            }
            if( !mx.isAssignableFrom( type ) )
            {
                final String message =
                    "Class " + type.getName() + " is not assignable from " +
                    "management interface " + mx.getName() + ".";
                throw new NotCompliantMBeanException( message );
            }
            defineManagementElements( type, mx.getMethods(), attributes, operations, true );
        }

        final ModelMBeanAttributeInfo[] attributeInfos =
            attributes.values().toArray( new ModelMBeanAttributeInfo[attributes.size()] );
        final ModelMBeanOperationInfo[] operationInfos =
            operations.values().toArray( new ModelMBeanOperationInfo[operations.size()] );

        final String name = type.getName();
        return new ModelMBeanInfoSupport( name,
                                          fixEmptyString( mBean.description(), name ),
                                          attributeInfos,
                                          new ModelMBeanConstructorInfo[0],
                                          operationInfos,
                                          new ModelMBeanNotificationInfo[0] );
    }

    /**
     * Define management interface for specified type.
     *
     * @param type the type.
     * @param methods the set of methods that are candidate management operations.
     * @param attributes the map of managed attributes. (IN/OUT)
     * @param operations the map of managed operations. (IN/OUT)
     * @param force if true will add all methods as managed operations even if
     *              they are not annotated.
     * @throws NotCompliantMBeanException if type does not follow MBean conventions.
     */
    private static void defineManagementElements( final Class<?> type,
                                                  final Method[] methods,
                                                  final HashMap<String, ModelMBeanAttributeInfo> attributes,
                                                  final HashMap<String, ModelMBeanOperationInfo> operations,
                                                  final boolean force )
        throws NotCompliantMBeanException
    {
        for( final Method method : methods )
        {
            final MxAttribute attribute = method.getAnnotation( MxAttribute.class );
            if( null != attribute )
            {
                defineAttribute( operations, attributes, type, method );
            }
            final MxOperation operation = method.getAnnotation( MxOperation.class );
            if( null != operation )
            {
                defineOperation( operations, method, operation );
            }
            if( force && null == attribute && null == operation )
            {
                defineOperation( operations, method, operation );
            }
        }
    }

    /**
     * Define operation from method.
     *
     * @param operations the existing operations.
     * @param method the method.
     * @param annotation the annotation for method if any.
     */
    private static void defineOperation( final HashMap<String, ModelMBeanOperationInfo> operations,
                                         final Method method,
                                         final MxOperation annotation )
    {
        final String fqn = BeanUtil.makeFullyQualifiedName( method );

        final MBeanParameterInfo[] params = getParameters( method );

        final String description =
            ( null != annotation ) ? annotation.description() : EMPTY_STRING;
        final MxField[] fields = ( null != annotation ) ? annotation.fields() : EMPTY_FIELDS;
        final DescriptorSupport descriptor = createDescriptor( fields );

        final String name = method.getName();
        final String displayName = ( null != annotation ) ? annotation.displayName() : EMPTY_STRING;
        setFieldIfUnset( descriptor, DISPLAY_NAME_FIELD, fixEmptyString( displayName, name ) );
        descriptor.setField( NAME_FIELD, name );
        descriptor.setField( ROLE_FIELD, OPERATION_FIELD_VALUE );
        descriptor.setField( DESCRIPTOR_TYPE_FIELD, OPERATION_FIELD_VALUE );

        final ModelMBeanOperationInfo operation =
            new ModelMBeanOperationInfo( name,
                                         fixEmptyString( description, name ),
                                         params,
                                         method.getReturnType().getName(),
                                         MBeanOperationInfo.ACTION,
                                         descriptor );
        operations.put( fqn, operation );
    }

    /**
     * Define attribute
     *
     * @param attributes the existing attributes.
     * @param type the type that declared attribute.
     * @param method the attribute method.
     * @throws NotCompliantMBeanException if attribute does not confrm to MBean patterns.
     */
    private static void defineAttribute( final HashMap<String, ModelMBeanOperationInfo> operations,
                                         final HashMap<String, ModelMBeanAttributeInfo> attributes,
                                         final Class<?> type,
                                         final Method method )
        throws NotCompliantMBeanException
    {
        if( !BeanUtil.isAttributeMethod( method ) )
        {
            final String message =
                "MxAttribute attribute declared on method named '" + method.getName() +
                "' of class '" + type.getName() + "' but method does not conform to " +
                "MBean conventions.";
            throw new NotCompliantMBeanException( message );
        }
        final String name = BeanUtil.getAttributeName( method );
        final boolean accessor = BeanUtil.isAccessor( method );
        Method reader = ( accessor ) ? method : getAccessor( type, name );
        Method writer = ( !accessor ) ? method : getMutator( type, name, reader.getReturnType() );

        String displayName = EMPTY_STRING;
        String description = EMPTY_STRING;
        MxField[] fields = EMPTY_FIELDS;
        if( null != writer )
        {
            final MxAttribute annotation = writer.getAnnotation( MxAttribute.class );
            if( null != annotation )
            {
                description = annotation.description();
                fields = annotation.fields();
                displayName = annotation.displayName();
            }
            else
            {
                writer = null;
            }
        }
        if( null != reader && EMPTY_STRING.equals( description ) )
        {
            final MxAttribute annotation = reader.getAnnotation( MxAttribute.class );
            if( null != annotation )
            {
                description = annotation.description();
                fields = annotation.fields();
                displayName = annotation.displayName();
            }
            else
            {
                reader = null;
            }
        }

        final Class<?> attributeType = ( null == reader ) ? writer.getParameterTypes()[0] : reader.getReturnType();

        if( null != reader && null != writer )
        {
            final Class<?> readerType = reader.getReturnType();
            final Class<?> writerType = writer.getParameterTypes()[0];
            if( readerType != writerType )
            {
                final String message =
                    "Accessor and mutator have different types for attribute named " + name +
                    ". " + readerType.getName() + " != " + writerType.getName();
                throw new NotCompliantMBeanException( message );
            }
        }

        final DescriptorSupport descriptor = createDescriptor( fields );
        setFieldIfUnset( descriptor, CURRENCY_TIME_LIMIT_FIELD, new Integer( 1 ) );

        setFieldIfUnset( descriptor, DISPLAY_NAME_FIELD, fixEmptyString( displayName, name ) );
        descriptor.setField( NAME_FIELD, name );
        descriptor.setField( DESCRIPTOR_TYPE_FIELD, ATTRIBUTE_FIELD_VALUE );

        if( null != reader )
        {
            descriptor.setField( GET_METHOD_FIELD, reader.getName() );
            final MxOperation operation = reader.getAnnotation( MxOperation.class );
            defineOperation( operations, reader, operation );
        }
        if( null != writer )
        {
            descriptor.setField( SET_METHOD_FIELD, writer.getName() );
            final MxOperation operation = writer.getAnnotation( MxOperation.class );
            defineOperation( operations, writer, operation );
        }

        final ModelMBeanAttributeInfo attribute =
            new ModelMBeanAttributeInfo( name,
                                         attributeType.getName(),
                                         fixEmptyString( description, name ),
                                         null != reader,
                                         null != writer,
                                         name.startsWith( "is" ),
                                         descriptor );

        attributes.put( name, attribute );
    }

    /**
     * Return mutator for attribute with specified name or null if no such mutator.
     *
     * @param type the type to get mutator from.
     * @param name the name of the attribute.
     * @param attributeType the type of the attribute.
     * @return the mutator method or null if no accessor exists.
     */
    private static Method getMutator( final Class<?> type,
                                      final String name,
                                      final Class<?> attributeType )
    {
        try
        {
            return BeanUtil.getMutator( type, name, attributeType, true );
        }
        catch( final OpenDataException ode )
        {
            return null;
        }
    }

    /**
     * Return accessor for attribute with specified name or null if no such accessor.
     *
     * @param type the type to get accessor from.
     * @param name the name of the attribute.
     * @return the accessor method or null if no accessor exists.
     */
    private static Method getAccessor( final Class<?> type, final String name )
    {
        try
        {
            return BeanUtil.getAccessor( type, name );
        }
        catch( final OpenDataException ode )
        {
            return null;
        }
    }

    /**
     * Create a descriptor with values specified in set of fields.
     *
     * @param fields the fields.
     * @return the descriptor.
     */
    private static DescriptorSupport createDescriptor( final MxField[] fields )
    {
        final DescriptorSupport descriptor = new DescriptorSupport();
        for( final MxField field : fields )
        {
            descriptor.setField( field.name(), field.value() );
        }
        return descriptor;
    }

    /**
     * Return the parameter infos for specified method.
     *
     * @param method the method.
     * @return the parameter infos.
     */
    private static MBeanParameterInfo[] getParameters( final Method method )
    {
        final Class[] types = method.getParameterTypes();
        final MBeanParameterInfo[] params = new MBeanParameterInfo[types.length];
        final Annotation[][] annotations = method.getParameterAnnotations();
        for( int i = 0; i < types.length; i++ )
        {
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

            final MBeanParameterInfo info =
                new MBeanParameterInfo( name,
                                        types[i].getName(),
                                        fixEmptyString( description, name ) );
            params[i] = info;
        }

        return params;
    }

    /**
     * Set field in descriptor if it is not already set.
     *
     * @param descriptor the descriptor.
     * @param key the key.
     * @param value the value.
     */
    private static void setFieldIfUnset( final DescriptorSupport descriptor,
                                         final String key,
                                         final Object value )
    {
        if( null == descriptor.getFieldValue( key ) )
        {
            descriptor.setField( key, value );
        }
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
    private static String fixEmptyString( final String description,
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

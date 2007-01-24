package panmx.rmx;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import panmx.util.BeanUtil;

/**
 * RMXCompositeType adapt java types to Open MBeans CompositeData type.
 *
 * <p>The RMXCompositeType must first be configured by invoking
 * {@link #defineField(String)} with names of all the attributes to expose
 * in CompositeData. After that {@link #freeze()} must be invoked. After
 * {@link #freeze()} has been called it becomes possible to invoke the
 * {@link #getCompositeType()}, {@link #toCompositeData(Object)} and
 * {@link #fromCompositeData(Object, javax.management.openmbean.CompositeData)}
 * methods.</p>
 */
class RMXCompositeType
{
    /** The type that is adapted. */
    private final Class m_type;
    /**
     * The list of fields during configuration.
     * This nulled out after {@link #freeze()} is called.
     */
    private Map<String, DataFieldDescriptor> m_fields = new HashMap<String, DataFieldDescriptor>();
    /** Flag indicating whether this object is frozen. */
    private boolean m_frozen;
    /**
     * The OpenType corresponding to java type.
     * This is only valid after {@link #freeze()} is called.
     */
    private CompositeType m_compositeType;
    /**
     * The descriptors for fields.
     * This is only valid after {@link #freeze()} is called.
     */
    private DataFieldDescriptor[] m_descriptors;

    /**
     * Create composite type for specified type.
     *
     * @param type the java type.
     */
    RMXCompositeType( final Class type )
    {
        if( null == type )
        {
            throw new NullPointerException( "type" );
        }
        m_type = type;
    }

    /**
     * Freeze the object making it ready for usage.
     *
     * @throws OpenDataException if bean does not follow conventions.
     */
    final synchronized void freeze()
        throws OpenDataException
    {
        if( m_frozen )
        {
            return;
        }
        m_frozen = true;
        final String name = m_type.getName();
        final int size = m_fields.size();
        final String[] items = new String[size];
        final OpenType[] types = new OpenType[size];
        m_descriptors = new DataFieldDescriptor[size];

        int index = 0;
        final Set<Map.Entry<String, DataFieldDescriptor>> entries = m_fields.entrySet();
        for ( final Entry<String, DataFieldDescriptor> entry : entries )
        {
            items[ index ] = entry.getKey();
            final DataFieldDescriptor descriptor = entry.getValue();
            m_descriptors[ index ] = descriptor;
            types[ index ] = descriptor.getConverter().getOpenType();
            index++;
        }
        m_compositeType = new CompositeType( name, name, items, items, types );
        m_fields = null;
    }

    /**
     * Add a field to adapted.
     * The field should follow the <a href="#Field">Field</a> conventions.
     * This method should NOT be called after {@link #freeze()} has been called
     * otherwise it will generate an OpenDataException.
     *
     * @param name the name of the attribute.
     * @throws OpenDataException if no attribute exists or attribute does not
     *                           follow conventions.
     */
    final synchronized void defineField( final String name )
        throws OpenDataException
    {
        if( m_frozen )
        {
            final String message =
                "Type has been frozen and no more fields can be defined.";
            throw new OpenDataException( message );
        }
        final String baseName =
            Character.toUpperCase( name.charAt( 0 ) ) + name.substring( 1 );

        final Method accessor = BeanUtil.getAccessor( m_type, baseName );
        final Class type = accessor.getReturnType();
        final Method mutator = BeanUtil.getMutator( m_type, baseName, type, false );

        final Type returnType = accessor.getGenericReturnType();
        final Converter converter = ConverterManager.getConverterFor( returnType );

        final DataFieldDescriptor descriptor =
            new DataFieldDescriptor( name, converter, accessor, mutator );
        m_fields.put( name, descriptor );
    }

    /**
     * Return the type that this class works with.
     *
     * @return the type.
     */
    Class getType()
    {
        return m_type;
    }

    /**
     * Return the CompositeType that this class works with.
     *
     * @return the CompositeType.
     */
    final CompositeType getCompositeType()
    {
        return m_compositeType;
    }

    /**
     * Create object from the specified CompositeData representation. The object
     * will be an instance of type {@link #getType())
     *
     * @param the CompositeData representation of object.
     * @throws OpenDataException if there is an error creating object.
     */
    final Object fromCompositeData( final CompositeData compositeData )
        throws OpenDataException
    {
        final Object object;
        try
        {
            object = m_type.newInstance();
        }
        catch( final Exception e )
        {
            final String message = "Error creating object of type " + m_type.getName();
            final OpenDataException exception = new OpenDataException( message );
            exception.initCause( e );
            throw exception;
        }
        fromCompositeData( object, compositeData );
        return object;
    }

    /**
     * Synchronize the state of the specified object with the specified
     * CompositeData representation. This object should be an instance
     * of type {@link #getType())
     *
     * @param object the object.
     * @param the CompositeData representation of object.
     * @throws OpenDataException if there is an error converting object.
     */
    final void fromCompositeData( final Object object, final CompositeData compositeData )
        throws OpenDataException
    {
        for( final DataFieldDescriptor descriptor : m_descriptors )
        {
            final String name = descriptor.getName();
            final Object value = compositeData.get( name );
            final Object javaValue = descriptor.getConverter().toJavaType( value );
            try
            {
                descriptor.getMutator().invoke( object, javaValue );
            }
            catch( final Exception e )
            {
                final String message = "Error mutating field " + name;
                final OpenDataException exception = new OpenDataException( message );
                exception.initCause( e );
                throw exception;
            }
        }
    }

    /**
     * Convert specified object to CompositeData representation.
     * This object should be an instance of type {@link #getType())
     *
     * @param object the object.
     * @return the CompositeData representation of object.
     * @throws OpenDataException if there is an error converting object.
     */
    final CompositeData toCompositeData( final Object object )
        throws OpenDataException
    {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        for( final DataFieldDescriptor descriptor : m_descriptors )
        {
            final String name = descriptor.getName();
            final Object value;
            try
            {
                value = descriptor.getAccessor().invoke( object );
            }
            catch( final Exception e )
            {
                final String message = "Error accessing field " + name;
                final OpenDataException exception = new OpenDataException( message );
                exception.initCause( e );
                throw exception;
            }

            final Object openValue = descriptor.getConverter().toOpenType( value );
            map.put( name, openValue );
        }
        return new CompositeDataSupport( m_compositeType, map );
    }
}

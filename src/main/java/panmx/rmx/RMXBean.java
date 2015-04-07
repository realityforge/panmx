package panmx.rmx;

import java.util.Iterator;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

class RMXBean
    implements DynamicMBean, MBeanRegistration
{
    /** The Java type for bean. */
    private final RMXBeanType m_type;
    /** The target object that methods are invoked on. */
    private final Object m_target;

    RMXBean( final RMXBeanType type, final Object target )
    {
        if( null == type )
        {
            throw new NullPointerException( "type" );
        }
        if( null == target )
        {
            throw new NullPointerException( "target" );
        }
        m_type = type;
        m_target = target;
    }

    /**
     * Return the RMXBeanType for bean.
     *
     * @return the RMXBeanType.
     */
    RMXBeanType getType()
    {
        return m_type;
    }

    /**
     * {@inheritDoc}
     */
    public ObjectName preRegister( MBeanServer server, ObjectName name )
        throws Exception
    {
        if( m_target instanceof MBeanRegistration )
        {
            return ( (MBeanRegistration)m_target ).preRegister( server, name );
        }
        else
        {
            return name;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void postRegister( Boolean registrationDone )
    {
        if( m_target instanceof MBeanRegistration )
        {
            ( (MBeanRegistration)m_target ).postRegister( registrationDone );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void preDeregister()
        throws Exception
    {
        if( m_target instanceof MBeanRegistration )
        {
            ( (MBeanRegistration)m_target ).preDeregister();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void postDeregister()
    {
        if( m_target instanceof MBeanRegistration )
        {
            ( (MBeanRegistration)m_target ).postDeregister();
        }
    }

    /**
     * {@inheritDoc}
     */
    public MBeanInfo getMBeanInfo()
    {
        return m_type.getMBeanInfo();
    }

    /**
     * {@inheritDoc}
     */
    public AttributeList getAttributes( final String[] names )
    {
        final AttributeList list = new AttributeList();
        for( final String name : names )
        {
            try
            {
                final Object value = getAttribute( name );
                list.add( new Attribute( name, value ) );
            }
            catch( final Exception e )
            {
                //Ignore.
            }
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    public AttributeList setAttributes( final AttributeList attributes )
    {
        final AttributeList result = new AttributeList();
        final Iterator iterator = attributes.iterator();
        while( iterator.hasNext() )
        {
            final Attribute attribute = (Attribute)iterator.next();
            try
            {
                setAttribute( attribute );
                final String name = attribute.getName();
                final Object value = getAttribute( name );
                result.add( new Attribute( name, value ) );
            }
            catch( final Exception e )
            {
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Object getAttribute( final String name )
        throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        return m_type.getAttribute( m_target, name );
    }

    /**
     * {@inheritDoc}
     */
    public void setAttribute( final Attribute attribute )
        throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        m_type.setAttribute( m_target, attribute );
    }

    /**
     * {@inheritDoc}
     */
    public Object invoke( final String actionName,
                          final Object[] params,
                          final String[] signature )
        throws MBeanException, ReflectionException
    {
        return m_type.invoke( m_target, actionName, params, signature );
    }
}

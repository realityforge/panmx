package panmx.model;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.RequiredModelMBean;

/**
 * Wrapper for RequiredModelMBean that enables MBeanRegistration for resource.
 */
class WrapperRequiredModelMBean
    extends RequiredModelMBean
    implements MBeanRegistration
{
    /**
     * The managed resource.
     */
    private Object m_resource;

    /**
     * {@inheritDoc}
     */
    WrapperRequiredModelMBean( final ModelMBeanInfo info )
        throws MBeanException, RuntimeOperationsException
    {
        super( info );
    }

    /**
     * {@inheritDoc}
     */
    public void setManagedResource( final Object mr,
                                    final String mr_type )
        throws MBeanException, RuntimeOperationsException, InstanceNotFoundException, InvalidTargetObjectTypeException
    {
        super.setManagedResource( mr, mr_type );
        m_resource = mr;
    }

    /**
     * {@inheritDoc}
     */
    public ObjectName preRegister( final MBeanServer server,
                                   final ObjectName name )
        throws Exception
    {
        ObjectName result = super.preRegister( server, name );
        if( m_resource instanceof MBeanRegistration )
        {
            result = ( (MBeanRegistration)m_resource ).preRegister( server, result );
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public void postRegister( final Boolean registrationDone )
    {
        if( m_resource instanceof MBeanRegistration )
        {
            ( (MBeanRegistration)m_resource ).postRegister( registrationDone );
        }
        super.postRegister( registrationDone );
    }

    /**
     * {@inheritDoc}
     */
    public void preDeregister()
        throws Exception
    {
        super.preDeregister();
        if( m_resource instanceof MBeanRegistration )
        {
            ( (MBeanRegistration)m_resource ).preDeregister();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void postDeregister()
    {
        if( m_resource instanceof MBeanRegistration )
        {
            ( (MBeanRegistration)m_resource ).postDeregister();
        }
        super.postDeregister();
    }
}

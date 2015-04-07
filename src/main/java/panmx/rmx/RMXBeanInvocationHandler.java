package panmx.rmx;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.management.Attribute;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import panmx.util.BeanUtil;

class RMXBeanInvocationHandler
    implements InvocationHandler
{
    private final MBeanServerConnection m_connection;
    private final ObjectName m_objectName;
    private RMXBeanType m_type;

    RMXBeanInvocationHandler( final MBeanServerConnection connection,
                              final ObjectName objectName )
    {
        m_connection = connection;
        m_objectName = objectName;
    }

    static Object newProxyInstance( final MBeanServerConnection connection,
                                    final ObjectName objectName,
                                    final ClassLoader classLoader,
                                    final Class<?>[] mxTypes )
        throws NotCompliantMBeanException
    {
        final RMXBeanInvocationHandler handler =
            new RMXBeanInvocationHandler( connection, objectName );
        final Object proxy =
            Proxy.newProxyInstance( classLoader, mxTypes, handler );
        handler.m_type = new StandardRMXBeanType( proxy.getClass(), mxTypes );
        return proxy;
    }

    public Object invoke( final Object proxy, final Method method, final Object[] params )
        throws Throwable
    {
        if( BeanUtil.isAccessor( method ) )
        {
            final String name = BeanUtil.getAttributeName( method );
            return m_type.getAttribute( m_connection, m_objectName, name );
        }
        else if( BeanUtil.isMutator( method ) )
        {
            final String name = BeanUtil.getAttributeName( method );
            final Attribute attribute = new Attribute( name, params[0] );
            m_type.setAttribute( m_connection, m_objectName, attribute );
            return null;
        }
        else
        {
            final String[] signature = BeanUtil.getSignature( method );
            try
            {
                return m_type.invoke( m_connection, m_objectName, method.getName(), params, signature );
            }
            catch( final MBeanException mbe )
            {
                throw mbe.getTargetException();
            }
        }
    }
}

package panmx.rmx;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import javax.management.NotCompliantMBeanException;
import javax.management.openmbean.OpenDataException;
import panmx.annotations.MBean;
import panmx.annotations.MxAttribute;
import panmx.annotations.MxOperation;
import panmx.util.BeanUtil;

class AnnotatedRMXBeanType
    extends RMXBeanType
{
    /** Permission needed to clear complete cache. */
    private static final RuntimePermission CLEAR_CACHE_PERMISSION =
        new RuntimePermission( AnnotatedRMXBeanType.class.getName() + ".clearCache" );
    /** This maps Class to MBeanInfo. */
    private static final Map<Class<?>, AnnotatedRMXBeanType> c_types =
        new WeakHashMap<Class<?>, AnnotatedRMXBeanType>();

    /**
     * Clear the cache of AnnotatedRMXBeanType currently loaded into the system.
     *
     * <p>Note that the caller must have been granted the
     * "panmx.rmx.AnnotatedRMXBeanType.clearCache"mBeanInfoManager.clearCompleteCache"
     * {@link RuntimePermission} or else a security exception will be thrown.</p>
     *
     * @throws SecurityException if the caller does not have permission
     */
    static final synchronized void clearCache()
        throws SecurityException
    {
        final SecurityManager sm = System.getSecurityManager();
        if( null != sm )
        {
            sm.checkPermission( CLEAR_CACHE_PERMISSION );
        }

        c_types.clear();
    }

    /**
     * Return the AnnotatedRMXBeanType for specified class.
     *
     * @param clazz the class.
     * @return the AnnotatedRMXBeanType for specified object.
     * @throws NotCompliantMBeanException if malformed annotations for specified object.
     */
    static final AnnotatedRMXBeanType getAnnotatedRMXBeanType( final Class<?> clazz )
        throws NotCompliantMBeanException
    {
        AnnotatedRMXBeanType type = c_types.get( clazz );
        if( null == type )
        {
            type = new AnnotatedRMXBeanType( clazz );
            c_types.put( clazz, type );
        }

        return type;
    }

    AnnotatedRMXBeanType( final Class<?> type )
        throws NotCompliantMBeanException
    {
        super( type );

        final MBean mBean = type.getAnnotation( MBean.class );
        if( null == mBean )
        {
            final String message =
                "Class " + type.getName() + " is missing the MBean annotation.";
            throw new NotCompliantMBeanException( message );
        }

        try
        {
            defineManagementElements( type.getMethods(), false );
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
                defineManagementElements( mx.getMethods(), true );
            }
            freeze();
        }
        catch( final OpenDataException ode )
        {
            final NotCompliantMBeanException e =
                new NotCompliantMBeanException( ode.getMessage() );
            e.initCause( ode );
            throw e;
        }
    }

    /**
     * Define management elements from methods.
     *
     * @param methods the methods.
     * @param force if true always add method even if not annotated.
     * @throws OpenDataException if there is non-compliant methods.
     */
    private void defineManagementElements( final Method[] methods,
                                           final boolean force )
        throws OpenDataException
    {
        for( final Method method : methods )
        {
            final MxAttribute attribute = method.getAnnotation( MxAttribute.class );
            if( null != attribute )
            {
                defineAttribute( method );
            }
            final MxOperation operation = method.getAnnotation( MxOperation.class );
            if( null != operation )
            {
                defineOperation( method );
            }
            if( force && null == operation && null == attribute )
            {
                if( BeanUtil.isAccessor( method ) )
                {
                    defineAttribute( method );
                }
                else
                {
                    defineOperation( method );
                }
            }
        }
    }
}

package panmx.model;

import java.util.Map;
import java.util.WeakHashMap;
import javax.management.JMException;
import javax.management.NotCompliantMBeanException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.RequiredModelMBean;

/**
 * Utility class to create ModelMBeans and their associated MBeanInfo objects.
 */
public class ModelMBeanFactory
{
    /** The managed type for RequiredModelMBean objects. */
    private static final String OBJECT_REF_TYPE = "ObjectReference";
    /** Permission needed to clear complete cache. */
    private static final RuntimePermission CLEAR_CACHE_PERMISSION =
        new RuntimePermission( "panmx.model.ModelMBeanFactory.clearCache" );
    /** This maps Class to MBeanInfo. */
    private static final Map<Class<?>, ModelMBeanInfo> c_infos =
        new WeakHashMap<Class<?>, ModelMBeanInfo>();

    /**
     * Create ModelMBean for annotated object.
     *
     * @param object the object.
     * @return the ModelMBean.
     * @throws NotCompliantMBeanException if the object does not conform to
     *                                    ModelMBean specification.
     * @throws JMException if there is an error creating RequiredModelMBean.
     */
    public static ModelMBean createAnnotatedModelMBean( final Object object )
        throws NotCompliantMBeanException, JMException, InvalidTargetObjectTypeException
    {
        if( null == object )
        {
            throw new NullPointerException( "object" );
        }
        final ModelMBeanInfo info = getMBeanInfo( object.getClass() );
        final RequiredModelMBean mBean = new WrapperRequiredModelMBean( info );
        mBean.setManagedResource( object, OBJECT_REF_TYPE );
        return mBean;
    }

    /**
     * Clear the cache of all MBeanInfos currently loaded into the system.
     *
     * <p>Note that the caller must have been granted the
     * "panmx.model.ModelMBeanFactory.clearCache" {@link RuntimePermission}
     * or a security exception will be thrown.</p>
     *
     * @throws SecurityException if the caller does not have permission to clear cache.
     */
    public static final synchronized void clearCache()
        throws SecurityException
    {
        final SecurityManager sm = System.getSecurityManager();
        if( null != sm )
        {
            sm.checkPermission( CLEAR_CACHE_PERMISSION );
        }
        c_infos.clear();
    }

    /**
     * Return the MBeanInfo for type.
     *
     * @param type the type.
     * @return the MBeanInfo for specified type.
     * @throws NotCompliantMBeanException if malformed type.
     */
    public static final ModelMBeanInfo getMBeanInfo( final Class<?> type )
        throws NotCompliantMBeanException
    {
        ModelMBeanInfo info = c_infos.get( type );
        if( null == info )
        {
            info = ModelMBeanInfoCreator.createModelMBeanInfo( type );
            c_infos.put( type, info );
        }
        return info;
    }
}

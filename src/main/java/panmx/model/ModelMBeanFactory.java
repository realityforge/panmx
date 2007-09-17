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
 * Utility class to create ModelMBeans and their associated MBeanInfo 
 * objects from annotated Java classes.
 *
 * <p>The user can expose a java object as a ModelMBean by annotating
 * the java class using the Java 1.5 Annotations defined in the package 
 * 'panmx.annotations'. The ModelMBeanFactory class currently supports
 * the all of the annotations except for MxConstructor.</p>
 *
 * <p>Following are some code examples and a description of the 
 * corresponding ModelMBeanInfo that could be created from the 
 * class.</p>
 *
 * <p>Example 1: ModelMBeanInfo with a description "Component to measure 
 * and clean out toxins" but 0 attributes and 0 operations.</p>
 *
 * <code>
 * <pre>
 * &#64;MBean(description = "Component to measure and clean out toxins")
 *   public class Liver
 * {
 * } 
 * </pre>
 * </code>
 *
 *
 * <p>Example 2: ModelMBeanInfo with 0 attributes and 1 operation 'clean'</p>
 *
 * <code>
 * <pre>
 * &#64;MBean()
 *   public class Liver
 * {
 *    private float m_toxicity;
 *
 *    &#64;MxOperation(description = "Clean out the toxins")
 *      public void clean()
 *    {
 *      m_toxicity = 0;
 *    }
 * } 
 * </pre>
 * </code>
 *
 * <p>Example 3: ModelMBeanInfo with 1 read-only attribute 'toxicity', with 
 * a description "Level of toxicity" and display name of "Toxicity Level". 
 * Corresponding operation for accessor 'getToxicity'.</p>
 *
 * <code>
 * <pre>
 * &#64;MBean()
 *   public class Liver
 * {
 *    private float m_toxicity;
 *
 *    &#64;MxAttribute(description = "Level of toxicity", displayName = "Toxicity Level")
 *      public float getToxicity()
 *    {
 *      return m_toxicity;
 *    }
 * } 
 * </pre>
 * </code>
 *
 * <p>Example 4: ModelMBeanInfo with 1 attribute named 'toxicity', with 
 * a description "Level of toxicity" and display name of "Toxicity Level". 
 * Corresponding operations for accessor 'getToxicity' and mutator 'setToxicity'.</p>
 *
 * <code>
 * <pre>
 * &#64;MBean()
 *   public class Liver
 * {
 *    private float m_toxicity;
 *
 *    &#64;MxAttribute()
 *      public float getToxicity()
 *    {
 *      return m_toxicity;
 *    }
 *
 *    &#64;MxAttribute(description = "Level of toxicity", displayName = "Toxicity Level")
 *      public void setToxicity(final float toxicity)
 *    {
 *      m_toxicity = toxicity;
 *    }
 * } 
 * </pre>
 * </code>
 *
 * <p>Example 5: ModelMBeanInfo with 0 attributes and 1 operation 'clean'.</p>
 *
 * <code>
 * <pre>
 * public interface LiverRMXBean
 * {
 *   &#64;MxOperation(description = "Clean out the toxins")
 *   void clean();
 * }
 *
 * &#64;MBean(interfaces = {LiverMBean.class})
 *   public class Liver
 * {
 *    private float m_toxicity;
 *
 *      public void clean()
 *    {
 *      m_toxicity = 0;
 *    }
 * } 
 * </pre>
 * </code>
 * 
 * <p>Some points to note;</p>
 * <ul>
 *   <li>Annotations defined in management interfaces overide those defined 
 *       in the class.</li>
 *   <li>The properties of the MxAttribute on the writer method will override
 *       properties present on reader method if the description property is 
 *       specified.</li> 
 *   <li>The "currencyTimeLimit" field on a ModelMBeanAttributeInfo will
 *       default to 1 (millisecond) unless otherwise specified.</li>
 *   <li>Declaring a MxAttribute annotation on an accessor or mutator
 *       will also result in that method being declared as an operation.</li>
 *   <li>The "displayname" field in both ModelMBeanOperationInfo and 
 *       ModelMBeanAttributeInfo is set to the "name" of the attribute
 *       or operation unless otherwise specified.</li>
 * </ul>
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
    public static synchronized void clearCache()
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
    public static ModelMBeanInfo getMBeanInfo( final Class<?> type )
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

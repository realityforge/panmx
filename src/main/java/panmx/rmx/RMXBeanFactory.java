package panmx.rmx;

import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * The <tt>RMXBeanFactory</tt> consists of static methods to create RMXBeans.
 *
 * <h3>RMXBeans</h3>
 * A RMXBean is a MBean that only uses datatypes
 * that conform to Open MBean {@link javax.management.openmbean.OpenType types}.
 * This means that the JMX Connector Server and Connector Client need not share
 * mBean specific implementation classes. RMXBeans are strongly influenced by
 * the design of Suns MXBeans.
 *
 * <p>The RMXBean may use the following types;</p>
 * <ul>
 *   <li>
 *     Primitive types and the corresponding wrapper classes;
 *     <ul>
 *       <li><tt>boolean</tt> / {@link java.lang.Boolean java.lang.Boolean}</li>
 *       <li><tt>byte</tt> / {@link java.lang.Byte java.lang.Byte}</li>
 *       <li><tt>short</tt> / {@link java.lang.Short java.lang.Short}</li>
 *       <li><tt>int</tt> / {@link java.lang.Integer java.lang.Integer}</li>
 *       <li><tt>long</tt> / {@link java.lang.Long java.lang.Long}</li>
 *       <li><tt>float</tt> / {@link java.lang.Float java.lang.Float}</li>
 *       <li><tt>double</tt> / {@link java.lang.Double java.lang.Double}</li>
 *     </ul>
 *   </li>
 *   <li>
 *     Other simple types;
 *     <ul>
 *       <li>{@link java.lang.String java.lang.String}</li>
 *       <li>{@link java.math.BigDecimal java.math.BigDecimal}</li>
 *       <li>{@link java.math.BigInteger java.math.BigInteger}</li>
 *       <li>{@link java.util.Date java.util.Date}</li>
 *       <li>{@link javax.management.ObjectName javax.management.ObjectName}</li>
 *     </ul>
 *   </li>
 *   <li>
 *     {@link java.lang.Enum Enum} classes. An Enum is mapped to a String
 *     with same value as the name of the enum constant.
 *   </li>
 *   <li>
 *     Classes that defines three static methods with the following signatures
 *     where <tt>T</tt> is the Class type. These classes are converted to
 *     {@link javax.management.openmbean.CompositeData CompositeData} values.
 *     Theses methods can be passed null values and this scenario should be gracefully
 *     handled by the class.
 *     <pre>
 *     public static CompositeType getCompositeType()
 *         throws OpenDataException
 *     {
 *         ...
 *     }
 *
 *     public static T fromCompositeData( CompositeData compositeData )
 *         throws OpenDataException
 *     {
 *         ...
 *     }
 *
 *     public static CompositeData toCompositeData( T t )
 *         throws OpenDataException
 *     {
 *         ...
 *     }
 *     </pre>
 *   </li>
 *   <li>
 *     Classes that have fields that conform to the <a href="#Field">Field</a>
 *     specification. The fields can be any of the above types and are
 *     converted to
 *     {@link javax.management.openmbean.CompositeData CompositeData} values.
 *   </li>
 *   <li>
 *     {@link java.util.List List&lt;E&gt;} where <tt>E</tt> is one of the above
 *     types. The List is mapped to an array of element type <tt>E</tt>.
 *   </li>
 *   <li>
 *     {@link java.util.Map Map&lt;K,V&gt;} where <tt>K</tt> and <tt>V</tt> are one of
 *     the above types excluding List. The Map is mapped to a
 *     {@link javax.management.openmbean.TabularData TabularData} that has a row type is
 *     a {@link javax.management.openmbean.CompositeType CompositeType} with "key" and "value"
 *     fields of type <tt>K</tt> and <tt>V</tt>.
 *   </li>
 * </ul>
 *
 * <h3><a name="Field">Field</a></h3>
 *
 * <p>RMX Beans can have types that are java beans made up of fields that are expected to
 * have both accessor and mutator methods. The accessors are expected to follow the standard
 * Java Bean pattern;
 *
 * <pre>
 *   public [Type] get[name]() { ... }
 * </pre>
 *
 * <p>or if the attribute has a boolean type it can also be accessed via;</p>
 *
 * <pre>
 *   public boolean is[name]() { ... }
 * </pre>
 *
 * <p>Mutators also follow the standard Java Bean pattern with one
 * modification - mutators do NOT need to be public.
 *
 * <pre>
 *   void set[name]( [Type] t ) { ... }
 * </pre>
 *
 * <h3>Usage Patterns</h3>
 *
 * <p>There are two techniques for creating RMXBean beans. One is to create a RMXBean
 * from the component and a corresponding MXBean interface that defines the management
 * interface. These RMXBeans are "StandardRMXBean"s and are created using the
 * RMXBeanFactory.createStandardRMXBean(...) methods. If not otherwise specified a
 * RMXBean will have a management interface with a classname the same as it's classname
 * postfixed with "RMXBean".</p>
 *
 * <p>The other technique is to annotate the MBean using the annotations defined in
 * the {@link panmx.annotations annotations} package and use the
 * RMXBeanFactory.createAnnotatedRMXBean(...) method.</p>
 */
public final class RMXBeanFactory
{
    /** Suffix for the Standard RMXBean interface.. */
    private static final String RMXBEAN_SUFFIX = "RMXBean";

    /**
     * Create RMXBean from annotated object.
     *
     * @param object the object.
     * @return the RMXBean.
     * @throws NotCompliantMBeanException if the object does not conform to
     *                                    RMXBean specification.
     */
    public static Object createAnnotatedRMXBean( final Object object )
        throws NotCompliantMBeanException
    {
        if( null == object )
        {
            throw new NullPointerException( "object" );
        }
        final AnnotatedRMXBeanType beanType =
            AnnotatedRMXBeanType.getAnnotatedRMXBeanType( object.getClass() );
        return new RMXBean( beanType, object );
    }

    /**
     * Create StandardRMXBean using "default" management interface.
     *
     * @param object the object.
     * @return the RMXBean.
     * @throws NotCompliantMBeanException if the object does not conform to
     *                                    RMXBean specification.
     */
    public static Object createStandardRMXBean( final Object object )
        throws NotCompliantMBeanException
    {
        return createStandardRMXBean( object, findMxTypeFor( object.getClass() ) );
    }

    /**
     * Create StandardRMXBean with specified management interface.
     *
     * @param object the object.
     * @param mxInterface the management interface.
     * @return the RMXBean.
     * @throws NotCompliantMBeanException if the object does not conform to
     *                                    RMXBean specification.
     */
    public static Object createStandardRMXBean( final Object object,
                                                final Class mxInterface )
        throws NotCompliantMBeanException
    {
        return createStandardRMXBean( object, new Class[]{mxInterface} );
    }

    /**
     * Create StandardRMXBean with specified management interfaces.
     *
     * @param object the object.
     * @param mxInterfaces the management interfaces.
     * @return the RMXBean.
     * @throws NotCompliantMBeanException if the object does not conform to
     *                                    RMXBean specification.
     */
    public static Object createStandardRMXBean( final Object object,
                                                final Class[] mxInterfaces )
        throws NotCompliantMBeanException
    {
        if( null == object )
        {
            throw new NullPointerException( "object" );
        }
        if( null == mxInterfaces )
        {
            throw new NullPointerException( "mxInterfaces" );
        }
        for( int i = 0; i < mxInterfaces.length; i++ )
        {
            if( null == mxInterfaces[i] )
            {
                throw new NullPointerException( "mxInterfaces[" + i + "]" );
            }
        }

        final Class type = object.getClass();
        final StandardRMXBeanType beanType =
            new StandardRMXBeanType( type, mxInterfaces );
        return new RMXBean( beanType, object );
    }

    /**
     * Create proxy object for interacting with RMXBean.
     *
     * @param connection the MBeanServerConnection.
     * @param objectName the name of object on server.
     * @param mxType the interface to proxy.
     * @return the proxy object.
     */
    public static Object newProxyInstance( final MBeanServerConnection connection,
                                           final ObjectName objectName,
                                           final Class mxType )
        throws NotCompliantMBeanException
    {
        return newProxyInstance( connection,
                                 objectName,
                                 mxType.getClassLoader(),
                                 new Class<?>[]{mxType} );
    }

    /**
     * Create proxy object for interacting with RMXBean.
     *
     * @param connection the MBeanServerConnection.
     * @param objectName the name of object on server.
     * @param classLoader the parent classloader of proxy. The mxTypes
     *                    must be accessible through ClassLoader.
     * @param mxTypes the interfaces to proxy.
     * @return the proxy object.
     */
    public static Object newProxyInstance( final MBeanServerConnection connection,
                                           final ObjectName objectName,
                                           final ClassLoader classLoader,
                                           final Class<?>[] mxTypes )
        throws NotCompliantMBeanException
    {
        return RMXBeanInvocationHandler.
            newProxyInstance( connection, objectName, classLoader, mxTypes );
    }

    /**
     * Return MXType for specified class.
     * The MXType is just the class name of the type + "RMXBean".
     *
     * @param type the type.
     * @return the MX type.
     * @throws javax.management.NotCompliantMBeanException if unable to locate mx type class.
     */
    private static Class findMxTypeFor( final Class type )
        throws NotCompliantMBeanException
    {
        final String name = type.getName() + RMXBEAN_SUFFIX;
        try
        {
            return type.getClassLoader().loadClass( name );
        }
        catch( final ClassNotFoundException e )
        {
            final String message = "Missing class " + name;
            throw new NotCompliantMBeanException( message );
        }
    }
}

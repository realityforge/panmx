package panmx.rmx;

import javax.management.Attribute;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.SimpleType;
import junit.framework.TestCase;
import panmx.annotations.MBean;
import panmx.annotations.MxAttribute;
import panmx.annotations.MxOperation;

public class AnnotatedRMXBeanTypeTestCase
    extends TestCase
{
    static interface LiverRMXBean
    {
        float getToxicity();

        @MxAttribute(description = "Level of toxicity2")
        void setToxicity( float toxicity );

        @MxOperation(description = "Clean out the toxins2")
        void clean();
    }

    @MBean()
        static class Liver
        implements LiverRMXBean
    {
        private float m_toxicity;

        @MxAttribute()
            public float getToxicity()
        {
            return m_toxicity;
        }

        @MxAttribute(description = "Level of toxicity")
            public void setToxicity( float toxicity )
        {
            m_toxicity = toxicity;
        }

        @MxOperation(description = "Clean out the toxins")
            public void clean()
        {
            m_toxicity = 0;
        }
    }

    public void testSimpleAnnotatedClass()
        throws Exception
    {
        final AnnotatedRMXBeanType type =
            new AnnotatedRMXBeanType( Liver.class );

        final MBeanInfo info = type.getMBeanInfo();

        assertEquals( "info.getAttributes().length", 1, info.getAttributes().length );
        assertEquals( "info.getAttributes()[0].isReadable()",
                      true, info.getAttributes()[0].isReadable() );
        assertEquals( "info.getAttributes()[0].isIs()",
                      false, info.getAttributes()[0].isIs() );
        assertEquals( "info.getAttributes()[0].isWritable()",
                      true, info.getAttributes()[0].isWritable() );
        assertEquals( "info.getAttributes()[0].getName()",
                      "toxicity", info.getAttributes()[0].getName() );
        assertEquals( "info.getAttributes()[0].getDescription()",
                      "Level of toxicity",
                      info.getAttributes()[0].getDescription() );
        assertEquals( "info.getAttributes()[0].getOpenType()",
                      SimpleType.FLOAT,
                      ((OpenMBeanAttributeInfo)info.getAttributes()[0]).getOpenType() );

        assertEquals( "info.getAttributes().length", 1, info.getOperations().length );
        assertEquals( "info.getAttributes()[0].getName()",
                      "clean", info.getOperations()[0].getName() );
        assertEquals( "info.getAttributes()[0].getName()",
                      "Clean out the toxins", info.getOperations()[0].getDescription() );
        assertEquals( "info.getAttributes()[0].getImpact()",
                      MBeanOperationInfo.ACTION,
                      info.getOperations()[0].getImpact() );
        assertEquals( "info.getAttributes()[0].getReturnType()",
                      "java.lang.Void",
                      info.getOperations()[0].getReturnType() );
        assertEquals( "info.getAttributes()[0].getSignature().length",
                      0,
                      info.getOperations()[0].getSignature().length );

        final Liver liver = new Liver();
        liver.setToxicity( 42.0F );
        final RMXBean bean = new RMXBean( type, liver );

        assertEquals( "1. liver.getToxicity()", 42.0F, liver.getToxicity() );
        assertEquals( "1. bean.getAttribute(toxicity)", 42.0F, bean.getAttribute( "toxicity" ) );

        bean.setAttribute( new Attribute( "toxicity", 15.0F ) );

        assertEquals( "2. liver.getToxicity()", 15.0F, liver.getToxicity() );
        assertEquals( "2. bean.getAttribute(toxicity)", 15.0F, bean.getAttribute( "toxicity" ) );

        bean.invoke( "clean", new Object[0], new String[0] );

        assertEquals( "3. liver.getToxicity()", 0.0F, liver.getToxicity() );
        assertEquals( "3. bean.getAttribute(toxicity)", 0.0F, bean.getAttribute( "toxicity" ) );
    }

    @MBean(interfaces = {LiverRMXBean.class})
        static class Liver2
        implements LiverRMXBean
    {
        private float m_toxicity;

        public float getToxicity()
        {
            return m_toxicity;
        }

        public void setToxicity( float toxicity )
        {
            m_toxicity = toxicity;
        }

        public void clean()
        {
            m_toxicity = 0;
        }
    }

    public void testAnnotatedClassWithSeparateManagementInterface()
        throws Exception
    {
        final AnnotatedRMXBeanType type =
            new AnnotatedRMXBeanType( Liver2.class );

        final MBeanInfo info = type.getMBeanInfo();

        assertEquals( "info.getAttributes().length", 1, info.getAttributes().length );
        assertEquals( "info.getAttributes()[0].isReadable()",
                      true, info.getAttributes()[0].isReadable() );
        assertEquals( "info.getAttributes()[0].isIs()",
                      false, info.getAttributes()[0].isIs() );
        assertEquals( "info.getAttributes()[0].isWritable()",
                      true, info.getAttributes()[0].isWritable() );
        assertEquals( "info.getAttributes()[0].getName()",
                      "toxicity", info.getAttributes()[0].getName() );
        assertEquals( "info.getAttributes()[0].getDescription()",
                      "Level of toxicity2",
                      info.getAttributes()[0].getDescription() );
        assertEquals( "info.getAttributes()[0].getOpenType()",
                      SimpleType.FLOAT,
                      ((OpenMBeanAttributeInfo)info.getAttributes()[0]).getOpenType() );

        assertEquals( "info.getAttributes().length", 1, info.getOperations().length );
        assertEquals( "info.getAttributes()[0].getName()",
                      "clean", info.getOperations()[0].getName() );
        assertEquals( "info.getAttributes()[0].getName()",
                      "Clean out the toxins2", info.getOperations()[0].getDescription() );
        assertEquals( "info.getAttributes()[0].getImpact()",
                      MBeanOperationInfo.ACTION,
                      info.getOperations()[0].getImpact() );
        assertEquals( "info.getAttributes()[0].getReturnType()",
                      "java.lang.Void",
                      info.getOperations()[0].getReturnType() );
        assertEquals( "info.getAttributes()[0].getSignature().length",
                      0,
                      info.getOperations()[0].getSignature().length );

        final Liver2 liver = new Liver2();
        liver.setToxicity( 42.0F );
        final RMXBean bean = new RMXBean( type, liver );

        assertEquals( "1. liver.getToxicity()", 42.0F, liver.getToxicity() );
        assertEquals( "1. bean.getAttribute(toxicity)", 42.0F, bean.getAttribute( "toxicity" ) );

        bean.setAttribute( new Attribute( "toxicity", 15.0F ) );

        assertEquals( "2. liver.getToxicity()", 15.0F, liver.getToxicity() );
        assertEquals( "2. bean.getAttribute(toxicity)", 15.0F, bean.getAttribute( "toxicity" ) );

        bean.invoke( "clean", new Object[0], new String[0] );

        assertEquals( "3. liver.getToxicity()", 0.0F, liver.getToxicity() );
        assertEquals( "3. bean.getAttribute(toxicity)", 0.0F, bean.getAttribute( "toxicity" ) );
    }
}

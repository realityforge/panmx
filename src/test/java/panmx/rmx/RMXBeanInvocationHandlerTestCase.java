package panmx.rmx;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import junit.framework.TestCase;

public class RMXBeanInvocationHandlerTestCase
    extends TestCase
{
    static class Chemical
    {
        private String m_name;
        private int m_quantity;

        public String getName()
        {
            return m_name;
        }

        public void setName( String name )
        {
            m_name = name;
        }

        public int getQuantity()
        {
            return m_quantity;
        }

        public void setQuantity( int quantity )
        {
            m_quantity = quantity;
        }

        public int hashCode()
        {
            return m_quantity + m_name.hashCode();
        }

        public boolean equals( final Object other )
        {
            final Chemical chemical = (Chemical)other;
            return chemical.m_quantity == m_quantity && chemical.m_name.equals( m_name );
        }
    }

    static class Liver
        implements LiverRMXBean
    {
        private Chemical m_chemical;
        private float m_toxicity;

        public Chemical getChemical()
        {
            return m_chemical;
        }

        public boolean isChemicalCulprit( final Chemical chemical )
        {
            return chemical.equals( m_chemical );
        }

        public void setChemical( Chemical chemical )
        {
            m_chemical = chemical;
        }

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

    static interface LiverRMXBean
    {
        Chemical getChemical();

        void setChemical( Chemical chemical );

        boolean isChemicalCulprit( Chemical chemical );

        float getToxicity();

        void setToxicity( float toxicity );

        void clean();
    }

    public void testBasicInterfaceConstruction()
        throws Exception
    {
        final Chemical water = new Chemical();
        water.setName( "Water" );
        water.setQuantity( 0 );

        final Chemical caffeine = new Chemical();
        caffeine.setName( "Caffeine" );
        caffeine.setQuantity( 99 );

        final StandardRMXBeanType type =
            new StandardRMXBeanType( Liver.class, new Class[]{LiverRMXBean.class} );
        final Liver liver = new Liver();
        liver.setToxicity( 42.0F );
        liver.setChemical( caffeine );
        final RMXBean bean = new RMXBean( type, liver );

        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        final String sName =
            "rmxtest:class=RMXBeanInvocationHandlerTestCase," +
            "testcase=testBasicInterfaceConstruction," +
            "id=" + liver.hashCode();
        final ObjectName name = new ObjectName( sName );
        server.registerMBean( bean, name );
        final LiverRMXBean mx =
            (LiverRMXBean)RMXBeanInvocationHandler.newProxyInstance( server,
                                                                     name,
                                                                     LiverRMXBean.class.getClassLoader(),
                                                                     new Class<?>[]{LiverRMXBean.class} );

        assertEquals( "1. mx.getToxicity()", 42.0F, mx.getToxicity() );
        mx.setToxicity( 15.0F );

        assertEquals( "2. mx.getToxicity()", 15.0F, mx.getToxicity() );
        mx.clean();

        assertEquals( "3. mx.getToxicity()", 0.0F, mx.getToxicity() );
        assertEquals( "3. mx.getChemical()", caffeine, mx.getChemical() );
        assertTrue( "3. instance mx.getChemical()", caffeine != mx.getChemical() );
        assertEquals( "3. mx.isChemicalCulprit(caffeine)",
                      true, mx.isChemicalCulprit( caffeine ) );
        assertEquals( "3. mx.isChemicalCulprit(water)",
                      false, mx.isChemicalCulprit( water ) );

        mx.setChemical( water );

        assertEquals( "4. mx.isChemicalCulprit(caffeine)",
                      false, mx.isChemicalCulprit( caffeine ) );
        assertEquals( "4. mx.isChemicalCulprit(water)",
                      true, mx.isChemicalCulprit( water ) );

    }
}

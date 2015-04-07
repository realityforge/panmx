package panmx.rmx;

import javax.management.Attribute;
import junit.framework.TestCase;

public class StandardRMXBeanTypeTestCase
    extends TestCase
{
    static class Liver
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

    static interface LiverRMXBean
    {
        float getToxicity();

        void setToxicity( float toxicity );

        void clean();
    }

    public void testStandardRMXBeanType()
        throws Exception
    {
        final StandardRMXBeanType type =
            new StandardRMXBeanType( Liver.class, new Class[]{LiverRMXBean.class} );
        final Liver liver = new Liver();
        liver.setToxicity( 42.0F );
        final RMXBean bean = new RMXBean( type, liver );

        assertEquals( "1. liver.getToxicity()", 42.0F, liver.getToxicity() );
        assertEquals( "1. bean.getAttribute(toxicity)", 42.0F, bean.getAttribute( "toxicity" ) );

        bean.setAttribute( new Attribute( "toxicity", 15.0F ) );

        assertEquals( "2. liver.getToxicity()", 15.0F, liver.getToxicity() );
        assertEquals( "2. bean.getAttribute(toxicity)", 15.0F, bean.getAttribute( "toxicity" ) );
    }
}

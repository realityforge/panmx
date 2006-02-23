package panmx.rmx;

import java.lang.reflect.Method;
import javax.management.Attribute;
import junit.framework.TestCase;

public class RMXBeanTestCase
    extends TestCase
{
    static class Liver
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

    public void testSimpleAttribute()
        throws Exception
    {
        final Method read = Liver.class.getMethod( "getToxicity" );
        final Method write =
            Liver.class.getMethod( "setToxicity", new Class[]{Float.TYPE} );

        final RMXBeanType beanType = new RMXBeanType( Liver.class );
        beanType.defineAttribute( read );
        beanType.defineAttribute( write );
        beanType.freeze();

        final Liver liver = new Liver();
        liver.setToxicity( 23.0F );
        final RMXBean bean = new RMXBean( beanType, liver );

        assertEquals( "1. liver.getToxicity()", 23.0F, liver.getToxicity() );
        assertEquals( "1. bean.getAttribute(toxicity)", 23.0F, bean.getAttribute( "toxicity" ) );

        bean.setAttribute( new Attribute( "toxicity", 15.0F ) );

        assertEquals( "2. liver.getToxicity()", 15.0F, liver.getToxicity() );
        assertEquals( "2. bean.getAttribute(toxicity)", 15.0F, bean.getAttribute( "toxicity" ) );
    }


    public void testSimpleOperation()
        throws Exception
    {
        final Method method =
            Liver.class.getMethod( "clean", new Class[0] );

        final RMXBeanType beanType = new RMXBeanType( Liver.class );
        beanType.defineOperation( method );
        beanType.freeze();

        final Liver liver = new Liver();
        liver.setToxicity( 23.0F );
        final RMXBean bean = new RMXBean( beanType, liver );

        assertEquals( "1. liver.getToxicity()", 23.0F, liver.getToxicity() );
        try
        {
            bean.invoke( "clean",  null, null );
        }
        catch( final Exception e )
        {
            fail( "Unexpected exception when invoking operation 'clean': " + e );
        }

        assertEquals( "2. liver.getToxicity()", 0.0F, liver.getToxicity() );
    }
}

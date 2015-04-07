package panmx.rmx;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.OpenDataException;

public class RMXBeanTest
{
    static class Food
    {
        private String m_name;
        private boolean m_nutritious;

        public String getName()
        {
            return m_name;
        }

        public void setName( final String name )
        {
            m_name = name;
        }

        public boolean isNutritious()
        {
            return m_nutritious;
        }

        public void setNutritious( boolean nutritious )
        {
            m_nutritious = nutritious;
        }
    }

    static class Stomache
    {
        private Food m_contents;

        public Food getContents()
        {
            return m_contents;
        }

        public void setContents( Food contents )
        {
            m_contents = contents;
        }
    }

    static class Concept
    {
        private String m_name;
        private int m_importance;

        public String getName()
        {
            return m_name;
        }

        void setName( String name )
        {
            m_name = name;
        }

        public int getImportance()
        {
            return m_importance;
        }

        void setImportance( int importance )
        {
            m_importance = importance;
        }
    }

    static class Mind
    {
        private Map<String, Concept> m_contents = new HashMap<String, Concept>();
        private final List<ObjectName> m_controls = new ArrayList<ObjectName>();

        public List<ObjectName> getControls()
        {
            return m_controls;
        }

        public Map<String, Concept> getContents()
        {
            return m_contents;
        }

        public void setContents( Map<String, Concept> contents )
        {
            m_contents = contents;
        }
    }

    public static void main( final String[] args )
        throws Exception
    {
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        server.registerMBean( createFoodBean(), new ObjectName( "rmx:type=Food,name=f1" ) );
        server.registerMBean( createStomacheBean(), new ObjectName( "rmx:type=Stomache,name=s1" ) );
        server.registerMBean( createMindBean(), new ObjectName( "rmx:type=Mind,name=m1" ) );


        while( true )
        {
            Thread.sleep( 1000 );
        }
    }

    private static RMXBean createMindBean()
        throws Exception
    {
        final RMXBeanType type = new RMXBeanType( Mind.class );
        type.defineAttribute( Mind.class.getMethod( "getControls" ) );
        type.defineAttribute( Mind.class.getMethod( "getContents" ) );
        type.defineAttribute( Mind.class.getMethod( "setContents", new Class[]{Map.class} ) );
        type.freeze();

        final Concept concept = new Concept();
        concept.setName( "ChocolateIsGood" );

        final Mind mind = new Mind();
        mind.getContents().put( "ChocolateIsGood", concept );
        mind.getControls().add( new ObjectName( "magic:foo=blah"));

        return new RMXBean( type, mind );
    }

    private static RMXBean createStomacheBean()
        throws OpenDataException, NoSuchMethodException
    {
        final RMXBeanType type = new RMXBeanType( Stomache.class );
        type.defineAttribute( Stomache.class.getMethod( "getContents" ) );
        type.defineAttribute( Stomache.class.getMethod( "setContents", new Class[]{Food.class} ) );
        type.freeze();

        final Stomache value = new Stomache();
        final Food food = new Food();
        food.setName( "Chocolate" );
        food.setNutritious( true );

        value.setContents( food );

        return new RMXBean( type, value );
    }

    private static RMXBean createFoodBean()
        throws OpenDataException, NoSuchMethodException
    {
        final RMXBeanType type = new RMXBeanType( Food.class );
        type.defineAttribute( Food.class.getMethod( "getName" ) );
        type.defineAttribute( Food.class.getMethod( "setName", new Class[]{String.class} ) );
        type.defineAttribute( Food.class.getMethod( "isNutritious" ) );
        type.defineAttribute( Food.class.getMethod( "setNutritious", new Class[]{Boolean.TYPE} ) );
        type.freeze();

        final Food value = new Food();
        value.setName( "Chocolate" );
        value.setNutritious( true );

        return new RMXBean( type, value );
    }
}

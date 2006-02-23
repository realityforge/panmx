package panmx.rmx;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import junit.framework.TestCase;

public class CompositeConverterTestCase
    extends TestCase
{
    static class MyComposite
    {
        private static final String NAME = MyComposite.class.getName();
        private static final String NAME_KEY = "name";
        private static final String SCORE_KEY = "score";
        private static final String TIME_KEY = "time";
        private static final String[] ITEMS = new String[]{NAME_KEY, SCORE_KEY, TIME_KEY};
        private static final OpenType[] TYPES = new OpenType[]
        {
            SimpleType.STRING,
            SimpleType.INTEGER,
            SimpleType.LONG
        };
        static final CompositeType TYPE;

        static
        {
            try
            {
                TYPE = new CompositeType( NAME, NAME, ITEMS, ITEMS, TYPES );
            }
            catch( final OpenDataException ode )
            {
                final LinkageError error = new LinkageError();
                error.initCause( ode );
                throw error;
            }
        }

        private final String m_name;
        private final int m_score;
        private final long m_time;

        MyComposite( String name, int score, long time )
        {
            m_name = name;
            m_score = score;
            m_time = time;
        }

        public String getName()
        {
            return m_name;
        }

        public int getScore()
        {
            return m_score;
        }

        public long getTime()
        {
            return m_time;
        }

        public static CompositeType getCompositeType()
        {
            return TYPE;
        }

        public static MyComposite fromCompositeData( final CompositeData compositeData )
        {
            if( null == compositeData )
            {
                return null;
            }
            else
            {
                final String name = (String)compositeData.get( NAME_KEY );
                final Integer score = (Integer)compositeData.get( SCORE_KEY );
                final Long time = (Long)compositeData.get( TIME_KEY );
                return new MyComposite( name, score, time );
            }
        }

        public static CompositeData toCompositeData( final MyComposite myComposite )
            throws OpenDataException
        {
            if( null == myComposite )
            {
                return null;
            }
            else
            {
                final Object[] values =
                    new Object[]{myComposite.getName(), myComposite.getScore(), myComposite.getTime()};
                return new CompositeDataSupport( TYPE, ITEMS, values );
            }
        }
    }

    public void testBasicOperation()
        throws Exception
    {
        final CompositeConverter converter =
            new CompositeConverter( MyComposite.class );
        assertEquals( "getJavaType()", MyComposite.class, converter.getJavaType() );

        assertEquals( "getOpenType()", MyComposite.TYPE, converter.getOpenType() );

        final Object[] values = new Object[]{"Nair", 16, 1L};
        final CompositeDataSupport openValue =
            new CompositeDataSupport( MyComposite.TYPE, MyComposite.ITEMS, values );

        final MyComposite convertedOpenValue = (MyComposite)converter.toJavaType( openValue );
        assertEquals( "toJavaType().name", "Nair", convertedOpenValue.getName() );
        assertEquals( "toJavaType().score", 16, convertedOpenValue.getScore() );
        assertEquals( "toJavaType().time", 1L, convertedOpenValue.getTime() );

        final MyComposite javaValue = new MyComposite( "Pete", 100, 42L );
        final CompositeData convertedJavaValue = (CompositeData)converter.toOpenType( javaValue );
        assertEquals( "toOpenType().name", "Pete", convertedJavaValue.get( MyComposite.NAME_KEY ) );
        assertEquals( "toOpenType().score", 100, convertedJavaValue.get( MyComposite.SCORE_KEY ) );
        assertEquals( "toOpenType().time", 42L, convertedJavaValue.get( MyComposite.TIME_KEY ) );

        assertEquals( "toJavaType(null)", null, converter.toJavaType( null ) );
        assertEquals( "toOpenType(null)", null, converter.toOpenType( null ) );
    }
}

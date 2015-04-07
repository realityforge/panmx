package panmx.rmx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import junit.framework.TestCase;

public class RMXCompositeTypeTestCase
    extends TestCase
{
    static class PlayerData
    {
        private String m_name;

        public String getName()
        {
            return m_name;
        }

        public void setName( final String name )
        {
            m_name = name;
        }
    }

    public void testDataWithSimpleTypeField()
        throws Exception
    {
        final RMXCompositeType adapter = new RMXCompositeType( PlayerData.class );
        adapter.defineField( "name" );
        adapter.freeze();
        final PlayerData playerData = new PlayerData();
        playerData.setName( "Chitaka" );

        final CompositeType type = adapter.getCompositeType();
        assertNotNull( "getCompositeType()", type );
        assertEquals( "getCompositeType().getClassName()",
                      CompositeData.class.getName(),
                      type.getClassName() );
        assertEquals( "getCompositeType().getTypeName()",
                      PlayerData.class.getName(),
                      type.getTypeName() );
        assertEquals( "getCompositeType().getDescription()",
                      PlayerData.class.getName(),
                      type.getDescription() );
        assertEquals( "getCompositeType().getType(name)",
                      SimpleType.STRING,
                      type.getType( "name" ) );
        assertEquals( "getCompositeType().size()",
                      1,
                      type.keySet().size() );

        final CompositeData cd1 = adapter.toCompositeData( playerData );
        assertNotNull( "toCompositeData()", cd1 );
        assertEquals( "toCompositeData().size()", 1, cd1.values().size() );
        assertEquals( "toCompositeData().get(name)", "Chitaka", cd1.get( "name" ) );

        final HashMap<String, Object> items = new HashMap<String, Object>();
        items.put( "name", "Stan" );
        final CompositeData cd2 = new CompositeDataSupport( type, items );
        adapter.fromCompositeData( playerData, cd2 );
        assertEquals( "fromCompositeData() ==> getName()", "Stan", playerData.getName() );
    }

    static class ChapterData
    {
        private String m_name;
        private int m_totalScore;
        private long m_totalTime;

        public String getName()
        {
            return m_name;
        }

        void setName( String name )
        {
            m_name = name;
        }

        public int getTotalScore()
        {
            return m_totalScore;
        }

        void setTotalScore( int totalScore )
        {
            m_totalScore = totalScore;
        }

        public long getTotalTime()
        {
            return m_totalTime;
        }

        void setTotalTime( long totalTime )
        {
            m_totalTime = totalTime;
        }
    }

    public void testDataWithNonPublicSetters()
        throws Exception
    {
        final RMXCompositeType adapter = new RMXCompositeType( ChapterData.class );
        adapter.defineField( "name" );
        adapter.defineField( "totalScore" );
        adapter.defineField( "totalTime" );
        adapter.freeze();

        final ChapterData chapterData = new ChapterData();
        chapterData.setName( "Begining" );
        chapterData.setTotalScore( 42 );
        chapterData.setTotalTime( 16 );

        final CompositeType type = adapter.getCompositeType();
        assertNotNull( "getCompositeType()", type );
        assertEquals( "getCompositeType().getClassName()",
                      CompositeData.class.getName(),
                      type.getClassName() );
        assertEquals( "getCompositeType().getTypeName()",
                      ChapterData.class.getName(),
                      type.getTypeName() );
        assertEquals( "getCompositeType().getDescription()",
                      ChapterData.class.getName(),
                      type.getDescription() );
        assertEquals( "getCompositeType().size()",
                      3,
                      type.keySet().size() );
        assertEquals( "getCompositeType().getType(name)",
                      SimpleType.STRING,
                      type.getType( "name" ) );
        assertEquals( "getCompositeType().getType(totalScore)",
                      SimpleType.INTEGER,
                      type.getType( "totalScore" ) );
        assertEquals( "getCompositeType().getType(totalTime)",
                      SimpleType.LONG,
                      type.getType( "totalTime" ) );

        final CompositeData cd1 = adapter.toCompositeData( chapterData );
        assertNotNull( "toCompositeData()", cd1 );
        assertEquals( "toCompositeData().size()", 3, cd1.values().size() );
        assertEquals( "toCompositeData().get(name)", "Begining", cd1.get( "name" ) );
        assertEquals( "toCompositeData().get(totalScore)", 42, cd1.get( "totalScore" ) );
        assertEquals( "toCompositeData().get(totalTime)", 16L, cd1.get( "totalTime" ) );

        final HashMap<String, Object> items = new HashMap<String, Object>();
        items.put( "name", "Stan" );
        items.put( "totalScore", 52 );
        items.put( "totalTime", 7L );
        final CompositeData cd2 = new CompositeDataSupport( type, items );
        adapter.fromCompositeData( chapterData, cd2 );
        assertEquals( "fromCompositeData() ==> getName()", "Stan", chapterData.getName() );
        assertEquals( "fromCompositeData() ==> getTotalScore()", 52, chapterData.getTotalScore() );
        assertEquals( "fromCompositeData() ==> getTotalTime()", 7L, chapterData.getTotalTime() );
    }

    static class ScoreData
    {
        private List<Integer> m_scores = new ArrayList<Integer>();

        public List<Integer> getScores()
        {
            return m_scores;
        }

        public void setScores( List<Integer> scores )
        {
            m_scores = scores;
        }
    }

    public void testDataWithListFields()
        throws Exception
    {
        final RMXCompositeType adapter = new RMXCompositeType( ScoreData.class );
        adapter.defineField( "scores" );
        adapter.freeze();

        final ScoreData scoreData = new ScoreData();
        final ArrayList<Integer> scores = new ArrayList<Integer>();
        scores.add( 3 );
        scoreData.setScores( scores );

        final CompositeType type = adapter.getCompositeType();
        assertNotNull( "getCompositeType()", type );
        assertEquals( "getCompositeType().getClassName()",
                      CompositeData.class.getName(),
                      type.getClassName() );
        assertEquals( "getCompositeType().getTypeName()",
                      ScoreData.class.getName(),
                      type.getTypeName() );
        assertEquals( "getCompositeType().getDescription()",
                      ScoreData.class.getName(),
                      type.getDescription() );
        assertEquals( "getCompositeType().size()",
                      1,
                      type.keySet().size() );
        assertEquals( "getCompositeType().getType(chapters)",
                      new ArrayType( 1, SimpleType.INTEGER ),
                      type.getType( "scores" ) );

        final CompositeData cd1 = adapter.toCompositeData( scoreData );
        assertNotNull( "toCompositeData()", cd1 );
        assertEquals( "toCompositeData().size()", 1, cd1.values().size() );
        final Integer[] scoreSet = (Integer[])cd1.get( "scores" );
        assertEquals( "toCompositeData().get(scores).length", 1, scoreSet.length );
        assertEquals( "toCompositeData().get(scores)[0]", 3, (int)scoreSet[0] );

        final HashMap<String, Object> items = new HashMap<String, Object>();
        items.put( "scores", new Integer[0] );
        final CompositeData cd2 = new CompositeDataSupport( type, items );
        adapter.fromCompositeData( scoreData, cd2 );
        final List<Integer> newScores = scoreData.getScores();
        assertEquals( "fromCompositeData() ==> getScores().length", 0, newScores.size() );
    }

    static class ChapterSetData
    {
        private Map<String, ChapterData> m_chapters = new HashMap<String, ChapterData>();

        public Map<String, ChapterData> getChapters()
        {
            return m_chapters;
        }

        public void setChapters( Map<String, ChapterData> chapters )
        {
            m_chapters = chapters;
        }
    }

    public void testDataWithMapFielContainingNonSimpleTypeElement()
        throws Exception
    {
        final RMXCompositeType adapter = new RMXCompositeType( ChapterSetData.class );
        adapter.defineField( "chapters" );
        adapter.freeze();

        final ChapterData chapterData = new ChapterData();
        chapterData.setName( "StanDriver" );
        chapterData.setTotalScore( 0 );
        chapterData.setTotalTime( 0 );

        final ChapterSetData chapterSetData = new ChapterSetData();
        final Map<String, ChapterData> chapters = new HashMap<String, ChapterData>();
        chapters.put( "StanDriver", chapterData );
        chapterSetData.setChapters( chapters );

        final CompositeType type = adapter.getCompositeType();
        assertNotNull( "getCompositeType()", type );
        assertEquals( "getCompositeType().getClassName()",
                      CompositeData.class.getName(),
                      type.getClassName() );
        assertEquals( "getCompositeType().getTypeName()",
                      ChapterSetData.class.getName(),
                      type.getTypeName() );
        assertEquals( "getCompositeType().getDescription()",
                      ChapterSetData.class.getName(),
                      type.getDescription() );
        assertEquals( "getCompositeType().size()",
                      1,
                      type.keySet().size() );

        final String name = "Map<" + String.class.getName() + "," + ChapterData.class.getName() + ">";

        final String[] chapterItems = new String[]{"name", "totalScore", "totalTime"};
        final CompositeType chapterType =
            new CompositeType( ChapterData.class.getName(),
                               ChapterData.class.getName(),
                               chapterItems,
                               chapterItems,
                               new OpenType[]{SimpleType.STRING, SimpleType.INTEGER, SimpleType.LONG} );

        final String[] setItems = new String[]{"key", "value"};
        final OpenType[] setTypes =
            new OpenType[]{SimpleType.STRING, chapterType};
        final CompositeType rowType =
            new CompositeType( name, name, setItems, setItems, setTypes );

        final TabularType tabularType =
            new TabularType( name, name, rowType, new String[]{"key"} );
        assertEquals( "getCompositeType().getType(chapters)",
                      tabularType,
                      type.getType( "chapters" ) );

        final HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put( "name", "StanDriver" );
        hm.put( "totalScore", 0 );
        hm.put( "totalTime", 0L );
        final CompositeDataSupport chapterCD =
            new CompositeDataSupport( chapterType, hm );

        final CompositeData cd1 = adapter.toCompositeData( chapterSetData );
        assertNotNull( "toCompositeData()", cd1 );
        assertEquals( "toCompositeData().size()", 1, cd1.values().size() );
        final HashMap<String, Object> hm2 = new HashMap<String, Object>();
        hm2.put( "key", "StanDriver" );
        hm2.put( "value", chapterCD );
        final CompositeDataSupport cd3 = new CompositeDataSupport( rowType, hm2 );
        final TabularDataSupport expected = new TabularDataSupport( tabularType );
        expected.put( cd3 );
        assertEquals( "toCompositeData().get(chapters)", expected, cd1.get( "chapters" ) );

        final TabularDataSupport tds = new TabularDataSupport( tabularType );
        final HashMap<String, Object> items = new HashMap<String, Object>();
        items.put( "chapters", tds );
        final CompositeData cd2 = new CompositeDataSupport( type, items );
        adapter.fromCompositeData( chapterSetData, cd2 );
        assertEquals( "fromCompositeData() ==> getChapters().length", 0, chapterSetData.getChapters().size() );
    }
}

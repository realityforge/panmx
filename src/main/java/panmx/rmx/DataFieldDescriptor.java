package panmx.rmx;

import java.lang.reflect.Method;

class DataFieldDescriptor
{
    private final String m_name;
    private final Converter m_converter;
    private Method m_accessor;
    private Method m_mutator;

    DataFieldDescriptor( final String name,
                         final Converter converter, Method accessor, Method mutator )
    {
        m_name = name;
        m_converter = converter;
        m_accessor = accessor;
        m_mutator = mutator;
    }

    String getName()
    {
        return m_name;
    }

    Converter getConverter()
    {
        return m_converter;
    }

    Method getAccessor()
    {
        return m_accessor;
    }

    Method getMutator()
    {
        return m_mutator;
    }
}

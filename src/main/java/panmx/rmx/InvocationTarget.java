package panmx.rmx;

import java.lang.reflect.Method;

class InvocationTarget
{
    private final Method m_method;
    private final Converter[] m_parameterConverters;
    private final Converter m_returnValueConverter;

    InvocationTarget( final Method method,
                      final Converter[] parameterConverters,
                      final Converter returnValueConverter )
    {
        m_method = method;
        m_parameterConverters = parameterConverters;
        m_returnValueConverter = returnValueConverter;
    }

    Method getMethod()
    {
        return m_method;
    }

    Converter[] getParameterConverters()
    {
        return m_parameterConverters;
    }

    Converter getReturnValueConverter()
    {
        return m_returnValueConverter;
    }
}

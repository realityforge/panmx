package panmx.rmx;

import java.lang.reflect.Method;
import javax.management.NotCompliantMBeanException;
import javax.management.openmbean.OpenDataException;
import panmx.util.BeanUtil;

class StandardRMXBeanType
    extends RMXBeanType
{
    StandardRMXBeanType( final Class type,
                         final Class[] mxTypes )
        throws NotCompliantMBeanException
    {
        super( type );
        for( final Class mxType : mxTypes )
        {
            defineManagementElements( mxType );
        }
        try
        {
            freeze();
        }
        catch( final OpenDataException ode )
        {
            final NotCompliantMBeanException e =
                new NotCompliantMBeanException( ode.getMessage() );
            e.initCause( ode );
            throw e;
        }
    }

    /**
     * Define management elements from Management interface.
     *
     * @param mxType the Management interface.
     * @throws NotCompliantMBeanException if type is not an interface or
     *                                    it has non-compliant methods.
     */
    private void defineManagementElements( final Class mxType )
        throws NotCompliantMBeanException
    {
        if( !mxType.isInterface() )
        {
            final String message =
                "RMX Type " + mxType.getName() + " must be an interface.";
            throw new NotCompliantMBeanException( message );
        }
        final Method[] methods = mxType.getMethods();
        for( final Method method : methods )
        {
            if( Class.class != method.getDeclaringClass() )
            {
                try
                {
                    if( BeanUtil.isAttributeMethod( method ) )
                    {
                        defineAttribute( method );
                    }
                    else
                    {
                        defineOperation( method );
                    }
                }
                catch( final OpenDataException ode )
                {
                    final NotCompliantMBeanException e =
                        new NotCompliantMBeanException( ode.getMessage() );
                    e.initCause( ode );
                    throw e;
                }
            }
        }
    }
}

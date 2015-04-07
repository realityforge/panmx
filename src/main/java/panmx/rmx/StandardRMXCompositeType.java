package panmx.rmx;

import java.lang.reflect.Method;
import javax.management.openmbean.OpenDataException;
import panmx.util.BeanUtil;

/**
 * The StandardRMXCompositeType is a composite type that includes all
 * potential fields of type.
 */
class StandardRMXCompositeType
    extends RMXCompositeType
{
    /**
     * Create StandardRMXCompositeType for specified java type.
     *
     * @param type the java type.
     * @throws OpenDataException if type does not conform to specifications.
     */
    StandardRMXCompositeType( final Class type )
        throws OpenDataException
    {
        super( type );
        final Method[] methods = type.getMethods();
        for( final Method method : methods )
        {
            if( BeanUtil.isAccessor( method ) )
            {
                final String field = BeanUtil.getAttributeName( method );
                try
                {
                    defineField( field );
                }
                catch( final OpenDataException ode )
                {
                    //Ignore
                }
            }
        }
        freeze();
    }
}

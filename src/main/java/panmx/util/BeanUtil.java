package panmx.util;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import javax.management.openmbean.OpenDataException;

/**
 * Class with utility functions for working with beans.
 * This class SHOULD NOT be used outside the PanMX package as it
 * is likely to change without notice.
 */
public final class BeanUtil
{
    /** getClass() method name. */
    private static final String GET_CLASS_METHOD_NAME = "getClass";
    /** Prefix for all mutator methods. */
    private static final String MUTATOR_PREFIX = "set";
    /** Prefix for all isser accessor methods. */
    private static final String IS_ACCESSOR_PREFIX = "is";
    /** Prefix for all getter accessor methods. */
    private static final String GET_ACCESSOR_PREFIX = "get";

    /**
     * Return true if method is either an accessor or a mutator.
     *
     * @param method the method.
     * @return true if method is either an accessor or a mutator.
     */
    public static boolean isAttributeMethod( final Method method )
    {
        return isAccessor( method ) || isMutator( method );
    }

    /**
     * Return true if method matches naming convention for accessor.
     *
     * @param method the method.
     * @return true if method matches naming convention for accessor.
     */
    public static boolean isAccessor( final Method method )
    {
        final Type type = method.getGenericReturnType();
        if( Void.TYPE == type ||
            method.getParameterTypes().length != 0 )
        {
            return false;
        }
        final String name = method.getName();
        if( name.startsWith( GET_ACCESSOR_PREFIX ) &&
            name.length() > 3 &&
            !GET_CLASS_METHOD_NAME.equals( name ) )
        {
            return true;
        }
        else
        {
            return name.startsWith( IS_ACCESSOR_PREFIX ) &&
                   name.length() > 2 &&
                   Boolean.TYPE == type;
        }
    }

    /**
     * Return true if method matches naming convention for mutator.
     *
     * @param method the method.
     * @return true if method matches naming convention for mutator.
     */
    public static boolean isMutator( final Method method )
    {
        final String name = method.getName();
        return name.startsWith( MUTATOR_PREFIX ) &&
               name.length() > 3 &&
               Void.TYPE == method.getGenericReturnType() &&
               method.getParameterTypes().length == 1;
    }

    /**
     * Return name of attribute assuming method is a mutator or accesor.
     *
     * @param method the method.
     * @return the attribute name.
     */
    public static String getAttributeName( final Method method )
    {
        final String methodName = method.getName();
        final boolean isserAccessor = methodName.startsWith( IS_ACCESSOR_PREFIX );
        final String name;
        if( isserAccessor )
        {
            name = Character.toLowerCase( methodName.charAt( 2 ) ) +
                   methodName.substring( 3 );
        }
        else
        {
            name = Character.toLowerCase( methodName.charAt( 3 ) ) +
                   methodName.substring( 4 );
        }
        return name;
    }

    /**
     * Return the accessor method for attribute with specified name.
     *
     * @param type the type to retrieve method from.
     * @param name the name of the attribute.
     * @return the accessor method if any.
     * @throws OpenDataException if unable to find accessor method.
     */
    public static Method getAccessor( final Class type, final String name )
        throws OpenDataException
    {
        final String baseName =
            Character.toUpperCase( name.charAt( 0 ) ) + name.substring( 1 );
        try
        {
            final String accessorName = GET_ACCESSOR_PREFIX + baseName;
            return type.getMethod( accessorName, new Class[0] );
        }
        catch( final NoSuchMethodException nsme )
        {
            //try for "isX()" style getter
            try
            {
                final String accessorName = IS_ACCESSOR_PREFIX + name;
                final Method accessor = type.getMethod( accessorName, new Class[0] );
                final Class returnType = accessor.getReturnType();
                if( Boolean.TYPE != returnType )
                {
                    final String message =
                        "Accessor named " + accessorName + " should return a boolean value.";
                    throw new OpenDataException( message );
                }
                return accessor;
            }
            catch( final NoSuchMethodException nsme2 )
            {
                final String message = "Missing accessor for field " + name;
                throw new OpenDataException( message );
            }
        }
    }

    /**
     * Return the mutator method for attribute with specified name and type.
     *
     * @param type the type to retrieve method from.
     * @param name the name of the attribute.
     * @param attributeType the attribute attributeType.
     * @return the mutator method if any.
     * @throws OpenDataException if unable to find mutator method.
     */
    public static Method getMutator( final Class type,
                                     final String name,
                                     final Class attributeType,
                                     final boolean mustBePublic )
        throws OpenDataException
    {
        try
        {
            final String accessorName =
               MUTATOR_PREFIX + Character.toUpperCase( name.charAt( 0 ) ) + name.substring( 1 );
            final Class[] params = new Class[]{attributeType};
            final Method mutator;
            if( mustBePublic )
            {
                mutator = type.getMethod( accessorName, params );
            }
            else
            {
                mutator = type.getDeclaredMethod( accessorName, params );
            }
            if( Void.TYPE != mutator.getGenericReturnType() )
            {
                final String message =
                    "Mutator for " + name + " should not return any value.";
                throw new OpenDataException( message );
            }
            return mutator;
        }
        catch( final NoSuchMethodException nsme )
        {
            final String message = "Missing mutator for field " + name;
            throw new OpenDataException( message );
        }
    }

    /**
     * Return the signature of method as an array of strings.
     *
     * @param method the method.
     * @return the signature.
     */
    public static String[] getSignature( final Method method )
    {
        final Class[] types = method.getParameterTypes();
        final String[] signature = new String[types.length];
        for( int i = 0; i < types.length; i++ )
        {
            signature[i] = types[i].getName();
        }
        return signature;
    }

    /**
     * Make a fully qualified method name for specified method.
     *
     * @param method the method.
     * @return the fully qualified method name.
     */
    public static String makeFullyQualifiedName( final Method method )
    {
        final String[] signature = BeanUtil.getSignature( method );
        return makeFullyQualifiedName( method.getName(), signature );
    }

    /**
     * Make a fully qualified method name from specified parameters.
     *
     * @param actionName the name of method.
     * @param signature the types of each parameter.
     * @return the fully qualified method name.
     */
    public static String makeFullyQualifiedName( final String actionName,
                                                 final String[] signature )
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( actionName );
        sb.append( '(' );
        if( null != signature )
        {
            for( int i = 0; i < signature.length; i++ )
            {
                if( i != 0 )
                {
                    sb.append( ',' );
                }
                sb.append( signature[i] );
            }
        }
        sb.append( ')' );
        return sb.toString();
    }

}

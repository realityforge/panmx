package panmx.annotations;

/**
 * Enum indicating the management impact of operation.
 */
public enum Impact
{
    /** {@link javax.management.MBeanOperationInfo#INFO}. */
    INFO( 0 ),
    /** {@link javax.management.MBeanOperationInfo#ACTION}. */
    ACTION( 1 ),
    /** {@link javax.management.MBeanOperationInfo#ACTION_INFO}. */
    ACTION_INFO( 2 ),
    /** {@link javax.management.MBeanOperationInfo#UNKNOWN}. */
    UNKNOWN( 3 );
    /**
     * The impact level. Equivelent to constants defined by
     * {@link javax.management.MBeanOperationInfo}.
     */
    private final int m_impact;

    /**
     * Create instance of enum.
     *
     * @param impact the operation impact value.
     */
    private Impact( final int impact )
    {
        m_impact = impact;
    }

    /**
     * Return the impact value.
     *
     * @return the impact value.
     */
    public int getImpact()
    {
        return m_impact;
    }
}

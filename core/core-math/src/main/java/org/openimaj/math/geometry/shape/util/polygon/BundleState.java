package org.openimaj.math.geometry.shape.util.polygon;

/**
 * Edge bundle state
 */
public class BundleState
{
	private String m_State;

	private BundleState( String state )
	{
		m_State = state;
	}

	public final static BundleState UNBUNDLED = new BundleState( "UNBUNDLED" );
	public final static BundleState BUNDLE_HEAD = new BundleState( "BUNDLE_HEAD" ); 
	public final static BundleState BUNDLE_TAIL = new BundleState( "BUNDLE_TAIL" );

	public String toString()
	{
		return m_State;
	}
}
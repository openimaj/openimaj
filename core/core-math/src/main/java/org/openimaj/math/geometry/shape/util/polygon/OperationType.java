package org.openimaj.math.geometry.shape.util.polygon;

public class OperationType
{
	private String m_Type;

	private OperationType( String type )
	{
		m_Type = type;
	}

	public static final OperationType GPC_DIFF = new OperationType( "Difference" );

	public static final OperationType GPC_INT = new OperationType( "Intersection" );

	public static final OperationType GPC_XOR = new OperationType( "Exclusive or" );

	public static final OperationType GPC_UNION = new OperationType( "Union" );

	public String toString()
	{
		return m_Type;
	}
}

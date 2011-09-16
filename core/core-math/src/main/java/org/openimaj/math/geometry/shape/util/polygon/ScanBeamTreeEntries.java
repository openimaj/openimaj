package org.openimaj.math.geometry.shape.util.polygon;

/**
    *
    */
public class ScanBeamTreeEntries
{
	public int sbt_entries;

	public ScanBeamTree sb_tree;

	public double[] build_sbt()
	{
		double[] sbt = new double[sbt_entries];

		int entries = 0;
		entries = inner_build_sbt( entries, sbt, sb_tree );
		if( entries != sbt_entries )
		{
			throw new IllegalStateException( "Something went wrong buildign sbt from tree." );
		}
		return sbt;
	}

	private int inner_build_sbt( int entries, double[] sbt, ScanBeamTree sbt_node )
	{
		if( sbt_node.less != null )
		{
			entries = inner_build_sbt( entries, sbt, sbt_node.less );
		}
		sbt[entries] = sbt_node.y;
		entries++;
		if( sbt_node.more != null )
		{
			entries = inner_build_sbt( entries, sbt, sbt_node.more );
		}
		return entries;
	}
}

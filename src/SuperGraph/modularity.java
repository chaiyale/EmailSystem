package SuperGraph;

import graph.Edge;
import graph.Vertex;

import java.util.ArrayList;
import java.util.HashSet;

public class modularity 
{//社团的性能
	public static double modularityQ(ArrayList<Vertex> vertexList)   //社团模块度计算
	{
		double Q = 0.0;
		double m = 0.0;
		for(Vertex v: vertexList)
		{
			for(Edge e: v.edge)
			{
				m += e.weight;
			}
		}
		//m /= 2;
		
		for(int vid=0; vid<vertexList.size(); vid++)
		{
			Vertex v = vertexList.get(vid);
			double k1 = 0;
			for(Edge e: v.edge)
			{
				k1 += e.weight;
			}
			for(Edge e: v.edge)
			{
				int vid2 = e.nid1==vid? e.nid2: e.nid1;
				if(vid2 > vid)
				{
					Vertex v2 = vertexList.get(vid2);
					for(int i: v.communityId)
					{
						for(int j: v2.communityId)
						{
							if(i==j)
							{
								double k2 = 0;
								for(Edge e2: v2.edge)
								{
									k2 += e2.weight;
								}
								Q = Q + e.weight - (k1*k2)/m ;
								break;
							}
						}
					}
				}
			}
		}
		Q = Q/m;
		System.out.println("模块度Q: "+Q);
		return Q;
	}
	
	public static double modularityQ(SuperVertex SV, ArrayList<Vertex> vertexListAll)   //社团模块度计算
	{
		double Q = 0.0;
		double m = 0.0;
		for(int i: SV.containVertex)
		{
			Vertex v= vertexListAll.get(i);
			for(Edge e: v.edge)
			{
				m += e.weight;
			}
		}
		//m /= 2;
		
		for(int vid: SV.containVertex)
		{
			Vertex v = vertexListAll.get(vid);
			double k1 = 0;
			for(Edge e: v.edge)
			{
				k1 += e.weight;
			}
			for(Edge e: v.edge)
			{
				int vid2 = e.nid1==vid? e.nid2: e.nid1;
				if(vid2 > vid)
				{
					Vertex v2 = vertexListAll.get(vid2);
					for(int i: v.communityId)
					{
						for(int j: v2.communityId)
						{
							if(i==j)
							{
								double k2 = 0;
								for(Edge e2: v2.edge)
								{
									k2 += e2.weight;
								}
								Q = Q + e.weight - (k1*k2)/m ;
								break;
							}
						}
					}
				}
			}
		}
		Q = Q/m;
		System.out.println("模块度Q: "+Q);
		return Q;
	}
	
	public static double CompelxWithIn(ArrayList<SuperVertex> superVertexList)
	{
		double sum = 0;
		int size = superVertexList.size();
		for(SuperVertex sv: superVertexList)
		{
			sum += (sv.containVertex.size())*(sv.containVertex.size());
		}
		sum /= size;
		sum /= 8;
		
		return sum;
	}
}

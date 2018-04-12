package shortPathQuery;

import graph.Edge;
import graph.MakeGraph;
import graph.Vertex;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class reachRE2006 
{
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //原顶点集合
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //边集合
	
	public static ArrayList<Vertex> vertexListNew = new ArrayList<Vertex>();  //G'
	public static double[] reach = null;  //reach
	public static double[] bounds = null; 
	
	public static void ReachBoundComputation(ArrayList<Vertex> vertexList, int[] B)
	{	
		int n = vertexList.size();
		bounds = new double[n];
		reach = new double[n];
		
		for(int i=0; i<n; i++)
		{
			bounds[i] = Integer.MAX_VALUE;
			vertexListNew.add(vertexList.get(i));
		}
		
		for(int i=0; i<B.length; i++)
		{
			ArrayList<Vertex> removeList = new ArrayList<Vertex>();
			for(Vertex v: vertexListNew)
			{
				int id = v.getId();
				if(bounds[id] != Integer.MAX_VALUE)
				{
					removeList.add(v);  //不在这一轮的
				}
			}
			for(Vertex v: removeList)
			{
				vertexListNew.remove(v);
			}
			Iterate(vertexList, B[i]);
		}
	}
	
	public static void Iterate(ArrayList<Vertex> vertexList, int b)
	{
		ArrayList<Integer> vertexDiff = new ArrayList<Integer>(); //差集合
		double c = 0;
		for(Vertex v: vertexList)
		{
			if(!vertexListNew.contains(v))  //V-V'
			{
				vertexDiff.add(v.getId());
				if(c < bounds[v.getId()])
				{
					c = bounds[v.getId()];   //c: max{bounds[x]| x属于V-V'}
				}
			}
		}
		
		for(Vertex v: vertexListNew)
		{
			int vid = v.getId();  //set bounds[v] and reach[v] = 0
			bounds[vid] = 0;
			reach[vid] = 0;
		}
		
		ArrayList<Edge> EH = new ArrayList<Edge>();  //{(x,y)| x属于V， y属于V'}
		HashSet<Integer> VH = new HashSet<Integer>(); //V' 和 边涉及到的点的集合
		for(Vertex v: vertexListNew)
		{
			VH.add(v.getId());
			for(int vid2: v.neighborIdTypeALL)
			{
				if(!VH.contains(vid2))
				{
					VH.add(vid2);
				}
			}
		}
		
		for(Vertex s: vertexListNew)  //每一个顶点作为起点，生成最短路径树
		{
			double g=0, d=0;
			for(Edge e: s.edge)    //(x, s) 属于 E-E', E'是x,s都属于V', 所以x一定属于V-V'
			{
				int x = e.nid1==s.getId()? e.nid2: e.nid1;
				if(vertexDiff.contains(x))
				{
					double m = e.weight;
					if(g < bounds[x]+ m)
					{
						g = bounds[x]+ m;  //g: max{bounds[x]+m(x,s)| (x,s)属于E-E'}
					}
					if(d < m)
					{
						d = m;    //d: max{m(x,s)| (x,s)属于E-E'}
					}
				}
			}
			
			TraverseT(VH, s);
		}
	}
	
	public static void TraverseT(HashSet<Integer> VH, Vertex s)
	{
		double matrix[][] = new double[VH.size()][VH.size()];		//邻接矩阵的初始化
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();   //vertexId和矩阵序号的对应关系
		Map<Integer, Integer> mapReverse = new HashMap<Integer, Integer>();   //矩阵序号和vertexId的对应关系
		int count = 0;
	    for(int id: VH)
	    {
	    	map.put(id, count);
	    	mapReverse.put(count++, id);
	    }
		for(Vertex v:vertexListNew)
	    {
	    	for(Edge e:v.edge)
	    	{
	    		int v1 = map.get(e.nid1);
	    		int v2 = map.get(e.nid2);
    			matrix[v1][v2] = e.cost;
	    		matrix[v2][v1] = e.cost;
	    	}
	    }
		ArrayList<Integer> open = new ArrayList<Integer> ();  //待检查的
		ArrayList<Integer> close = new ArrayList<Integer> ();  //已经检查的
		int n = matrix.length;
		int s_id = map.get(s.getId());
		double s_dist[] = new double[n];  //s跟所有点之间的最短距离
	    int s_path[] = new int[n];    //记录所有点距离s的上一个节点，没有的话记为-1
		
		int i, j, u, k;
		for(i = 0; i < n; i++)
		{
			if(matrix[s_id][i] > 0 && i != s_id)  //所有邻居，距离为边的权重
            {
                s_dist[i] = matrix[s_id][i];
                s_path[i] = s_id;
                open.add(i);
            } 
            else          //如果不直接相连，距离为最大MAX_VALUE
            {
                s_dist[i] = Double.MAX_VALUE;
                s_path[i] = -1;
            }
		}
		
		while(open.size()!=0)
		{
			double min = Double.MAX_VALUE;  //扩展正序树
			int index = 0;
			u = -1;
			for(j=0; j<open.size(); j++) //找到最小的
			{
				int tmp = open.get(j);
				if(s_dist[tmp] < min)
				{
					min = s_dist[tmp];
		        	u = tmp;
		        	index = j;
				}
			}
			if(u==-1)
		    {
		    	break;  //剩余的所有的节点都不可达了
		    }
			close.add(u);
			open.remove(index);
			for(int vid: vertexList.get(mapReverse.get(u)).neighborIdTypeALL)  //对于所有邻居
			{
				if(!map.containsKey(vid)) continue;
				k = map.get(vid);
				if(min + matrix[u][k] < s_dist[k])	//根据新节点更新其他节点到源点的距离
				{
					s_dist[k] = min + matrix[u][k];
			        s_path[k] = u;
				}
				if(!open.contains(k) && !close.contains(k))
				{
					open.add(k);
				}
			}
			int v = u;
            v = s_path[v];
            while(s_id != v) 
            {
                v = s_path[v];
                int v_id = mapReverse.get(v);
                
            }
            s.shortestPath.put(mapReverse.get(u), s_dist[u]+""); 
		}
	}
	
	public static void main(String[] args) throws SQLException, ParseException 
	{
		MakeGraph mg = new MakeGraph();   //邮件数据集
		Date start = new Date(); //计时
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		vertexList = mg.getVertex();
		edgeList = mg.getEdge();
		Date end = new Date();
		System.out.println("将邮件数据集转化为图："+ (end.getTime() - start.getTime()) + " total milliseconds");
		
		common cn = new common();
		cn.initialCost(vertexList);
		int[] B = {0, 2, 4, 8, 16};
		ReachBoundComputation(vertexList, B);
	}
}

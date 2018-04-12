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
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //ԭ���㼯��
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //�߼���
	
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
					removeList.add(v);  //������һ�ֵ�
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
		ArrayList<Integer> vertexDiff = new ArrayList<Integer>(); //���
		double c = 0;
		for(Vertex v: vertexList)
		{
			if(!vertexListNew.contains(v))  //V-V'
			{
				vertexDiff.add(v.getId());
				if(c < bounds[v.getId()])
				{
					c = bounds[v.getId()];   //c: max{bounds[x]| x����V-V'}
				}
			}
		}
		
		for(Vertex v: vertexListNew)
		{
			int vid = v.getId();  //set bounds[v] and reach[v] = 0
			bounds[vid] = 0;
			reach[vid] = 0;
		}
		
		ArrayList<Edge> EH = new ArrayList<Edge>();  //{(x,y)| x����V�� y����V'}
		HashSet<Integer> VH = new HashSet<Integer>(); //V' �� ���漰���ĵ�ļ���
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
		
		for(Vertex s: vertexListNew)  //ÿһ��������Ϊ��㣬�������·����
		{
			double g=0, d=0;
			for(Edge e: s.edge)    //(x, s) ���� E-E', E'��x,s������V', ����xһ������V-V'
			{
				int x = e.nid1==s.getId()? e.nid2: e.nid1;
				if(vertexDiff.contains(x))
				{
					double m = e.weight;
					if(g < bounds[x]+ m)
					{
						g = bounds[x]+ m;  //g: max{bounds[x]+m(x,s)| (x,s)����E-E'}
					}
					if(d < m)
					{
						d = m;    //d: max{m(x,s)| (x,s)����E-E'}
					}
				}
			}
			
			TraverseT(VH, s);
		}
	}
	
	public static void TraverseT(HashSet<Integer> VH, Vertex s)
	{
		double matrix[][] = new double[VH.size()][VH.size()];		//�ڽӾ���ĳ�ʼ��
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();   //vertexId�;�����ŵĶ�Ӧ��ϵ
		Map<Integer, Integer> mapReverse = new HashMap<Integer, Integer>();   //������ź�vertexId�Ķ�Ӧ��ϵ
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
		ArrayList<Integer> open = new ArrayList<Integer> ();  //������
		ArrayList<Integer> close = new ArrayList<Integer> ();  //�Ѿ�����
		int n = matrix.length;
		int s_id = map.get(s.getId());
		double s_dist[] = new double[n];  //s�����е�֮�����̾���
	    int s_path[] = new int[n];    //��¼���е����s����һ���ڵ㣬û�еĻ���Ϊ-1
		
		int i, j, u, k;
		for(i = 0; i < n; i++)
		{
			if(matrix[s_id][i] > 0 && i != s_id)  //�����ھӣ�����Ϊ�ߵ�Ȩ��
            {
                s_dist[i] = matrix[s_id][i];
                s_path[i] = s_id;
                open.add(i);
            } 
            else          //�����ֱ������������Ϊ���MAX_VALUE
            {
                s_dist[i] = Double.MAX_VALUE;
                s_path[i] = -1;
            }
		}
		
		while(open.size()!=0)
		{
			double min = Double.MAX_VALUE;  //��չ������
			int index = 0;
			u = -1;
			for(j=0; j<open.size(); j++) //�ҵ���С��
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
		    	break;  //ʣ������еĽڵ㶼���ɴ���
		    }
			close.add(u);
			open.remove(index);
			for(int vid: vertexList.get(mapReverse.get(u)).neighborIdTypeALL)  //���������ھ�
			{
				if(!map.containsKey(vid)) continue;
				k = map.get(vid);
				if(min + matrix[u][k] < s_dist[k])	//�����½ڵ���������ڵ㵽Դ��ľ���
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
		MakeGraph mg = new MakeGraph();   //�ʼ����ݼ�
		Date start = new Date(); //��ʱ
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		vertexList = mg.getVertex();
		edgeList = mg.getEdge();
		Date end = new Date();
		System.out.println("���ʼ����ݼ�ת��Ϊͼ��"+ (end.getTime() - start.getTime()) + " total milliseconds");
		
		common cn = new common();
		cn.initialCost(vertexList);
		int[] B = {0, 2, 4, 8, 16};
		ReachBoundComputation(vertexList, B);
	}
}

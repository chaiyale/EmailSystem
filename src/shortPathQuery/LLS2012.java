package shortPathQuery;

import graph.Edge;
import graph.Vertex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import database.dataAccess;

public class LLS2012 
{
	public static ArrayList<ArrayList<Integer>> traceAll = new ArrayList<ArrayList<Integer>>();
	public static ArrayList<ArrayList<Integer>> LAll = new ArrayList<ArrayList<Integer>>();
	public static ArrayList<HashMap<Integer,Integer>> stampAll = new ArrayList<HashMap<Integer,Integer>>();
	public static HashSet<Vertex> GL = new HashSet<Vertex>();   //全局地标
	
	public static HashSet<Vertex> LLSPre(ArrayList<Vertex> vertexList)   //预处理
	{
		GL = chooseGlobalLandmark(vertexList);  //Step1: 获取全局地标
		for(Vertex s: GL)
		{
			SPT(vertexList, s); //以每个全局地标作为起始点构建最短路径树
		}
		return GL;
	}
	
	public static HashSet<Vertex> chooseGlobalLandmark(ArrayList<Vertex> vertexList)  //随机选取k=50个全局地标
	{
		int k=50;
		HashSet<Vertex> randomVertexList = new HashSet<Vertex>();
		int vertexSize = vertexList.size()-1;
		while(randomVertexList.size() != k)  //随机选取n对节点
		{
			int ra = (int)(Math.random()*(vertexSize-0+1)); 
			randomVertexList.add(vertexList.get(ra));
		}
		return randomVertexList;
	}
	
	public static void SPT(ArrayList<Vertex> vertexList, Vertex s)   
	{
		//*******************Step2: 以全局地标作s，作为起始点的最短路径树***********************
		HashMap<Integer, HashMap<Integer, Float>> matrixA = new HashMap<Integer, HashMap<Integer, Float>>();
		//double matrix[][] = new double[vertexList.size()][vertexList.size()];		//邻接矩阵的初始化
		for(Vertex v:vertexList)
	    {
			int vid = v.getId();
			HashMap<Integer, Float> line = new HashMap<Integer, Float>();
			for(Edge e:v.edge)
	    	{
				//matrix[e.nid1][e.nid2] = e.cost;
	    		//matrix[e.nid2][e.nid1] = e.cost;
				int an = (e.nid1==vid)?e.nid2:e.nid1;
				line.put(an, e.cost);
	    	}
			matrixA.put(vid, line);
	    	v.neighborIdSPT = new HashSet<Integer>(); 
	    }
		
		int SPTnode = 1;
		ArrayList<Integer> open = new ArrayList<Integer> ();  //待检查的
		ArrayList<Integer> close = new ArrayList<Integer> ();  //已经检查的
		int n = matrixA.size();
		int s_id = s.getId();
		List<Float> s_dist = new ArrayList<Float>();
		List<Integer> s_path = new ArrayList<Integer>();
		List<Integer> level = new ArrayList<Integer>();
		//double s_dist[] = new double[n];  //s跟所有点之间的最短距离
	    //int s_path[] = new int[n];    //记录所有点距离s的上一个节点，没有的话记为-1
	    //int level[] = new int[n];     //记录所有节点到s的跳步，没有的话记为-1
		
		int i, j, u;
		for(i = 0; i < n; i++)
		{
			if(matrixA.get(s_id).containsKey(i) && i != s_id)  //所有邻居，距离为边的权重
            {
                s_dist.add(matrixA.get(s_id).get(i));
                s_path.add(s_id);
                open.add(i);
            } 
            else          //如果不直接相连，距离为最大MAX_VALUE
            {
            	s_dist.add(Float.MAX_VALUE);
                s_path.add(-1);
            }
			level.add(0);
		}
		s_dist.set(s_id, (float) 0);
		level.set(s_id, 0);
		close.add(s_id);
		s.shortestPath.put(s.getId(), ":"+0); 
		
		while(open.size()!=0)
		{
			float min = Float.MAX_VALUE;  //扩展正序树
			int index = 0;
			u = -1;
			for(j=0; j<open.size(); j++) //找到最小的
			{
				int tmp = open.get(j);
				if(s_dist.get(tmp) < min)
				{
					min = s_dist.get(tmp);
		        	u = tmp;
		        	index = j;
				}
			}
			if(u==-1)
		    {
		    	break;  //剩余的所有的节点都不可达了
		    }
			SPTnode ++;
			close.add(u);
			open.remove(index);
			for(int k: vertexList.get(u).neighborIdTypeALL)  //对于所有邻居
			{
				if(min + matrixA.get(u).get(k) < s_dist.get(k))	//根据新节点更新其他节点到源点的距离
				{
					s_dist.set(k, min + matrixA.get(u).get(k));
					s_path.set(k, u);
				}
				if(!open.contains(k) && !close.contains(k))
				{
					open.add(k);
				}
			}
			int v = u;
			level.set(u, level.get(s_path.get(u))+1);
			Vertex Vv = vertexList.get(s_path.get(v));
			Vv.neighborIdSPT.add(v);
            v = s_path.get(v);
            while(s_id != v) 
            {
            	Vv = vertexList.get(s_path.get(v));
            	Vv.neighborIdSPT.add(v);
                v = s_path.get(v);            
            }
            
			//Add by 3/6/2018
            v=u;
            StringBuffer spath = new StringBuffer();
            spath.insert(0, v +"");
            v = s_path.get(v);
            while(s_id != v) 
            {
                spath.insert(0,v + ",");
                v = s_path.get(v);
            }
            spath.insert(0, s.getId() + ",");
            s.shortestPath.put(u, spath.toString()+":"+s_dist.get(u)); 
		}
		
		//*******************Step3: 给定SPT，找到以s为起始点的欧拉路径***************************
		//int[] trace = new int[2*SPTnode-1];
		i=0;
		ArrayList<Integer> trace = new ArrayList<Integer>();
		ArrayList<Integer> L = new ArrayList<Integer>();
		HashMap<Integer,Integer> stamp = new HashMap<Integer,Integer>();
		
		Stack<Integer> euler = new Stack<Integer>();
		euler.push(s_id);
		while(euler!=null && euler.size()!=0)
		{
			int root = euler.pop();
			int stop = -1;
			if(euler.size()!=0)
			{
				int exist = euler.peek();
				stop = s_path.get(exist);
			}
			//trace[i++] = root;
			trace.add(root);
			L.add(level.get(root));
			if(!stamp.containsKey(root)) stamp.put(root, i);
			i++;
			Vertex v = vertexList.get(root);
			for(int child: v.neighborIdSPT)
			{
				if(s_path.get(child) == root)
					euler.push(child);
			}
			if(v.neighborIdSPT.size() ==0)  //叶子节点，往上追溯
			{
				while(s_id != root && root!=stop)
				{
					//trace[i++] = s_path[root];
					int last = s_path.get(root);
					trace.add(last);
					L.add(level.get(last));
					if(!stamp.containsKey(last)) stamp.put(last, i);
					i++;
					root = last;
				}
			}
		}
		traceAll.add(trace);
		LAll.add(L);
		stampAll.add(stamp);
	}
	
	public static ArrayList<ArrayList<Integer>> gettraceAll()
	{
		return traceAll;
	}
	
	public static ArrayList<ArrayList<Integer>> getLAll()
	{
		return LAll;
	}
	
	public static ArrayList<HashMap<Integer,Integer>> getstampAll()
	{
		return stampAll;
	}
	
	public static double LLSQuery(HashSet<Vertex> GL, ArrayList<ArrayList<Integer>> traceAll, ArrayList<ArrayList<Integer>> LAll, ArrayList<HashMap<Integer,Integer>> stampAll, Vertex v1, Vertex v2)   //给定两个节点，估算最短距离
	{
		int index = 0;
		int a = v1.getId(), b=v2.getId();
		double appDis = Integer.MAX_VALUE;
		for(Vertex s: GL)
		{
			ArrayList<Integer> trace = traceAll.get(index);
			ArrayList<Integer> L = LAll.get(index);
			HashMap<Integer,Integer> stamp = stampAll.get(index);
			index ++;
			int LCAid = LCA(trace, L, stamp, a, b); //找到v1和v2的共同最小祖先
			if(LCAid == -1)
				continue;
			//System.out.println(LCAid);
			double total = 0;
			String[] two = s.shortestPath.get(a).split(":");
			double d1 = Double.parseDouble(two[1]);
			two = s.shortestPath.get(b).split(":");
			double d2 = Double.parseDouble(two[1]);
			two = s.shortestPath.get(LCAid).split(":");
			double d3 = Double.parseDouble(two[1]);
			total = d1 + d2 - 2*d3;		
			if(total < appDis)
				appDis = total;
		}
		return appDis;
	}
	
	public static int LCA(ArrayList<Integer> trace, ArrayList<Integer> L, HashMap<Integer,Integer> stamp, int a, int b)  //找到v1和v2的共同最小祖先
	{
		if(!stamp.containsKey(a) || !stamp.containsKey(b))
			return -1;
		int stampA = stamp.get(a);
		int stampB = stamp.get(b);
		int left = Math.min(stampA, stampB);
		int right = Math.max(stampA, stampB);
		int minL = Integer.MAX_VALUE;
		int LCAid = 0;
		for(int i=left; i<=right; i++)
		{
			if(L.get(i) < minL)
			{
				minL = L.get(i);
				LCAid = i;
			}
		}
		return trace.get(LCAid);
	}
	
	public static void WriteSpace(HashSet<Vertex> GL, ArrayList<ArrayList<Integer>> traceAll, ArrayList<ArrayList<Integer>> LAll, ArrayList<HashMap<Integer,Integer>> stampAll) throws IOException
	{
		String pathname = "C:/Users/17375/Desktop/中间结果/LLS.txt";
		File writename = new File(pathname); 
		writename.createNewFile(); // 创建新文件  
        BufferedWriter out = new BufferedWriter(new FileWriter(writename));  
		String writeIn = "";  
		int index = 0;
		for(Vertex s: GL)
		{
			ArrayList<Integer> trace = traceAll.get(index);
			ArrayList<Integer> L = LAll.get(index);
			HashMap<Integer,Integer> stamp = stampAll.get(index);
			index ++;
			for(int id:s.shortestPath.keySet())
			{
				writeIn += id+","+s.shortestPath.get(id);
			}
			for(int i: trace)
				writeIn += i;
			for(int i: L)
				writeIn += i;
			for(int id:stamp.keySet())
			{
				writeIn += id+","+stamp.get(id);
			}
			out.write(writeIn); // \r\n即为换行  
	        out.flush(); // 把缓存区内容压入文件  
	        writeIn = "";
		}
        out.close(); // 最后记得关闭文件 
	}
	
	public static void main(String[] args) throws SQLException
	{
		common cn = new common();
		dataAccess db = new dataAccess();
		ArrayList<Vertex> vertexList = db.readVertex();
		cn.initialCost(vertexList);
		LLSPre(vertexList);
		Vertex s = vertexList.get(0); 
	    Vertex d = vertexList.get(444);
		//LLSQuery(s, d);
	}
}

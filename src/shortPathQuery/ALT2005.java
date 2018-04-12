package shortPathQuery;

import graph.Edge;
import graph.MakeGraph;
import graph.Vertex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import SuperGraph.SuperEdge;
import SuperGraph.SuperVertex;

public class ALT2005 
{ //基于A*算法
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //原顶点集合
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //边集合
	public static ArrayList<Vertex> landMark = new ArrayList<Vertex>();  //地标集合
	
	public static ArrayList<ArrayList<Double>> shortestPathfromCb(ArrayList<ArrayList<Double>> vectorbyCb)    //将每个节点用地标表示
	{
 		/*int size = vectorbyCb.length;
		for(int i=0; i<vertexList.size(); i++)
		{
			//vectorbyCb[i] = new double[20];
			for(int j=0; j<size; j++)
			{
				vectorbyCb[i][j] = -1;
			}
		}*/
		
		for(int j=0; j<landMark.size(); j++)
		{
			Vertex la = landMark.get(j);
			for(Map.Entry<Integer, String> entry : la.shortestPath.entrySet()) 
			{
				int key = entry.getKey();
				String[] pathLasts = entry.getValue().split(":");
				double cost = Double.parseDouble(pathLasts[1]);
				ArrayList<Double> line = vectorbyCb.get(key);
				line.set(j, cost);
				vectorbyCb.set(key, line);
			}
		}
		return vectorbyCb;
	}
	
	public static float getH(int vid1, int vid2, ArrayList<ArrayList<Float>> vectorbyCb) //给两个点，返回估算的距离
	{
		//double[] add = new double[20];
		int no = 0;
		float dis = 0; 
		
		for(int j=0; j<landMark.size(); j++)
		{
			if(vectorbyCb.get(vid1).get(j) == -1 || vectorbyCb.get(vid2).get(j) == -1)
			{
				no ++;
			}
			else
			{
				dis += Math.abs(vectorbyCb.get(vid1).get(j) - vectorbyCb.get(vid2).get(j));
			}
		}
		if(landMark.size() == no) 
			return Integer.MAX_VALUE;
		dis /= (landMark.size() - no);
		return dis;
	}
	
	public static String ALT2(ArrayList<Vertex> vertexList, Vertex s, Vertex d, ArrayList<ArrayList< Float>> vectorbyCb)  //普通Dijkstra
	{
		ArrayList<Integer> open = new ArrayList<Integer> ();  //待检查的
		ArrayList<Integer> close = new ArrayList<Integer> ();  //已经检查的
		int n = vertexList.size();
		HashMap<Integer, HashMap<Integer,  Float>> matrixA = new HashMap<Integer, HashMap<Integer,  Float>>();
		//double matrix[][] = new double[n][n];		//邻接矩阵的初始化	    
		for(Vertex v:vertexList)
	    {
			int vid = v.getId();
			HashMap<Integer, Float> line = new HashMap<Integer, Float>();
			for(Edge e:v.edge)
	    	{
				int an = (e.nid1==vid)?e.nid2:e.nid1;
				line.put(an, e.cost);
	    	}
			matrixA.put(vid, line);
	    }
		
		int s_id = s.getId();
		int d_id = d.getId();
		List<Float> s_dist = new ArrayList<Float>();
		List<Integer> s_path = new ArrayList<Integer>();
		List<Float> estimateTod = new ArrayList<Float>();
		//double s_dist[] = new double[n];  //s跟所有点之间的最短距离
	    //int s_path[] = new int[n];    //记录所有点距离s的上一个节点，没有的话记为-1
	    //double estimateTod[] = new double[n];    //估计距离d的距离
		
		int i, j, u, k;
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
			estimateTod.add(getH(i, d_id, vectorbyCb));
		}
		
		while(open.size()!=0)
		{
			float min = Float.MAX_VALUE;  //扩展正序树
			float minAll = Float.MAX_VALUE;
			int index = 0;
			u = -1;
			for(j=0; j<open.size(); j++) //找到最小的
			{
				int tmp = open.get(j);
				if(s_dist.get(tmp)+ estimateTod.get(tmp) < minAll)
				{
					min = s_dist.get(tmp);
		        	minAll = s_dist.get(tmp) + estimateTod.get(tmp);
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
			
			if(u == d_id)
		    {
		    	int v = u;
	            StringBuffer spath = new StringBuffer();
	            spath.insert(0,v+"");
	            v = s_path.get(v);
	            while(s_id != v) 
	            {
	                spath.insert(0,v + ",");
	                v = s_path.get(v);
	            }
	            spath.insert(0, s.getId() + ",");
	            //System.out.println(spath + ": " + s_dist[u]);
	            return spath.toString();
		    }
	        
			for(int vid: vertexList.get(u).neighborIdTypeALL)  //对于所有邻居
			{
				if(min + matrixA.get(u).get(vid) < s_dist.get(vid))	//根据新节点更新其他节点到源点的距离
				{
					s_dist.set(vid, min + matrixA.get(u).get(vid));
					s_path.set(vid, u);
				}
				if(!open.contains(vid) && !close.contains(vid))
				{
					open.add(vid);
				}
			}
		}
		return null;
	}
	
	/*public static String biALT(Vertex s, Vertex d, ArrayList<ArrayList<Double>> vectorbyCb)  //双向Dijkstra
	{
		int n = vertexList.size();
		double matrix[][] = new double[n][n];		//邻接矩阵的初始化	    
		for(Vertex v:vertexList)
	    {
	    	for(Edge e:v.edge)
	    	{
	    		matrix[e.nid1][e.nid2] = e.cost;
	    		matrix[e.nid2][e.nid1] = e.cost;
	    	}
	    }
		
		StringBuffer shortestpath = new StringBuffer();
	    int s_id = s.getId(); int d_id = d.getId();
	    double s_dist[] = new double[n];  //s跟所有点之间的最短距离
	    int s_path[] = new int[n];    //记录所有点距离s的上一个节点，没有的话记为-1
	    boolean s_visited[] = new boolean[n];  //记录从s这边是否访问过该节点
	    double d_dist[] = new double[n];  //d跟所有点之间的最短距离
	    int d_path[] = new int[n];    //记录所有点距离d的上一个节点
	    boolean d_visited[] = new boolean[n];  //记录从d这边是否访问过该节点
	    double estimateTod[] = new double[n];    //估计距离d的距离
	    double estimateTos[] = new double[n];    //估计距离s的距离
	    int i,j,k,u = 0,count = 0;
	    double cost = Double.MAX_VALUE;   //花费
	    
	    for(i = 0; i < n; i++)     //初始化正序树&逆序树
	    {
            if(matrix[s_id][i] > 0 && i != s_id)  //所有邻居，距离为边的权重
            {
                s_dist[i] = matrix[s_id][i];
                s_path[i] = s_id;
            } 
            else          //如果不直接相连，距离为最大MAX_VALUE
            {
                s_dist[i] = Double.MAX_VALUE;
                s_path[i] = -1;
            }
            
            if(matrix[d_id][i] > 0 && i != d_id)  //所有邻居，距离为边的权重
            {
                d_dist[i] = matrix[d_id][i];
                d_path[i] = d_id;
            } 
            else          //如果不直接相连，距离为最大MAX_VALUE
            {
                d_dist[i] = Double.MAX_VALUE;
                d_path[i] = -1;
            }
            s_visited[i] = false;
            d_visited[i] = false;
            estimateTod[i] = getH(i, d_id, vectorbyCb);
            estimateTos[i] = getH(i, s_id, vectorbyCb);
        }
        s_path[s_id] = s_id;
        s_dist[s_id] = 0;
	    s_visited[s_id] = true;
        d_path[d_id] = d_id;
        d_dist[d_id] = 0;
	    d_visited[d_id] = true;
    
	    while(s_visited[d_id] == false && d_visited[s_id] == false) //开始扩展树
	    {
	        double min;
	        double minAll;
	        if(count == matrix.length)
	        {
	        	if(shortestpath.toString()!="")
	        	{
	        		break;
	        	}
	        	else
	        	{
	        		return null;
	        	}	        	
	        }
	        if(count++%2 == 0) 
	        {	            
        	    min = Double.MAX_VALUE;  //扩展正序树
        	    minAll = Double.MAX_VALUE;  //扩展正序树
        	    u = 0;
        	    for(j = 0;j < n;j++) 
        	    {
        	        if(s_visited[j] == false && s_dist[j] + estimateTod[j] < minAll) //找到下一个最近的节点
        	        {
        	            min = s_dist[j];
        	            minAll = s_dist[j] + estimateTod[j];
        	            u = j;
        	        }
        	    }
        	    s_visited[u] = true;
        	    for(k = 0;k < n;k++) 
        	    {
        	        if(s_visited[k] == false && matrix[u][k] > 0 && min + matrix[u][k] < s_dist[k]) //根据新节点更新其他节点到源点的距离
        	        {
        	           s_dist[k] = min + matrix[u][k];
        	           s_path[k] = u;
        	        }
        	    } 	
        	    if(d_visited[u] == false)   //如果正序和逆序存在交汇点
        	    {
        	        continue;
        	    }
	        }
	        else    //扩展逆序树
	        {	            
	            min = Double.MAX_VALUE;
	            minAll = Double.MAX_VALUE;  
                u = 0;
                for(j = 0;j <n;j++) 
                {
                    if(d_visited[j] == false && d_dist[j] + estimateTos[j] < min) 
                    {
                        min = d_dist[j];
                        minAll = d_dist[j] + estimateTos[j];
                        u = j;
                    }
                }
                d_visited[u] = true;
                for(k = 0;k <n;k++) 
                {
                    if(d_visited[k] == false && matrix[u][k] > 0 && min + matrix[u][k] < d_dist[k]) 
                    {
                       d_dist[k] = min + matrix[u][k];
                       d_path[k] = u;
                    }
                }
                //如果正序和逆序存在交汇点
                if(s_visited[u] == false) 
                {
                    continue;
                }
	        }
	       
	        if(cost > s_dist[u] + d_dist[u])  //存在了花费更小的路径，则更新shortestpath
	        {
	        	int v = u;
	            StringBuffer spath = new StringBuffer();
	            StringBuffer dpath = new StringBuffer();
	            if(u == d_id) 
	            {
	                //只使用正序树就得到了最优路径
    	            while(s_id != v) 
    	            {
    	                spath.insert(0,"," + v);
    	                v = s_path[v];
    	            }
    	            spath.insert(0, s.getId());
    	            shortestpath = spath;
	            } 
	            else 
	            {
	                //双向搜索得到了最优路径
	                while(s_id != v) 
	                {
	                    spath.insert(0,v + ",");
	                    v = s_path[v];
	                }
	                spath.insert(0, s.getId() + ",");
    	            v = d_path[u];
    	            while(d_id != v) 
    	            {
    	                dpath.append(v);
    	                dpath.append(",");
    	                v = d_path[v];
    	            }
    	            String sString = spath.toString();
    	            String[] ss = sString.split(",");
    	            boolean exist = false;
    	            for(String xiaos:ss)
    	            {
    	            	if(xiaos.equals(Integer.toString(d_id)))
    	            	{
    	            		exist = true;
    	            	}
    	            }
    	            if(exist == false) 
    	            {
    	                dpath.append(d.getId());
    	            }
    	            shortestpath = spath.append(dpath);
	            }
	            cost = s_dist[u] + d_dist[u];
	        }
	    }
	    //s.shortestPath.put(d.getId(), shortestpath.toString()); 
	    //d.shortestPath.put(s.getId(), shortestpath.toString()); 
	    System.out.println(shortestpath.toString() + ":" +cost);
		return shortestpath.toString() + ":" +cost;
	}*/
	
	public static int minF(HashMap<Integer, Double> open)  //返回f(n)最小的节点
	{
		double min = Integer.MAX_VALUE;
		int minId = open.keySet().iterator().next();
		for(Map.Entry<Integer, Double> entry:open.entrySet())
		{
			if(entry.getValue() < min)
			{
				min = entry.getValue();
				minId = entry.getKey();
			}
		}
		return minId;
	}
	
	public static ArrayList<ArrayList<Double>> ALTPre(ArrayList<Vertex> vertexList) throws IOException
	{
		Date start = new Date(); //计时
		common cn = new common();
		landMark = brandesForCentrality.TopCb(vertexList); //按照中心性，选择地标20个
		cn.calculateShortestPath(vertexList, vertexList, landMark); //计算每个节点到地标的最短距离
		ArrayList<ArrayList<Double>> vectorbyCb = new ArrayList<ArrayList<Double>>();
		for(int i=0; i<vertexList.size(); i++)
		{
			ArrayList<Double> line = new ArrayList<Double>();
			for(int j=0; j<landMark.size(); j++)
			{
				line.add((double) -1);
			}
			vectorbyCb.add(line);
		}
		//double[][] vectorbyCb = new double[vertexList.size()][landMark.size()];  //将每个节点用地标表示
		vectorbyCb = shortestPathfromCb(vectorbyCb);    //将每个节点用地标表示
		Date end = new Date();
		System.out.println("ALT预处理："+ (end.getTime() - start.getTime()) + " total milliseconds");

		//***********************************将中间结果写出***************************************
		String pathname = "C:/Users/17375/Desktop/中间结果/ALT.txt";
		File writename = new File(pathname); 
		writename.createNewFile(); // 创建新文件  
        BufferedWriter out = new BufferedWriter(new FileWriter(writename));  
		String writeIn = "";   
		for(int i=0; i<vectorbyCb.size(); i++)
		{
			ArrayList<Double> line = vectorbyCb.get(i);
			for(int j=0; j<vectorbyCb.get(0).size(); j++)
			{
				writeIn += line.get(j);
			}
			out.write(writeIn); // \r\n即为换行  
	        out.flush(); // 把缓存区内容压入文件  
	        writeIn = "";
		}
        out.close(); // 最后记得关闭文件  
		
		return vectorbyCb;
	}
	
	/*public static void main(String[] args) throws SQLException, ParseException
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
		landMark = brandesForCentrality.TopCb(vertexList); //按照中心性，选择地标20个
		cn.calculateShortestPath(vertexList, vertexList, landMark); //计算每个节点到地标的最短距离
		double[][] vectorbyCb = new double[vertexList.size()][landMark.size()];  //将每个节点用地标表示
		vectorbyCb = shortestPathfromCb(vectorbyCb);    //将每个节点用地标表示
		
		Vertex s = vertexList.get(74);
		Vertex d = vertexList.get(673);
		/*start = new Date(); //计时
		ALT(s,d,vectorbyCb);
		end = new Date();
		System.out.println("ALT："+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		start = new Date(); //计时
		ALT2(s,d,vectorbyCb);
		end = new Date();
		System.out.println("ALT2："+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		//System.out.println(str);
		
		cn.calculateShortestPath(vertexList, vertexList, s, d);
	}*/
}

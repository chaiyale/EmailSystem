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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import SuperGraph.SuperVertex;
import reachQuery.reachQuery;

public class common 
{
	public common()
	{}
	
	public double initialCost(ArrayList<Vertex> vertexList)   //初始化每条边的花费
	{
		float max = 0;
		for(Vertex v:vertexList)
	    {
	    	for(Edge e:v.edge)
	    	{
	    		if(max<e.weight)
	    		{
	    			max = e.weight;
	    		}
	    	}
	    }
	    max += 1;
	    
	    for(Vertex v:vertexList)
	    {
	    	for(Edge e:v.edge)
	    	{
	    		e.cost = max - e.weight;
	    		//e.cost = max*1.0/e.weight*1.0;
	    	}
	    }
	    return max+1;
	}
	
	public void calculateShortestPath(ArrayList<Vertex> vertexListALL, ArrayList<Vertex> vertexList, ArrayList<Vertex> vertexListOut)   //计算社团内部的最短路径
	{
		HashMap<Integer, HashMap<Integer, Float>> matrixA = new HashMap<Integer, HashMap<Integer, Float>>();
		//double matrix[][] = new double[vertexList.size()][vertexList.size()];		//邻接矩阵的初始化
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();   //vertexId和矩阵序号的对应关系
		Map<Integer, Integer> mapReverse = new HashMap<Integer, Integer>();   //矩阵序号和vertexId的对应关系
		int count = 0;
	    for(Vertex v:vertexList)
	    {
	    	map.put(v.getId(), count);
	    	mapReverse.put(count++, v.getId());
	    }
	    
	    for(Vertex v:vertexList)
	    {
	    	HashMap<Integer, Float> line = new HashMap<Integer, Float>();
	    	int v1 = map.get(v.getId());
	    	for(Edge e:v.edge)
	    	{
	    		int vid2 = (e.nid1==v.getId())? e.nid2:e.nid1;
	    		if(map.containsKey(vid2))  //要考虑边不在社团内的情况
	    		{
		    		int v2 = map.get(vid2);
		    		line.put(v2, e.cost);
	    		}
	    	}
	    	matrixA.put(v1, line);
	    }
	    
	    for(Vertex v:vertexListOut)
	    {
	    	dijkstraMatrix(vertexListALL, v, map.get(v.getId()), matrixA, map, mapReverse);
	    }
	}
	
	public String calculateShortestPath(ArrayList<Vertex> vertexListALL, ArrayList<Vertex> vertexList, Vertex s, Vertex d)  //计算两点之间的最短路径
	{
		double matrix[][] = new double[vertexList.size()][vertexList.size()];		//邻接矩阵的初始化
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();   //vertexId和矩阵序号的对应关系
		Map<Integer, Integer> mapReverse = new HashMap<Integer, Integer>();   //矩阵序号和vertexId的对应关系
		int count = 0;
	    for(Vertex v:vertexList)
	    {
	    	map.put(v.getId(), count);
	    	mapReverse.put(count++, v.getId());
	    }
	    
	    for(Vertex v:vertexList)
	    {
	    	for(Edge e:v.edge)
	    	{
	    		int vid2 = (e.nid1==v.getId())? e.nid2:e.nid1;
	    		if(map.containsKey(vid2))  //要考虑边不在社团内的情况
	    		{
	    			int v1 = map.get(e.nid1);
		    		int v2 = map.get(e.nid2);
	    			matrix[v1][v2] = e.cost;
		    		matrix[v2][v1] = e.cost;
	    		}
	    	}
	    }
	    
	    String str = "";
	    
	    /*Date start = new Date(); //计时
	    str = dijkstraMatrix(s, d, map.get(s.getId()), map.get(d.getId()), matrix, mapReverse);
	    Date end = new Date();
		System.out.println("Dijkstra最短路径："+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		Date start2 = new Date(); //计时
		str = biDijkstraNew(vertexListALL, s, d, map.get(s.getId()), map.get(d.getId()), matrix, map, mapReverse);
	    Date end2 = new Date();
		System.out.println("bi-DijkstraNew计算两点之间最短路径："+ (end2.getTime() - start2.getTime()) + " total milliseconds"); */
		
		//Date start2 = new Date(); //计时
		//str = biDijkstra(s, d, map.get(s.getId()), map.get(d.getId()), matrix, mapReverse);
	    //Date end2 = new Date();
		//System.out.println("bi-Dijkstra计算两点之间最短路径："+ (end2.getTime() - start2.getTime()) + " total milliseconds"); 
		
		//start2 = new Date(); //计时
		str = dijkstraMatrixNew(vertexListALL, s, d, map.get(s.getId()), map.get(d.getId()), matrix, map, mapReverse);
	    //end2 = new Date();
		//System.out.println("DijkstraNew计算两点之间最短路径："+ (end2.getTime() - start2.getTime()) + " total milliseconds"); 
	    
	    return str;
	}
	
	public String calculateShortestPathforWholeGraph(ArrayList<Vertex> vertexList, Vertex s, Vertex d)  //计算两点之间的最短路径
	{
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
	    }
	    
        String str = biDijkstra(s, d, matrixA);    
	    return str;
	}
	
	public String calculateShortestPathforWholeGraph2(ArrayList<Vertex> vertexList, Vertex s, Vertex d)  //计算两点之间的最短路径
	{
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
	    }
	    
        String str = dijkstraMatrix(s,d,s.getId(),d.getId(),matrixA);
	    return str;
	}
	
	public void dijkstraMatrix(ArrayList<Vertex> vertexList, Vertex s, int sid, HashMap<Integer, HashMap<Integer, Float>> matrixA, Map<Integer, Integer> map, Map<Integer, Integer> mapReverse)   //单源dijkstra-邻接表
	{
		ArrayList<Integer> open = new ArrayList<Integer> ();  //待检查的
		ArrayList<Integer> close = new ArrayList<Integer> ();  //已经检查的
		int n = matrixA.size();
		//int s_id = s.getId();
		int s_id = sid;
		List<Float> s_dist = new ArrayList<Float>();
		List<Integer> s_path = new ArrayList<Integer>();
		//double s_dist[] = new double[n];  //s跟所有点之间的最短距离
	    //int s_path[] = new int[n];    //记录所有点距离s的上一个节点，没有的话记为-1
		
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
		}
		s_dist.set(s_id, (float) 0);
		close.add(s_id);
		s.shortestPath.put(s.getId(), s.getId()+":"+0); 
		
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
			close.add(u);
			open.remove(index);
			for(int vid: vertexList.get(mapReverse.get(u)).neighborIdTypeALL)  //对于所有邻居
			{
				if(!map.containsKey(vid)) continue;
				k = map.get(vid);
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
            StringBuffer spath = new StringBuffer();
            spath.insert(0,mapReverse.get(v)+"");
            v = s_path.get(v);
            while(s_id != v) 
            {
                spath.insert(0,mapReverse.get(v) + ",");
                v = s_path.get(v);
            }
            spath.insert(0, s.getId() + ",");
            s.shortestPath.put(mapReverse.get(u), spath.toString()+":"+s_dist.get(u)); 
		}
	}
	
	public String dijkstraMatrix(Vertex s, Vertex d, int sid, int did, HashMap<Integer, HashMap<Integer, Float>> matrixA)   //单源dijkstra计算两点之间最短距离-邻接表
	{
		int n = matrixA.size();
		//int s_id = s.getId();
		int s_id = sid; 
		List<Float> s_dist = new ArrayList<Float>();
		List<Integer> s_path = new ArrayList<Integer>();
		List<Boolean> s_visited = new ArrayList<Boolean>();
		//double s_dist[] = new double[n];  //s跟所有点之间的最短距离
	    //int s_path[] = new int[n];    //记录所有点距离s的上一个节点，没有的话记为-1
	    //boolean s_visited[] = new boolean[n];  //记录从s这边是否访问过该节点
		
		int i, j, u, k;
		for(i = 0; i < n; i++)
		{
			if(matrixA.get(s_id).containsKey(i) && i != s_id)  //所有邻居，距离为边的权重
            {
				s_dist.add(matrixA.get(s_id).get(i));
                s_path.add(s_id);
            } 
            else          //如果不直接相连，距离为最大MAX_VALUE
            {
            	s_dist.add(Float.MAX_VALUE);
                s_path.add(-1);
            }
			s_visited.add(false);
		}
		s_visited.set(s_id,true);
		s_dist.set(s_id, (float) 0);
		
		for( i=0 ; i<n-1; i++)
		{
			float min = Float.MAX_VALUE;  //扩展正序树
		    u = -1;
		    for(j = 0;j < n;j++) 
		    {
		        if(s_visited.get(j) == false && s_dist.get(j) < min) //找到下一个最近的节点
		        {
		            min = s_dist.get(j);
		            u = j;
		        }
		    }
		    if(u==-1)
		    {
		    	break;  //剩余的所有的节点都不可达了
		    }
		    s_visited.set(u, true);
		    
		    if(u == did)
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
	            //System.out.println(spath + ":  " + s_dist[u]);
	            return spath.toString() + ":" + s_dist.get(u);
		    }
            
		    for(k = 0;k < n;k++) 
		    {
		        if(s_visited.get(k) == false && matrixA.get(u).containsKey(k) && (min+matrixA.get(u).get(k)<s_dist.get(k))) //根据新节点更新其他节点到源点的距离
		        {
		           s_dist.set(k, min + matrixA.get(u).get(k));
				   s_path.set(k, u);
		        }
		    } 
		}
		return null;
	}
	
	public String dijkstraMatrixNew(ArrayList<Vertex> vertexList, Vertex s, Vertex d, int sid, int did, double[][] matrix, Map<Integer, Integer> map, Map<Integer, Integer> mapReverse)   //单源dijkstra计算两点之间最短距离-邻接表
	{
		ArrayList<Integer> open = new ArrayList<Integer> ();  //待检查的
		ArrayList<Integer> close = new ArrayList<Integer> ();  //已经检查的
		int n = matrix.length;
		
		int s_id = sid;
		int d_id = did;
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
		s_dist[s_id] = 0;
		close.add(s_id);
		
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
			
			if(u == d_id)
		    {
		    	int v = u;
	            StringBuffer spath = new StringBuffer();
	            spath.insert(0,mapReverse.get(v)+"");
	            v = s_path[v];
	            while(s_id != v) 
	            {
	                spath.insert(0,mapReverse.get(v) + ",");
	                v = s_path[v];
	            }
	            spath.insert(0, s.getId() + ",");
	            //System.out.println(spath + ": " + s_dist[u]);
	            return spath.toString()+":"+s_dist[u];
		    }
	        
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
		}
		return null;
	}
	
	/**
	 * 计算某两个点之间的最短路径
	 * @param s 起点
	 * @param d 终点
	 * @param matrix 图的邻接矩阵
	 * @return 最短路径的字符串
	 */
	public String biDijkstra(Vertex s, Vertex d, int sid, int did, double[][] matrix, Map<Integer, Integer> mapReverse)   //双向dijkstra寻找两点之间最短路径
	{
	    if(s.equals(d))
	    {
	    	//System.out.println("同一个节点");
	    	return ":"+0;
	    }
		StringBuffer shortestpath = new StringBuffer();
	    int n = matrix.length;
	    //int s_id = s.getId(); int d_id = d.getId();
	    int s_id = sid;  int d_id = did;
	    double s_dist[] = new double[n];  //s跟所有点之间的最短距离
	    int s_path[] = new int[n];    //记录所有点距离s的上一个节点，没有的话记为-1
	    boolean s_visited[] = new boolean[n];  //记录从s这边是否访问过该节点
	    double d_dist[] = new double[n];  //d跟所有点之间的最短距离
	    int d_path[] = new int[n];    //记录所有点距离d的上一个节点
	    boolean d_visited[] = new boolean[n];  //记录从d这边是否访问过该节点
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
	        if(count == matrix.length)
	        {
	        	if(shortestpath.length()!=0 && shortestpath.toString()!="")
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
        	    u = 0;
        	    for(j = 0;j < n;j++) 
        	    {
        	        if(s_visited[j] == false && s_dist[j] < min) //找到下一个最近的节点
        	        {
        	            min = s_dist[j];
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
                u = 0;
                for(j = 0;j <n;j++) 
                {
                    if(d_visited[j] == false && d_dist[j] < min) {
                        min = d_dist[j];
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
    	                spath.insert(0,"," + mapReverse.get(v));
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
	                    spath.insert(0,mapReverse.get(v) + ",");
	                    v = s_path[v];
	                }
	                spath.insert(0, s.getId() + ",");
    	            v = d_path[u];
    	            while(d_id != v) 
    	            {
    	                dpath.append(mapReverse.get(v));
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
	    s.shortestPath.put(d.getId(), shortestpath.toString()+":"+cost); 
	    d.shortestPath.put(s.getId(), shortestpath.toString()+":"+cost); 
	    return shortestpath.toString() + ":" + cost;
	}
	
	public String biDijkstraNew(ArrayList<Vertex> vertexList, Vertex s, Vertex d, int sid, int did, double[][] matrix, Map<Integer, Integer> map, Map<Integer, Integer> mapReverse)   //双向dijkstra寻找两点之间最短路径
	{
	    if(s.equals(d))
	    {
	    	//System.out.println("同一个节点");
	    	return ":"+0;
	    }
		StringBuffer shortestpath = new StringBuffer();
	    int n = matrix.length;
	    //int s_id = s.getId(); int d_id = d.getId();
	    int s_id = sid;  int d_id = did;
	    double s_dist[] = new double[n];  //s跟所有点之间的最短距离
	    int s_path[] = new int[n];    //记录所有点距离s的上一个节点，没有的话记为-1
	    
	    double d_dist[] = new double[n];  //d跟所有点之间的最短距离
	    int d_path[] = new int[n];    //记录所有点距离d的上一个节点
	    
	    ArrayList<Integer> openS = new ArrayList<Integer> ();  //待检查的
		ArrayList<Integer> closeS = new ArrayList<Integer> ();  //已经检查的
		ArrayList<Integer> openD = new ArrayList<Integer> ();  //待检查的
		ArrayList<Integer> closeD = new ArrayList<Integer> ();  //已经检查的
		
	    int i,j,k,u = 0,count = 0;
	    double cost = Double.MAX_VALUE;   //花费
	    
	    for(i = 0; i < n; i++)     //初始化正序树&逆序树
	    {
            if(matrix[s_id][i] > 0 && i != s_id)  //所有邻居，距离为边的权重
            {
                s_dist[i] = matrix[s_id][i];
                s_path[i] = s_id;
                openS.add(i);
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
                openD.add(i);
            } 
            else          //如果不直接相连，距离为最大MAX_VALUE
            {
                d_dist[i] = Double.MAX_VALUE;
                d_path[i] = -1;
            }
        }
        s_path[s_id] = s_id;
        s_dist[s_id] = 0;
        d_path[d_id] = d_id;
        d_dist[d_id] = 0;
    
	    while(closeS.contains(d_id) == false && closeD.contains(s_id) == false) //开始扩展树
	    {
	        double min;
	        if(count == matrix.length)
	        {
	        	if(shortestpath.length()!=0 && shortestpath.toString()!="")
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
        	    int index = 0;
        	    u = 0;
        	    for(j=0; j<openS.size(); j++) //找到最小的
    			{
    				int tmp = openS.get(j);
    				if(s_dist[tmp] < min)
    				{
    					min = s_dist[tmp];
    		        	u = tmp;
    		        	index = j;
    				}
    			}
        	    closeS.add(u);
    			openS.remove(index);
    			
    			for(int vid: vertexList.get(mapReverse.get(u)).neighborIdTypeALL)  //对于所有邻居
    			{
    				if(!map.containsKey(vid)) continue;
    				k = map.get(vid);
    				if(min + matrix[u][k] < s_dist[k])	//根据新节点更新其他节点到源点的距离
    				{
    					s_dist[k] = min + matrix[u][k];
    			        s_path[k] = u;
    				}
    				if(!openS.contains(k) && !closeS.contains(k))
    				{
    					openS.add(k);
    				}
    			}
    			
        	    if(!closeD.contains(u))   //如果正序和逆序存在交汇点
        	    {
        	        continue;
        	    }
	        }
	        else    //扩展逆序树
	        {	            
	        	min = Double.MAX_VALUE;  //扩展正序树
        	    int index = 0;
        	    u = 0;
        	    for(j=0; j<openD.size(); j++) //找到最小的
    			{
    				int tmp = openD.get(j);
    				if(d_dist[tmp] < min)
    				{
    					min = d_dist[tmp];
    		        	u = tmp;
    		        	index = j;
    				}
    			}
        	    closeD.add(u);
    			openD.remove(index);
    			
    			for(int vid: vertexList.get(mapReverse.get(u)).neighborIdTypeALL)  //对于所有邻居
    			{
    				if(!map.containsKey(vid)) continue;
    				k = map.get(vid);
    				if(min + matrix[u][k] < d_dist[k])	//根据新节点更新其他节点到源点的距离
    				{
    					d_dist[k] = min + matrix[u][k];
    			        d_path[k] = u;
    				}
    				if(!openD.contains(k) && !closeD.contains(k))
    				{
    					openD.add(k);
    				}
    			}
    			
        	    if(!closeS.contains(u))   //如果正序和逆序存在交汇点
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
    	                spath.insert(0,"," + mapReverse.get(v));
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
	                    spath.insert(0,mapReverse.get(v) + ",");
	                    v = s_path[v];
	                }
	                spath.insert(0, s.getId() + ",");
    	            v = d_path[u];
    	            while(d_id != v) 
    	            {
    	                dpath.append(mapReverse.get(v));
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
	    s.shortestPath.put(d.getId(), shortestpath.toString()+":"+cost); 
	    d.shortestPath.put(s.getId(), shortestpath.toString()+":"+cost); 
	    //System.out.println(shortestpath.toString() + ":" + cost);
	    return shortestpath.toString() + ":" + cost;
	}
	
	public String biDijkstra(Vertex s, Vertex d, HashMap<Integer, HashMap<Integer, Float>> matrixA)
	{		
		StringBuffer shortestpath = new StringBuffer();
		int n = matrixA.size();
	    int s_id = s.getId(); int d_id = d.getId();
	    List<Float> s_dist = new ArrayList<Float>();
		List<Integer> s_path = new ArrayList<Integer>();
		List<Boolean> s_visited = new ArrayList<Boolean>();
		List<Float> d_dist = new ArrayList<Float>();
		List<Integer> d_path = new ArrayList<Integer>();
		List<Boolean> d_visited = new ArrayList<Boolean>();
	    //double s_dist[] = new double[n];  //s跟所有点之间的最短距离
	    //int s_path[] = new int[n];    //记录所有点距离s的上一个节点，没有的话记为-1
	    //boolean s_visited[] = new boolean[n];  //记录从s这边是否访问过该节点
	    //double d_dist[] = new double[n];  //d跟所有点之间的最短距离
	    //int d_path[] = new int[n];    //记录所有点距离d的上一个节点
	    //boolean d_visited[] = new boolean[n];  //记录从d这边是否访问过该节点
	    int i,j,k,u = 0,count = 0;
	    double cost = Float.MAX_VALUE;   //花费
	    
	    for(i = 0; i < n; i++)     //初始化正序树&逆序树
	    {
            if(matrixA.get(s_id).containsKey(i) && i != s_id)  //所有邻居，距离为边的权重
            {
            	s_dist.add(matrixA.get(s_id).get(i));
                s_path.add(s_id);
            } 
            else          //如果不直接相连，距离为最大MAX_VALUE
            {
            	s_dist.add(Float.MAX_VALUE);
                s_path.add(-1);
            }
			s_visited.add(false);
            
            if(matrixA.get(d_id).containsKey(i) && i != d_id)  //所有邻居，距离为边的权重
            {
            	d_dist.add(matrixA.get(d_id).get(i));
                d_path.add(d_id);
            } 
            else          //如果不直接相连，距离为最大MAX_VALUE
            {
            	d_dist.add(Float.MAX_VALUE);
                d_path.add(-1);
            }
			d_visited.add(false);
        }
	    s_visited.set(s_id,true);
		s_dist.set(s_id, (float) 0);
		s_path.set(s_id, s_id);
		d_visited.set(d_id,true);
		d_dist.set(d_id, (float) 0);
		d_path.set(d_id, d_id);
    
	    while(s_visited.get(d_id) == false && d_visited.get(s_id) == false) //开始扩展树
	    {
	    	float min;
	        if(count == n)
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
        	    min = Float.MAX_VALUE;  //扩展正序树
        	    u = 0;
        	    for(j = 0;j < n;j++) 
        	    {
        	        if(s_visited.get(j) == false && s_dist.get(j) < min) //找到下一个最近的节点
        	        {
        	        	min = s_dist.get(j);
        	            u = j;
        	        }
        	    }
        	    s_visited.set(u, true);
        	    for(k = 0;k < n;k++) 
        	    {
        	        if(s_visited.get(k) == false && matrixA.get(u).containsKey(k) && (min+matrixA.get(u).get(k)<s_dist.get(k))) //根据新节点更新其他节点到源点的距离
        	        {
        	        	s_dist.set(k, min + matrixA.get(u).get(k));
     				    s_path.set(k, u);
        	        }
        	    } 	
        	    if(d_visited.get(u) == false)   //如果正序和逆序存在交汇点
        	    {
        	        continue;
        	    }
	        }
	        else    //扩展逆序树
	        {	            
	            min = Float.MAX_VALUE;
                u = 0;
                for(j = 0;j <n;j++) 
                {
                    if(d_visited.get(j) == false && d_dist.get(j) < min) 
                    {
                    	min = d_dist.get(j);
                        u = j;
                    }
                }
                d_visited.set(u, true);
                for(k = 0;k <n;k++) 
                {
                    if(d_visited.get(k) == false && matrixA.get(u).containsKey(k) && (min+matrixA.get(u).get(k)<d_dist.get(k))) 
                    {
                    	d_dist.set(k, min + matrixA.get(u).get(k));
     				    d_path.set(k, u);
                    }
                }
                //如果正序和逆序存在交汇点
                if(s_visited.get(u) == false) 
                {
                    continue;
                }
	        }
	       
	        if(cost > s_dist.get(u) + d_dist.get(u))  //存在了花费更小的路径，则更新shortestpath
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
    	                v = s_path.get(v);
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
	                    v = s_path.get(v);
	                }
	                spath.insert(0, s.getId() + ",");
    	            v = d_path.get(u);
    	            while(d_id != v) 
    	            {
    	                dpath.append(v);
    	                dpath.append(",");
    	                v = d_path.get(v);
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
	            cost = s_dist.get(u) + d_dist.get(u);
	        }
	    }
	    //s.shortestPath.put(d.getId(), shortestpath.toString()); 
	    //d.shortestPath.put(s.getId(), shortestpath.toString()); 
		return shortestpath.toString() + ":" +cost;
	}
	
	/*public static void main(String[] args) throws SQLException, ParseException
	{
		MakeGraph mg = new MakeGraph();   //邮件数据集
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		ArrayList<Vertex> vertexList = mg.getVertex();
		
		common cn = new common();
		cn.calculateShortestPath(vertexList);
	}*/
}

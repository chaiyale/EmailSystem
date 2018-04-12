package shortPathQuery;

import graph.Edge;
import graph.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import SuperGraph.SuperEdge;
import SuperGraph.SuperVertex;

public class commonForSuper 
{
	public void initialCost(ArrayList<SuperVertex> superVertexList)   //初始化每条边的花费
	{
	    double max = 0;
	    for(SuperVertex sv:superVertexList)
	    {
	    	if(sv.edge != null && sv.edge.size()!=0)
	    	{
	    		for(SuperEdge se:sv.edge)
		    	{
		    		if(max<se.weight)
		    		{
		    			max = se.weight;
		    		}
		    	}
	    	}
	    }
	    max += 1;
	    
	    for(SuperVertex sv:superVertexList)
	    {
	    	if(sv.edge != null && sv.edge.size()!=0)
	    	{
	    		for(SuperEdge se:sv.edge)
		    	{
		    		se.cost =  max - se.weight;
	    			//se.cost = max*1.0/se.weight*1.0;
		    	}
	    	}
	    }
	}
	
	public void initialCost2(ArrayList<SuperEdge> superEdgeList)   //初始化每条边的花费
	{
	    double max = 0;
	    for(SuperEdge se:superEdgeList)
	    {
	    	if(max<se.weight)
    		{
    			max = se.weight;
    		}
	    }
	    max += 1;
	    
	    for(SuperEdge se:superEdgeList)
	    {
	    	se.cost =  max - se.weight;
	    }
	}
	
	public void calculateShortestPath(ArrayList<SuperVertex> superVertexList)   //计算社团内部的最短路径
	{
		double matrix[][] = new double[superVertexList.size()][superVertexList.size()];		//邻接矩阵的初始化
	    
	    for(SuperVertex sv:superVertexList)
	    {
	    	if(sv.edge != null && sv.edge.size()!=0)
	    	{
	    		for(SuperEdge se:sv.edge)
		    	{
		    		matrix[se.cid1][se.cid2] = se.cost;
		    	}
	    	}
	    }
	    
	    for(SuperVertex sv:superVertexList)
	    {
	    	dijkstraMatrix(sv, matrix);
	    }
	}
	
	public String calculateShortestPath(ArrayList<SuperEdge> superEdgeList, ArrayList<SuperVertex> superVertexList, SuperVertex s, SuperVertex d)  //计算两点之间的最短路径
	{
		double matrix[][] = new double[superVertexList.size()][superVertexList.size()];		//邻接矩阵的初始化
	    
	    /*for(SuperVertex sv:superVertexList)
	    {
	    	if(sv.edge != null && sv.edge.size()!=0)
	    	{
	    		for(SuperEdge se:sv.edge)
		    	{
		    		matrix[se.cid1][se.cid2] = se.cost;
		    		matrix[se.cid2][se.cid1] = se.cost;
		    	}
	    	}
	    }*/
		for(SuperEdge se:superEdgeList)
	    {
			matrix[se.cid1][se.cid2] = se.cost;
    		matrix[se.cid2][se.cid1] = se.cost;
	    }
	    
	    //String str= biDijkstraAll(s, d, matrix);
	    String str = dijkstraMatrixNew(superVertexList, s, d, matrix);
	    return str;
	}
	
	public String dijkstraMatrixNew(ArrayList<SuperVertex> superVertexList, SuperVertex s, SuperVertex d, double[][] matrix)   //单源dijkstra计算两点之间最短距离-邻接表
	{
		ArrayList<Integer> open = new ArrayList<Integer> ();  //待检查的
		ArrayList<Integer> close = new ArrayList<Integer> ();  //已经检查的
		int n = matrix.length;
		
		int s_id = s.cid; int d_id = d.cid;
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
			
			double totalPath = s_dist[u];
			if(u == d_id)
		    {
		    	int v = u;
	            StringBuffer spath = new StringBuffer();
	            spath.insert(0,v+"");
	            v = s_path[v];
	            while(s_id != v) 
	            {
	                spath.insert(0,v + ",");
	                s.shortestPath.put(v, s_dist[v]);
	                d.shortestPath.put(v, totalPath-s_dist[v]);
	                v = s_path[v];
	            }
	            spath.insert(0, s.cid + ",");
	            //System.out.println(spath + ": " + s_dist[u]);
	            return spath.toString();
		    }
	        
			for(SuperEdge edge: superVertexList.get(u).edge)  //对于所有邻居
			{
				k = edge.cid1==u? edge.cid2: edge.cid1;
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
	
	public String biDijkstraAll(SuperVertex s, SuperVertex d, double[][] matrix)  //返回两点之间的所有路径
	{
		StringBuffer shortestpath = new StringBuffer();
		int n = matrix.length;
	    int s_id = s.cid; int d_id = d.cid;
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
    	                spath.insert(0,"," + v);
    	                v = s_path[v];
    	            }
    	            spath.insert(0, s.cid);
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
	                spath.insert(0, s.cid + ",");
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
    	                dpath.append(d.cid);
    	            }
    	            shortestpath = spath.append(dpath);
	            }
	            cost = s_dist[u] + d_dist[u];
	        }
	    }
	    //s.shortestPath.put(d.getId(), shortestpath.toString()); 
	    //d.shortestPath.put(s.getId(), shortestpath.toString()); 
	    return shortestpath.toString();
	}
	
	public void dijkstraMatrix(SuperVertex s, double[][] matrix)   //单源dijkstra-邻接表
	{
		int n = matrix.length;
		int s_id = s.cid;
		double s_dist[] = new double[n];  //s跟所有点之间的最短距离
	    int s_path[] = new int[n];    //记录所有点距离s的上一个节点，没有的话记为-1
	    boolean s_visited[] = new boolean[n];  //记录从s这边是否访问过该节点
		
		int i, j, u, k;
		for(i = 0; i < n; i++)
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
			s_visited[i] = false;
		}
		s_visited[s_id] = true;
		s_dist[s_id] = 0;
		
		for( i=0 ; i<n-1; i++)
		{
			double min = Double.MAX_VALUE;  //扩展正序树
		    u = -1;
		    for(j = 0;j < n;j++) 
		    {
		        if(s_visited[j] == false && s_dist[j] < min) //找到下一个最近的节点
		        {
		            min = s_dist[j];
		            u = j;
		        }
		    }
		    if(u==-1)
		    {
		    	break;  //剩余的所有的节点都不可达了
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
		    int v = u;
            StringBuffer spath = new StringBuffer();
            spath.insert(0,v+"");
            v = s_path[v];
            while(s_id != v) 
            {
                spath.insert(0,v + ",");
                v = s_path[v];
            }
            spath.insert(0, s_id + ",");
            //s.shortestPath.put(u, spath.toString()); 
            //matrix[s_id][u] = s_dist[u];
            //System.out.println(spath + ":  " + s_dist[u]);
		}
	}
	
	public HashMap<Integer,String> dijkstraMatrixforNeighbor(ArrayList<Vertex> vertexList, Vertex s, int sid, double[][] matrix, Map<Integer, Integer> map, Map<Integer, Integer> mapReverse, HashSet<Integer> VSet, double diffToDes)   //单源dijkstra-邻接表
	{
		HashMap<Integer,String> shortestPathTillNow = new HashMap<Integer,String>();   //截止到目前为止的节点的最短路径
		int n = matrix.length;
		//int s_id = s.getId();
		int s_id = sid;
		double s_dist[] = new double[n];  //s跟所有点之间的最短距离
	    int s_path[] = new int[n];    //记录所有点距离s的上一个节点，没有的话记为-1
	    ArrayList<Integer> open = new ArrayList<Integer> ();  //待检查的
		ArrayList<Integer> close = new ArrayList<Integer> ();  //已经检查的
		double MinCost = Integer.MAX_VALUE;
		
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
			if(!VSet.contains(mapReverse.get(u)))
		    {
		    	continue;
		    }
		    
		    //如果是另一个社团的节点，记为最短路径
		    int v = u;
            StringBuffer spath = new StringBuffer();
            v = s_path[v];
            while(s_id != v) 
            {
                spath.insert(0,mapReverse.get(v) + ",");
                v = s_path[v];
            }
            spath.insert(0, s.getId() + ",");
            String str = spath.toString()+mapReverse.get(u)+":"+s_dist[u];
            shortestPathTillNow.put(mapReverse.get(u), str);
            if(s_dist[u]<MinCost) MinCost=s_dist[u];
            
            //matrix[s_id][u] = s_dist[u];
            //System.out.println(spath + ":  " + s_dist[u]);
		}
		
		HashMap<Integer,String> shortestPathTillNowPrune = new HashMap<Integer,String>();   //截止到目前为止的节点的最短路径--修剪过
		for(Map.Entry<Integer, String> entry : shortestPathTillNow.entrySet())
		{
			String[] pathLasts = entry.getValue().split(":");
			double cost1 = Double.parseDouble(pathLasts[1]);
			if(cost1 <= MinCost+diffToDes)
			{
				shortestPathTillNowPrune.put(entry.getKey(), entry.getValue());
			}
		}
		
		return shortestPathTillNowPrune;
	}
	
	public HashMap<Integer,String> dijkstraMatrixforNeighborWithoutPrune(ArrayList<Vertex> vertexList, Vertex s, int sid, double[][] matrix, Map<Integer, Integer> map, Map<Integer, Integer> mapReverse, HashSet<Integer> VSet)   //单源dijkstra-邻接表
	{
		HashMap<Integer,String> shortestPathTillNow = new HashMap<Integer,String>();   //截止到目前为止的节点的最短路径
		int n = matrix.length;
		//int s_id = s.getId();
		int s_id = sid;
		double s_dist[] = new double[n];  //s跟所有点之间的最短距离
	    int s_path[] = new int[n];    //记录所有点距离s的上一个节点，没有的话记为-1
	    ArrayList<Integer> open = new ArrayList<Integer> ();  //待检查的
		ArrayList<Integer> close = new ArrayList<Integer> ();  //已经检查的
		double MinCost = Integer.MAX_VALUE;
		
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
			if(!VSet.contains(mapReverse.get(u)))
		    {
		    	continue;
		    }
		    
		    //如果是另一个社团的节点，记为最短路径
		    int v = u;
            StringBuffer spath = new StringBuffer();
            v = s_path[v];
            while(s_id != v) 
            {
                spath.insert(0,mapReverse.get(v) + ",");
                v = s_path[v];
            }
            spath.insert(0, s.getId() + ",");
            String str = spath.toString()+mapReverse.get(u)+":"+s_dist[u];
            shortestPathTillNow.put(mapReverse.get(u), str);
            if(s_dist[u]<MinCost) MinCost=s_dist[u];
            
            //matrix[s_id][u] = s_dist[u];
            //System.out.println(spath + ":  " + s_dist[u]);
		}
		
		return shortestPathTillNow;
	}
	
	public String biDijkstraforNeibor(Vertex s, Vertex d, int sid, int did, ArrayList<ArrayList<Float>> matrixA, Map<Integer, Integer> mapReverse)   //双向dijkstra寻找两点之间最短路径
	{
		if(s.equals(d))
	    {
	    	System.out.println("同一个节点");
	    	return null;
	    }
		StringBuffer shortestpath = new StringBuffer();
		int n = matrixA.size();
	    int s_id = sid; int d_id = did;
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
	    float cost = Float.MAX_VALUE;   //花费
	    
	    for(i = 0; i < n; i++)     //初始化正序树&逆序树
	    {
            if(matrixA.get(s_id).get(i)!=-1 && i != s_id)  //所有邻居，距离为边的权重
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
            
            if(matrixA.get(d_id).get(i)!=-1 && i != d_id)  //所有邻居，距离为边的权重
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
        	        if(s_visited.get(k) == false && matrixA.get(u).get(k)!=-1 && (min+matrixA.get(u).get(k)<s_dist.get(k))) //根据新节点更新其他节点到源点的距离
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
                    if(d_visited.get(k) == false && matrixA.get(u).get(k)!=-1 && (min+matrixA.get(u).get(k)<d_dist.get(k))) 
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
    	                spath.insert(0,"," + mapReverse.get(v));
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
	                    spath.insert(0,mapReverse.get(v) + ",");
	                    v = s_path.get(v);
	                }
	                spath.insert(0, s.getId() + ",");
    	            v = d_path.get(u);
    	            while(d_id != v) 
    	            {
    	                dpath.append(mapReverse.get(v));
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
	
	public HashMap<Integer,String> calculateNeighbor(HashSet<Integer> VSet, HashSet<Integer> VSet1, HashSet<Integer> VSet2, HashMap<Integer,String> shortestPathLastStep, ArrayList<Vertex> vertexList)   //计算相邻社团之间的最短路径 
	{
		HashMap<Integer,String> shortestPathTillNow = new HashMap<Integer,String>();   //截止到目前为止的节点的最短路径
		for(int vi: VSet2)  VSet1.add(vi);
		
		//Step1: 首先初始化matrix矩阵
		int size = VSet.size() + VSet1.size();
		ArrayList<ArrayList<Float>> matrixA = new ArrayList<ArrayList<Float>>();
		for(int i=0; i<size; i++)
		{
			ArrayList<Float> line = new ArrayList<Float>();
			for(int j=0; j<size; j++)
			{
				line.add((float)-1);
			}
			matrixA.add(line);
		}
		//double matrix[][] = new double[size][size];		//邻接矩阵的初始化
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();   //vertexId和矩阵序号的对应关系
		Map<Integer, Integer> mapReverse = new HashMap<Integer, Integer>();   //矩阵序号和vertexId的对应关系
		int count = 0;
		
		for(int vid: VSet)
		{
			map.put(vid, count);
			mapReverse.put(count++, vid);
		}
		for(int vid2: VSet1)
		{
			map.put(vid2, count);
			mapReverse.put(count++, vid2);
		}
		
		for(int vid: VSet)    
		{
			Vertex v = vertexList.get(vid);
			for(Edge e:v.edge)     //两个社团间相连的边
			{
				int d = (e.nid1==vid)?e.nid2:e.nid1;
				if(VSet1.contains(d))
				{
					int v1 = map.get(e.nid1);
		    		int v2 = map.get(e.nid2);
	    			//matrix[v1][v2] = e.cost;
		    		//matrix[v2][v1] = e.cost;
		    		ArrayList<Float> line = matrixA.get(v1);
					line.set(v2, e.cost);
					matrixA.set(v1, line);
					ArrayList<Float> line2 = matrixA.get(v2);
					line2.set(v1, e.cost);
					matrixA.set(v2, line2);
				}
			}
			
			for(int key:VSet)
			{
				if(v.shortestPath.containsKey(key) && key!=vid)
				{
					if(v.shortestPath.get(key) == null || !v.shortestPath.get(key).contains(":"))
					{
						System.out.println();
					}
					String[] pathLasts = v.shortestPath.get(key).split(":");
					float cost = Float.parseFloat(pathLasts[1]);
					int v1 = map.get(vid);
		    		int v2 = map.get(key);
		    		ArrayList<Float> line = matrixA.get(v1);
					line.set(v2, cost);
					matrixA.set(v1, line);
					ArrayList<Float> line2 = matrixA.get(v2);
					line2.set(v1, cost);
					matrixA.set(v2, line2);
				}
			}
		}
		
		for(int vid: VSet2)
		{
			Vertex v = vertexList.get(vid);
			for(int key:VSet1)
			{
				if(v.shortestPath.containsKey(key))
				{
					String[] pathLasts = v.shortestPath.get(key).split(":");
					float cost = Float.parseFloat(pathLasts[1]);
					int v1 = map.get(vid);
		    		int v2 = map.get(key);
		    		ArrayList<Float> line = matrixA.get(v1);
					line.set(v2, cost);
					matrixA.set(v1, line);
					ArrayList<Float> line2 = matrixA.get(v2);
					line2.set(v1, cost);
					matrixA.set(v2, line2);
				}
			}
		}
		
		//Step2: 计算最短路径
		for(int vid2: VSet2)   
		{
			Vertex v2 = vertexList.get(vid2);
			double min = Double.MAX_VALUE;
			String path = "";
			
			for(Map.Entry<Integer, String> entry : shortestPathLastStep.entrySet())
			{
				if(entry.getValue() == null)
					System.out.println("");
				String[] pathLasts = entry.getValue().split(":");
				String path0 = pathLasts[0];
				double cost1 = Double.parseDouble(pathLasts[1]);
				int vid = entry.getKey();
				Vertex v1 = vertexList.get(vid);
				String path1 = "";  double cost2 = 0.0;
				if(!v1.equals(v2))
				{
					String str = biDijkstraforNeibor(v1, v2, map.get(vid), map.get(vid2), matrixA, mapReverse);  //可以改成数组形式
					if(str == null) continue;
				    String[] strs = str.split(":");
				    path1 = strs[0];
				    cost2 = Double.parseDouble(strs[1]);
				}
				if(cost2+cost1 < min)
				{
					min = cost2+cost1;
					path = path0+","+path1;
				}
			}
			shortestPathTillNow.put(vid2, path+":"+min);
		}
		return shortestPathTillNow;
	}
	
	public HashMap<Integer,String> calculateNeighborNew(HashSet<Integer> VSet, HashSet<Integer> VSet1, HashSet<Integer> VSet2, HashMap<Integer,String> shortestPathLastStep, ArrayList<Vertex> vertexList, double costToDes, double diffToDes)   //计算相邻社团之间的最短路径 
	{
		HashMap<Integer,String> shortestPathTillNow = new HashMap<Integer,String>();   //截止到目前为止的节点的最短路径
		for(int vi: VSet2)  VSet1.add(vi);
		
		//Step1: 首先初始化matrix矩阵
				int size = VSet.size() + VSet1.size();
				ArrayList<ArrayList<Float>> matrixA = new ArrayList<ArrayList<Float>>();
				for(int i=0; i<size; i++)
				{
					ArrayList<Float> line = new ArrayList<Float>();
					for(int j=0; j<size; j++)
					{
						line.add((float)-1);
					}
					matrixA.add(line);
				}
				//double matrix[][] = new double[size][size];		//邻接矩阵的初始化
				Map<Integer, Integer> map = new HashMap<Integer, Integer>();   //vertexId和矩阵序号的对应关系
				Map<Integer, Integer> mapReverse = new HashMap<Integer, Integer>();   //矩阵序号和vertexId的对应关系
				int count = 0;
				
				for(int vid: VSet)
				{
					map.put(vid, count);
					mapReverse.put(count++, vid);
				}
				for(int vid2: VSet1)
				{
					map.put(vid2, count);
					mapReverse.put(count++, vid2);
				}
				
				for(int vid: VSet)    
				{
					Vertex v = vertexList.get(vid);
					for(Edge e:v.edge)     //两个社团间相连的边
					{
						int d = (e.nid1==vid)?e.nid2:e.nid1;
						if(VSet1.contains(d))
						{
							int v1 = map.get(e.nid1);
				    		int v2 = map.get(e.nid2);
			    			//matrix[v1][v2] = e.cost;
				    		//matrix[v2][v1] = e.cost;
				    		ArrayList<Float> line = matrixA.get(v1);
							line.set(v2, e.cost);
							matrixA.set(v1, line);
							ArrayList<Float> line2 = matrixA.get(v2);
							line2.set(v1, e.cost);
							matrixA.set(v2, line2);
						}
					}
					
					for(int key:VSet)
					{
						if(v.shortestPath.containsKey(key) && key!=vid)
						{
							if(v.shortestPath.get(key) == null || !v.shortestPath.get(key).contains(":"))
							{
								System.out.println();
							}
							String[] pathLasts = v.shortestPath.get(key).split(":");
							float cost = Float.parseFloat(pathLasts[1]);
							int v1 = map.get(vid);
				    		int v2 = map.get(key);
				    		ArrayList<Float> line = matrixA.get(v1);
							line.set(v2, cost);
							matrixA.set(v1, line);
							ArrayList<Float> line2 = matrixA.get(v2);
							line2.set(v1, cost);
							matrixA.set(v2, line2);
						}
					}
				}
				
				for(int vid: VSet2)
				{
					Vertex v = vertexList.get(vid);
					for(int key:VSet1)
					{
						if(v.shortestPath.containsKey(key))
						{
							String[] pathLasts = v.shortestPath.get(key).split(":");
							float cost = Float.parseFloat(pathLasts[1]);
							int v1 = map.get(vid);
				    		int v2 = map.get(key);
				    		ArrayList<Float> line = matrixA.get(v1);
							line.set(v2, cost);
							matrixA.set(v1, line);
							ArrayList<Float> line2 = matrixA.get(v2);
							line2.set(v1, cost);
							matrixA.set(v2, line2);
						}
					}
				}
		
		//Step2: 计算最短路径
		double TotalMin = Double.MAX_VALUE;
		for(int vid2: VSet2)   
		{
			Vertex v2 = vertexList.get(vid2);
			double min = Double.MAX_VALUE;
			String path = "";
			
			for(Map.Entry<Integer, String> entry : shortestPathLastStep.entrySet())
			{
				String[] pathLasts = entry.getValue().split(":");
				String path0 = pathLasts[0];
				double cost1 = Double.parseDouble(pathLasts[1]);
				int vid = entry.getKey();
				Vertex v1 = vertexList.get(vid);
				String path1 = "";  double cost2 = 0.0;
				if(!v1.equals(v2))
				{
					String str = biDijkstraforNeibor(v1, v2, map.get(vid), map.get(vid2), matrixA, mapReverse);  //可以改成数组形
					if(str==null || str.equals("")) continue;
				    String[] strs = str.split(":");
				    path1 = strs[0];
				    cost2 = Double.parseDouble(strs[1]);
				}
				if(cost2+cost1 < min)
				{
					min = cost2+cost1;
					path = path0+","+path1;
				}
			}
			if(TotalMin > min) TotalMin = min;
			shortestPathTillNow.put(vid2, path+":"+min);
		}
		
		HashMap<Integer,String> shortestPathTillNowPrune = new HashMap<Integer,String>();   //截止到目前为止的节点的最短路径--修剪过
		double small = TotalMin + diffToDes;
		for(Map.Entry<Integer, String> entry : shortestPathTillNow.entrySet())
		{
			String[] pathLasts = entry.getValue().split(":");
			double cost1 = Double.parseDouble(pathLasts[1]);
			if(cost1 <= small)
			{
				shortestPathTillNowPrune.put(entry.getKey(), entry.getValue());
			}
		}
		
		return shortestPathTillNowPrune;
	}
	
	public HashMap<Integer,String> calculatedijkstraMatrix(ArrayList<Vertex> vertexList, Vertex s, HashSet<Integer> VSet, ArrayList<Vertex> vertexListThis, double diffToDes)
	{
		double matrix[][] = new double[vertexListThis.size()][vertexListThis.size()];		//邻接矩阵的初始化
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();   //vertexId和矩阵序号的对应关系
		Map<Integer, Integer> mapReverse = new HashMap<Integer, Integer>();   //矩阵序号和vertexId的对应关系
		int count = 0;
	    for(Vertex v:vertexListThis)
	    {
	    	map.put(v.getId(), count);
	    	mapReverse.put(count++, v.getId());
	    }
	    
	    for(Vertex v:vertexListThis)
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
	    
		HashMap<Integer,String> shortestPathTillNow = dijkstraMatrixforNeighbor(vertexList, s, map.get(s.getId()), matrix, map, mapReverse, VSet, diffToDes);
		return shortestPathTillNow;
	}
	
	public HashMap<Integer,String> calculatedijkstraMatrixWithoutPrune(ArrayList<Vertex> vertexList, Vertex s, HashSet<Integer> VSet, ArrayList<Vertex> vertexListThis)
	{
		double matrix[][] = new double[vertexListThis.size()][vertexListThis.size()];		//邻接矩阵的初始化
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();   //vertexId和矩阵序号的对应关系
		Map<Integer, Integer> mapReverse = new HashMap<Integer, Integer>();   //矩阵序号和vertexId的对应关系
		int count = 0;
	    for(Vertex v:vertexListThis)
	    {
	    	map.put(v.getId(), count);
	    	mapReverse.put(count++, v.getId());
	    }
	    
	    for(Vertex v:vertexListThis)
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
	    
		HashMap<Integer,String> shortestPathTillNow = dijkstraMatrixforNeighborWithoutPrune(vertexList, s, map.get(s.getId()), matrix, map, mapReverse, VSet);
		return shortestPathTillNow;
	}
}

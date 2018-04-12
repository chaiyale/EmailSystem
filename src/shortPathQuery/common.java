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
	
	public double initialCost(ArrayList<Vertex> vertexList)   //��ʼ��ÿ���ߵĻ���
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
	
	public void calculateShortestPath(ArrayList<Vertex> vertexListALL, ArrayList<Vertex> vertexList, ArrayList<Vertex> vertexListOut)   //���������ڲ������·��
	{
		HashMap<Integer, HashMap<Integer, Float>> matrixA = new HashMap<Integer, HashMap<Integer, Float>>();
		//double matrix[][] = new double[vertexList.size()][vertexList.size()];		//�ڽӾ���ĳ�ʼ��
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();   //vertexId�;�����ŵĶ�Ӧ��ϵ
		Map<Integer, Integer> mapReverse = new HashMap<Integer, Integer>();   //������ź�vertexId�Ķ�Ӧ��ϵ
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
	    		if(map.containsKey(vid2))  //Ҫ���Ǳ߲��������ڵ����
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
	
	public String calculateShortestPath(ArrayList<Vertex> vertexListALL, ArrayList<Vertex> vertexList, Vertex s, Vertex d)  //��������֮������·��
	{
		double matrix[][] = new double[vertexList.size()][vertexList.size()];		//�ڽӾ���ĳ�ʼ��
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();   //vertexId�;�����ŵĶ�Ӧ��ϵ
		Map<Integer, Integer> mapReverse = new HashMap<Integer, Integer>();   //������ź�vertexId�Ķ�Ӧ��ϵ
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
	    		if(map.containsKey(vid2))  //Ҫ���Ǳ߲��������ڵ����
	    		{
	    			int v1 = map.get(e.nid1);
		    		int v2 = map.get(e.nid2);
	    			matrix[v1][v2] = e.cost;
		    		matrix[v2][v1] = e.cost;
	    		}
	    	}
	    }
	    
	    String str = "";
	    
	    /*Date start = new Date(); //��ʱ
	    str = dijkstraMatrix(s, d, map.get(s.getId()), map.get(d.getId()), matrix, mapReverse);
	    Date end = new Date();
		System.out.println("Dijkstra���·����"+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		Date start2 = new Date(); //��ʱ
		str = biDijkstraNew(vertexListALL, s, d, map.get(s.getId()), map.get(d.getId()), matrix, map, mapReverse);
	    Date end2 = new Date();
		System.out.println("bi-DijkstraNew��������֮�����·����"+ (end2.getTime() - start2.getTime()) + " total milliseconds"); */
		
		//Date start2 = new Date(); //��ʱ
		//str = biDijkstra(s, d, map.get(s.getId()), map.get(d.getId()), matrix, mapReverse);
	    //Date end2 = new Date();
		//System.out.println("bi-Dijkstra��������֮�����·����"+ (end2.getTime() - start2.getTime()) + " total milliseconds"); 
		
		//start2 = new Date(); //��ʱ
		str = dijkstraMatrixNew(vertexListALL, s, d, map.get(s.getId()), map.get(d.getId()), matrix, map, mapReverse);
	    //end2 = new Date();
		//System.out.println("DijkstraNew��������֮�����·����"+ (end2.getTime() - start2.getTime()) + " total milliseconds"); 
	    
	    return str;
	}
	
	public String calculateShortestPathforWholeGraph(ArrayList<Vertex> vertexList, Vertex s, Vertex d)  //��������֮������·��
	{
		HashMap<Integer, HashMap<Integer, Float>> matrixA = new HashMap<Integer, HashMap<Integer, Float>>();
		//double matrix[][] = new double[vertexList.size()][vertexList.size()];		//�ڽӾ���ĳ�ʼ��
	    
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
	
	public String calculateShortestPathforWholeGraph2(ArrayList<Vertex> vertexList, Vertex s, Vertex d)  //��������֮������·��
	{
		HashMap<Integer, HashMap<Integer, Float>> matrixA = new HashMap<Integer, HashMap<Integer, Float>>();
		//double matrix[][] = new double[vertexList.size()][vertexList.size()];		//�ڽӾ���ĳ�ʼ��
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
	
	public void dijkstraMatrix(ArrayList<Vertex> vertexList, Vertex s, int sid, HashMap<Integer, HashMap<Integer, Float>> matrixA, Map<Integer, Integer> map, Map<Integer, Integer> mapReverse)   //��Դdijkstra-�ڽӱ�
	{
		ArrayList<Integer> open = new ArrayList<Integer> ();  //������
		ArrayList<Integer> close = new ArrayList<Integer> ();  //�Ѿ�����
		int n = matrixA.size();
		//int s_id = s.getId();
		int s_id = sid;
		List<Float> s_dist = new ArrayList<Float>();
		List<Integer> s_path = new ArrayList<Integer>();
		//double s_dist[] = new double[n];  //s�����е�֮�����̾���
	    //int s_path[] = new int[n];    //��¼���е����s����һ���ڵ㣬û�еĻ���Ϊ-1
		
		int i, j, u, k;
		for(i = 0; i < n; i++)
		{
			if(matrixA.get(s_id).containsKey(i) && i != s_id)  //�����ھӣ�����Ϊ�ߵ�Ȩ��
            {
				s_dist.add(matrixA.get(s_id).get(i));
                s_path.add(s_id);
                open.add(i);
            } 
            else          //�����ֱ������������Ϊ���MAX_VALUE
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
			float min = Float.MAX_VALUE;  //��չ������
			int index = 0;
			u = -1;
			for(j=0; j<open.size(); j++) //�ҵ���С��
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
		    	break;  //ʣ������еĽڵ㶼���ɴ���
		    }
			close.add(u);
			open.remove(index);
			for(int vid: vertexList.get(mapReverse.get(u)).neighborIdTypeALL)  //���������ھ�
			{
				if(!map.containsKey(vid)) continue;
				k = map.get(vid);
				if(min + matrixA.get(u).get(k) < s_dist.get(k))	//�����½ڵ���������ڵ㵽Դ��ľ���
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
	
	public String dijkstraMatrix(Vertex s, Vertex d, int sid, int did, HashMap<Integer, HashMap<Integer, Float>> matrixA)   //��Դdijkstra��������֮����̾���-�ڽӱ�
	{
		int n = matrixA.size();
		//int s_id = s.getId();
		int s_id = sid; 
		List<Float> s_dist = new ArrayList<Float>();
		List<Integer> s_path = new ArrayList<Integer>();
		List<Boolean> s_visited = new ArrayList<Boolean>();
		//double s_dist[] = new double[n];  //s�����е�֮�����̾���
	    //int s_path[] = new int[n];    //��¼���е����s����һ���ڵ㣬û�еĻ���Ϊ-1
	    //boolean s_visited[] = new boolean[n];  //��¼��s����Ƿ���ʹ��ýڵ�
		
		int i, j, u, k;
		for(i = 0; i < n; i++)
		{
			if(matrixA.get(s_id).containsKey(i) && i != s_id)  //�����ھӣ�����Ϊ�ߵ�Ȩ��
            {
				s_dist.add(matrixA.get(s_id).get(i));
                s_path.add(s_id);
            } 
            else          //�����ֱ������������Ϊ���MAX_VALUE
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
			float min = Float.MAX_VALUE;  //��չ������
		    u = -1;
		    for(j = 0;j < n;j++) 
		    {
		        if(s_visited.get(j) == false && s_dist.get(j) < min) //�ҵ���һ������Ľڵ�
		        {
		            min = s_dist.get(j);
		            u = j;
		        }
		    }
		    if(u==-1)
		    {
		    	break;  //ʣ������еĽڵ㶼���ɴ���
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
		        if(s_visited.get(k) == false && matrixA.get(u).containsKey(k) && (min+matrixA.get(u).get(k)<s_dist.get(k))) //�����½ڵ���������ڵ㵽Դ��ľ���
		        {
		           s_dist.set(k, min + matrixA.get(u).get(k));
				   s_path.set(k, u);
		        }
		    } 
		}
		return null;
	}
	
	public String dijkstraMatrixNew(ArrayList<Vertex> vertexList, Vertex s, Vertex d, int sid, int did, double[][] matrix, Map<Integer, Integer> map, Map<Integer, Integer> mapReverse)   //��Դdijkstra��������֮����̾���-�ڽӱ�
	{
		ArrayList<Integer> open = new ArrayList<Integer> ();  //������
		ArrayList<Integer> close = new ArrayList<Integer> ();  //�Ѿ�����
		int n = matrix.length;
		
		int s_id = sid;
		int d_id = did;
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
		s_dist[s_id] = 0;
		close.add(s_id);
		
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
		}
		return null;
	}
	
	/**
	 * ����ĳ������֮������·��
	 * @param s ���
	 * @param d �յ�
	 * @param matrix ͼ���ڽӾ���
	 * @return ���·�����ַ���
	 */
	public String biDijkstra(Vertex s, Vertex d, int sid, int did, double[][] matrix, Map<Integer, Integer> mapReverse)   //˫��dijkstraѰ������֮�����·��
	{
	    if(s.equals(d))
	    {
	    	//System.out.println("ͬһ���ڵ�");
	    	return ":"+0;
	    }
		StringBuffer shortestpath = new StringBuffer();
	    int n = matrix.length;
	    //int s_id = s.getId(); int d_id = d.getId();
	    int s_id = sid;  int d_id = did;
	    double s_dist[] = new double[n];  //s�����е�֮�����̾���
	    int s_path[] = new int[n];    //��¼���е����s����һ���ڵ㣬û�еĻ���Ϊ-1
	    boolean s_visited[] = new boolean[n];  //��¼��s����Ƿ���ʹ��ýڵ�
	    double d_dist[] = new double[n];  //d�����е�֮�����̾���
	    int d_path[] = new int[n];    //��¼���е����d����һ���ڵ�
	    boolean d_visited[] = new boolean[n];  //��¼��d����Ƿ���ʹ��ýڵ�
	    int i,j,k,u = 0,count = 0;
	    double cost = Double.MAX_VALUE;   //����
	    
	    for(i = 0; i < n; i++)     //��ʼ��������&������
	    {
            if(matrix[s_id][i] > 0 && i != s_id)  //�����ھӣ�����Ϊ�ߵ�Ȩ��
            {
                s_dist[i] = matrix[s_id][i];
                s_path[i] = s_id;
            } 
            else          //�����ֱ������������Ϊ���MAX_VALUE
            {
                s_dist[i] = Double.MAX_VALUE;
                s_path[i] = -1;
            }
            
            if(matrix[d_id][i] > 0 && i != d_id)  //�����ھӣ�����Ϊ�ߵ�Ȩ��
            {
                d_dist[i] = matrix[d_id][i];
                d_path[i] = d_id;
            } 
            else          //�����ֱ������������Ϊ���MAX_VALUE
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
    
	    while(s_visited[d_id] == false && d_visited[s_id] == false) //��ʼ��չ��
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
        	    min = Double.MAX_VALUE;  //��չ������
        	    u = 0;
        	    for(j = 0;j < n;j++) 
        	    {
        	        if(s_visited[j] == false && s_dist[j] < min) //�ҵ���һ������Ľڵ�
        	        {
        	            min = s_dist[j];
        	            u = j;
        	        }
        	    }
        	    s_visited[u] = true;
        	    for(k = 0;k < n;k++) 
        	    {
        	        if(s_visited[k] == false && matrix[u][k] > 0 && min + matrix[u][k] < s_dist[k]) //�����½ڵ���������ڵ㵽Դ��ľ���
        	        {
        	           s_dist[k] = min + matrix[u][k];
        	           s_path[k] = u;
        	        }
        	    } 	
        	    if(d_visited[u] == false)   //��������������ڽ����
        	    {
        	        continue;
        	    }
	        }
	        else    //��չ������
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
                //��������������ڽ����
                if(s_visited[u] == false) 
                {
                    continue;
                }
	        }
	       
	        if(cost > s_dist[u] + d_dist[u])  //�����˻��Ѹ�С��·���������shortestpath
	        {
	        	int v = u;
	            StringBuffer spath = new StringBuffer();
	            StringBuffer dpath = new StringBuffer();
	            if(u == d_id) 
	            {
	                //ֻʹ���������͵õ�������·��
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
	                //˫�������õ�������·��
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
	
	public String biDijkstraNew(ArrayList<Vertex> vertexList, Vertex s, Vertex d, int sid, int did, double[][] matrix, Map<Integer, Integer> map, Map<Integer, Integer> mapReverse)   //˫��dijkstraѰ������֮�����·��
	{
	    if(s.equals(d))
	    {
	    	//System.out.println("ͬһ���ڵ�");
	    	return ":"+0;
	    }
		StringBuffer shortestpath = new StringBuffer();
	    int n = matrix.length;
	    //int s_id = s.getId(); int d_id = d.getId();
	    int s_id = sid;  int d_id = did;
	    double s_dist[] = new double[n];  //s�����е�֮�����̾���
	    int s_path[] = new int[n];    //��¼���е����s����һ���ڵ㣬û�еĻ���Ϊ-1
	    
	    double d_dist[] = new double[n];  //d�����е�֮�����̾���
	    int d_path[] = new int[n];    //��¼���е����d����һ���ڵ�
	    
	    ArrayList<Integer> openS = new ArrayList<Integer> ();  //������
		ArrayList<Integer> closeS = new ArrayList<Integer> ();  //�Ѿ�����
		ArrayList<Integer> openD = new ArrayList<Integer> ();  //������
		ArrayList<Integer> closeD = new ArrayList<Integer> ();  //�Ѿ�����
		
	    int i,j,k,u = 0,count = 0;
	    double cost = Double.MAX_VALUE;   //����
	    
	    for(i = 0; i < n; i++)     //��ʼ��������&������
	    {
            if(matrix[s_id][i] > 0 && i != s_id)  //�����ھӣ�����Ϊ�ߵ�Ȩ��
            {
                s_dist[i] = matrix[s_id][i];
                s_path[i] = s_id;
                openS.add(i);
            } 
            else          //�����ֱ������������Ϊ���MAX_VALUE
            {
                s_dist[i] = Double.MAX_VALUE;
                s_path[i] = -1;
            }
            
            if(matrix[d_id][i] > 0 && i != d_id)  //�����ھӣ�����Ϊ�ߵ�Ȩ��
            {
                d_dist[i] = matrix[d_id][i];
                d_path[i] = d_id;
                openD.add(i);
            } 
            else          //�����ֱ������������Ϊ���MAX_VALUE
            {
                d_dist[i] = Double.MAX_VALUE;
                d_path[i] = -1;
            }
        }
        s_path[s_id] = s_id;
        s_dist[s_id] = 0;
        d_path[d_id] = d_id;
        d_dist[d_id] = 0;
    
	    while(closeS.contains(d_id) == false && closeD.contains(s_id) == false) //��ʼ��չ��
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
        	    min = Double.MAX_VALUE;  //��չ������
        	    int index = 0;
        	    u = 0;
        	    for(j=0; j<openS.size(); j++) //�ҵ���С��
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
    			
    			for(int vid: vertexList.get(mapReverse.get(u)).neighborIdTypeALL)  //���������ھ�
    			{
    				if(!map.containsKey(vid)) continue;
    				k = map.get(vid);
    				if(min + matrix[u][k] < s_dist[k])	//�����½ڵ���������ڵ㵽Դ��ľ���
    				{
    					s_dist[k] = min + matrix[u][k];
    			        s_path[k] = u;
    				}
    				if(!openS.contains(k) && !closeS.contains(k))
    				{
    					openS.add(k);
    				}
    			}
    			
        	    if(!closeD.contains(u))   //��������������ڽ����
        	    {
        	        continue;
        	    }
	        }
	        else    //��չ������
	        {	            
	        	min = Double.MAX_VALUE;  //��չ������
        	    int index = 0;
        	    u = 0;
        	    for(j=0; j<openD.size(); j++) //�ҵ���С��
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
    			
    			for(int vid: vertexList.get(mapReverse.get(u)).neighborIdTypeALL)  //���������ھ�
    			{
    				if(!map.containsKey(vid)) continue;
    				k = map.get(vid);
    				if(min + matrix[u][k] < d_dist[k])	//�����½ڵ���������ڵ㵽Դ��ľ���
    				{
    					d_dist[k] = min + matrix[u][k];
    			        d_path[k] = u;
    				}
    				if(!openD.contains(k) && !closeD.contains(k))
    				{
    					openD.add(k);
    				}
    			}
    			
        	    if(!closeS.contains(u))   //��������������ڽ����
        	    {
        	        continue;
        	    }
	        }
	       
	        if(cost > s_dist[u] + d_dist[u])  //�����˻��Ѹ�С��·���������shortestpath
	        {
	        	int v = u;
	            StringBuffer spath = new StringBuffer();
	            StringBuffer dpath = new StringBuffer();
	            if(u == d_id) 
	            {
	                //ֻʹ���������͵õ�������·��
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
	                //˫�������õ�������·��
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
	    //double s_dist[] = new double[n];  //s�����е�֮�����̾���
	    //int s_path[] = new int[n];    //��¼���е����s����һ���ڵ㣬û�еĻ���Ϊ-1
	    //boolean s_visited[] = new boolean[n];  //��¼��s����Ƿ���ʹ��ýڵ�
	    //double d_dist[] = new double[n];  //d�����е�֮�����̾���
	    //int d_path[] = new int[n];    //��¼���е����d����һ���ڵ�
	    //boolean d_visited[] = new boolean[n];  //��¼��d����Ƿ���ʹ��ýڵ�
	    int i,j,k,u = 0,count = 0;
	    double cost = Float.MAX_VALUE;   //����
	    
	    for(i = 0; i < n; i++)     //��ʼ��������&������
	    {
            if(matrixA.get(s_id).containsKey(i) && i != s_id)  //�����ھӣ�����Ϊ�ߵ�Ȩ��
            {
            	s_dist.add(matrixA.get(s_id).get(i));
                s_path.add(s_id);
            } 
            else          //�����ֱ������������Ϊ���MAX_VALUE
            {
            	s_dist.add(Float.MAX_VALUE);
                s_path.add(-1);
            }
			s_visited.add(false);
            
            if(matrixA.get(d_id).containsKey(i) && i != d_id)  //�����ھӣ�����Ϊ�ߵ�Ȩ��
            {
            	d_dist.add(matrixA.get(d_id).get(i));
                d_path.add(d_id);
            } 
            else          //�����ֱ������������Ϊ���MAX_VALUE
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
    
	    while(s_visited.get(d_id) == false && d_visited.get(s_id) == false) //��ʼ��չ��
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
        	    min = Float.MAX_VALUE;  //��չ������
        	    u = 0;
        	    for(j = 0;j < n;j++) 
        	    {
        	        if(s_visited.get(j) == false && s_dist.get(j) < min) //�ҵ���һ������Ľڵ�
        	        {
        	        	min = s_dist.get(j);
        	            u = j;
        	        }
        	    }
        	    s_visited.set(u, true);
        	    for(k = 0;k < n;k++) 
        	    {
        	        if(s_visited.get(k) == false && matrixA.get(u).containsKey(k) && (min+matrixA.get(u).get(k)<s_dist.get(k))) //�����½ڵ���������ڵ㵽Դ��ľ���
        	        {
        	        	s_dist.set(k, min + matrixA.get(u).get(k));
     				    s_path.set(k, u);
        	        }
        	    } 	
        	    if(d_visited.get(u) == false)   //��������������ڽ����
        	    {
        	        continue;
        	    }
	        }
	        else    //��չ������
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
                //��������������ڽ����
                if(s_visited.get(u) == false) 
                {
                    continue;
                }
	        }
	       
	        if(cost > s_dist.get(u) + d_dist.get(u))  //�����˻��Ѹ�С��·���������shortestpath
	        {
	        	int v = u;
	            StringBuffer spath = new StringBuffer();
	            StringBuffer dpath = new StringBuffer();
	            if(u == d_id) 
	            {
	                //ֻʹ���������͵õ�������·��
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
	                //˫�������õ�������·��
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
		MakeGraph mg = new MakeGraph();   //�ʼ����ݼ�
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		ArrayList<Vertex> vertexList = mg.getVertex();
		
		common cn = new common();
		cn.calculateShortestPath(vertexList);
	}*/
}

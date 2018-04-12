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
{ //����A*�㷨
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //ԭ���㼯��
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //�߼���
	public static ArrayList<Vertex> landMark = new ArrayList<Vertex>();  //�ر꼯��
	
	public static ArrayList<ArrayList<Double>> shortestPathfromCb(ArrayList<ArrayList<Double>> vectorbyCb)    //��ÿ���ڵ��õر��ʾ
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
	
	public static float getH(int vid1, int vid2, ArrayList<ArrayList<Float>> vectorbyCb) //�������㣬���ع���ľ���
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
	
	public static String ALT2(ArrayList<Vertex> vertexList, Vertex s, Vertex d, ArrayList<ArrayList< Float>> vectorbyCb)  //��ͨDijkstra
	{
		ArrayList<Integer> open = new ArrayList<Integer> ();  //������
		ArrayList<Integer> close = new ArrayList<Integer> ();  //�Ѿ�����
		int n = vertexList.size();
		HashMap<Integer, HashMap<Integer,  Float>> matrixA = new HashMap<Integer, HashMap<Integer,  Float>>();
		//double matrix[][] = new double[n][n];		//�ڽӾ���ĳ�ʼ��	    
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
		//double s_dist[] = new double[n];  //s�����е�֮�����̾���
	    //int s_path[] = new int[n];    //��¼���е����s����һ���ڵ㣬û�еĻ���Ϊ-1
	    //double estimateTod[] = new double[n];    //���ƾ���d�ľ���
		
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
			estimateTod.add(getH(i, d_id, vectorbyCb));
		}
		
		while(open.size()!=0)
		{
			float min = Float.MAX_VALUE;  //��չ������
			float minAll = Float.MAX_VALUE;
			int index = 0;
			u = -1;
			for(j=0; j<open.size(); j++) //�ҵ���С��
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
		    	break;  //ʣ������еĽڵ㶼���ɴ���
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
	        
			for(int vid: vertexList.get(u).neighborIdTypeALL)  //���������ھ�
			{
				if(min + matrixA.get(u).get(vid) < s_dist.get(vid))	//�����½ڵ���������ڵ㵽Դ��ľ���
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
	
	/*public static String biALT(Vertex s, Vertex d, ArrayList<ArrayList<Double>> vectorbyCb)  //˫��Dijkstra
	{
		int n = vertexList.size();
		double matrix[][] = new double[n][n];		//�ڽӾ���ĳ�ʼ��	    
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
	    double s_dist[] = new double[n];  //s�����е�֮�����̾���
	    int s_path[] = new int[n];    //��¼���е����s����һ���ڵ㣬û�еĻ���Ϊ-1
	    boolean s_visited[] = new boolean[n];  //��¼��s����Ƿ���ʹ��ýڵ�
	    double d_dist[] = new double[n];  //d�����е�֮�����̾���
	    int d_path[] = new int[n];    //��¼���е����d����һ���ڵ�
	    boolean d_visited[] = new boolean[n];  //��¼��d����Ƿ���ʹ��ýڵ�
	    double estimateTod[] = new double[n];    //���ƾ���d�ľ���
	    double estimateTos[] = new double[n];    //���ƾ���s�ľ���
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
            estimateTod[i] = getH(i, d_id, vectorbyCb);
            estimateTos[i] = getH(i, s_id, vectorbyCb);
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
        	    min = Double.MAX_VALUE;  //��չ������
        	    minAll = Double.MAX_VALUE;  //��չ������
        	    u = 0;
        	    for(j = 0;j < n;j++) 
        	    {
        	        if(s_visited[j] == false && s_dist[j] + estimateTod[j] < minAll) //�ҵ���һ������Ľڵ�
        	        {
        	            min = s_dist[j];
        	            minAll = s_dist[j] + estimateTod[j];
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
    	                spath.insert(0,"," + v);
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
	
	public static int minF(HashMap<Integer, Double> open)  //����f(n)��С�Ľڵ�
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
		Date start = new Date(); //��ʱ
		common cn = new common();
		landMark = brandesForCentrality.TopCb(vertexList); //���������ԣ�ѡ��ر�20��
		cn.calculateShortestPath(vertexList, vertexList, landMark); //����ÿ���ڵ㵽�ر����̾���
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
		//double[][] vectorbyCb = new double[vertexList.size()][landMark.size()];  //��ÿ���ڵ��õر��ʾ
		vectorbyCb = shortestPathfromCb(vectorbyCb);    //��ÿ���ڵ��õر��ʾ
		Date end = new Date();
		System.out.println("ALTԤ����"+ (end.getTime() - start.getTime()) + " total milliseconds");

		//***********************************���м���д��***************************************
		String pathname = "C:/Users/17375/Desktop/�м���/ALT.txt";
		File writename = new File(pathname); 
		writename.createNewFile(); // �������ļ�  
        BufferedWriter out = new BufferedWriter(new FileWriter(writename));  
		String writeIn = "";   
		for(int i=0; i<vectorbyCb.size(); i++)
		{
			ArrayList<Double> line = vectorbyCb.get(i);
			for(int j=0; j<vectorbyCb.get(0).size(); j++)
			{
				writeIn += line.get(j);
			}
			out.write(writeIn); // \r\n��Ϊ����  
	        out.flush(); // �ѻ���������ѹ���ļ�  
	        writeIn = "";
		}
        out.close(); // ���ǵùر��ļ�  
		
		return vectorbyCb;
	}
	
	/*public static void main(String[] args) throws SQLException, ParseException
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
		landMark = brandesForCentrality.TopCb(vertexList); //���������ԣ�ѡ��ر�20��
		cn.calculateShortestPath(vertexList, vertexList, landMark); //����ÿ���ڵ㵽�ر����̾���
		double[][] vectorbyCb = new double[vertexList.size()][landMark.size()];  //��ÿ���ڵ��õر��ʾ
		vectorbyCb = shortestPathfromCb(vectorbyCb);    //��ÿ���ڵ��õر��ʾ
		
		Vertex s = vertexList.get(74);
		Vertex d = vertexList.get(673);
		/*start = new Date(); //��ʱ
		ALT(s,d,vectorbyCb);
		end = new Date();
		System.out.println("ALT��"+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		start = new Date(); //��ʱ
		ALT2(s,d,vectorbyCb);
		end = new Date();
		System.out.println("ALT2��"+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		//System.out.println(str);
		
		cn.calculateShortestPath(vertexList, vertexList, s, d);
	}*/
}

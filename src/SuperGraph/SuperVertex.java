package SuperGraph;

import graph.MakeGraph;
import graph.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class SuperVertex 
{//超级节点中包含着多个Vertex
	public int cid = 0; //顶点编号
	public int kid = 0; //核心节点编号（对应某个Vertex，虚拟核心节点这个并不存在）
	public HashSet<Integer> containVertex = new HashSet<Integer>();  //社团中包含的顶点编号
	public HashSet<Integer> coreVertex = new HashSet<Integer>();  //社团中包含的核心顶点编号
	public HashSet<Integer> outlierVertex = new HashSet<Integer>();  //社团中包含的外部顶点编号
	public HashSet<Integer> outVertex = new HashSet<Integer>();  //社团中包含的顶点编号---外部顶点
	public HashSet<Integer> hubVertex = new HashSet<Integer>();  //社团中包含的hub顶点编号
	public HashSet<SuperEdge> edge = new HashSet<SuperEdge>(); //边集合
	public HashSet<Integer> neighborId = new HashSet<Integer>();  //相连邻居节点Id
	
	public HashSet<Integer> reachId = new HashSet<Integer>();  //传递闭包
	public HashMap<Integer,Double> shortestPath = new HashMap<Integer,Double>();   //到其他节点的最短路径
	public HashMap<Integer,String> shortestPath2 = new HashMap<Integer,String>();   //到其他节点的最短路径
	public HashMap<Integer,Double> Diff = new HashMap<Integer,Double>();   //到其他节点的最长边-最短边
	public HashMap<Integer,Integer> landmark = new HashMap<Integer,Integer>();   //<id1,id2>记录面向id1的超点的地标-- id2=landmark(cid,id1)
	
	public Integer parentId;  
	
	public SuperVertex(int id) //构造函数
	{
		this.cid = id;
	}
	
	public HashSet<Integer> isInternalReachable(ArrayList<Vertex> vertexList) //判断SuperVertex是否内部可达
	{ 
		boolean flag = true;
		boolean arr[][] = new boolean[containVertex.size()][containVertex.size()];
		int arr2[] = new int[containVertex.size()];
	    Map<Integer,Integer> VertexArray = new HashMap<Integer,Integer>();
	    Map<Integer,Integer> VertexArray2 = new HashMap<Integer,Integer>();
	    int count = 0;
	    for(int itVertex:containVertex)
	    {
	    	VertexArray.put(itVertex,count);
	        VertexArray2.put(count++, itVertex);
	    }
	    for(int i = 0;i < vertexList.size();i++) {  //初始化矩阵
	        if(containVertex.contains(i)) 
	        {
	        	for(int tempid: vertexList.get(i).neighborIdTypeALL)
	            {
	                if(containVertex.contains(tempid)) 
	                {
            	        arr[VertexArray.get(i)][VertexArray.get(tempid)] = true;
            	        arr[VertexArray.get(tempid)][VertexArray.get(i)] = true;
	                }
	            }
	        }
	    }
	    
	    for(int i = 0;i < containVertex.size();i++) 
	    { //列
	        for(int j = 0;j < containVertex.size();j++) 
	        {  //行
	            for(int k = 0;k < containVertex.size();k++) 
	            {  //每行中的列
	                arr[j][k] = arr[j][k] || (arr[j][i] && arr[i][k]);
	            }
	        }
	    }
	    
	    for(int i =0 ;i<containVertex.size();i++)
	    {
	    	arr2[i] = 0;
	    }
	    for(int i = 0;i < containVertex.size();i++) 
	    {
	        for(int j = 0;j < containVertex.size();j++) 
	        {
	            if(arr[i][j] == false) 
	            {
	                arr2[i]++;
	            	flag = false;
	            }
	        }
	    }
	    
	    HashSet<Integer> vList = new HashSet<Integer>();
	    while(flag == false)
	    {
	    	flag = true;
	    	int maxIndex = 0;
	    	for(int i =1 ;i<containVertex.size();i++)
		    {
	    		if(arr2[i]>arr2[maxIndex])
	    		{
	    			maxIndex = i;    //找到最大的
	    		}
		    }
	    	vList.add(VertexArray2.get(maxIndex));
	    	for(int i = 0;i < containVertex.size();i++) 
		    {
		        arr[maxIndex][i] = true;
		        arr[i][maxIndex] = true;
		        arr2[i] = 0;
		    }
	    	for(int i = 0;i < containVertex.size();i++) 
		    {
		        for(int j = 0;j < containVertex.size();j++) 
		        {
		            if(arr[i][j] == false) 
		            {
		                arr2[i]++;
		            	flag = false;
		            }
		        }
		    }
	    }
	    return vList;
	}
}

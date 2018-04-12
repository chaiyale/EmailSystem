package SuperGraph;

import graph.MakeGraph;
import graph.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class SuperVertex 
{//�����ڵ��а����Ŷ��Vertex
	public int cid = 0; //������
	public int kid = 0; //���Ľڵ��ţ���Ӧĳ��Vertex��������Ľڵ�����������ڣ�
	public HashSet<Integer> containVertex = new HashSet<Integer>();  //�����а����Ķ�����
	public HashSet<Integer> coreVertex = new HashSet<Integer>();  //�����а����ĺ��Ķ�����
	public HashSet<Integer> outlierVertex = new HashSet<Integer>();  //�����а������ⲿ������
	public HashSet<Integer> outVertex = new HashSet<Integer>();  //�����а����Ķ�����---�ⲿ����
	public HashSet<Integer> hubVertex = new HashSet<Integer>();  //�����а�����hub������
	public HashSet<SuperEdge> edge = new HashSet<SuperEdge>(); //�߼���
	public HashSet<Integer> neighborId = new HashSet<Integer>();  //�����ھӽڵ�Id
	
	public HashSet<Integer> reachId = new HashSet<Integer>();  //���ݱհ�
	public HashMap<Integer,Double> shortestPath = new HashMap<Integer,Double>();   //�������ڵ�����·��
	public HashMap<Integer,String> shortestPath2 = new HashMap<Integer,String>();   //�������ڵ�����·��
	public HashMap<Integer,Double> Diff = new HashMap<Integer,Double>();   //�������ڵ�����-��̱�
	public HashMap<Integer,Integer> landmark = new HashMap<Integer,Integer>();   //<id1,id2>��¼����id1�ĳ���ĵر�-- id2=landmark(cid,id1)
	
	public Integer parentId;  
	
	public SuperVertex(int id) //���캯��
	{
		this.cid = id;
	}
	
	public HashSet<Integer> isInternalReachable(ArrayList<Vertex> vertexList) //�ж�SuperVertex�Ƿ��ڲ��ɴ�
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
	    for(int i = 0;i < vertexList.size();i++) {  //��ʼ������
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
	    { //��
	        for(int j = 0;j < containVertex.size();j++) 
	        {  //��
	            for(int k = 0;k < containVertex.size();k++) 
	            {  //ÿ���е���
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
	    			maxIndex = i;    //�ҵ�����
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

package reachQuery;

import graph.Edge;
import graph.MakeGraph;
import graph.Vertex;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import dataProcess.proDataset;

public class DFSReachQuery 
{//ʹ��DFS��������ͼ��������ͨ����������ͬһ��ͨ������֮��˴˻�ͨ
	public static boolean[] isVisited;  //��¼ÿ���ڵ��Ƿ���ʹ�
	public static int[] parent;  //��¼ÿ���ڵ��Ƿ���ʹ�
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //ԭ���㼯��
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //�߼���
	public static int superVertex_Id = 0; //����ID
	
	public static void dfs(Vertex u)   //������ȱ���---�ݹ�
	{
		isVisited[u.getId()] = true;  
		//u.SuperVId.add(superVertex_Id);
		//System.out.println(u.getId()+":  "+superVertex_Id);
		//parent[u.getId()] = superVertex_Id;
		for(int vid:u.neighborIdTypeALLbySort)
		{
			if(!isVisited[vid])
			{
				Vertex v = vertexList.get(vid);
				dfs(v);
			}
		}
	}
	
	public static void DFS(ArrayList<Vertex> vList,  ArrayList<Edge> eList)
	{
		vertexList = vList;
		edgeList = eList;
		int n = vertexList.size();
		isVisited = new boolean[n];
		parent = new int[n];
		for(int i=0; i<n; i++)
		{
			isVisited[i] = false;  //��ʼ��Ϊδ����
		}
		
		for(Vertex u:vertexList)
		{
			if(!isVisited[u.getId()])  //����uû�б�����
			{
				dfs(u);
				superVertex_Id ++;
			}
		}
	}
	
	public static void main(String[] args) throws SQLException, ParseException
	{
		Date start = new Date(); //��ʱ
		proDataset pd = new proDataset();  //�������ݼ�
		String filePath = "./Cit-HepTh.txt";
		pd.readTxtFile(filePath);
		pd.makeGraph();
		vertexList = pd.getVertex();
		edgeList = pd.getEdge(); 
	
		/*Date start = new Date(); //��ʱ
		MakeGraph mg = new MakeGraph();   
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		vertexList = mg.getVertex();
		edgeList = mg.getEdge();
		Date end = new Date();
		System.out.println("���ʼ����ݼ�ת��Ϊͼ��"+ (end.getTime() - start.getTime()) + " total milliseconds"); */
		
		start = new Date(); //��ʱ
		int n = vertexList.size();
		isVisited = new boolean[n];
		for(int i=0; i<n; i++)
		{
			isVisited[i] = false;  //��ʼ��Ϊδ����
		}
		
		for(Vertex u:vertexList)
		{
			if(!isVisited[u.getId()])  //����uû�б�����
			{
				dfs(u);
				superVertex_Id ++;
			}
		}
		Date end = new Date();
		System.out.println("DFS������ͨ������"+ (end.getTime() - start.getTime()) + " total milliseconds"); 
	}
}

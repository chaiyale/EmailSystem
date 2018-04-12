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
{//使用DFS计算无向图的所有连通分量，处于同一连通分量的之间彼此互通
	public static boolean[] isVisited;  //记录每个节点是否访问过
	public static int[] parent;  //记录每个节点是否访问过
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //原顶点集合
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //边集合
	public static int superVertex_Id = 0; //顶点ID
	
	public static void dfs(Vertex u)   //深度优先遍历---递归
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
			isVisited[i] = false;  //初始化为未访问
		}
		
		for(Vertex u:vertexList)
		{
			if(!isVisited[u.getId()])  //顶点u没有被访问
			{
				dfs(u);
				superVertex_Id ++;
			}
		}
	}
	
	public static void main(String[] args) throws SQLException, ParseException
	{
		Date start = new Date(); //计时
		proDataset pd = new proDataset();  //测试数据集
		String filePath = "./Cit-HepTh.txt";
		pd.readTxtFile(filePath);
		pd.makeGraph();
		vertexList = pd.getVertex();
		edgeList = pd.getEdge(); 
	
		/*Date start = new Date(); //计时
		MakeGraph mg = new MakeGraph();   
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		vertexList = mg.getVertex();
		edgeList = mg.getEdge();
		Date end = new Date();
		System.out.println("将邮件数据集转化为图："+ (end.getTime() - start.getTime()) + " total milliseconds"); */
		
		start = new Date(); //计时
		int n = vertexList.size();
		isVisited = new boolean[n];
		for(int i=0; i<n; i++)
		{
			isVisited[i] = false;  //初始化为未访问
		}
		
		for(Vertex u:vertexList)
		{
			if(!isVisited[u.getId()])  //顶点u没有被访问
			{
				dfs(u);
				superVertex_Id ++;
			}
		}
		Date end = new Date();
		System.out.println("DFS计算连通分量："+ (end.getTime() - start.getTime()) + " total milliseconds"); 
	}
}

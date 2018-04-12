package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Vertex 
{
	private int id = 0; //顶点编号
	private String name = ""; //邮件地址
	public HashSet<Edge> edge = new HashSet<Edge>(); //边集合
	public HashSet<Integer> neighborIdType1 = new HashSet<Integer>();  //实线相连邻居节点Id
	public HashSet<Integer> neighborIdTypeALL = new HashSet<Integer>();  //所有相连邻居节点Id
	public ArrayList<Integer> neighborIdTypeALLbySort = new ArrayList<Integer>();  //所有相连邻居节点Id
	public HashSet<Integer> neighborIdSPT = new HashSet<Integer>();  //SPT中邻居节点Id
	
	public int centrality;  //中心性
	public double centralityBrandes;  //中心性
	public HashMap<Integer, Integer> communityDistribution = new HashMap<Integer, Integer>(); //标签，key为标签id,value为该标签在此点上所出现的次数
	public HashSet<Integer> communityId = new HashSet<Integer>();  //所属的社团id
	public HashSet<Integer> triangles = new HashSet<Integer>();//该节点在哪些三角形中	
	public HashSet<Integer> SuperVId = new HashSet<Integer>();  //所属的超级节点id
	
	public int sd = 0; //similar_degree
	public int ed; //effective_degree
	public double weight = 0;  //edge的weight之和
	public int type = 0;  //type为0是一般节点，type为-2是outlier，type为-1是hub
	public HashSet<Integer> reachId = new HashSet<Integer>();  //相连邻居节点Id
	
	public HashMap<Integer,String> shortestPath = new HashMap<Integer,String>();   //到其他节点的最短路径
	
	public Vertex(int Id) //构造函数
	{
		this.id = Id;
	}
	
	public Vertex(int Id, String Name) //构造函数
	{
		this.id = Id;
		this.name = Name;
	}
	
	public int getId() //返回ID
	{
		return this.id;
	}
	
	public String getName() //返回邮件地址，即顶点名称
	{
		return this.name;
	}
}

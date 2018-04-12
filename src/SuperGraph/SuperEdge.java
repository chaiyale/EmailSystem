package SuperGraph;

import graph.Edge;
import graph.Vertex;

import java.util.ArrayList;
import java.util.HashSet;

public class SuperEdge 
{
	public int ceid;   //顶点编号
	public int cid1 = 0;  //甲方编号
	public int cid2 = 0;  //乙方编号
	
	public HashSet<Edge> cid1Withcid2 = new HashSet<Edge>(); 
	public HashSet<Integer> Incid1 = new HashSet<Integer>();    //在cid1内部的节点
	public HashSet<Integer> Incid2 = new HashSet<Integer>();    //在cid2内部的节点
	public double weight = 0.0;  //权重
	public double cost;  //耗费
	
	public SuperEdge(int ceid ,int cid1, int cid2) //构造函数
	{
		this.ceid = ceid;
		this.cid1 = cid1;
		this.cid2 = cid2;
	}
}

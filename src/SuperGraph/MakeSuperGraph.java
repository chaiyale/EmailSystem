package SuperGraph;

import graph.*;
import community.*;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import reachQuery.DFSReachQuery;
import reachQuery.NormalReachQuery;
import shortPathQuery.ComparativeExperiment;
import shortPathQuery.brandesForCentrality;
import shortPathQuery.common;
import shortPathQuery.commonForSuper;
import dataProcess.proDataset;
import dataProcess.proDatasetWeighted;
import dataProcess.proDatasetWeighted2;
import database.dataAccess;

public class MakeSuperGraph 
{
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //原顶点集合
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //边集合
	public static ArrayList<SuperVertex> superVertexList;  //超级节点集合
	public static ArrayList<SuperEdge> superEdgeList; //边集合
	public static int superVertex_Id = 0; //顶点ID
	public static int superEdge_Id = 0;   //边ID
	public static Map<Integer, Integer> map; //cid和原始社团id的对应关系
	
	static ArrayList<SuperVertex> chaiSuperVertex;  //需要拆分的超级节点集合
	static ArrayList<SuperVertex> heSuperVertex;  //需要合并的超级节点集合
	static ArrayList<Integer> singleSuperVertex;  //只有一个节点的超级节点集合
	static int labelNum = 0;
	static int itertime = 0;
	
	public static boolean[] isVisited;  //记录每个节点是否访问过
	public static int[] connectId;   //顶点i属于第几个连通分量  
	public static int connectCount = 0;//连通分量数目  
	public static double[] Cb;   //中心性
	
	public static SuperVertex containVertex(int orginalId) //顶点集合中是否包含
	{
		for(SuperVertex v:superVertexList)
		{
			if(map.get(v.cid) == orginalId)
			{
				return v;
			}
		}
		return null;
	}
	
	public static SuperVertex addVertex(int orginalId)  //增加顶点
	{
		SuperVertex v = new SuperVertex(superVertex_Id);
		superVertexList.add(v);
		map.put(superVertex_Id, orginalId);
		superVertex_Id++;
		return v;
	}
	
	public static SuperEdge containEdge(int from, int to)  //边集合中是否包含
	{
		for(SuperEdge e:superEdgeList)
		{
			if((e.cid1 == from && e.cid2 == to) || (e.cid2 == from && e.cid1 == to))
			{
				return e;
			}
		}
		return null;
	}
	
	/**
	 * XXX
	 * @param from XXX
	 * @param to xxx
	 * @return xx
	 */
	public static SuperEdge addEdge(int from, int to)  //增加边
	{
		SuperEdge e = new SuperEdge(superEdge_Id, from, to);
		superEdgeList.add(e);
		superEdge_Id++;
		return e;
	}
	
	public ArrayList<SuperVertex> makeGraph(ArrayList<Vertex> vertexList)
	{
		Date start = new Date(); //计时

		superVertexList = new ArrayList<SuperVertex>();  //超级节点集合
		superEdgeList = new ArrayList<SuperEdge>(); 
		superVertex_Id = 0; //顶点ID
		superEdge_Id = 0;
		map = new HashMap<Integer, Integer>(); //cid和原始社团id的对应关系
		for(Vertex v:vertexList)
		{
			v.SuperVId.clear();
			for(Edge e:v.edge)
			{
				e.visited = false;
			}
		}
		
		Iterator<Vertex> iter1 = vertexList.iterator();
		while(iter1.hasNext())  //遍历每个顶点
		{
			Vertex v = iter1.next();
			int vid = v.getId();
			for(int id:v.communityId)  //对于其每个所属社团
			{
				//如果该社团不存在，则添加；否则，把该节点加入这个社团
				SuperVertex c;
				if(id == -1 || (c=containVertex(id))==null)
				{
					c=addVertex(id);
				}
				c.containVertex.add(vid);
				if(v.type == -2)
				{
					c.outlierVertex.add(vid);
				}
				else if(v.type == -1)
				{
					c.hubVertex.add(vid);
				}
				else
				{
					c.coreVertex.add(vid);
				}
				v.SuperVId.add(c.cid);
			}
		}
		
		for(SuperVertex c1: superVertexList)
		{
			for(int vid:c1.containVertex)    //查看其包含的点所指向的社团
			{
				Vertex v = vertexList.get(vid);
				if(v.type ==-2)
				{
					continue;
				}
				for(Edge e:v.edge)
				{
					if(e.visited == true)   //已经考察过这个节点了
					{
						continue;
					}
					int nid1 = e.nid1;
					int nid2 = e.nid2;
					int v2id = (v.getId() == nid1)?nid2:nid1;
					Vertex v2 = vertexList.get(v2id);
					if(v2.type ==-2)
					{
						continue;
					}
					for(int SVid:v2.SuperVId)   //所在的超级节点
					{
						if(SVid == c1.cid)   //指向自己
						{
							continue;
						}
						SuperEdge se;
						if((se=containEdge(c1.cid, SVid))==null)
						{
							se = addEdge(c1.cid, SVid);
						}
						se.cid1Withcid2.add(e);
						se.Incid1.add(vid);
						se.Incid2.add(v2id);
						superVertexList.get(c1.cid).edge.add(se);
						superVertexList.get(SVid).edge.add(se);
						superVertexList.get(c1.cid).outVertex.add(vid);
						superVertexList.get(SVid).outVertex.add(v2id);   //外部节点加上
						c1.neighborId.add(SVid);
						SuperVertex c2 = superVertexList.get(SVid);
						c2.neighborId.add(c1.cid);
						e.visited = true;
					}
				}
			}
		}
		
		Date end = new Date();
		return superVertexList;
		//System.out.println(end.getTime() - start.getTime() + " total milliseconds"); 
	}
	
	public Boolean followUp(int avg) throws ParseException    //后续处理
	{
		Boolean flag = false;
		//chaiSuperVertex = new ArrayList<SuperVertex>();  //需要拆分的超级节点集合
		heSuperVertex = new ArrayList<SuperVertex>();  //需要合并的超级节点集合
		singleSuperVertex = new ArrayList<Integer>();  //需要合并的超级节点集合
		Iterator<SuperVertex> iter1 = superVertexList.iterator();
		int count1 = 0, count2 = 0; int max = 0;
		while(iter1.hasNext())
		{
			SuperVertex v = iter1.next();
			if(v.containVertex.size()>1)
			{
				//判断一下每个社团中每个节点是否互相连通
				if(v.containVertex.size()>max)
				{
					max= v.containVertex.size();
				}
				if(v.containVertex.size()>3*avg)  //大于n倍的平均社团大小时，进行拆分
				{
					chaiFen(v);
					flag = true;
				}
				if(v.containVertex.size()<0.1*avg)  //小于n倍的平均社团大小时，进行合并
				{
					if(!heSuperVertex.contains(v))
					    heSuperVertex.add(v);
					flag = true;
				}
				count1++;
			}
			else if(v.containVertex.size()==1)
			{
				if(!heSuperVertex.contains(v))
				    heSuperVertex.add(v);
				if(!singleSuperVertex.contains(v))
				    singleSuperVertex.add(v.cid);
				flag = true;
				count2++;
			}
		}
		heBing();
		System.out.println(count1+",  "+count2+"  ,"+max);
		return flag;
	}
	
	public HashSet<Integer> singleVertex()   //单独的节点处理
	{
		HashSet<Integer> skep = new HashSet<Integer>();
		HashSet<Integer> hubs = new HashSet<Integer>();
		for(int svid:singleSuperVertex)
		{
			SuperVertex sv = superVertexList.get(svid);
			if(skep.contains(svid) || sv.containVertex.size()>1)
			{
				continue;
			}
			/*if(sv.containVertex.contains(343))
			{
				System.out.println(sv.cid);
			}*/
			int vid = sv.containVertex.iterator().next();
			Vertex v= vertexList.get(vid);
			v.communityId.clear();
			boolean flag = false;
			if(v.neighborIdTypeALL.size()>1)   //hub
			{
				for(int v2id:v.neighborIdTypeALL)
				{
					Vertex v2 = vertexList.get(v2id);
					for(int SVid:v2.SuperVId)   //所在的超级节点
					{
						SuperVertex sv2 = superVertexList.get(SVid);
						sv2.containVertex.add(v.getId());
						int comid = map.get(SVid);
						if(comid == 1658)
						{
							System.out.println();
						}
						v.communityId.add(comid);
						skep.add(SVid);
					}
				}
				if(v.communityId.size()>1)
				{
					v.type = -1;
					hubs.add(v.getId());
				}
			}
			else             //outlier
			{
				int v2id = v.neighborIdTypeALL.iterator().next();
				Vertex v2 = vertexList.get(v2id);
				for(int SVid:v2.SuperVId)   //所在的超级节点
				{
					SuperVertex sv2 = superVertexList.get(SVid);
					if(sv2.containVertex.size()>1)   //不是小社团
					{
						int comid = map.get(SVid);
						sv2.containVertex.add(v.getId());
						v.communityId.add(comid);
						v.type = -2;
						flag = true;
						skep.add(sv2.cid);
					}
				}
				if(flag==false)
				{
					for(int SVid:v2.SuperVId)   //所在的超级节点
					{
						SuperVertex sv2 = superVertexList.get(SVid);
						sv2.containVertex.add(v.getId());
						int comid = map.get(SVid);
						v.communityId.add(comid);
						skep.add(sv2.cid);
					}
				}
			}
		}
		return hubs;
	}
	
	public void chaiFen(SuperVertex SV) throws ParseException   //将超大社团进行拆分
	{
		//将其中包含的节点再次进行社团划分
		vertexList = SLPA.getVertex2(vertexList, labelNum, SV);
		//modularity.modularityQ(SV, vertexList);    //社团模块度计算
		labelNum += SV.containVertex.size();
	}
	 
	public void heBing()                  //将联系紧密且小的社团进行合并
	{
		ArrayList<Integer> skep = new ArrayList<Integer>();
		for(int i=0; i<heSuperVertex.size()-1; i++)
		{
			SuperVertex c1 = heSuperVertex.get(i);
			if(skep.contains(c1.cid))
			{
				continue;
			}
			for(int j=i+1; j<heSuperVertex.size(); j++)
			{
				boolean hebing = false;
				SuperVertex c2 = heSuperVertex.get(j);
				/*if(c2.containVertex.contains(20))
				{
					System.out.println();
				}*/
				if(intersect(c1.containVertex, c2.containVertex)!=null)   //有共同节点
				{
					hebing = true;//合并
				}
				else    //相连接的点大于等于二分之一的点的并集，合并
				{
					for(SuperEdge se:c1.edge)
					{
						int cid1 = se.cid1;
						int cid2 = se.cid2;
						int c2id = (c1.cid == cid1)?cid2:cid1;
						if(c2id == c2.cid)
						{
							if(se.cid1Withcid2.size()>= (int)(0.5*(c1.containVertex.size()*c2.containVertex.size())) && se.cid1Withcid2.size()>=1)
							{
								hebing = true;
								break;
							}
						}
					}
				}
				
				
				if(hebing == true)   //满足上述条件，合并
				{
					int comid = map.get(c1.cid);
					for(int vid:c2.containVertex)   //其中一个所有顶点的编号转成另外一个
					{
						vertexList.get(vid).communityId.clear();
						vertexList.get(vid).communityId.add(comid);
					}
					skep.add(c2.cid);
					continue;
				}
			}
		}
	}
	
	public void heBing2(SuperVertex sv1, SuperVertex sv2)   
	{
		int comid = map.get(sv2.cid);
		for(int vid:sv1.containVertex)
		{
			vertexList.get(vid).communityId.clear();
			vertexList.get(vid).communityId.add(comid);
		}
	}
	
	public void heBing3(Vertex v, HashSet<Integer> vList)
	{
		int minIndex = -1;   int minSize = Integer.MAX_VALUE;
		for(int v2id:v.neighborIdTypeALL)
		{
			if(vList.contains(v2id))
			{
				continue;
			}
			Vertex v2 = vertexList.get(v2id);
			for(int SVid:v2.SuperVId)   //所在的超级节点
			{
				SuperVertex sv = superVertexList.get(SVid);
				if(sv.containVertex.size()<minSize)
				{
					minIndex = SVid;
					minSize = sv.containVertex.size();
				}
			}
		}
		if(minIndex == -1)
		{
			v.communityId.clear();
			v.communityId.add(labelNum++);
		}
		else
		{
			int comid = map.get(minIndex);
			v.communityId.clear();
			v.communityId.add(comid);
		}
	}
	
	public HashSet<Integer> intersect(HashSet<Integer> list1,HashSet<Integer> list2) //如果有交集，则返回true
	{
		HashSet<Integer> a1 = new HashSet<Integer>(list1);
		HashSet<Integer> a2 = new HashSet<Integer>(list2);
		if(!(a1.isEmpty()&& a2.isEmpty()))//全不为空
		{
			a1.retainAll(a2);
			if(!a1.isEmpty())
			{
				return a1;
			}
		}
		return null;
	}
	
	public Boolean computeWeightPlan2()    //计算边的权重，也就是虚拟中心节点之间的距离：分为两部分，一部分是社团间接口点之间的平均距离，另一部分是社团内节点到接口点的平均距离
	{//由两部分组成，一部分是社团间的平均最短距离（d1），一部分是社团内部平均的最短距离的一半（d2,d3）。
		boolean reMake = false;
		for(SuperEdge se:superEdgeList)
		{
			SuperVertex sv1 = superVertexList.get(se.cid1);
			SuperVertex sv2 = superVertexList.get(se.cid2);
			
			double sum = 0.0;  
			for(Edge e:se.cid1Withcid2) 
			{
				sum += e.weight;    //计算社团间存在边的权重，权重跟距离成反比
			}
			//se.weight += sum/(se.Incid1.size()*se.Incid2.size());
			se.weight += sum/se.cid1Withcid2.size();
			
			int notIn1 = 0;   //在社团内部而不在边界的点个数
			double sumW = 0.0;   int num = 0;
			for(int vid:sv1.coreVertex)    //计算社团内部之间到边界点的平均距离
			{
				Vertex v1 = vertexList.get(vid);
				if(!se.Incid1.contains(vid))
				{
					notIn1 ++;
					for(int v2id:se.Incid1)   //计算到每个边界点的距离
					{
						if(v1.neighborIdTypeALL.contains(v2id))
						{
							for(Edge e:v1.edge)
							{
								if(e.nid1 == v2id || e.nid2 == v2id)
								{
									sumW += e.weight;
									num ++;
									break;
								}
							}
						}
					}
				}
			}
			if(notIn1 == 0)
			{
				heBing2(sv1,sv2);
				reMake = true;
			}
			else
			{
				//se.weight += sumW/(notIn1*se.Incid1.size());
				se.weight += sumW/num;
			}
			
			int notIn2 = 0;   //在社团内部而不在边界的点个数
			sumW = 0.0; num =0 ;
			for(int vid:sv2.coreVertex)    //计算社团内部之间到边界点的平均距离
			{
				Vertex v1 = vertexList.get(vid);
				if(!se.Incid2.contains(vid))
				{
					notIn2 ++;
					for(int v2id:se.Incid2)   //计算到每个边界点的距离
					{
						if(v1.neighborIdTypeALL.contains(v2id))
						{
							for(Edge e:v1.edge)
							{
								if(e.nid1 == v2id || e.nid2 == v2id)
								{
									sumW += e.weight;
									num++;
									break;
								}
							}
						}
					}
				}
			}
			if(notIn2 == 0)
			{
				heBing2(sv2,sv1);
				reMake = true;
			}
			else
			{
				//se.weight += sumW/(notIn2*se.Incid2.size());
				se.weight += sumW/num;
			}
		}
		return reMake;
	}
	
	public Boolean computeWeightPlanNo1()   //计算边的权重，也就是虚拟中心节点之间的距离：随机从每个interface节点中选取一个作为地标，以地标之间的距离作为社团间距
	{
		for(SuperEdge se:superEdgeList)
		{	
			//int seed1 =  (int) (Math.random() * se.Incid1.size()); 
			Vertex land1 = vertexList.get(se.Incid1.iterator().next());
			for(Edge e: land1.edge)
			{
				int vid2 = (e.nid1 == land1.getId())? e.nid2:e.nid1;
				if(se.Incid2.contains(vid2))
				{
					se.weight += e.weight;
					break;
				}
			}
		}
		return false;
	}
	
	public Boolean computeWeightPlanNo2()   //计算边的权重，也就是虚拟中心节点之间的距离：选取社团间最短的一条作为最短路径（最短的也就是权重最大的）
	{
		for(SuperEdge se:superEdgeList)
		{	
			double max = 0;  
			for(Edge e:se.cid1Withcid2) 
			{
				if(e.weight > max)
				{
					max = e.weight;
				}
			}
			se.weight = max;
		}
		return false;
	}
	
	public Boolean computeWeightPlanNo3()   //计算边的权重，也就是虚拟中心节点之间的距离：从每个interface节点中选取一个中心性最高的作为地标，以地标之间的距离作为社团间距
	{
		for(SuperEdge se:superEdgeList)
		{	
			int max1 = 0;  Vertex land1 = null;
			for(int vid: se.Incid1)
			{
				Vertex v1 = vertexList.get(vid);
				if(Cb[vid] > max1)
				{
					max1 = v1.edge.size();
					land1 = v1;
				}
			}
			int max2 = 0;  Vertex land2 = null;
			for(int vid: se.Incid2)
			{
				Vertex v1 = vertexList.get(vid);
				if(Cb[vid] > max2)
				{
					max2 = v1.edge.size();
					land2 = v1;
				}
			}
			for(Edge e: land1.edge) 
			{
				int vid2 = (e.nid1 == land1.getId())? e.nid2:e.nid1;
				if(land2.getId() == vid2)
				{
					se.weight = e.weight;
					break;
				}
			}
		}
		return false;
	}
	
	public Boolean computeWeightPlanHave1()   //随机从每个interface节点中选取一个作为地标，以地标之间的距离作为社团间距，考虑社团内部的距离
	{
		common cn = new common();
		double maxCost = cn.initialCost(vertexList);
		
		//Step1: 计算社团内部所有外部节点之间的最短路径
		Date start = new Date();
		for(SuperVertex sv: superVertexList)
		{
			 ArrayList<Vertex> outVertexList = new ArrayList<Vertex>();
			 ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
			 if(sv.containVertex != null)
		     {
				  for(int vid:sv.containVertex)
				  {
				      allVertexList.add(vertexList.get(vid));
				  }
			 }
			 if(sv.outVertex != null)
			 {
				 for(int vid:sv.outVertex)
				 {
					 outVertexList.add(vertexList.get(vid));
				 }
				 cn.calculateShortestPath(vertexList, allVertexList, outVertexList); 
			 }
		}
		
		for(SuperEdge se:superEdgeList)
		{	
			//int seed1 =  (int) (Math.random() * se.Incid1.size()); 
			Vertex land1 = vertexList.get(se.Incid1.iterator().next());
			Vertex land2 = null;
			for(Edge e: land1.edge)
			{
				int vid2 = (e.nid1 == land1.getId())? e.nid2:e.nid1;
				if(se.Incid2.contains(vid2))
				{
					land2 = vertexList.get(vid2);
					se.weight += e.weight;
					break;
				}
			}
			double d1 = 0, d2 = 0;
			if(land1.shortestPath.size()!=0)
			{
				for(Map.Entry<Integer, String> entry : land1.shortestPath.entrySet()) 
				{
					String[] pathLasts = entry.getValue().split(":");
					int strlen = (pathLasts[0].split(",")).length - 1;
					double cost = Double.parseDouble(pathLasts[1]);
					double weight = maxCost*strlen - cost;
					d1 += weight;
				}
				se.weight += d1/land1.shortestPath.size();
			}
			if(land2.shortestPath.size()!=0)
			{
				for(Map.Entry<Integer, String> entry : land2.shortestPath.entrySet()) 
				{
					String[] pathLasts = entry.getValue().split(":");
					int strlen = (pathLasts[0].split(",")).length - 1;
					double cost = Double.parseDouble(pathLasts[1]);
					double weight = maxCost*strlen - cost;
					d2 += weight;
				}
				se.weight += d2/land2.shortestPath.size();
			}
		}
		return false;
	}
	
	public Boolean computeWeightPlanHave2()   //选取最短一个作为地标，以地标之间的距离作为社团间距，考虑社团内部的距离
	{
		common cn = new common();
		double maxCost = cn.initialCost(vertexList);
		
		//Step1: 计算社团内部所有外部节点之间的最短路径
		Date start = new Date();
		for(SuperVertex sv: superVertexList)
		{
			 ArrayList<Vertex> outVertexList = new ArrayList<Vertex>();
			 ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
			 if(sv.containVertex != null)
		     {
				  for(int vid:sv.containVertex)
				  {
				      allVertexList.add(vertexList.get(vid));
				  }
			 }
			 if(sv.outVertex != null)
			 {
				 for(int vid:sv.outVertex)
				 {
					 outVertexList.add(vertexList.get(vid));
				 }
				 cn.calculateShortestPath(vertexList, allVertexList, outVertexList); 
			 }
		}
		
		for(SuperEdge se:superEdgeList)
		{	
			int land1id = 0, land2id = 0;
			double max = 0;  
			for(Edge e:se.cid1Withcid2) 
			{
				if(e.weight > max)
				{
					max = e.weight;
					land1id = e.nid1;
					land2id = e.nid2;
				}
			}
			se.weight += max;
			
		    Vertex land1 = vertexList.get(land1id);
			Vertex land2 = vertexList.get(land2id);
			double d1 = 0, d2 = 0;
			
			if(land1.shortestPath.size()!=0)
			{
				for(Map.Entry<Integer, String> entry : land1.shortestPath.entrySet()) 
				{
					String[] pathLasts = entry.getValue().split(":");
					int strlen = (pathLasts[0].split(",")).length - 1;
					double cost = Double.parseDouble(pathLasts[1]);
					double weight = maxCost*strlen - cost;
					d1 += weight;
				}
				se.weight += d1/land1.shortestPath.size();
			}
			if(land2.shortestPath.size()!=0)
			{
				for(Map.Entry<Integer, String> entry : land2.shortestPath.entrySet()) 
				{
					String[] pathLasts = entry.getValue().split(":");
					int strlen = (pathLasts[0].split(",")).length - 1;
					double cost = Double.parseDouble(pathLasts[1]);
					double weight = maxCost*strlen - cost;
					d2 += weight;
				}
				se.weight += d2/land2.shortestPath.size();
			}
		}
		return false;
	}
	
	public Boolean computeWeightPlanHave3()   //从每个interface节点中选取一个中心性最高的作为地标，考虑社团内部的距离
	{
		common cn = new common();
		double maxCost = cn.initialCost(vertexList);
		
		//Step1: 计算社团内部所有外部节点之间的最短路径
		Date start = new Date();
		for(SuperVertex sv: superVertexList)
		{
			 ArrayList<Vertex> outVertexList = new ArrayList<Vertex>();
			 ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
			 if(sv.containVertex != null)
		     {
				  for(int vid:sv.containVertex)
				  {
				      allVertexList.add(vertexList.get(vid));
				  }
			 }
			 if(sv.outVertex != null)
			 {
				 for(int vid:sv.outVertex)
				 {
					 outVertexList.add(vertexList.get(vid));
				 }
				 cn.calculateShortestPath(vertexList, allVertexList, outVertexList); 
			 }
		}
		
		for(SuperEdge se:superEdgeList)
		{	
			int max1 = 0;  Vertex land1 = null;
			for(int vid: se.Incid1)
			{
				Vertex v1 = vertexList.get(vid);
				if(Cb[vid] > max1)
				{
					max1 = v1.edge.size();
					land1 = v1;
				}
			}
			int max2 = 0;  Vertex land2 = null;
			for(int vid: se.Incid2)
			{
				Vertex v1 = vertexList.get(vid);
				if(Cb[vid] > max2)
				{
					max2 = v1.edge.size();
					land2 = v1;
				}
			}
			for(Edge e: land1.edge) 
			{
				int vid2 = (e.nid1 == land1.getId())? e.nid2:e.nid1;
				if(land2.getId() == vid2)
				{
					se.weight += e.weight;
					break;
				}
			}
			double d1 = 0, d2 = 0;
			if(land1.shortestPath.size()!=0)
			{
				for(Map.Entry<Integer, String> entry : land1.shortestPath.entrySet()) 
				{
					String[] pathLasts = entry.getValue().split(":");
					int strlen = (pathLasts[0].split(",")).length - 1;
					double cost = Double.parseDouble(pathLasts[1]);
					double weight = maxCost*strlen - cost;
					d1 += weight;
				}
				se.weight += d1/land1.shortestPath.size();
			}
			if(land2.shortestPath.size()!=0)
			{
				for(Map.Entry<Integer, String> entry : land2.shortestPath.entrySet()) 
				{
					String[] pathLasts = entry.getValue().split(":");
					int strlen = (pathLasts[0].split(",")).length - 1;
					double cost = Double.parseDouble(pathLasts[1]);
					double weight = maxCost*strlen - cost;
					d2 += weight;
				}
				se.weight += d2/land2.shortestPath.size();
			}
		}
		return false;
	}
	
	public Boolean computeWeightPlan5()   //计算边的权重，也就是虚拟中心节点之间的距离：计算边之间的路径时，考虑两边节点的中心性
	{
		for(SuperEdge se:superEdgeList)
		{	 
			Map<Edge, Float> map = new HashMap<Edge, Float>();  
			double sum = 0.0;
			for(Edge e:se.cid1Withcid2) 
			{
				sum += e.weight;
				map.put(e, e.weight);
			}
			double avg = sum/se.cid1Withcid2.size();
			List<Entry<Edge, Float>> list = new ArrayList<Map.Entry<Edge,Float>>(map.entrySet());  //这里将map.entrySet()转换成list
			Collections.sort(list,new Comparator<Map.Entry<Edge,Float>>() {
	            //升序排序
	            public int compare(Entry<Edge, Float> o1,
	                    Entry<Edge, Float> o2) {
	                return o2.getValue().compareTo(o1.getValue());
	            }           
	        });
			
			sum = 0.0;  int n = 0;
			for(Map.Entry<Edge,Float> mapping:list)
			{
				if(n>se.cid1Withcid2.size()/3)
				{
					break;
				}
				double w = mapping.getValue();
				if(w >= avg)
				{
					sum += w;
					Edge e = mapping.getKey(); //考虑两边节点的中心性
					sum += Math.sqrt(vertexList.get(e.nid1).centrality);
					sum += Math.sqrt(vertexList.get(e.nid2).centrality);
					n++;
				}
			}
			avg = sum/n;
			se.weight = avg;
		}
		return false;
	}
	
	public void computeWeight2(HashSet<Integer> hubs)
	{//计算Hub连接的社团的权重
		for(int vid:hubs)
		{
			Vertex v = vertexList.get(vid);
			for(int svid:v.SuperVId)
			{
				SuperVertex c1 = superVertexList.get(svid);
				for(int svid2:v.SuperVId)
				{
					if(svid2>svid)
					{
						SuperVertex c2 = superVertexList.get(svid2);
						SuperEdge se;
						if((se=containEdge(svid, svid2))==null)
						{
							se = addEdge(svid, svid2);
						}
						se.weight += 1;
					}
				}
			}
		}
	}
	
	public void calculateReachId()    //计算超级节点的传递闭包
	{
	   boolean arr[][] = new boolean[superVertexList.size()][superVertexList.size()];
	   Iterator<SuperEdge> itEdge = superEdgeList.iterator();
       while(itEdge.hasNext()) 
       {  //初始化邻接矩阵
           SuperEdge se = itEdge.next();
           arr[se.cid1][se.cid2] = true;
           arr[se.cid2][se.cid1] = true;
       }
       for(int i = 0;i < superVertexList.size();i++) { //列
           for(int j = 0;j < superVertexList.size();j++) {  //行
               for(int k = 0;k < superVertexList.size();k++) {  //每行中的列
                   arr[j][k] = arr[j][k] || (arr[j][i] && arr[i][k]);
               }
           }
       }
       for(int i = 0;i < superVertexList.size();i++) 
       {
           for (int j = 0;j < superVertexList.size();j++) 
           {
        	   if(arr[i][j] == true && j!= i) 
        	   {
        		   superVertexList.get(i).reachId.add(j);
        	   }
           }
       }
	}
	
	public void calculateReachId2()    //计算超级节点的传递闭包DFS
	{
		int n = superVertexList.size();
		System.out.println("n: "+n);
		isVisited = new boolean[n];
		connectId = new int[n];
		for(int i=0; i<n; i++)
		{
			isVisited[i] = false;  //初始化为未访问
		}
		
		for(SuperVertex sv : superVertexList)
		{
			if(!isVisited[sv.cid])  //顶点u没有被访问
			{
				dfs(sv);
				connectCount ++;
			}
		}
	}
	
	public void dfs(SuperVertex u)   //深度优先遍历---递归
	{
		isVisited[u.cid] = true;  
		connectId[u.cid] = connectCount;
		u.parentId = connectCount;
		//System.out.println(u.cid+":  "+connectCount);
		for(int vid:u.neighborId)
		{
			if(!isVisited[vid])
			{
				SuperVertex sv = superVertexList.get(vid);
				dfs(sv);
			}
		}
	}
	
	public void superEdgeDiff()   //计算最大消耗和最小消耗的差
	{
		for(SuperVertex sv: superVertexList)
		{
			for(SuperEdge se:sv.edge)
			{
				int id = se.cid1==sv.cid? se.cid2:se.cid1;
				SuperVertex sv2 = superVertexList.get(id);
				double min = Double.MAX_VALUE;
				double max = 0;
				for(Edge e:se.cid1Withcid2)
				{
					if(e.weight < min)  min = e.weight;
					if(e.weight > max)  max = e.weight;
				}
				double diff = max-min;
				sv.Diff.put(id, diff);
				sv2.Diff.put(sv.cid, diff);
			}
		}
	}
	
	public static void makeSuperGraphForReach() throws SQLException, ParseException  //为可达查询构建超图
	{
		Date start = new Date(); //计时
		//vertexList = SLPA.getVertex(vertexList, 0);   //使用SLPA进行社团划分
		vertexList = pSCAN.getVertex(vertexList,edgeList);      //使用pCAN进行社团划分
		//vertexList = wSCAN.getVertex();      //使用wCAN进行社团划分
		Date end = new Date();
		System.out.println("社团聚类："+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		//labelNum += vertexList.size();
		int nodeNum = vertexList.size();
		int r = (int) Math.floor(Math.sqrt(nodeNum));
		int avg = nodeNum/r;
		System.out.println(nodeNum+","+r+","+avg);
		
		start = new Date(); //计时
		MakeSuperGraph MSG = new MakeSuperGraph();
		MSG.makeGraph(vertexList);  int iter = 0;
		end = new Date();
		System.out.println("构建超图："+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		itertime = 1;
		
		//MSG.calculateReachId();   //计算可达性
		start = new Date();
		MSG.calculateReachId2();
		Date end2 = new Date();
		System.out.println("整个预处理时间："+ (end2.getTime() - start.getTime()) + " total milliseconds"); 
					
		/*start = new Date(); //计时
		DFSReachQuery.DFS(vertexList, edgeList);
		end = new Date();
	    System.out.println("DFS计算连通分量："+ (end.getTime() - start.getTime()) + " total milliseconds");
				
		start = new Date(); //计时
		NormalReachQuery.Normal(vertexList, edgeList);
		end = new Date();
		System.out.println("Normal计算点对传递闭包："+ (end.getTime() - start.getTime()) + " total milliseconds"); */
	    
	    dataAccess db = new dataAccess();
		db.writeVertex(vertexList);
		db.writeEdge(edgeList);
		db.writeSuperEdge(superEdgeList);
		db.writeSuperVertex(superVertexList);
		System.out.println();
	}
	
	public static void makeSuperGraphForShortestforBen() throws SQLException, ParseException  //为最短路径查询构建超图
	{
		Date start = new Date(); //计时
		//vertexList = SLPA.getVertex(vertexList, 0);   //使用SLPA进行社团划分
		vertexList = pSCAN.getVertex(vertexList,edgeList);      //使用pCAN进行社团划分
		//vertexList = wSCAN.getVertex(vertexList,edgeList);      //使用wCAN进行社团划分
		MakeSuperGraph MSG = new MakeSuperGraph();
		MSG.makeGraph(vertexList);
		//MSG.computeWeightPlanNo2();
		
		MSG.calculateReachId2();
		MSG.superEdgeDiff();
		Date end = new Date();
		System.out.println("社团聚类："+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		dataAccess db = new dataAccess();
		db.writeVertex(vertexList);
		db.writeEdge(edgeList);
		db.writeSuperEdge(superEdgeList);
		db.writeSuperVertex(superVertexList);
		System.out.println();
	}
	
	public static void makeSuperGraphForShortest() throws SQLException, ParseException  //为最短路径查询构建超图
	{
		Date start = new Date(); //计时
		//vertexList = SLPA.getVertex(vertexList, 0);   //使用SLPA进行社团划分
		vertexList = pSCAN.getVertex(vertexList,edgeList);      //使用pCAN进行社团划分
		//vertexList = wSCAN.getVertex(vertexList,edgeList);      //使用wCAN进行社团划分
		Date end = new Date();
		System.out.println("社团聚类："+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		labelNum += vertexList.size();
		int nodeNum = vertexList.size();
		int r = (int) Math.floor(Math.sqrt(nodeNum));
		int avg = nodeNum/r;
		System.out.println(nodeNum+","+r+","+avg);
		
		start = new Date(); //计时
		MakeSuperGraph MSG = new MakeSuperGraph();
		MSG.makeGraph(vertexList);  int iter = 0;
		//end = new Date();
		//System.out.println("构建超图："+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		itertime = 1;
		
		while(MSG.followUp(avg) && iter++<3)   //需要拆分或者合并，一般迭代三次就没问题
		{
			itertime++;
			MSG.makeGraph(vertexList);
		}
		HashSet<Integer> hubs = MSG.singleVertex();    //处理单独的节点
		MSG.makeGraph(vertexList);
		end = new Date();
		System.out.println("构建超图："+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		start = new Date(); //计时
		//MSG.computeWeightPlanHave3();//可达查询不用计算社团之间边的权重，计算社团之间的最短路径可有多种方案
		//MSG.makeGraph(vertexList);
		//MSG.computeWeight2(hubs);
		end = new Date();
		System.out.println("计算边的权重："+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		//MSG.calculateReachId();   //计算可达性
		start = new Date();
		MSG.calculateReachId2();
		Date end2 = new Date();
		System.out.println("计算ReachId："+ (end2.getTime() - start.getTime()) + " total milliseconds"); 
		
		MSG.superEdgeDiff();
		
		/*dataAccess db = new dataAccess();
		db.writeVertex(vertexList);
		db.writeEdge(edgeList);
		db.writeSuperEdge(superEdgeList);
		db.writeSuperVertex(superVertexList);
		System.out.println();*/
	}
	
	public static void main(String[] args) throws SQLException, ParseException
	{
		//从数据库中读出vertexList
		
		/*Date start = new Date(); //计时  for  Benchmark
		proDatasetWeighted pdw = new proDatasetWeighted();  //测试数据集
		String filePath = "./network10000_0.1.txt";
		//String filePath2 = "./community1000_0.7.txt";
		pdw.readTxtFile(filePath);
		//pdw.readTxtFile2(filePath2);
		pdw.makeGraph();
		vertexList = pdw.getVertex();
		edgeList = pdw.getEdge();
		Date end = new Date();
		System.out.println("将数据集转化为图："+ (end.getTime() - start.getTime()) + " total milliseconds");*/
		
		/*Date start = new Date(); //计时
		proDatasetWeighted2 pdw = new proDatasetWeighted2();  //测试数据集
		String filePath = "./soc-sign-bitcoinotc.txt";
		pdw.readTxtFile(filePath);
		pdw.makeGraph();
		vertexList = pdw.getVertex();
		edgeList = pdw.getEdge();
		Date end = new Date();
		System.out.println("将数据集转化为图："+ (end.getTime() - start.getTime()) + " total milliseconds");*/
		
		Date start = new Date(); //计时
		proDataset pd = new proDataset();  //测试数据集
		String filePath = "./Email-EuAll.txt";
		pd.readTxtFile(filePath);
		pd.makeGraph();
		vertexList = pd.getVertex();
		edgeList = pd.getEdge(); 
		Date end = new Date();
		System.out.println("将数据集转化为图："+ (end.getTime() - start.getTime()) + " total milliseconds");
		
		/*MakeGraph mg = new MakeGraph();   //邮件数据集
		Date start = new Date(); //计时
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		vertexList = mg.getVertex();
		edgeList = mg.getEdge();
		Date end = new Date();
		System.out.println("将邮件数据集转化为图："+ (end.getTime() - start.getTime()) + " total milliseconds");*/
		
		//makeSuperGraphForReach();   //可达查询
		//Cb = brandesForCentrality.brande(vertexList);
		//makeSuperGraphForShortestforBen();  //最短路径查询
		makeSuperGraphForShortest();  //最短路径查询
		modularity.modularityQ(vertexList);    //社团模块度计算
		System.out.println("超点："+superVertexList.size() + ",超边" +superEdgeList.size()+ ", 第一阶段:" + modularity.CompelxWithIn(superVertexList) + ", 第二阶段:" + (superVertexList.size()+superEdgeList.size()));
		//ComparativeExperiment.Ep(superVertexList, vertexList, 50);
	}
}

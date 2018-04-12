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
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //ԭ���㼯��
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //�߼���
	public static ArrayList<SuperVertex> superVertexList;  //�����ڵ㼯��
	public static ArrayList<SuperEdge> superEdgeList; //�߼���
	public static int superVertex_Id = 0; //����ID
	public static int superEdge_Id = 0;   //��ID
	public static Map<Integer, Integer> map; //cid��ԭʼ����id�Ķ�Ӧ��ϵ
	
	static ArrayList<SuperVertex> chaiSuperVertex;  //��Ҫ��ֵĳ����ڵ㼯��
	static ArrayList<SuperVertex> heSuperVertex;  //��Ҫ�ϲ��ĳ����ڵ㼯��
	static ArrayList<Integer> singleSuperVertex;  //ֻ��һ���ڵ�ĳ����ڵ㼯��
	static int labelNum = 0;
	static int itertime = 0;
	
	public static boolean[] isVisited;  //��¼ÿ���ڵ��Ƿ���ʹ�
	public static int[] connectId;   //����i���ڵڼ�����ͨ����  
	public static int connectCount = 0;//��ͨ������Ŀ  
	public static double[] Cb;   //������
	
	public static SuperVertex containVertex(int orginalId) //���㼯�����Ƿ����
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
	
	public static SuperVertex addVertex(int orginalId)  //���Ӷ���
	{
		SuperVertex v = new SuperVertex(superVertex_Id);
		superVertexList.add(v);
		map.put(superVertex_Id, orginalId);
		superVertex_Id++;
		return v;
	}
	
	public static SuperEdge containEdge(int from, int to)  //�߼������Ƿ����
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
	public static SuperEdge addEdge(int from, int to)  //���ӱ�
	{
		SuperEdge e = new SuperEdge(superEdge_Id, from, to);
		superEdgeList.add(e);
		superEdge_Id++;
		return e;
	}
	
	public ArrayList<SuperVertex> makeGraph(ArrayList<Vertex> vertexList)
	{
		Date start = new Date(); //��ʱ

		superVertexList = new ArrayList<SuperVertex>();  //�����ڵ㼯��
		superEdgeList = new ArrayList<SuperEdge>(); 
		superVertex_Id = 0; //����ID
		superEdge_Id = 0;
		map = new HashMap<Integer, Integer>(); //cid��ԭʼ����id�Ķ�Ӧ��ϵ
		for(Vertex v:vertexList)
		{
			v.SuperVId.clear();
			for(Edge e:v.edge)
			{
				e.visited = false;
			}
		}
		
		Iterator<Vertex> iter1 = vertexList.iterator();
		while(iter1.hasNext())  //����ÿ������
		{
			Vertex v = iter1.next();
			int vid = v.getId();
			for(int id:v.communityId)  //������ÿ����������
			{
				//��������Ų����ڣ�����ӣ����򣬰Ѹýڵ�����������
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
			for(int vid:c1.containVertex)    //�鿴������ĵ���ָ�������
			{
				Vertex v = vertexList.get(vid);
				if(v.type ==-2)
				{
					continue;
				}
				for(Edge e:v.edge)
				{
					if(e.visited == true)   //�Ѿ����������ڵ���
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
					for(int SVid:v2.SuperVId)   //���ڵĳ����ڵ�
					{
						if(SVid == c1.cid)   //ָ���Լ�
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
						superVertexList.get(SVid).outVertex.add(v2id);   //�ⲿ�ڵ����
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
	
	public Boolean followUp(int avg) throws ParseException    //��������
	{
		Boolean flag = false;
		//chaiSuperVertex = new ArrayList<SuperVertex>();  //��Ҫ��ֵĳ����ڵ㼯��
		heSuperVertex = new ArrayList<SuperVertex>();  //��Ҫ�ϲ��ĳ����ڵ㼯��
		singleSuperVertex = new ArrayList<Integer>();  //��Ҫ�ϲ��ĳ����ڵ㼯��
		Iterator<SuperVertex> iter1 = superVertexList.iterator();
		int count1 = 0, count2 = 0; int max = 0;
		while(iter1.hasNext())
		{
			SuperVertex v = iter1.next();
			if(v.containVertex.size()>1)
			{
				//�ж�һ��ÿ��������ÿ���ڵ��Ƿ�����ͨ
				if(v.containVertex.size()>max)
				{
					max= v.containVertex.size();
				}
				if(v.containVertex.size()>3*avg)  //����n����ƽ�����Ŵ�Сʱ�����в��
				{
					chaiFen(v);
					flag = true;
				}
				if(v.containVertex.size()<0.1*avg)  //С��n����ƽ�����Ŵ�Сʱ�����кϲ�
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
	
	public HashSet<Integer> singleVertex()   //�����Ľڵ㴦��
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
					for(int SVid:v2.SuperVId)   //���ڵĳ����ڵ�
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
				for(int SVid:v2.SuperVId)   //���ڵĳ����ڵ�
				{
					SuperVertex sv2 = superVertexList.get(SVid);
					if(sv2.containVertex.size()>1)   //����С����
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
					for(int SVid:v2.SuperVId)   //���ڵĳ����ڵ�
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
	
	public void chaiFen(SuperVertex SV) throws ParseException   //���������Ž��в��
	{
		//�����а����Ľڵ��ٴν������Ż���
		vertexList = SLPA.getVertex2(vertexList, labelNum, SV);
		//modularity.modularityQ(SV, vertexList);    //����ģ��ȼ���
		labelNum += SV.containVertex.size();
	}
	 
	public void heBing()                  //����ϵ������С�����Ž��кϲ�
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
				if(intersect(c1.containVertex, c2.containVertex)!=null)   //�й�ͬ�ڵ�
				{
					hebing = true;//�ϲ�
				}
				else    //�����ӵĵ���ڵ��ڶ���֮һ�ĵ�Ĳ������ϲ�
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
				
				
				if(hebing == true)   //���������������ϲ�
				{
					int comid = map.get(c1.cid);
					for(int vid:c2.containVertex)   //����һ�����ж���ı��ת������һ��
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
			for(int SVid:v2.SuperVId)   //���ڵĳ����ڵ�
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
	
	public HashSet<Integer> intersect(HashSet<Integer> list1,HashSet<Integer> list2) //����н������򷵻�true
	{
		HashSet<Integer> a1 = new HashSet<Integer>(list1);
		HashSet<Integer> a2 = new HashSet<Integer>(list2);
		if(!(a1.isEmpty()&& a2.isEmpty()))//ȫ��Ϊ��
		{
			a1.retainAll(a2);
			if(!a1.isEmpty())
			{
				return a1;
			}
		}
		return null;
	}
	
	public Boolean computeWeightPlan2()    //����ߵ�Ȩ�أ�Ҳ�����������Ľڵ�֮��ľ��룺��Ϊ�����֣�һ���������ż�ӿڵ�֮���ƽ�����룬��һ�����������ڽڵ㵽�ӿڵ��ƽ������
	{//����������ɣ�һ���������ż��ƽ����̾��루d1����һ�����������ڲ�ƽ������̾����һ�루d2,d3����
		boolean reMake = false;
		for(SuperEdge se:superEdgeList)
		{
			SuperVertex sv1 = superVertexList.get(se.cid1);
			SuperVertex sv2 = superVertexList.get(se.cid2);
			
			double sum = 0.0;  
			for(Edge e:se.cid1Withcid2) 
			{
				sum += e.weight;    //�������ż���ڱߵ�Ȩ�أ�Ȩ�ظ�����ɷ���
			}
			//se.weight += sum/(se.Incid1.size()*se.Incid2.size());
			se.weight += sum/se.cid1Withcid2.size();
			
			int notIn1 = 0;   //�������ڲ������ڱ߽�ĵ����
			double sumW = 0.0;   int num = 0;
			for(int vid:sv1.coreVertex)    //���������ڲ�֮�䵽�߽���ƽ������
			{
				Vertex v1 = vertexList.get(vid);
				if(!se.Incid1.contains(vid))
				{
					notIn1 ++;
					for(int v2id:se.Incid1)   //���㵽ÿ���߽��ľ���
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
			
			int notIn2 = 0;   //�������ڲ������ڱ߽�ĵ����
			sumW = 0.0; num =0 ;
			for(int vid:sv2.coreVertex)    //���������ڲ�֮�䵽�߽���ƽ������
			{
				Vertex v1 = vertexList.get(vid);
				if(!se.Incid2.contains(vid))
				{
					notIn2 ++;
					for(int v2id:se.Incid2)   //���㵽ÿ���߽��ľ���
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
	
	public Boolean computeWeightPlanNo1()   //����ߵ�Ȩ�أ�Ҳ�����������Ľڵ�֮��ľ��룺�����ÿ��interface�ڵ���ѡȡһ����Ϊ�ر꣬�Եر�֮��ľ�����Ϊ���ż��
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
	
	public Boolean computeWeightPlanNo2()   //����ߵ�Ȩ�أ�Ҳ�����������Ľڵ�֮��ľ��룺ѡȡ���ż���̵�һ����Ϊ���·������̵�Ҳ����Ȩ�����ģ�
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
	
	public Boolean computeWeightPlanNo3()   //����ߵ�Ȩ�أ�Ҳ�����������Ľڵ�֮��ľ��룺��ÿ��interface�ڵ���ѡȡһ����������ߵ���Ϊ�ر꣬�Եر�֮��ľ�����Ϊ���ż��
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
	
	public Boolean computeWeightPlanHave1()   //�����ÿ��interface�ڵ���ѡȡһ����Ϊ�ر꣬�Եر�֮��ľ�����Ϊ���ż�࣬���������ڲ��ľ���
	{
		common cn = new common();
		double maxCost = cn.initialCost(vertexList);
		
		//Step1: ���������ڲ������ⲿ�ڵ�֮������·��
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
	
	public Boolean computeWeightPlanHave2()   //ѡȡ���һ����Ϊ�ر꣬�Եر�֮��ľ�����Ϊ���ż�࣬���������ڲ��ľ���
	{
		common cn = new common();
		double maxCost = cn.initialCost(vertexList);
		
		//Step1: ���������ڲ������ⲿ�ڵ�֮������·��
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
	
	public Boolean computeWeightPlanHave3()   //��ÿ��interface�ڵ���ѡȡһ����������ߵ���Ϊ�ر꣬���������ڲ��ľ���
	{
		common cn = new common();
		double maxCost = cn.initialCost(vertexList);
		
		//Step1: ���������ڲ������ⲿ�ڵ�֮������·��
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
	
	public Boolean computeWeightPlan5()   //����ߵ�Ȩ�أ�Ҳ�����������Ľڵ�֮��ľ��룺�����֮���·��ʱ���������߽ڵ��������
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
			List<Entry<Edge, Float>> list = new ArrayList<Map.Entry<Edge,Float>>(map.entrySet());  //���ｫmap.entrySet()ת����list
			Collections.sort(list,new Comparator<Map.Entry<Edge,Float>>() {
	            //��������
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
					Edge e = mapping.getKey(); //�������߽ڵ��������
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
	{//����Hub���ӵ����ŵ�Ȩ��
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
	
	public void calculateReachId()    //���㳬���ڵ�Ĵ��ݱհ�
	{
	   boolean arr[][] = new boolean[superVertexList.size()][superVertexList.size()];
	   Iterator<SuperEdge> itEdge = superEdgeList.iterator();
       while(itEdge.hasNext()) 
       {  //��ʼ���ڽӾ���
           SuperEdge se = itEdge.next();
           arr[se.cid1][se.cid2] = true;
           arr[se.cid2][se.cid1] = true;
       }
       for(int i = 0;i < superVertexList.size();i++) { //��
           for(int j = 0;j < superVertexList.size();j++) {  //��
               for(int k = 0;k < superVertexList.size();k++) {  //ÿ���е���
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
	
	public void calculateReachId2()    //���㳬���ڵ�Ĵ��ݱհ�DFS
	{
		int n = superVertexList.size();
		System.out.println("n: "+n);
		isVisited = new boolean[n];
		connectId = new int[n];
		for(int i=0; i<n; i++)
		{
			isVisited[i] = false;  //��ʼ��Ϊδ����
		}
		
		for(SuperVertex sv : superVertexList)
		{
			if(!isVisited[sv.cid])  //����uû�б�����
			{
				dfs(sv);
				connectCount ++;
			}
		}
	}
	
	public void dfs(SuperVertex u)   //������ȱ���---�ݹ�
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
	
	public void superEdgeDiff()   //����������ĺ���С���ĵĲ�
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
	
	public static void makeSuperGraphForReach() throws SQLException, ParseException  //Ϊ�ɴ��ѯ������ͼ
	{
		Date start = new Date(); //��ʱ
		//vertexList = SLPA.getVertex(vertexList, 0);   //ʹ��SLPA�������Ż���
		vertexList = pSCAN.getVertex(vertexList,edgeList);      //ʹ��pCAN�������Ż���
		//vertexList = wSCAN.getVertex();      //ʹ��wCAN�������Ż���
		Date end = new Date();
		System.out.println("���ž��ࣺ"+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		//labelNum += vertexList.size();
		int nodeNum = vertexList.size();
		int r = (int) Math.floor(Math.sqrt(nodeNum));
		int avg = nodeNum/r;
		System.out.println(nodeNum+","+r+","+avg);
		
		start = new Date(); //��ʱ
		MakeSuperGraph MSG = new MakeSuperGraph();
		MSG.makeGraph(vertexList);  int iter = 0;
		end = new Date();
		System.out.println("������ͼ��"+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		itertime = 1;
		
		//MSG.calculateReachId();   //����ɴ���
		start = new Date();
		MSG.calculateReachId2();
		Date end2 = new Date();
		System.out.println("����Ԥ����ʱ�䣺"+ (end2.getTime() - start.getTime()) + " total milliseconds"); 
					
		/*start = new Date(); //��ʱ
		DFSReachQuery.DFS(vertexList, edgeList);
		end = new Date();
	    System.out.println("DFS������ͨ������"+ (end.getTime() - start.getTime()) + " total milliseconds");
				
		start = new Date(); //��ʱ
		NormalReachQuery.Normal(vertexList, edgeList);
		end = new Date();
		System.out.println("Normal�����Դ��ݱհ���"+ (end.getTime() - start.getTime()) + " total milliseconds"); */
	    
	    dataAccess db = new dataAccess();
		db.writeVertex(vertexList);
		db.writeEdge(edgeList);
		db.writeSuperEdge(superEdgeList);
		db.writeSuperVertex(superVertexList);
		System.out.println();
	}
	
	public static void makeSuperGraphForShortestforBen() throws SQLException, ParseException  //Ϊ���·����ѯ������ͼ
	{
		Date start = new Date(); //��ʱ
		//vertexList = SLPA.getVertex(vertexList, 0);   //ʹ��SLPA�������Ż���
		vertexList = pSCAN.getVertex(vertexList,edgeList);      //ʹ��pCAN�������Ż���
		//vertexList = wSCAN.getVertex(vertexList,edgeList);      //ʹ��wCAN�������Ż���
		MakeSuperGraph MSG = new MakeSuperGraph();
		MSG.makeGraph(vertexList);
		//MSG.computeWeightPlanNo2();
		
		MSG.calculateReachId2();
		MSG.superEdgeDiff();
		Date end = new Date();
		System.out.println("���ž��ࣺ"+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		dataAccess db = new dataAccess();
		db.writeVertex(vertexList);
		db.writeEdge(edgeList);
		db.writeSuperEdge(superEdgeList);
		db.writeSuperVertex(superVertexList);
		System.out.println();
	}
	
	public static void makeSuperGraphForShortest() throws SQLException, ParseException  //Ϊ���·����ѯ������ͼ
	{
		Date start = new Date(); //��ʱ
		//vertexList = SLPA.getVertex(vertexList, 0);   //ʹ��SLPA�������Ż���
		vertexList = pSCAN.getVertex(vertexList,edgeList);      //ʹ��pCAN�������Ż���
		//vertexList = wSCAN.getVertex(vertexList,edgeList);      //ʹ��wCAN�������Ż���
		Date end = new Date();
		System.out.println("���ž��ࣺ"+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		labelNum += vertexList.size();
		int nodeNum = vertexList.size();
		int r = (int) Math.floor(Math.sqrt(nodeNum));
		int avg = nodeNum/r;
		System.out.println(nodeNum+","+r+","+avg);
		
		start = new Date(); //��ʱ
		MakeSuperGraph MSG = new MakeSuperGraph();
		MSG.makeGraph(vertexList);  int iter = 0;
		//end = new Date();
		//System.out.println("������ͼ��"+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		itertime = 1;
		
		while(MSG.followUp(avg) && iter++<3)   //��Ҫ��ֻ��ߺϲ���һ��������ξ�û����
		{
			itertime++;
			MSG.makeGraph(vertexList);
		}
		HashSet<Integer> hubs = MSG.singleVertex();    //�������Ľڵ�
		MSG.makeGraph(vertexList);
		end = new Date();
		System.out.println("������ͼ��"+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		start = new Date(); //��ʱ
		//MSG.computeWeightPlanHave3();//�ɴ��ѯ���ü�������֮��ߵ�Ȩ�أ���������֮������·�����ж��ַ���
		//MSG.makeGraph(vertexList);
		//MSG.computeWeight2(hubs);
		end = new Date();
		System.out.println("����ߵ�Ȩ�أ�"+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		//MSG.calculateReachId();   //����ɴ���
		start = new Date();
		MSG.calculateReachId2();
		Date end2 = new Date();
		System.out.println("����ReachId��"+ (end2.getTime() - start.getTime()) + " total milliseconds"); 
		
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
		//�����ݿ��ж���vertexList
		
		/*Date start = new Date(); //��ʱ  for  Benchmark
		proDatasetWeighted pdw = new proDatasetWeighted();  //�������ݼ�
		String filePath = "./network10000_0.1.txt";
		//String filePath2 = "./community1000_0.7.txt";
		pdw.readTxtFile(filePath);
		//pdw.readTxtFile2(filePath2);
		pdw.makeGraph();
		vertexList = pdw.getVertex();
		edgeList = pdw.getEdge();
		Date end = new Date();
		System.out.println("�����ݼ�ת��Ϊͼ��"+ (end.getTime() - start.getTime()) + " total milliseconds");*/
		
		/*Date start = new Date(); //��ʱ
		proDatasetWeighted2 pdw = new proDatasetWeighted2();  //�������ݼ�
		String filePath = "./soc-sign-bitcoinotc.txt";
		pdw.readTxtFile(filePath);
		pdw.makeGraph();
		vertexList = pdw.getVertex();
		edgeList = pdw.getEdge();
		Date end = new Date();
		System.out.println("�����ݼ�ת��Ϊͼ��"+ (end.getTime() - start.getTime()) + " total milliseconds");*/
		
		Date start = new Date(); //��ʱ
		proDataset pd = new proDataset();  //�������ݼ�
		String filePath = "./Email-EuAll.txt";
		pd.readTxtFile(filePath);
		pd.makeGraph();
		vertexList = pd.getVertex();
		edgeList = pd.getEdge(); 
		Date end = new Date();
		System.out.println("�����ݼ�ת��Ϊͼ��"+ (end.getTime() - start.getTime()) + " total milliseconds");
		
		/*MakeGraph mg = new MakeGraph();   //�ʼ����ݼ�
		Date start = new Date(); //��ʱ
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		vertexList = mg.getVertex();
		edgeList = mg.getEdge();
		Date end = new Date();
		System.out.println("���ʼ����ݼ�ת��Ϊͼ��"+ (end.getTime() - start.getTime()) + " total milliseconds");*/
		
		//makeSuperGraphForReach();   //�ɴ��ѯ
		//Cb = brandesForCentrality.brande(vertexList);
		//makeSuperGraphForShortestforBen();  //���·����ѯ
		makeSuperGraphForShortest();  //���·����ѯ
		modularity.modularityQ(vertexList);    //����ģ��ȼ���
		System.out.println("���㣺"+superVertexList.size() + ",����" +superEdgeList.size()+ ", ��һ�׶�:" + modularity.CompelxWithIn(superVertexList) + ", �ڶ��׶�:" + (superVertexList.size()+superEdgeList.size()));
		//ComparativeExperiment.Ep(superVertexList, vertexList, 50);
	}
}

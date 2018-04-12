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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import reachQuery.reachQuery;
import SuperGraph.MakeSuperGraph;
import SuperGraph.SuperEdge;
import SuperGraph.SuperVertex;
import community.SLPA;
import database.dataAccess;

public class communityStructure2016 
{
	public static ArrayList<SuperVertex> superVertexList;  //�����ڵ㼯��
	public static ArrayList<SuperEdge> superEdgeList; //�߼���
	public static ArrayList<Vertex> vertexList;  //ԭ���㼯��
	public static double Terror = 0;
	public static int NotFind = 0;
	public static void CSpre() throws SQLException, ParseException   //Ԥ����
	{
		//Step1: ʹ��SLPA��ԭͼ���о��࣬��ѡȡ��̵�һ����Ϊ���·��
		
		Date start1 = new Date(); //��ʱ
		
		Date end1 = new Date();
		System.out.println("communityStructureԤ����"+ (end1.getTime() - start1.getTime()) + " total milliseconds"); 
	}
	
	public static double CSquery(ArrayList<SuperVertex> superVertexList, ArrayList<Vertex> vertexList, Vertex v1, Vertex v2, double cost) throws SQLException   //���������㣬�������·��
	{
		//�ҵ�v1,v2���ڵĳ����ڵ㡣
		common cn = new common();
		commonForSuper cn2 = new commonForSuper();
		for(int superVId1:v1.SuperVId)
		{
			for(int superVId2:v2.SuperVId)
			{
				//case1: �����һ�������ڵ�֮��
				if(superVId1 == superVId2)   
				{
					SuperVertex sv = superVertexList.get(superVId1);
					ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
					if(sv.containVertex != null)
					{
						 for(int vid:sv.containVertex)
						 {
							 allVertexList.add(vertexList.get(vid));
						 }
						 String str = cn.calculateShortestPath(vertexList, allVertexList, v1, v2); 
						 return 0;
					}
				}
				
				//case2: �������һ�������ڵ�֮�У��ڶ�������ڵ�֮��
				else
				{
					SuperVertex sv1 = superVertexList.get(superVId1);
					SuperVertex sv2 = superVertexList.get(superVId2);
					String ser = cn2.calculateShortestPath(superEdgeList, superVertexList, sv1, sv2); //����sv1��sv2֮������·��
					//String ser = allPath.findAllPath(superVId1, superVId1, superVId2, superVertexList, 1).get(0); 
					if(ser==null)
					{
						System.out.println("");
						Terror += 1;
						return 1.0;
					}
					
					ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();   //���⼸������չ��
					String[] CVId = null;   //;��������
					CVId = ser.split(",");
					for(String svi: CVId)
					{
						int svid = Integer.parseInt(svi);
						SuperVertex sv = superVertexList.get(svid);
						if(sv.containVertex != null)
						{
							 for(int vid:sv.containVertex)
							 {
								 allVertexList.add(vertexList.get(vid));
							 }
						}
					}
					String str = cn.calculateShortestPath(vertexList, allVertexList, v1, v2); 
					//System.out.println(str);
					
					if(str == null)
					{
						NotFind++;
						Terror += 1;
						return 1.0;
					}
					String[] ss = str.split(":");
					double cost1 = Double.parseDouble(ss[1]);
					double error = (cost1-cost)/cost;
					Terror += error;
					System.out.println("cost:"+ cost+ ", wecost:"+cost1); //�������
					return error;
				}
			}
		}
		return 0;
	}
	
	public static Boolean computeWeightPlanNewNo1() throws SQLException   //����ߵ�Ȩ�أ������ÿ��interface�ڵ���ѡȡһ����Ϊ�ر�
	{
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
					se.weight = e.weight;
					break;
				}
			}
			superVertexList.get(se.cid1).landmark.put(se.cid2, land1.getId());
			superVertexList.get(se.cid2).landmark.put(se.cid1, land2.getId());
		}
		return false;
	}
	
	public static Boolean computeWeightPlanNewNo2() throws SQLException   //����ߵ�Ȩ�أ�ѡȡ���ż���̵�һ����Ϊ���·������̵�Ҳ����Ȩ�����ģ�
	{
		for(SuperEdge se:superEdgeList)
		{	
			int land1id = 0, land2id = 0;
			double max = 0;  
			for(int vid1: se.Incid1)
			{
				Vertex v1 = vertexList.get(vid1);
				for(Edge e: v1.edge)
				{
					int vid2 = (e.nid1 == vid1)? e.nid2:e.nid1;
					if(se.Incid2.contains(vid2) && e.weight > max)
					{
						land1id = vid1;
						land2id = vid2;
						max = e.weight;
					}
				}
			}
			se.weight = max;
			superVertexList.get(se.cid1).landmark.put(se.cid2, land1id);
			superVertexList.get(se.cid2).landmark.put(se.cid1, land2id);
		}
		return false;
	}
	
	public static Boolean computeWeightPlanNewNo3() throws SQLException   //����ߵ�Ȩ�أ���ÿ��interface�ڵ���ѡȡһ����������ߵ���Ϊ�ر꣬�Եر�֮��ľ�����Ϊ���ż��
	{
		double[] Cb = brandesForCentrality.brande(vertexList);
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
			superVertexList.get(se.cid1).landmark.put(se.cid2, land1.getId());
			superVertexList.get(se.cid2).landmark.put(se.cid1, land2.getId());
		}
		return false;
	}
	
	public static Boolean computeWeightPlanNewHave1(double maxCost)   //�����ÿ��interface�ڵ���ѡȡһ����Ϊ�ر꣬�Եر�֮��ľ�����Ϊ���ż�࣬���������ڲ��ľ���
	{
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
			superVertexList.get(se.cid1).landmark.put(se.cid2, land1.getId());
			superVertexList.get(se.cid2).landmark.put(se.cid1, land2.getId());
		}
		return false;
	}
	
	public static Boolean computeWeightPlanNewHave2(double maxCost)   //ѡȡ���һ����Ϊ�ر꣬�Եر�֮��ľ�����Ϊ���ż�࣬���������ڲ��ľ���
	{	
		for(SuperEdge se:superEdgeList)
		{	
			int land1id = 0, land2id = 0;
			double max = 0;  
			for(int vid1: se.Incid1)
			{
				Vertex v1 = vertexList.get(vid1);
				for(Edge e: v1.edge)
				{
					int vid2 = (e.nid1 == vid1)? e.nid2:e.nid1;
					if(se.Incid2.contains(vid2) && e.weight > max)
					{
						land1id = vid1;
						land2id = vid2;
						max = e.weight;
					}
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
			superVertexList.get(se.cid1).landmark.put(se.cid2, land1.getId());
			superVertexList.get(se.cid2).landmark.put(se.cid1, land2.getId());
		}
		return false;
	}
	
	public static Boolean computeWeightPlanNewHave3(double maxCost)   //��ÿ��interface�ڵ���ѡȡһ����������ߵ���Ϊ�ر꣬���������ڲ��ľ���
	{	
		double[] Cb = brandesForCentrality.brande(vertexList);
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
			superVertexList.get(se.cid1).landmark.put(se.cid2, land1.getId());
			superVertexList.get(se.cid2).landmark.put(se.cid1, land2.getId());
		}
		return false;
	}
	
	public static void main(String[] args) throws SQLException, ParseException
	{
		CSpre();
		common cn = new common();
		commonForSuper cn2 = new commonForSuper();
		dataAccess db = new dataAccess();
		superVertexList = db.readSuperVertex();
		vertexList = db.readVertex();
		superEdgeList = db.readSuperEdge();
		double maxWeight = cn.initialCost(vertexList);
		cn.initialCost(vertexList);
		computeWeightPlanNewNo2();
		//computeWeightPlanNewHave2(maxWeight);
		cn2.initialCost2(superEdgeList);
		
		int N = 200; double sum1 = 0, sum2 = 0, sum = 0;  int k=0;
		ArrayList<Vertex> randomVertexList = new ArrayList<Vertex>();
		int vertexSize = vertexList.size()-1;
		for(int i=0; i<N*2; i++)    //���ѡȡn�Խڵ�
		{
			int ra = (int)(Math.random()*(vertexSize-0+1)); 
			randomVertexList.add(vertexList.get(ra));
		}
		for(int i=0; i<randomVertexList.size(); i+=2)
		{
			Vertex s = randomVertexList.get(i); 
			Vertex d = randomVertexList.get(i+1);
			if(reachQuery.ReachQuery(s.getId(), d.getId()) == false)
			{
				continue;
			}
			System.out.print("s:"+s.getId() + ", d:"+d.getId()+ "===");
			k++;
			
			//Vertex s = vertexList.get(4201); 
		    //Vertex d = vertexList.get(1028);
			
			//��ͨDijkstra
			Date start = new Date();
			String str = cn.calculateShortestPathforWholeGraph2(vertexList, s, d);  //ʹ����ͨDijkstra�㷨
			Date end = new Date();
			sum1 += end.getTime() - start.getTime();
			System.out.print((end.getTime() - start.getTime()) + ", "); 
			
			//˫��Dijkstra
		    /*Date start2 = new Date();
			str = cn.calculateShortestPathforWholeGraph(vertexList, s, d);  //ʹ��˫��Dijkstra�㷨
			Date end2 = new Date();
			sum2 += end2.getTime() - start2.getTime();
			System.out.print((end2.getTime() - start2.getTime()) + ", "); */
			
			//CS
			if(str == null)  
			{
				k--;
				continue;
			}
			String[] strs = str.split(":");
			double cost = Double.parseDouble(strs[1]);
			Date start3 = new Date();
			double error = CSquery(superVertexList, vertexList, s, d, cost);
			Date end3 = new Date();
			sum += end3.getTime() - start3.getTime();
			System.out.print((end3.getTime() - start3.getTime())); 
			System.out.println("  error:"+error);
		}
		System.out.println("sum1="+sum1+", "+"sum2="+sum2+", "+"sum="+sum);
		System.out.println("sum1="+sum1*1.0/k*1.0+", "+"sum2="+sum2*1.0/k*1.0+", "+"sum="+sum*1.0/k*1.0);
		System.out.println("k="+k+",  error:"+Terror*1.0/k + ",  NotFind:"+NotFind);
	}
}

package shortPathQuery;

import graph.Edge;
import graph.Vertex;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import reachQuery.reachQuery;
import database.dataAccess;
import SuperGraph.SuperEdge;
import SuperGraph.SuperVertex;

public class ourApproach 
{
	public static ArrayList<SuperVertex> superVertexList;  //�����ڵ㼯��
	public static ArrayList<SuperEdge> superEdgeList; //�߼���
	public static ArrayList<Vertex> vertexList;  //ԭ���㼯��
	public static ArrayList<String> sers;   //�洢·���ļ��ϣ�ֻ�洢top3
	
	public ourApproach() {}
	
	public static void shortestPathPre() throws SQLException   //Ԥ����
	{
		//��ó�ͼ��;��һ�������ݿ��ж�ȡ��;������ֱ�����ɳ�ͼ
		dataAccess db = new dataAccess();
		common cn = new common();
		commonForSuper cn2 = new commonForSuper();
		superVertexList = db.readSuperVertex();
		vertexList = db.readVertex();
		cn.initialCost(vertexList);
		cn2.initialCost(superVertexList);
		
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
		Date end = new Date();
		System.out.println("Dijkstra���������ڽڵ�����·����"+ (end.getTime() - start.getTime()) + " total milliseconds"); 		
	}
	
	public static int shortestDistanceQuery2Time(ArrayList<SuperEdge> superEdgeList, ArrayList<SuperVertex> superVertexList, ArrayList<Vertex> vertexList, Vertex v1, Vertex v2)    //�ԱȲ�ͬ�ļ�������֮��·���ķ���
	{//��������;�����ż��ľ���			
		//�ҵ�ad1,ad2���ڵĳ����ڵ㡣
		common cn = new common();
		commonForSuper cn2 = new commonForSuper();
		int VId1 = v1.getId();
		int VId2 = v2.getId();
		for(int superVId1:v1.SuperVId)
		{
			for(int superVId2:v2.SuperVId)
			{
				//Step0: �����ȫͼ��������֮��ľ���
				
				//case1: �����һ�������ڵ�֮��
				if(superVId1 == superVId2)
				{
					Date start2 = new Date();
					//case11: �����㶼���ⲿ�ڵ㣬��ô�Ѿ���������·����
					SuperVertex sv = superVertexList.get(superVId1);
					if(sv.outVertex != null)
					{
						if(sv.outVertex.contains(VId1) && sv.outVertex.contains(VId2))
					    {
							String str = v1.shortestPath.get(VId2);
							Date end2 = new Date();
							return (int) (end2.getTime() - start2.getTime());
					    }
					}
								
					//case12: ֮��û�м�������·�������߼���
					ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
					if(sv.containVertex != null)
					{
						 for(int vid:sv.containVertex)
						 {
							 allVertexList.add(vertexList.get(vid));
						 }
						 String str = cn.calculateShortestPath(vertexList, allVertexList, v1, v2); 
						 start2 = new Date();
						 Date end2 = new Date();
						 return (int) (end2.getTime() - start2.getTime());
					}
				}
				
				//case2: �������һ�������ڵ�֮�У��ڶ�������ڵ�֮��
				else
				{
					SuperVertex sv1 = superVertexList.get(superVId1);
					SuperVertex sv2 = superVertexList.get(superVId2);
					String ser = cn2.calculateShortestPath(superEdgeList, superVertexList, sv1, sv2);
					//System.out.println("ser:"+ser);
					
					//case21: ��������֮�䲻�ɴֱ�ӷ���null ---Ӧ�ò������е������һ��ʼ���Ѿ��ж���
					Date start2 = new Date();
					if(ser==null)
					{
						Date end2 = new Date();
						return (int) (end2.getTime() - start2.getTime());
					}
					
					//case22: ��������֮��ɴ�
					double totalCost = 0;
					String[] CVId = null;   //;��������
					CVId = ser.split(",");
					int[] SVId = new int[CVId.length];
					for(int i=0; i<CVId.length; i++)
					{
						SVId[i] = Integer.parseInt(CVId[i]);
					}
					for(int i=0; i<CVId.length-1; i++)
					{
						SuperVertex tmp1 = superVertexList.get(SVId[i]);
						for(SuperEdge seid: tmp1.edge)
						{
							SuperEdge se = superEdgeList.get(seid.ceid);
							int cid2 = (se.cid1 == tmp1.cid)? se.cid2:se.cid1;
							if(cid2 == SVId[i+1])
							{
								totalCost += se.cost;
								break;
							}
						}
					}
					
					Vertex land1 = vertexList.get(sv1.landmark.get(SVId[1]));  //��v1����һ�����ŵ�Landmark
					String s = land1.shortestPath.get(VId1);
					if(s!=null) 
					{
						String[] strs = s.split(":");
						totalCost += Double.parseDouble(strs[1]);
					}			
					
					Vertex land2 = vertexList.get(sv2.landmark.get(SVId[CVId.length-2]));  //��v1����һ�����ŵ�Landmark
					s = land2.shortestPath.get(VId2);
					if(s!=null) 
					{
						String[] strs = s.split(":");
						totalCost += Double.parseDouble(strs[1]);
					}
					Date end2 = new Date();
					return (int) (end2.getTime() - start2.getTime());				
				}
			}
		}
		return 0;
	}
	
	public static double shortestDistanceQuery2Error(ArrayList<SuperEdge> superEdgeList, ArrayList<SuperVertex> superVertexList, ArrayList<Vertex> vertexList, Vertex v1, Vertex v2, double cost)    //�ԱȲ�ͬ�ļ�������֮��·���ķ���
	{//��������;�����ż��ľ���			
		//�ҵ�ad1,ad2���ڵĳ����ڵ㡣
		common cn = new common();
		commonForSuper cn2 = new commonForSuper();
		int VId1 = v1.getId();
		int VId2 = v2.getId();
		for(int superVId1:v1.SuperVId)
		{
			for(int superVId2:v2.SuperVId)
			{
				//Step0: �����ȫͼ��������֮��ľ���
				
				//case1: �����һ�������ڵ�֮��
				if(superVId1 == superVId2)
				{
					Date start2 = new Date();
					//case11: �����㶼���ⲿ�ڵ㣬��ô�Ѿ���������·����
					SuperVertex sv = superVertexList.get(superVId1);
					if(sv.outVertex != null)
					{
						if(sv.outVertex.contains(VId1) && sv.outVertex.contains(VId2))
					    {
							String str = v1.shortestPath.get(VId2);
							Date end2 = new Date();
							//return (int) (end2.getTime() - start2.getTime());
							return 0;
					    }
					}
								
					//case12: ֮��û�м�������·�������߼���
					ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
					if(sv.containVertex != null)
					{
						 for(int vid:sv.containVertex)
						 {
							 allVertexList.add(vertexList.get(vid));
						 }
						 String str = cn.calculateShortestPath(vertexList, allVertexList, v1, v2); 
						 Date end2 = new Date();
						 //return (int) (end2.getTime() - start2.getTime());
						 return 0;
					}
				}
				
				//case2: �������һ�������ڵ�֮�У��ڶ�������ڵ�֮��
				else
				{
					SuperVertex sv1 = superVertexList.get(superVId1);
					SuperVertex sv2 = superVertexList.get(superVId2);
					Date start2 = new Date();
					String ser = cn2.calculateShortestPath(superEdgeList, superVertexList, sv1, sv2);
					//System.out.println("ser:"+ser);
					
					//case21: ��������֮�䲻�ɴֱ�ӷ���null ---Ӧ�ò������е������һ��ʼ���Ѿ��ж���
					if(ser==null)
					{
						Date end2 = new Date();
						//return (int) (end2.getTime() - start2.getTime());
						return 0;
					}
					
					//case22: ��������֮��ɴ�
					double totalCost = 0;
					String[] CVId = null;   //;��������
					CVId = ser.split(",");
					int[] SVId = new int[CVId.length];
					for(int i=0; i<CVId.length; i++)
					{
						SVId[i] = Integer.parseInt(CVId[i]);
					}
					for(int i=0; i<CVId.length-1; i++)
					{
						SuperVertex tmp1 = superVertexList.get(SVId[i]);
						for(SuperEdge seid: tmp1.edge)
						{
							SuperEdge se = superEdgeList.get(seid.ceid);
							int cid2 = (se.cid1 == tmp1.cid)? se.cid2:se.cid1;
							if(cid2 == SVId[i+1])
							{
								totalCost += se.cost;
								break;
							}
						}
					}
					
					Vertex land1 = vertexList.get(sv1.landmark.get(SVId[1]));  //��v1����һ�����ŵ�Landmark
					String s = land1.shortestPath.get(VId1);
					if(s!=null) 
					{
						String[] strs = s.split(":");
						totalCost += Double.parseDouble(strs[1]);
					}			
					
					Vertex land2 = vertexList.get(sv2.landmark.get(SVId[CVId.length-2]));  //��v1����һ�����ŵ�Landmark
					s = land2.shortestPath.get(VId2);
					if(s!=null) 
					{
						String[] strs = s.split(":");
						totalCost += Double.parseDouble(strs[1]);
					}
					
					double big = (totalCost>cost)? cost:totalCost;
					double error = Math.abs((totalCost-cost)/big);
					System.out.print("  error:" + error + " ");
					return error;					
				}
			}
		}
		return 0;
	}
	
	public static double shortestDistanceQuery1Error(ArrayList<SuperEdge> superEdgeList, ArrayList<SuperVertex> superVertexList, ArrayList<Vertex> vertexList, Vertex v1, Vertex v2, double cost)    //�ԱȲ�ͬ�ļ�������֮��·���ķ���
	{//��������;���ر�֮��ľ���			
		//�ҵ�ad1,ad2���ڵĳ����ڵ㡣
		common cn = new common();
		commonForSuper cn2 = new commonForSuper();
		int VId1 = v1.getId();
		int VId2 = v2.getId();
		for(int superVId1:v1.SuperVId)
		{
			for(int superVId2:v2.SuperVId)
			{
				//Step0: �����ȫͼ��������֮��ľ���
				
				//case1: �����һ�������ڵ�֮��
				if(superVId1 == superVId2)
				{
					Date start2 = new Date();
					//case11: �����㶼���ⲿ�ڵ㣬��ô�Ѿ���������·����
					SuperVertex sv = superVertexList.get(superVId1);
					if(sv.outVertex != null)
					{
						if(sv.outVertex.contains(VId1) && sv.outVertex.contains(VId2))
					    {
							String str = v1.shortestPath.get(VId2);
							Date end2 = new Date();
							//return (int) (end2.getTime() - start2.getTime());
							return 0;
					    }
					}
								
					//case12: ֮��û�м�������·�������߼���
					ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
					if(sv.containVertex != null)
					{
						 for(int vid:sv.containVertex)
						 {
							 allVertexList.add(vertexList.get(vid));
						 }
						 String str = cn.calculateShortestPath(vertexList, allVertexList, v1, v2); 
						 Date end2 = new Date();
						 //return (int) (end2.getTime() - start2.getTime());
						 return 0;
					}
				}
				
				//case2: �������һ�������ڵ�֮�У��ڶ�������ڵ�֮��
				else
				{
					SuperVertex sv1 = superVertexList.get(superVId1);
					SuperVertex sv2 = superVertexList.get(superVId2);
					Date start2 = new Date();
					String ser = cn2.calculateShortestPath(superEdgeList, superVertexList, sv1, sv2);
					//System.out.println("ser:"+ser);
					
					//case21: ��������֮�䲻�ɴֱ�ӷ���null ---Ӧ�ò������е������һ��ʼ���Ѿ��ж���
					if(ser==null)
					{
						Date end2 = new Date();
						//return (int) (end2.getTime() - start2.getTime());
						return 0;
					}
					
					//case22: ��������֮��ɴ�
					double totalCost = 0;
					String[] CVId = null;   //;��������
					CVId = ser.split(",");
					int[] SVId = new int[CVId.length];
					for(int i=0; i<CVId.length; i++)
					{
						SVId[i] = Integer.parseInt(CVId[i]);
					}
					for(int i=0; i<CVId.length-2; i++)    //�ӵ�һ�����ŵ�Landmark�������ڶ������ŵ�Landmark
					{
						SuperVertex tmp1 = superVertexList.get(SVId[i]);
						SuperVertex tmp2 = superVertexList.get(SVId[i+1]);
						SuperVertex tmp3 = superVertexList.get(SVId[i+2]);
						Vertex land1 = vertexList.get(tmp1.landmark.get(tmp2.cid));
						Vertex land2 = vertexList.get(tmp2.landmark.get(tmp1.cid));
						int land3 = tmp2.landmark.get(tmp3.cid);
						for(Edge e: land1.edge) 
						{
							int vid2 = (e.nid1 == land1.getId())? e.nid2:e.nid1;
							if(land2.getId() == vid2)
							{
								totalCost += e.cost;
								break;
							}
						}
						String s = land2.shortestPath.get(land3);
						String[] strs = s.split(":");
						totalCost +=  Double.parseDouble(strs[1]);
					}
					SuperVertex tmp = superVertexList.get(SVId[CVId.length-2]);  //�ӵ����ڶ������ŵ�Landmark�����һ�����ŵ�Landmark
					Vertex land1 = vertexList.get(tmp.landmark.get(sv2.cid));
					Vertex land2 = vertexList.get(sv2.landmark.get(tmp.cid));
					for(Edge e: land1.edge) 
					{
						int vid2 = (e.nid1 == land1.getId())? e.nid2:e.nid1;
						if(land2.getId() == vid2)
						{
							totalCost += e.cost;
							break;
						}
					}
					land1 = vertexList.get(sv1.landmark.get(SVId[1]));  //��v1����һ�����ŵ�Landmark
					String s = land1.shortestPath.get(VId1);
					String[] strs = s.split(":");
					totalCost += Double.parseDouble(strs[1]);
					
					land2 = vertexList.get(sv2.landmark.get(SVId[CVId.length-2]));  //��v1����һ�����ŵ�Landmark
					s = land2.shortestPath.get(VId2);
					strs = s.split(":");
					totalCost += Double.parseDouble(strs[1]);
					
					double error = Math.abs((totalCost-cost)/cost);
					System.out.print("  error:" + error + " ");
					return error;					
				}
			}
		}
		return 0;
	}
	
	public static int shortestPathQueryNewTimeWithoutPrune(ArrayList<SuperEdge> superEdgeList, ArrayList<SuperVertex> superVertexList, ArrayList<Vertex> vertexList, Vertex v1, Vertex v2, double cost)    //�ԱȲ�ͬ�ļ�������֮��·���ķ���
	{			
		//�ҵ�ad1,ad2���ڵĳ����ڵ㡣
		common cn = new common();
		commonForSuper cn2 = new commonForSuper();
		int VId1 = v1.getId();
		int VId2 = v2.getId();
		for(int superVId1:v1.SuperVId)
		{
			for(int superVId2:v2.SuperVId)
			{
				//Step0: �����ȫͼ��������֮��ľ���
				
				//case1: �����һ�������ڵ�֮��
				if(superVId1 == superVId2)
				{
					Date start2 = new Date();
					//case11: �����㶼���ⲿ�ڵ㣬��ô�Ѿ���������·����
					SuperVertex sv = superVertexList.get(superVId1);
					if(sv.outVertex != null)
					{
						if(sv.outVertex.contains(VId1) && sv.outVertex.contains(VId2))
					    {
						for(Map.Entry<Integer, String> entry : v1.shortestPath.entrySet()) //�������·��
						{
							if(entry.getKey() == VId2)
							{
								Date end2 = new Date();
								return (int) (end2.getTime() - start2.getTime());
							}
						}
					  }
					}
								
					//case12: ֮��û�м�������·�������߼���
					ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
					if(sv.containVertex != null)
					{
						 for(int vid:sv.containVertex)
						 {
							 allVertexList.add(vertexList.get(vid));
						 }
						 start2 = new Date();
						 String str = cn.calculateShortestPath(vertexList, allVertexList, v1, v2); 
						 Date end2 = new Date();
						 return (int) (end2.getTime() - start2.getTime());
					}
				}
				
				//case2: �������һ�������ڵ�֮�У��ڶ�������ڵ�֮��
				else
				{
					SuperVertex sv1 = superVertexList.get(superVId1);
					SuperVertex sv2 = superVertexList.get(superVId2);
					Date start2 = new Date();
					String ser = cn2.calculateShortestPath(superEdgeList, superVertexList, sv1, sv2);
					//System.out.println("ser:"+ser);
					
					//case21: ��������֮�䲻�ɴֱ�ӷ���null ---Ӧ�ò������е������һ��ʼ���Ѿ��ж���
					if(ser==null)
					{
						Date end2 = new Date();
						return (int) (end2.getTime() - start2.getTime());
					}
					
					//case22: ��������֮��ɴ������
					String[] CVId = null;   //;��������
					CVId = ser.split(",");
					if(CVId.length == 2)
					{
						ArrayList<Vertex> allVertexListt = new ArrayList<Vertex>();   //���⼸������չ��
						for(String svi: CVId)
						{
							int svid = Integer.parseInt(svi);
							SuperVertex sv = superVertexList.get(svid);
							if(sv.containVertex != null)
							{
								 for(int vid:sv.containVertex)
								 {
									 allVertexListt.add(vertexList.get(vid));
								 }
							}
						}
						start2 = new Date();
						String strr = cn.calculateShortestPath(vertexList, allVertexListt, v1, v2); 
						Date end2 = new Date();
						return (int) (end2.getTime() - start2.getTime());
					}
					
					//case23: ��������֮��ɴ�������·�����ҿ�Խ����ڵ�
					//Step1: �ҵ�sv1,sv2���·����;�������� CVId
					
					//Step2: �ҵ����нӿڽڵ�
					ArrayList<HashSet<Integer>> VSets = new ArrayList<HashSet<Integer>>(); 
					for(int i=0; i<CVId.length-1; i++)
					{
						HashSet<Integer> VSet1 = new HashSet<Integer>();
						HashSet<Integer> VSet2 = new HashSet<Integer>();
						int last = Integer.parseInt(CVId[i]);
						int next = Integer.parseInt(CVId[i+1]);
						SuperVertex lastSV = superVertexList.get(last);
						for(SuperEdge se: lastSV.edge)
						{
							int dd = (lastSV.cid == se.cid1)? se.cid2: se.cid1;
							if(dd == next)
							{
								if(dd == se.cid2)
								{
									for(int vid: se.Incid1)
									{
										VSet1.add(vid);   //VSet��sv1������sv2�Ľӿڽڵ�
									}
									for(int vid: se.Incid2)
									{
										VSet2.add(vid);
									}
								}
								else
								{
									for(int vid: se.Incid2)
									{
										VSet1.add(vid);   
									}
									for(int vid: se.Incid1)
									{
										VSet2.add(vid);
									}
								}
								break;
							}
						}
						VSets.add(VSet1);
						VSets.add(VSet2);
					} 
						
					//Step3: ��ʼ��sv1���ⲿ�ڵ�
					double[] diffDisFrom = new double[CVId.length-1]; 
					double totalDiff = 0;
					for(int i=0; i<CVId.length-1; i++)
					{
						int last = Integer.parseInt(CVId[i]);
						int next = Integer.parseInt(CVId[i+1]);
						SuperVertex lastSV = superVertexList.get(last);
						double thisDiff = lastSV.Diff.get(next);
						totalDiff += thisDiff;
						diffDisFrom[i] = totalDiff;
					}
					double[] diffDisTo = new double[CVId.length-1];
					diffDisTo[0] = diffDisFrom[CVId.length-2];  //����Ŀ��ڵ����������̾���֮��
					for(int i=1; i<CVId.length-1; i++)
					{
						diffDisTo[i] = diffDisTo[0] - diffDisFrom[i-1];
					}
					
					HashMap<Integer,String> shortestPathOrigin = new HashMap<Integer,String>();   //��ֹ��ĿǰΪֹ�Ľڵ�����·��������������ɣ��ֱ���<��id, ����һ������id�����·��:����>
					if(sv1.outVertex.contains(VId1))   //���v1���ⲿ�ڵ㣬��ô�Ѿ������VSet�����·��
					{
						/*double min = Double.MAX_VALUE;
						for(String str : v1.shortestPath.values())
						{
							String[] strs = str.split(":");
							double cost1 = Double.parseDouble(strs[1]);
							if(cost1<min) min=cost1;
						}*/
						for(Map.Entry<Integer, String> entry : v1.shortestPath.entrySet())
						{
							int vid2 = entry.getKey();
							/*String[] strs = entry.getValue().split(":");
							double cost1 = Double.parseDouble(strs[1]);
							if(cost1 <= min+diffDisTo[0])
							    shortestPathOrigin.put(vid2, entry.getValue()); */
							if(VSets.get(0).contains(vid2))
								shortestPathOrigin.put(vid2, entry.getValue());
						}
						shortestPathOrigin.put(v1.getId(), v1.getId()+":0");
					}
					else  //���v1�����ⲿ�ڵ㣬��ʼ����
					{
						ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
						if(sv1.containVertex != null)
						{
							 for(int vid:sv1.containVertex)
							 {
								 allVertexList.add(vertexList.get(vid));
							 }
							 shortestPathOrigin = cn2.calculatedijkstraMatrixWithoutPrune(vertexList, v1, VSets.get(0), allVertexList);//��v1������VSet�нڵ����̾���
						}
					} 
					
					//Step4: ��sv1��ʼ��sv2�ⲿ�ڵ�����·��
					for(int i=0; i<CVId.length-2; i++)
					{
						//shortestPathOrigin = cn2.calculateNeighbor(VSets.get(i*2), VSets.get(i*2+1), VSets.get(i*2+2), shortestPathOrigin, vertexList);
						int thisCVId = Integer.parseInt(CVId[i+1]);
						double costToDes =  sv2.shortestPath.get(thisCVId);
						//��СVSets�Ĵ�С
						HashSet<Integer> VNew = new HashSet<Integer>();
						for(int id: shortestPathOrigin.keySet())
						{
							VNew.add(id);
						}
						shortestPathOrigin = cn2.calculateNeighbor(VNew, VSets.get(i*2+1), VSets.get(i*2+2), shortestPathOrigin, vertexList);
					}
					
					//Step5: �ӵ����ڶ������ⲿ�ڵ㵽���һ�����ⲿ�ڵ�
					int size = CVId.length;
					//��СVSets�Ĵ�С
					HashSet<Integer> VNew = new HashSet<Integer>();
					for(int id: shortestPathOrigin.keySet())
					{
						VNew.add(id);
					}
					//From Here is new
					if(sv2.outVertex.contains(VId2)) 
					{
						HashSet<Integer> VSet3 = new HashSet<Integer>();
						VSet3.add(VId2);
						shortestPathOrigin = cn2.calculateNeighbor(VNew, VSets.get((size-2)*2+1), VSet3, shortestPathOrigin, vertexList);
						Date end3 = new Date();
						String strr = shortestPathOrigin.get(VId2);
						String[] strs = strr.split(":");
						double cost1 = Double.parseDouble(strs[1]);
						double error = (cost1-cost)/cost1;
						System.out.print("  error:" + error + " ");
						return (int) (end3.getTime() - start2.getTime());
						//return error;
					}
					else
					{
						ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
						if(sv2.containVertex != null)
						{
							 for(int vid:sv2.containVertex)
							 {
								 allVertexList.add(vertexList.get(vid));
							 }
						}
						for(int vid: VNew)
						{
							allVertexList.add(vertexList.get(vid));
						}
						
						double min = Double.MAX_VALUE;
						String path = "";
						
						for(Map.Entry<Integer, String> entry : shortestPathOrigin.entrySet())
						{
							String[] pathLasts = entry.getValue().split(":");
							String path0 = pathLasts[0];
							double cost1 = Double.parseDouble(pathLasts[1]);
							int vid = entry.getKey();
							Vertex v0 = vertexList.get(vid);
							String str = cn.calculateShortestPath(vertexList, allVertexList, v0, v2); 
							if(str == null) continue;
							String[] strs = str.split(":");
							String path1 = strs[0];
							double cost2 = Double.parseDouble(strs[1]);
							if(cost2+cost1 < min)
							{
								min = cost2+cost1;
								path = path0+","+path1;
							}
						}
						Date end3 = new Date();
						//System.out.println("Our������������·����"+ (end3.getTime() - start2.getTime()) + " total milliseconds");
						//System.out.print("  error:" + (min-cost)/min + " ");
						//System.out.println("step62��"+ (endd.getTime() - startt.getTime()) + " total milliseconds"); 
						return (int) (end3.getTime() - start2.getTime());
					}
				}
			}
		}
		return 0;
	}
	
	public static int shortestPathQueryNewTime(ArrayList<SuperEdge> superEdgeList, ArrayList<SuperVertex> superVertexList, ArrayList<Vertex> vertexList, Vertex v1, Vertex v2, double cost)    //�ԱȲ�ͬ�ļ�������֮��·���ķ���
	{			
		//�ҵ�ad1,ad2���ڵĳ����ڵ㡣
		common cn = new common();
		commonForSuper cn2 = new commonForSuper();
		int VId1 = v1.getId();
		int VId2 = v2.getId();
		for(int superVId1:v1.SuperVId)
		{
			for(int superVId2:v2.SuperVId)
			{
				//Step0: �����ȫͼ��������֮��ľ���
				
				//case1: �����һ�������ڵ�֮��
				if(superVId1 == superVId2)
				{
					Date start2 = new Date();
					//case11: �����㶼���ⲿ�ڵ㣬��ô�Ѿ���������·����
					SuperVertex sv = superVertexList.get(superVId1);
					if(sv.outVertex != null)
					{
						if(sv.outVertex.contains(VId1) && sv.outVertex.contains(VId2))
					    {
						for(Map.Entry<Integer, String> entry : v1.shortestPath.entrySet()) //�������·��
						{
							if(entry.getKey() == VId2)
							{
								Date end2 = new Date();
								return (int) (end2.getTime() - start2.getTime());
							}
						}
					  }
					}
								
					//case12: ֮��û�м�������·�������߼���
					ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
					if(sv.containVertex != null)
					{
						 for(int vid:sv.containVertex)
						 {
							 allVertexList.add(vertexList.get(vid));
						 }
						 start2 = new Date();
						 String str = cn.calculateShortestPath(vertexList, allVertexList, v1, v2); 
						 Date end2 = new Date();
						 return (int) (end2.getTime() - start2.getTime());
					}
				}
				
				//case2: �������һ�������ڵ�֮�У��ڶ�������ڵ�֮��
				else
				{
					SuperVertex sv1 = superVertexList.get(superVId1);
					SuperVertex sv2 = superVertexList.get(superVId2);
					String ser = cn2.calculateShortestPath(superEdgeList, superVertexList, sv1, sv2);
					//System.out.println("ser:"+ser);
					
					//case21: ��������֮�䲻�ɴֱ�ӷ���null ---Ӧ�ò������е������һ��ʼ���Ѿ��ж���
					Date start2 = new Date();
					if(ser==null)
					{
						Date end2 = new Date();
						return (int) (end2.getTime() - start2.getTime());
					}
					
					//case22: ��������֮��ɴ������
					String[] CVId = null;   //;��������
					CVId = ser.split(",");
					if(CVId.length == 2)
					{
						ArrayList<Vertex> allVertexListt = new ArrayList<Vertex>();   //���⼸������չ��
						for(String svi: CVId)
						{
							int svid = Integer.parseInt(svi);
							SuperVertex sv = superVertexList.get(svid);
							if(sv.containVertex != null)
							{
								 for(int vid:sv.containVertex)
								 {
									 allVertexListt.add(vertexList.get(vid));
								 }
							}
						}
						start2 = new Date();
						String strr = cn.calculateShortestPath(vertexList, allVertexListt, v1, v2); 
						Date end2 = new Date();
						return (int) (end2.getTime() - start2.getTime());
					}
					
					//case23: ��������֮��ɴ�������·�����ҿ�Խ����ڵ�
					//Step1: �ҵ�sv1,sv2���·����;�������� CVId
					
					//Step2: �ҵ����нӿڽڵ�
					ArrayList<HashSet<Integer>> VSets = new ArrayList<HashSet<Integer>>(); 
					for(int i=0; i<CVId.length-1; i++)
					{
						HashSet<Integer> VSet1 = new HashSet<Integer>();
						HashSet<Integer> VSet2 = new HashSet<Integer>();
						int last = Integer.parseInt(CVId[i]);
						int next = Integer.parseInt(CVId[i+1]);
						SuperVertex lastSV = superVertexList.get(last);
						for(SuperEdge se: lastSV.edge)
						{
							int dd = (lastSV.cid == se.cid1)? se.cid2: se.cid1;
							if(dd == next)
							{
								if(dd == se.cid2)
								{
									for(int vid: se.Incid1)
									{
										VSet1.add(vid);   //VSet��sv1������sv2�Ľӿڽڵ�
									}
									for(int vid: se.Incid2)
									{
										VSet2.add(vid);
									}
								}
								else
								{
									for(int vid: se.Incid2)
									{
										VSet1.add(vid);   
									}
									for(int vid: se.Incid1)
									{
										VSet2.add(vid);
									}
								}
								break;
							}
						}
						VSets.add(VSet1);
						VSets.add(VSet2);
					} 
						
					//Step3: ��ʼ��sv1���ⲿ�ڵ�
					double[] diffDisFrom = new double[CVId.length-1]; 
					double totalDiff = 0;
					for(int i=0; i<CVId.length-1; i++)
					{
						int last = Integer.parseInt(CVId[i]);
						int next = Integer.parseInt(CVId[i+1]);
						SuperVertex lastSV = superVertexList.get(last);
						double thisDiff = lastSV.Diff.get(next);
						totalDiff += thisDiff;
						diffDisFrom[i] = totalDiff;
					}
					double[] diffDisTo = new double[CVId.length-1];
					diffDisTo[0] = diffDisFrom[CVId.length-2];  //����Ŀ��ڵ����������̾���֮��
					for(int i=1; i<CVId.length-1; i++)
					{
						diffDisTo[i] = diffDisTo[0] - diffDisFrom[i-1];
					}
					
					start2 = new Date();
					HashMap<Integer,String> shortestPathOrigin = new HashMap<Integer,String>();   //��ֹ��ĿǰΪֹ�Ľڵ�����·��������������ɣ��ֱ���<��id, ����һ������id�����·��:����>
					if(sv1.outVertex.contains(VId1))   //���v1���ⲿ�ڵ㣬��ô�Ѿ������VSet�����·��
					{
						/*double min = Double.MAX_VALUE;
						for(String str : v1.shortestPath.values())
						{
							String[] strs = str.split(":");
							double cost1 = Double.parseDouble(strs[1]);
							if(cost1<min) min=cost1;
						}*/
						for(Map.Entry<Integer, String> entry : v1.shortestPath.entrySet())
						{
							int vid2 = entry.getKey();
							/*String[] strs = entry.getValue().split(":");
							double cost1 = Double.parseDouble(strs[1]);
							if(cost1 <= min+diffDisTo[0])
							    shortestPathOrigin.put(vid2, entry.getValue()); */
							if(VSets.get(0).contains(vid2))
								shortestPathOrigin.put(vid2, entry.getValue());
						}
						shortestPathOrigin.put(v1.getId(), v1.getId()+":0");
					}
					else  //���v1�����ⲿ�ڵ㣬��ʼ����
					{
						ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
						if(sv1.containVertex != null)
						{
							 for(int vid:sv1.containVertex)
							 {
								 allVertexList.add(vertexList.get(vid));
							 }
							 shortestPathOrigin = cn2.calculatedijkstraMatrix(vertexList, v1, VSets.get(0), allVertexList, diffDisTo[0]);//��v1������VSet�нڵ����̾���
						}
					} 
					
					//Step4: ��sv1��ʼ��sv2�ⲿ�ڵ�����·��
					for(int i=0; i<CVId.length-2; i++)
					{
						//shortestPathOrigin = cn2.calculateNeighbor(VSets.get(i*2), VSets.get(i*2+1), VSets.get(i*2+2), shortestPathOrigin, vertexList);
						int thisCVId = Integer.parseInt(CVId[i+1]);
						double costToDes =  sv2.shortestPath.get(thisCVId);
						//��СVSets�Ĵ�С
						HashSet<Integer> VNew = new HashSet<Integer>();
						for(int id: shortestPathOrigin.keySet())
						{
							VNew.add(id);
						}
						shortestPathOrigin = cn2.calculateNeighborNew(VNew, VSets.get(i*2+1), VSets.get(i*2+2), shortestPathOrigin, vertexList, costToDes, diffDisTo[i+1]);
					}
					
					//Step5: �ӵ����ڶ������ⲿ�ڵ㵽���һ�����ⲿ�ڵ�
					int size = CVId.length;
					//��СVSets�Ĵ�С
					HashSet<Integer> VNew = new HashSet<Integer>();
					for(int id: shortestPathOrigin.keySet())
					{
						VNew.add(id);
					}
					//From Here is new
					if(sv2.outVertex.contains(VId2)) 
					{
						HashSet<Integer> VSet3 = new HashSet<Integer>();
						VSet3.add(VId2);
						shortestPathOrigin = cn2.calculateNeighbor(VNew, VSets.get((size-2)*2+1), VSet3, shortestPathOrigin, vertexList);
						Date end3 = new Date();
						return (int) (end3.getTime() - start2.getTime());
						//return error;
					}
					else
					{
						ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
						if(sv2.containVertex != null)
						{
							 for(int vid:sv2.containVertex)
							 {
								 allVertexList.add(vertexList.get(vid));
							 }
						}
						for(int vid: VNew)
						{
							allVertexList.add(vertexList.get(vid));
						}
						
						double min = Double.MAX_VALUE;
						String path = "";
						
						for(Map.Entry<Integer, String> entry : shortestPathOrigin.entrySet())
						{
							String[] pathLasts = entry.getValue().split(":");
							String path0 = pathLasts[0];
							double cost1 = Double.parseDouble(pathLasts[1]);
							int vid = entry.getKey();
							Vertex v0 = vertexList.get(vid);
							String str = cn.calculateShortestPath(vertexList, allVertexList, v0, v2); 
							if(str == null) continue;
							String[] strs = str.split(":");
							String path1 = strs[0];
							double cost2 = Double.parseDouble(strs[1]);
							if(cost2+cost1 < min)
							{
								min = cost2+cost1;
								path = path0+","+path1;
							}
						}
						Date end3 = new Date();
						//System.out.println("Our������������·����"+ (end3.getTime() - start2.getTime()) + " total milliseconds");
						//System.out.print("  error:" + (min-cost)/min + " ");
						//System.out.println("step62��"+ (endd.getTime() - startt.getTime()) + " total milliseconds"); 
						return (int) (end3.getTime() - start2.getTime());
					}
				}
			}
		}
		return 0;
	}
	
	public static double shortestPathQueryNewError(ArrayList<SuperEdge> superEdgeList, ArrayList<SuperVertex> superVertexList, ArrayList<Vertex> vertexList, Vertex v1, Vertex v2, double cost)    //�ԱȲ�ͬ�ļ�������֮��·���ķ���
	{			
		//�ҵ�ad1,ad2���ڵĳ����ڵ㡣
		common cn = new common();
		commonForSuper cn2 = new commonForSuper();
		int VId1 = v1.getId();
		int VId2 = v2.getId();
		for(int superVId1:v1.SuperVId)
		{
			for(int superVId2:v2.SuperVId)
			{
				//Step0: �����ȫͼ��������֮��ľ���
				
				//case1: �����һ�������ڵ�֮��
				if(superVId1 == superVId2)
				{
					Date start2 = new Date();
					//case11: �����㶼���ⲿ�ڵ㣬��ô�Ѿ���������·����
					SuperVertex sv = superVertexList.get(superVId1);
					if(sv.outVertex != null)
					{
						if(sv.outVertex.contains(VId1) && sv.outVertex.contains(VId2))
					    {
						for(Map.Entry<Integer, String> entry : v1.shortestPath.entrySet()) //�������·��
						{
							if(entry.getKey() == VId2)
							{
								Date end2 = new Date();
								//return (int) (end2.getTime() - start2.getTime());
								return 0;
							}
						}
					  }
					}
								
					//case12: ֮��û�м�������·�������߼���
					ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
					if(sv.containVertex != null)
					{
						 for(int vid:sv.containVertex)
						 {
							 allVertexList.add(vertexList.get(vid));
						 }
						 start2 = new Date();
						 String str = cn.calculateShortestPath(vertexList, allVertexList, v1, v2); 
						 Date end2 = new Date();
						 //return (int) (end2.getTime() - start2.getTime());
						 return 0;
					}
				}
				
				//case2: �������һ�������ڵ�֮�У��ڶ�������ڵ�֮��
				else
				{
					SuperVertex sv1 = superVertexList.get(superVId1);
					SuperVertex sv2 = superVertexList.get(superVId2);
					String ser = cn2.calculateShortestPath(superEdgeList, superVertexList, sv1, sv2);
					//System.out.println("ser:"+ser);
					
					//case21: ��������֮�䲻�ɴֱ�ӷ���null ---Ӧ�ò������е������һ��ʼ���Ѿ��ж���
					if(ser==null)
					{
						Date end2 = new Date();
						//return (int) (end2.getTime() - start2.getTime());
						return 0;
					}
					
					//case22: ��������֮��ɴ������
					String[] CVId = null;   //;��������
					CVId = ser.split(",");
					if(CVId.length == 2)
					{
						ArrayList<Vertex> allVertexListt = new ArrayList<Vertex>();   //���⼸������չ��
						for(String svi: CVId)
						{
							int svid = Integer.parseInt(svi);
							SuperVertex sv = superVertexList.get(svid);
							if(sv.containVertex != null)
							{
								 for(int vid:sv.containVertex)
								 {
									 allVertexListt.add(vertexList.get(vid));
								 }
							}
						}
						String strr = cn.calculateShortestPath(vertexList, allVertexListt, v1, v2); 
						Date end2 = new Date();
						
						if(strr == null)
							return 1.0;
						String[] strs = strr.split(":");
						if(strs.length > 2)
						{
							double cost1 = Double.parseDouble(strs[1]);
							double error = (cost1-cost)/cost;
							return error;
						}
						//System.out.print("  error:" + error + " ");
						//return (int) (end2.getTime() - start2.getTime());
					}
					
					//case23: ��������֮��ɴ�������·�����ҿ�Խ����ڵ�
					//Step1: �ҵ�sv1,sv2���·����;�������� CVId
					ArrayList<ArrayList<Integer>> alter = new ArrayList<ArrayList<Integer>>();
					ArrayList<Integer> level0 = new ArrayList<Integer>();
					level0.add(superVId1);
					alter.add(level0);
					for(int i=1; i<CVId.length-1; i++)
					{
						ArrayList<Integer> level = new ArrayList<Integer>();
						int tmp = Integer.parseInt(CVId[i]);
						level.add(tmp);
						SuperVertex tmpV = superVertexList.get(tmp);
						for(int oneStep: tmpV.neighborId)
						{
							if(oneStep!=superVId1 && oneStep!=superVId2)
							{
								level.add(oneStep);
							}
						}
						alter.add(level);
					}
					ArrayList<Integer> level1 = new ArrayList<Integer>();
					level1.add(superVId2);
					alter.add(level1);
					
					//Step2: �ҵ����нӿڽڵ�
					ArrayList<HashSet<Integer>> VSets = new ArrayList<HashSet<Integer>>(); 
					for(int i=0; i<alter.size()-1; i++)
					{
						HashSet<Integer> VSet1 = new HashSet<Integer>();
						HashSet<Integer> VSet2 = new HashSet<Integer>();
						for(int last : alter.get(i))
						{
							SuperVertex lastSV = superVertexList.get(last);
							for(SuperEdge se: lastSV.edge)
							{
								int dd = (lastSV.cid == se.cid1)? se.cid2: se.cid1;
								if(alter.get(i+1).contains(dd))
								{
									if(dd == se.cid2)
									{
										for(int vid: se.Incid1)
										{
											VSet1.add(vid);   //VSet��sv1������sv2�Ľӿڽڵ�
										}
										for(int vid: se.Incid2)
										{
											VSet2.add(vid);
										}
									}
									else
									{
										for(int vid: se.Incid2)
										{
											VSet1.add(vid);   
										}
										for(int vid: se.Incid1)
										{
											VSet2.add(vid);
										}
									}
								}
							}
						}
						VSets.add(VSet1);
						VSets.add(VSet2);
					}
						
					//Step3: ��ʼ��sv1���ⲿ�ڵ�
					
					HashMap<Integer,String> shortestPathOrigin = new HashMap<Integer,String>();   //��ֹ��ĿǰΪֹ�Ľڵ�����·��������������ɣ��ֱ���<��id, ����һ������id�����·��:����>
					for(int vid: VSets.get(0))
					{
						Vertex v = vertexList.get(vid);
						String s = v.shortestPath.get(VId1);
						//String[] strs = s.split(":");
						//double costd = Double.parseDouble(strs[1]);
						if(s!=null)
							shortestPathOrigin.put(vid, s);
					}
					
					//Step4: ��sv1��ʼ��sv2�ⲿ�ڵ�����·��
					for(int i=0; i<CVId.length-2; i++)
					{
						//shortestPathOrigin = cn2.calculateNeighbor(VSets.get(i*2), VSets.get(i*2+1), VSets.get(i*2+2), shortestPathOrigin, vertexList);
						//��СVSets�Ĵ�С
						HashSet<Integer> VNew = new HashSet<Integer>();
						for(int id: shortestPathOrigin.keySet())
						{
							VNew.add(id);
						}
						shortestPathOrigin = cn2.calculateNeighbor(VNew, VSets.get(i*2+1), VSets.get(i*2+2), shortestPathOrigin, vertexList);
					}
					
					//Step5: �ӵ����ڶ������ⲿ�ڵ㵽���һ�����ⲿ�ڵ�
					int size = CVId.length;
					//��СVSets�Ĵ�С
					HashSet<Integer> VNew = new HashSet<Integer>();
					for(int id: shortestPathOrigin.keySet())
					{
						VNew.add(id);
					}
					//From Here is new
					
					if(sv2.outVertex.contains(VId2)) 
					{
						HashSet<Integer> VSet3 = new HashSet<Integer>();
						VSet3.add(VId2);
						shortestPathOrigin = cn2.calculateNeighbor(VNew, VSets.get((size-2)*2+1), VSet3, shortestPathOrigin, vertexList);
						Date end3 = new Date();
						String strr = shortestPathOrigin.get(VId2);
						String[] strs = strr.split(":");
						double cost1 = Double.parseDouble(strs[1]);
						double error = (cost1-cost)/cost;
						System.out.print("  error:" + error + " ");
						//return (int) (end3.getTime() - start2.getTime());
						return error;
					}
					else
					{
						shortestPathOrigin = cn2.calculateNeighbor(VNew, VSets.get((size-2)*2+1), VSets.get((size-2)*2+1), shortestPathOrigin, vertexList);
						double min = Double.MAX_VALUE;
						String path = "";
						
						for(Map.Entry<Integer, String> entry : shortestPathOrigin.entrySet())
						{
							
							String[] pathLasts = entry.getValue().split(":");
							String path0 = pathLasts[0];
							double cost1 = Double.parseDouble(pathLasts[1]);
							int vid = entry.getKey();
							Vertex v0 = vertexList.get(vid);
							String s = v0.shortestPath.get(VId2);
							if(s==null) 
							{
								return 1.0;
							}
							String[] strs = s.split(":");
							double cost2 =  Double.parseDouble(strs[1]);
							if(cost2+cost1 < min)
							{
								min = cost2+cost1;
							}
						}
						Date end3 = new Date();
						//System.out.println("Our������������·����"+ (end3.getTime() - start2.getTime()) + " total milliseconds");
						System.out.print("  error:" + (min-cost)/cost + " ");
						//System.out.println("step62��"+ (endd.getTime() - startt.getTime()) + " total milliseconds"); 
						//return (int) (end3.getTime() - start2.getTime());
						return (min-cost)/cost;
					}
				}
			}
		}
		return 0;
	}
	
	public static int shortestPathQuery(ArrayList<SuperVertex> superVertexList, ArrayList<Vertex> vertexList, Vertex v1, Vertex v2, double cost)    //�ԱȲ�ͬ�ļ�������֮��·���ķ���
	{	
		String shortestPathFinal = "";
		double minCost = Double.MAX_VALUE;
		
		//�ҵ�ad1,ad2���ڵĳ����ڵ㡣
		dataAccess db = new dataAccess();
		common cn = new common();
		commonForSuper cn2 = new commonForSuper();
		int VId1 = v1.getId();
		int VId2 = v2.getId();
		for(int superVId1:v1.SuperVId)
		{
			for(int superVId2:v2.SuperVId)
			{
				//Step0: �����ȫͼ��������֮��ľ���
				
				//case1: �����һ�������ڵ�֮��
				if(superVId1 == superVId2)
				{
					Date start2 = new Date();
					//case11: �����㶼���ⲿ�ڵ㣬��ô�Ѿ���������·����
					SuperVertex sv = superVertexList.get(superVId1);
					if(sv.outVertex != null)
					{
						if(sv.outVertex.contains(VId1) && sv.outVertex.contains(VId2))
					    {
						for(Map.Entry<Integer, String> entry : v1.shortestPath.entrySet()) //�������·��
						{
							if(entry.getKey() == VId2)
							{
								Date end2 = new Date();
								//System.out.println("Our������������·����"+ (end2.getTime() - start2.getTime()) + " total milliseconds"); 
								//return entry.getValue();
								return (int) (end2.getTime() - start2.getTime());
							}
						}
					  }
					}
					
					
					//case12: ֮��û�м�������·�������߼���
					ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
					if(sv.containVertex != null)
					{
						 for(int vid:sv.containVertex)
						 {
							 allVertexList.add(vertexList.get(vid));
						 }
						 start2 = new Date();
						 String str = cn.calculateShortestPath(vertexList, allVertexList, v1, v2); 
						 Date end2 = new Date();
						 //System.out.println("Our������������·����"+ (end2.getTime() - start2.getTime()) + " total milliseconds"); 
						 //return str;
						 return (int) (end2.getTime() - start2.getTime());
					}
				}
				
				//case2: �������һ�������ڵ�֮�У��ڶ�������ڵ�֮��
				else
				{
					SuperVertex sv1 = superVertexList.get(superVId1);
					SuperVertex sv2 = superVertexList.get(superVId2);
					Date start2 = new Date();
					String ser = cn2.calculateShortestPath(superEdgeList, superVertexList, sv1, sv2);
					//System.out.println(" ser:"+ser);
					//Date end2 = new Date();
					//System.out.println("�ҵ�;�����ţ�"+ (end2.getTime() - start2.getTime()) + " total milliseconds"); 
					
					//case21: ��������֮�䲻�ɴֱ�ӷ���null ---Ӧ�ò������е������һ��ʼ���Ѿ��ж���
					if(ser==null)
					{
						Date end2 = new Date();
						return (int) (end2.getTime() - start2.getTime());
					}
					
					//case22: ��������֮��ɴ�������·��
					/*String[] CVId = null;   //;��������
					for(Map.Entry<Integer, String> entry : sv1.shortestPath.entrySet()) //�������·��
					{
						if(entry.getKey() == superVId2)
						{
							System.out.println(";�������ţ�"+entry.getValue());
							CVId = entry.getValue().split(",");
							break;
						}
					}*/
					//int iter = 0; int time = 0;
					//for(String ser: sers)
					{
						//Step1: �ҵ�sv1,sv2���·����;��������
						String shortestPath = "";
						String[] CVId = null;   //;��������
						CVId = ser.split(",");
						
						//Step2: �ҵ����нӿڽڵ�
						ArrayList<HashSet<Integer>> VSets = new ArrayList<HashSet<Integer>>(); 
						for(int i=0; i<CVId.length-1; i++)
						{
							HashSet<Integer> VSet1 = new HashSet<Integer>();
							HashSet<Integer> VSet2 = new HashSet<Integer>();
							int last = Integer.parseInt(CVId[i]);
							int next = Integer.parseInt(CVId[i+1]);
							SuperVertex lastSV = superVertexList.get(last);
							for(SuperEdge se: lastSV.edge)
							{
								int dd = (lastSV.cid == se.cid1)? se.cid2: se.cid1;
								if(dd == next)
								{
									if(dd == se.cid2)
									{
										for(int vid: se.Incid1)
										{
											VSet1.add(vid);   //VSet��sv1������sv2�Ľӿڽڵ�
										}
										for(int vid: se.Incid2)
										{
											VSet2.add(vid);
										}
									}
									else
									{
										for(int vid: se.Incid2)
										{
											VSet1.add(vid);   
										}
										for(int vid: se.Incid1)
										{
											VSet2.add(vid);
										}
									}
									break;
								}
							}
							VSets.add(VSet1);
							VSets.add(VSet2);
						} 
							
						//Step3: ��ʼ��sv1���ⲿ�ڵ�
						HashMap<Integer,String> shortestPathOrigin = new HashMap<Integer,String>();   //��ֹ��ĿǰΪֹ�Ľڵ�����·��������������ɣ��ֱ���<��id, ����һ������id�����·��:����>
						if(sv1.outVertex.contains(VId1))   //���v1���ⲿ�ڵ㣬��ô�Ѿ������VSet�����·��
						{
							for(Map.Entry<Integer, String> entry : v1.shortestPath.entrySet())
							{
								int vid2 = entry.getKey();
								if(VSets.get(0).contains(vid2))
								{
									shortestPathOrigin.put(vid2, entry.getValue());  //TODO ˳������
								}
							}
						}
						else  //���v1���ⲿ�ڵ㣬��ʼ����
						{
							ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
							if(sv1.containVertex != null)
							{
								 for(int vid:sv1.containVertex)
								 {
									 allVertexList.add(vertexList.get(vid));
								 }
								 double diffToDes = Double.MAX_VALUE;
								 shortestPathOrigin = cn2.calculatedijkstraMatrix(vertexList, v1, VSets.get(0), allVertexList, diffToDes);//��v1������VSet�нڵ����̾���
							}
						} 
						
						//Step4: ��sv1��ʼ��sv2�ⲿ�ڵ�����·��
						for(int i=0; i<CVId.length-2; i++)
						{
							shortestPathOrigin = cn2.calculateNeighbor(VSets.get(i*2), VSets.get(i*2+1), VSets.get(i*2+2), shortestPathOrigin, vertexList);
						}
						
						//Step5: �ӵ����ڶ������ⲿ�ڵ㵽���һ�����ⲿ�ڵ�
						Date startt = new Date();
						int size = CVId.length;
						if(VSets.get((size-2)*2+1).contains(VId2))  
						{
							HashSet<Integer> VSet3 = new HashSet<Integer>();
							VSet3.add(VId2);
							shortestPathOrigin = cn2.calculateNeighbor(VSets.get((size-2)*2), VSets.get((size-2)*2+1), VSet3, shortestPathOrigin, vertexList);
							Date end3 = new Date();
							//System.out.println("Our������������·����"+ (end3.getTime() - start2.getTime()) + " total milliseconds"); 
							String sss = shortestPathOrigin.get(VId2);
							String[] ss = sss.split(":");
							double cost1 = Double.parseDouble(ss[1]);
							System.out.print("  error:" + (cost1-cost)/cost + " ");
							return (int) (end3.getTime() - start2.getTime());
						}
						else
						{
							startt = new Date();
							shortestPathOrigin = cn2.calculateNeighbor(VSets.get((size-2)*2), VSets.get((size-2)*2+1), VSets.get((size-2)*2+1), shortestPathOrigin, vertexList);
						}
						Date endd = new Date();
						System.out.println("step5��"+ (endd.getTime() - startt.getTime()) + " total milliseconds"); 
						
						//Step6: �����һ�����ⲿ�ڵ㵽v2
						startt = new Date();
						if(sv2.outVertex.contains(VId2))   //���v2���ⲿ�ڵ㣬��ô�Ѿ������VSet�����·��
						{
							double min = Double.MAX_VALUE;
							String path = "";
							for(Map.Entry<Integer, String> entry : shortestPathOrigin.entrySet())
							{
								String[] pathLasts = entry.getValue().split(":");
								String path0 = pathLasts[0];
								double cost1 = Double.parseDouble(pathLasts[1]);
								int vid = entry.getKey();
								String str = null;
								if(v2.shortestPath.get(vid)!=null)
								{
									str = v2.shortestPath.get(vid);
									String[] strs = str.split(":");
									double cost2 = Double.parseDouble(strs[1]);
									String path1 = strs[0];
									if(cost2+cost1 < min)
									{
										min = cost2+cost1;
										path = path0+","+path1;
									}
								}
							}
							Date end3 = new Date();
							//System.out.println("Our������������·����"+ (end3.getTime() - start2.getTime()) + " total milliseconds"); 
							System.out.print("  error:" + (min-cost)/cost + " ");
							endd = new Date();
							System.out.println("step61��"+ (endd.getTime() - startt.getTime()) + " total milliseconds"); 
							return (int) (end3.getTime() - start2.getTime());
						}
						else  //���v2���ڲ��ⲿ�ڵ㣬��ʼ����
						{
							ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
							if(sv2.containVertex != null)
							{
								 for(int vid:sv2.containVertex)
								 {
									 allVertexList.add(vertexList.get(vid));
								 }
							}
							
							double min = Double.MAX_VALUE;
							String path = "";
							
							for(Map.Entry<Integer, String> entry : shortestPathOrigin.entrySet())
							{
								String[] pathLasts = entry.getValue().split(":");
								String path0 = pathLasts[0];
								double cost1 = Double.parseDouble(pathLasts[1]);
								int vid = entry.getKey();
								Vertex v0 = vertexList.get(vid);
								String str = cn.calculateShortestPath(vertexList, allVertexList, v0, v2); 
								if(str == null) continue;
								String[] strs = str.split(":");
								String path1 = strs[0];
								double cost2 = Double.parseDouble(strs[1]);
								if(cost2+cost1 < min)
								{
									min = cost2+cost1;
									path = path0+","+path1;
								}
							}
							Date end3 = new Date();
							//System.out.println("Our������������·����"+ (end3.getTime() - start2.getTime()) + " total milliseconds");
							//System.out.print("  error:" + (min-cost)/cost + " ");
							endd = new Date();
							System.out.println("step62��"+ (endd.getTime() - startt.getTime()) + " total milliseconds"); 
							return (int) (end3.getTime() - start2.getTime());
						}
					}
				}
			}
		}
		return 0;
	}
	
	public static String shortestPathQuery(String ad1, String ad2, int K) throws SQLException    //���߲�ѯ   
	{
		String shortestPathFinal = "";
		double minCost = Double.MAX_VALUE;
		
		//����������ַ�������ж϶����Ƿ�ɴ���ɴ�ֱ�ӷ���
		if(reachQuery.ReachQuery(ad1, ad2) == false)
		{
			System.out.println("���ɴ�");
			return null;
		}
		
		//�ҵ�ad1,ad2���ڵĳ����ڵ㡣
		dataAccess db = new dataAccess();
		common cn = new common();
		commonForSuper cn2 = new commonForSuper();
		int VId1 = db.getVId(ad1);
		int VId2 = db.getVId(ad2);
		Vertex v1 = vertexList.get(VId1);
		Vertex v2 = vertexList.get(VId2);
		int superVId1 = v1.SuperVId.iterator().next();
		int superVId2 = v2.SuperVId.iterator().next();
		
		//Step0: �����ȫͼ��������֮��ľ���
		Date start = new Date();
		String s = cn.calculateShortestPathforWholeGraph(vertexList, v1, v2);
		Date end = new Date();
		System.out.println("siDijkstra������������·����"+ s + ", " + (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		start = new Date();
		s = cn.calculateShortestPathforWholeGraph(vertexList, v1, v2);
		end = new Date();
		System.out.println("biDijkstra������������·����"+ s + ", " + (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		//case1: �����һ�������ڵ�֮��
		Date start2 = new Date();
		if(superVId1 == superVId2)
		{
			//case11: �����㶼���ⲿ�ڵ㣬��ô�Ѿ���������·����
			SuperVertex sv = superVertexList.get(superVId1);
			if(sv.outVertex.contains(VId1) && sv.outVertex.contains(VId2))
			{
				for(Map.Entry<Integer, String> entry : v1.shortestPath.entrySet()) //�������·��
				{
					if(entry.getKey() == VId2)
					{
						System.out.println(entry.getValue());
						Date end2 = new Date();
						System.out.println("Our������������·����"+ (end2.getTime() - start2.getTime()) + " total milliseconds"); 
						return entry.getValue();
					}
				}
			}
			
			//case12: ֮��û�м�������·�������߼���
			ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
			if(sv.containVertex != null)
			{
				 for(int vid:sv.containVertex)
				 {
					 allVertexList.add(vertexList.get(vid));
				 }
				 String str = cn.calculateShortestPath(vertexList, allVertexList, v1, v2); 
				 System.out.println(str);
				 Date end2 = new Date();
				 System.out.println("Our������������·����"+ (end2.getTime() - start2.getTime()) + " total milliseconds"); 
				 return str;
			}
		}
		
		//case2: �������һ�������ڵ�֮�У��ڶ�������ڵ�֮��
		else
		{
			SuperVertex sv1 = superVertexList.get(superVId1);
			SuperVertex sv2 = superVertexList.get(superVId2);
			sers = allPath.findAllPath(superVId1, superVId1, superVId2, superVertexList, K);  //���غķ���̵�����·��
			
			//case21: ��������֮�䲻�ɴֱ�ӷ���null ---Ӧ�ò������е������һ��ʼ���Ѿ��ж���
			
			//case22: ��������֮��ɴ�������·��
			/*String[] CVId = null;   //;��������
			for(Map.Entry<Integer, String> entry : sv1.shortestPath.entrySet()) //�������·��
			{
				if(entry.getKey() == superVId2)
				{
					System.out.println(";�������ţ�"+entry.getValue());
					CVId = entry.getValue().split(",");
					break;
				}
			}*/
			int iter = 0; int time = 0;
			for(String ser: sers)
			{
				//Step1: �ҵ�sv1,sv2���·����;��������
				start2 = new Date();
				String shortestPath = "";
				String[] CVId = null;   //;��������
				System.out.println("**************************"+ ++iter + "****************************");
				System.out.println(";�������ţ�"+ser);
				CVId = ser.split(",");
				
				//Step2: �ҵ����нӿڽڵ�
				ArrayList<HashSet<Integer>> VSets = new ArrayList<HashSet<Integer>>(); 
				for(int i=0; i<CVId.length-1; i++)
				{
					HashSet<Integer> VSet1 = new HashSet<Integer>();
					HashSet<Integer> VSet2 = new HashSet<Integer>();
					int last = Integer.parseInt(CVId[i]);
					int next = Integer.parseInt(CVId[i+1]);
					SuperVertex lastSV = superVertexList.get(last);
					for(SuperEdge se: lastSV.edge)
					{
						int dd = (lastSV.cid == se.cid1)? se.cid2: se.cid1;
						if(dd == next)
						{
							if(dd == se.cid2)
							{
								for(int vid: se.Incid1)
								{
									VSet1.add(vid);   //VSet��sv1������sv2�Ľӿڽڵ�
								}
								for(int vid: se.Incid2)
								{
									VSet2.add(vid);
								}
							}
							else
							{
								for(int vid: se.Incid2)
								{
									VSet1.add(vid);   
								}
								for(int vid: se.Incid1)
								{
									VSet2.add(vid);
								}
							}
							break;
						}
					}
					VSets.add(VSet1);
					VSets.add(VSet2);
				}
					
				//Step3: ��ʼ��sv1���ⲿ�ڵ�
				HashMap<Integer,String> shortestPathOrigin = new HashMap<Integer,String>();   //��ֹ��ĿǰΪֹ�Ľڵ�����·��������������ɣ��ֱ���<��id, ����һ������id�����·��:����>
				if(sv1.outVertex.contains(VId1))   //���v1���ⲿ�ڵ㣬��ô�Ѿ������VSet�����·��
				{
					for(Map.Entry<Integer, String> entry : v1.shortestPath.entrySet())
					{
						int vid2 = entry.getKey();
						if(VSets.get(0).contains(vid2))
						{
							shortestPathOrigin.put(vid2, entry.getValue());  //TODO ˳������
						}
					}
				}
				else  //���v1���ⲿ�ڵ㣬��ʼ����
				{
					ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
					if(sv1.containVertex != null)
					{
						 for(int vid:sv1.containVertex)
						 {
							 allVertexList.add(vertexList.get(vid));
						 }
						 double diffToDes = Double.MAX_VALUE;
						 shortestPathOrigin = cn2.calculatedijkstraMatrix(vertexList, v1, VSets.get(0), allVertexList,diffToDes);//��v1������VSet�нڵ����̾���
					}
				}
				
				
				//Step4: ��sv1��ʼ��sv2�ⲿ�ڵ�����·��
				for(int i=0; i<CVId.length-2; i++)
				{
					//int last = Integer.parseInt(CVId[i]);
					//int next = Integer.parseInt(CVId[i+1]);
					shortestPathOrigin = cn2.calculateNeighbor(VSets.get(i*2), VSets.get(i*2+1), VSets.get(i*2+2), shortestPathOrigin, vertexList);
				}
				
				//Step5: �ӵ����ڶ������ⲿ�ڵ㵽���һ�����ⲿ�ڵ�
				int size = CVId.length;
				//int last = Integer.parseInt(CVId[size-2]);
				//int next = Integer.parseInt(CVId[size-1]);
				shortestPathOrigin = cn2.calculateNeighbor(VSets.get((size-2)*2), VSets.get((size-2)*2+1), VSets.get((size-2)*2+1), shortestPathOrigin, vertexList);
				
				//Step6: �����һ�����ⲿ�ڵ㵽v2
				if(VSets.get((size-2)*2+1).contains(VId2))   //���v1���ⲿ�ڵ㣬��ô�Ѿ������VSet�����·��
				{
					for(Map.Entry<Integer, String> entry : shortestPathOrigin.entrySet())
					{
						int vid2 = entry.getKey();
						if(vid2 == VId2)
						{
							String str = entry.getValue();
							String[] pathFinal = str.split(",");
							for(int i=0 ;i<pathFinal.length-1; i++)
							{
								String ss = pathFinal[i];
								if(ss!=null && !ss.equals("") && !ss.equals(pathFinal[i+1]))
								{
									shortestPath+=ss + ",";
								}
							}
							shortestPath+= pathFinal[pathFinal.length-1];
							//System.out.println(shortestPath);
							//System.out.println("Our���������ڽڵ�����·����"+ (end2.getTime() - start2.getTime()) + " total milliseconds"); 
							return str;
						}
					}
				}
				else  //���v1���ⲿ�ڵ㣬��ʼ����
				{
					ArrayList<Vertex> allVertexList = new ArrayList<Vertex>();
					if(sv2.containVertex != null)
					{
						 for(int vid:sv2.containVertex)
						 {
							 allVertexList.add(vertexList.get(vid));
						 }
					}
					
					double min = Double.MAX_VALUE;
					String path = "";
					
					for(Map.Entry<Integer, String> entry : shortestPathOrigin.entrySet())
					{
						String[] pathLasts = entry.getValue().split(":");
						String path0 = pathLasts[0];
						double cost1 = Double.parseDouble(pathLasts[1]);
						int vid = entry.getKey();
						Vertex v0 = vertexList.get(vid);
						String str = cn.calculateShortestPath(vertexList, allVertexList, v0, v2); 
						String[] strs = str.split(":");
						String path1 = strs[0];
						double cost2 = Double.parseDouble(strs[1]);
						if(cost2+cost1 < min)
						{
							min = cost2+cost1;
							path = path0+","+path1;
						}
					}
					
					String[] pathFinal = path.split(",");
					for(int i=0 ;i<pathFinal.length-1; i++)
					{
						String ss = pathFinal[i];
						if(ss!=null && !ss.equals("") && !ss.equals(pathFinal[i+1]))
						{
							shortestPath+=ss + ",";
						}
					}
					shortestPath+= pathFinal[pathFinal.length-1] + ":" + min;
					//System.out.println(shortestPath);
					//Date end2 = new Date();
					//System.out.println("Our���������ڽڵ�����·����"+ (end2.getTime() - start2.getTime()) + " total milliseconds"); 
					//return shortestpath;
				}
				System.out.println(shortestPath);
				Date end2 = new Date();
				System.out.println("Our������������·����"+ (end2.getTime() - start2.getTime()) + " total milliseconds"); 
				String[] cc = shortestPath.split(":");
				if(Double.parseDouble(cc[1])<minCost)
				{
					minCost = Double.parseDouble(cc[1]);
					shortestPathFinal = shortestPath;
					time = iter;
				}
			}
			System.out.println("�����ڵ�" + time + "�ε����У�������·����"+ shortestPathFinal);
		}
		return shortestPathFinal;
	}
	
	public static void main(String[] args) throws SQLException
	{
		/* case11:ͬһ���ţ������������ⲿ�ڵ�
		 String ad1 = "john.lavorato@enron.com";
		 String ad2 = "season@restructuringtoday.com";*/
		
		/* case11:ͬһ���ţ����������ⲿ�ڵ�
		 String ad1 = "bs_stone@yahoo.com";
		 String ad2 = "phillip.k.allen@enron.com";*/
		
		/* case21:��ͬ���ţ������㲻�ɴ�
		String ad1 = "andrea.richards@enron.com";
		String ad2 = "Julie Pechersky/ENRON@enronXgate@ENRON";*/
		
		/* case22:��ͬ����������ɴ�  */
		String ad1 = "llewter@austin.rr.com";
		String ad2 = ".susie@enron.com";
		//String ad2 = ".robert@enron.com";
		shortestPathPre();
		//sers = allPath.findAllPath(superVertexList.get(3), null, superVertexList.get(3), superVertexList.get(33), superVertexList);  //���غķ���̵�����·��
		int K  = 1;
		shortestPathQuery(ad1,ad2,K);
	}
}

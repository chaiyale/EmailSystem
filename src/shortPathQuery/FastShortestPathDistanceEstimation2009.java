package shortPathQuery;

import graph.Edge;
import graph.MakeGraph;
import graph.Vertex;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import SuperGraph.MakeSuperGraph;
import SuperGraph.SuperEdge;
import SuperGraph.SuperVertex;
import community.pSCAN;

public class FastShortestPathDistanceEstimation2009 
{
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //ԭ���㼯��
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //�߼���
	public static ArrayList<Vertex> landMark = new ArrayList<Vertex>();  //�ر꼯��
	public static ArrayList<SuperVertex> superVertexList;  //�����ڵ㼯��
	public static ArrayList<SuperEdge> superEdgeList; //�߼���
	public static int superVertex_Id = 0; //����ID
	public static int superEdge_Id = 0;   //��ID
	public static Map<Integer, Integer> map; //cid��ԭʼ����id�Ķ�Ӧ��ϵ
	
	public static double[][] shortestPathfromCb(double[][] vectorbyCb)    //��ÿ���ڵ��õر��ʾ
	{
 		int size = vectorbyCb[0].length;
		for(int i=0; i<vertexList.size(); i++)
		{
			//vectorbyCb[i] = new double[20];
			for(int j=0; j<size; j++)
			{
				vectorbyCb[i][j] = -1;
			}
		}
		
		for(int j=0; j<landMark.size(); j++)
		{
			Vertex la = landMark.get(j);
			for(Map.Entry<Integer, String> entry : la.shortestPath.entrySet()) 
			{
				int key = entry.getKey();
				String[] pathLasts = entry.getValue().split(":");
				double cost = Double.parseDouble(pathLasts[1]);
				vectorbyCb[key][j] = cost;
			}
		}
		return vectorbyCb;
	}
	
	public static void BorderLandMark() throws SQLException, ParseException   //��ԭͼ�ֳɶ��partition��Ȼ��ѡ��߽��ϵĵ㣨������partition�������ĵ㣩
	{
		//��ԭͼ�ֳɶ��partition ʹ��pSCAN
		vertexList = pSCAN.getVertex(vertexList,edgeList);      //ʹ��pCAN�������Ż���
		makeSuperGraph(); 
		
		//��ÿ��partition�еĵ㣬���㵽����partition��bu
		for(int i = 0;i < superVertexList.size();i++) 
		{
			SuperVertex sv = superVertexList.get(i);
			int max = 0;
			int maxId = sv.containVertex.iterator().next();
			for(Integer vid: sv.containVertex)
			{
				Vertex v = vertexList.get(vid);
				int degree = v.edge.size();
				int sum = 0;
				for(int v2id: v.neighborIdTypeALL)
				{
					Vertex v2 = vertexList.get(v2id);
					if(v2.communityId.iterator().next() != sv.cid)
					{
						sum += degree;
					}
				}
				if(sum > max)
				{
					max = sum;
					maxId = vid;
				}
			}
			landMark.add(vertexList.get(maxId));
		}
	}
	
	public static void makeSuperGraph()
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
		//System.out.println(end.getTime() - start.getTime() + " total milliseconds"); 
	}
	
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
	
	public static double estimationDistance(Vertex v1, Vertex v2, double[][] vectorbyCb) //�������㣬���ع���ľ���
	{
		//double[] add = new double[20];
		int no = 0;
		int vid1 = v1.getId();
		int vid2 = v2.getId();
		double dis = 0.0; 
		
		for(int j=0; j<landMark.size(); j++)
		{
			if(vectorbyCb[vid1][j] == -1 || vectorbyCb[vid2][j] == -1)
			{
				no ++;
			}
			else
			{
				dis += vectorbyCb[vid1][j] + vectorbyCb[vid2][j];
			}
		}
		dis /= (landMark.size() - no);
		return dis;
	}
	
	public static void main(String[] args) throws SQLException, ParseException 
	{
		//�����ݿ��ж���vertexList
		
		/*Date start = new Date(); //��ʱ
		proDataset pd = new proDataset();  //�������ݼ�
		String filePath = "./CA-GrQc.txt";
		pd.readTxtFile(filePath);
		pd.makeGraph();
		vertexList = pd.getVertex();
		edgeList = pd.getEdge(); */
		
		MakeGraph mg = new MakeGraph();   //�ʼ����ݼ�
		Date start = new Date(); //��ʱ
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		vertexList = mg.getVertex();
		edgeList = mg.getEdge();
		Date end = new Date();
		System.out.println("���ʼ����ݼ�ת��Ϊͼ��"+ (end.getTime() - start.getTime()) + " total milliseconds");
		
		common cn = new common();
		cn.initialCost(vertexList);
		
		//One:ѡȡ�ر�Ĳ��ԣ�������
		//landMark = brandesForCentrality.TopCb(vertexList); //���������ԣ�ѡ��ر�20��
		
		//Two:ѡȡ�ر�Ĳ��ԣ���ԭͼ�ֳɶ��partition��Ȼ��ѡ��߽��ϵĵ㣨������partition�������ĵ㣩
		BorderLandMark();
		
		cn.calculateShortestPath(vertexList, vertexList, landMark); //����ÿ���ڵ㵽�ر����̾���
		double[][] vectorbyCb = new double[vertexList.size()][landMark.size()];  //��ÿ���ڵ��õر��ʾ
		vectorbyCb = shortestPathfromCb(vectorbyCb);    //��ÿ���ڵ��õر��ʾ
	    
		//@Test
		System.out.println(estimationDistance(vertexList.get(74),vertexList.get(673),vectorbyCb));
	}
}

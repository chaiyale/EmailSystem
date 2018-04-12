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
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //原顶点集合
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //边集合
	public static ArrayList<Vertex> landMark = new ArrayList<Vertex>();  //地标集合
	public static ArrayList<SuperVertex> superVertexList;  //超级节点集合
	public static ArrayList<SuperEdge> superEdgeList; //边集合
	public static int superVertex_Id = 0; //顶点ID
	public static int superEdge_Id = 0;   //边ID
	public static Map<Integer, Integer> map; //cid和原始社团id的对应关系
	
	public static double[][] shortestPathfromCb(double[][] vectorbyCb)    //将每个节点用地标表示
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
	
	public static void BorderLandMark() throws SQLException, ParseException   //将原图分成多个partition，然后选择边界上的点（与其他partition连接最多的点）
	{
		//将原图分成多个partition 使用pSCAN
		vertexList = pSCAN.getVertex(vertexList,edgeList);      //使用pCAN进行社团划分
		makeSuperGraph(); 
		
		//对每个partition中的点，计算到其他partition的bu
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
		//System.out.println(end.getTime() - start.getTime() + " total milliseconds"); 
	}
	
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
	
	public static double estimationDistance(Vertex v1, Vertex v2, double[][] vectorbyCb) //给两个点，返回估算的距离
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
		//从数据库中读出vertexList
		
		/*Date start = new Date(); //计时
		proDataset pd = new proDataset();  //测试数据集
		String filePath = "./CA-GrQc.txt";
		pd.readTxtFile(filePath);
		pd.makeGraph();
		vertexList = pd.getVertex();
		edgeList = pd.getEdge(); */
		
		MakeGraph mg = new MakeGraph();   //邮件数据集
		Date start = new Date(); //计时
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		vertexList = mg.getVertex();
		edgeList = mg.getEdge();
		Date end = new Date();
		System.out.println("将邮件数据集转化为图："+ (end.getTime() - start.getTime()) + " total milliseconds");
		
		common cn = new common();
		cn.initialCost(vertexList);
		
		//One:选取地标的策略：中心性
		//landMark = brandesForCentrality.TopCb(vertexList); //按照中心性，选择地标20个
		
		//Two:选取地标的策略：将原图分成多个partition，然后选择边界上的点（与其他partition连接最多的点）
		BorderLandMark();
		
		cn.calculateShortestPath(vertexList, vertexList, landMark); //计算每个节点到地标的最短距离
		double[][] vectorbyCb = new double[vertexList.size()][landMark.size()];  //将每个节点用地标表示
		vectorbyCb = shortestPathfromCb(vectorbyCb);    //将每个节点用地标表示
	    
		//@Test
		System.out.println(estimationDistance(vertexList.get(74),vertexList.get(673),vectorbyCb));
	}
}

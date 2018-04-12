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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import reachQuery.reachQuery;
import dataProcess.proDataset;
import database.dataAccess;
import SuperGraph.MakeSuperGraph;
import SuperGraph.SuperEdge;
import SuperGraph.SuperVertex;
import SuperGraph.modularity;

public class ComparativeExperiment 
{
	public static ArrayList<SuperVertex> superVertexList;  //超级节点集合
	public static ArrayList<SuperEdge> superEdgeList; //边集合
	public static ArrayList<Vertex> vertexList;  //原顶点集合
	//public static ArrayList<Vertex> landMark = new ArrayList<Vertex>();  //地标集合
	//public static int N = 10;
	public static double Terror = 0, Terror2 = 0, Terror3 = 0, Terror4 = 0;
		
	public static void WriteSpaceSPBOC()  throws IOException
	{
		String pathname = "C:/Users/17375/Desktop/中间结果/SPBOC.txt";
		File writename = new File(pathname); 
		writename.createNewFile(); // 创建新文件  
        BufferedWriter out = new BufferedWriter(new FileWriter(writename));  
		String writeIn = "";  int totalOut = 0;
		for(SuperVertex sv: superVertexList)
		{
			writeIn += sv.cid + sv.parentId;
			for(int i: sv.containVertex)
				writeIn += i;
			if(sv.outlierVertex.size()!=0)
			{
				for(int i: sv.outlierVertex)
					writeIn += i;
			}		
			if(sv.outVertex!=null && sv.outVertex.size()!=0)
			{
				for(int i: sv.outVertex)
					writeIn += i;
			}
			if(sv.edge!=null && sv.edge.size()!=0)
			{
				for(SuperEdge i: sv.edge)
					writeIn += i.ceid;
			}
			if(sv.neighborId!=null && sv.neighborId.size()!=0)
			{
				for(int i: sv.neighborId)
					writeIn += i;
			}
			if(sv.outVertex!=null)
				totalOut += (sv.outVertex.size())*(sv.outVertex.size());
			if(sv.shortestPath2!= null)
			{
				for(int id: sv.shortestPath2.keySet())
					writeIn += id+","+ sv.shortestPath2.get(id);
			}
			out.write(writeIn); // \r\n即为换行  
	        out.flush(); // 把缓存区内容压入文件  
	        writeIn = "";
		}
		for(SuperEdge se: superEdgeList)
		{
			writeIn += se.ceid + se.cid1 + se.cid2 + se.cost + se.weight;
			out.write(writeIn); // \r\n即为换行  
	        out.flush(); // 把缓存区内容压入文件  
	        writeIn = "";
		}
		for(int i=0;i<totalOut;i++)
		{
			writeIn += "1" + "1" + "1" + "1";
			out.write(writeIn); // \r\n即为换行  
	        out.flush(); // 把缓存区内容压入文件  
	        writeIn = "";
		}
        out.close(); // 最后记得关闭文件  
	}
	
	public static void WriteSpaceSPCD()  throws IOException
	{
		String pathname = "C:/Users/17375/Desktop/中间结果/SPCD.txt";
		File writename = new File(pathname); 
		writename.createNewFile(); // 创建新文件  
        BufferedWriter out2 = new BufferedWriter(new FileWriter(writename));  
	    String writeIn = "";  
        for(SuperVertex sv: superVertexList)
		{
			writeIn += sv.cid + sv.parentId;
			for(int i: sv.containVertex)
				writeIn += i;
			out2.write(writeIn); // \r\n即为换行  
	        out2.flush(); // 把缓存区内容压入文件  
	        writeIn = "";
		}
		for(SuperEdge se: superEdgeList)
		{
			writeIn += se.ceid + se.cid1 + se.cid2 + se.cost + se.weight;
			out2.write(writeIn); // \r\n即为换行  
	        out2.flush(); // 把缓存区内容压入文件  
	        writeIn = "";
		}
        out2.close(); // 最后记得关闭文件  
	}
	
	public static void shortestPathPreSixStep()    //计算每个超点三个跳步之内的最短路径
	{
		Date start = new Date();
		for(SuperVertex sv: superVertexList)
		{
			if(sv.edge ==null || sv.edge.size()==0)
				continue;
			HashMap<Integer,String> SP = new HashMap<Integer,String>();
			HashMap<Integer,Double> SD = new HashMap<Integer,Double>();
			ArrayList<Integer> nextLevel = new ArrayList<Integer>();
			ArrayList<Integer> nextLevel2 = new ArrayList<Integer>();
			for(SuperEdge se: sv.edge)
			{
				int nei = (se.cid1==sv.cid)?se.cid2:se.cid1;
				SP.put(nei, ":" + se.cost);
				SD.put(nei, se.cost);
				nextLevel.add(nei);
			}
			for(int svid: nextLevel)
			{
				double co = SD.get(svid);
				SuperVertex newsv = superVertexList.get(svid);
				for(SuperEdge se: newsv.edge)
				{
					int nei = (se.cid1==svid)?se.cid2:se.cid1;
					if(nei == sv.cid) continue;
					if(SP.containsKey(nei) && (SD.get(nei)> co+se.cost))
					{
						String a = SP.get(svid)+","+svid;
						String b = co+se.cost+"";
						SP.put(nei, a + ":" + b);
						SD.put(nei, co+se.cost);
					}
					else
					{
						String a = SP.get(svid)+","+svid;
						String b = co+se.cost+"";
						SP.put(nei, a + ":" + b);
						SD.put(nei, co+se.cost);
						nextLevel2.add(nei);
					}
				}
			}
			for(int svid: nextLevel2)
			{
				double co = SD.get(svid);
				SuperVertex newsv = superVertexList.get(svid);
				for(SuperEdge se: newsv.edge)
				{
					int nei = (se.cid1==svid)?se.cid2:se.cid1;
					if(nei == sv.cid) continue;
					if((SP.containsKey(nei) && (SD.get(nei)> co+se.cost)) || !SP.containsKey(nei))
					{
						String a = SP.get(svid)+","+svid;
						String b = co+se.cost+"";
						SP.put(nei, a + ":" + b);
						SD.put(nei, co+se.cost);
					}
				}
			}
			for(int id: SP.keySet())
				sv.shortestPath2.put(id, SP.get(id));
		}
		Date end = new Date();
		System.out.println("OurApproach预处理时间PartII："+ (end.getTime() - start.getTime()) + " total milliseconds");
	}
	
	public static void shortestPathPre() throws SQLException, ParseException, IOException   //预处理
	{
		//获得超图，途径一：从数据库中读取；途径二：直接生成超图	
		common cn = new common();
		commonForSuper cn2 = new commonForSuper();
		dataAccess db = new dataAccess();
        superVertexList = db.readSuperVertex();
		vertexList = db.readVertex();
		superEdgeList = db.readSuperEdge();
		double maxWeight = cn.initialCost(vertexList);
		
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
	
		//Step2: 计算社团间距和landmark
		computeWeightPlanNewNo2();
		//computeWeightPlanNewHave3(maxWeight);
		cn2.initialCost2(superEdgeList);
		Date end = new Date();
		System.out.println("OurApproach预处理时间："+ (end.getTime() - start.getTime()) + " total milliseconds");
	}
	
	public static Boolean computeWeightPlanNewNo1() throws SQLException   //计算边的权重：随机从每个interface节点中选取一个作为地标
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
	
	public static Boolean computeWeightPlanNewNo2() throws SQLException   //计算边的权重：选取社团间最短的一条作为最短路径（最短的也就是权重最大的）
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
	
	public static Boolean computeWeightPlanNewNo3() throws SQLException   //计算边的权重：从每个interface节点中选取一个中心性最高的作为地标，以地标之间的距离作为社团间距
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
	
	public static Boolean computeWeightPlanNewHave1(double maxCost)   //随机从每个interface节点中选取一个作为地标，以地标之间的距离作为社团间距，考虑社团内部的距离
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
	
	public static Boolean computeWeightPlanNewHave2(double maxCost)   //选取最短一个作为地标，以地标之间的距离作为社团间距，考虑社团内部的距离
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
	
	public static Boolean computeWeightPlanNewHave3(double maxCost)   //从每个interface节点中选取一个中心性最高的作为地标，考虑社团内部的距离
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
	
	/*public static void Ep(ArrayList<SuperVertex> superVertexList, ArrayList<Vertex> vertexList, int N) throws SQLException, ParseException
	{   //预处理
		common cn = new common();
		commonForSuper cn2 = new commonForSuper();
		shortestPathPre(superVertexList, vertexList);   //OurApproach
		//double[][] vectorbyCb = ALT2005.ALTPre(vertexList);
		
		//测试数据
		ArrayList<Vertex> randomVertexList = new ArrayList<Vertex>();
		int vertexSize = vertexList.size()-1;
		for(int i=0; i<N*2; i++)    //随机选取n对节点
		{
			int ra = (int)(Math.random()*(vertexSize-0+1)); 
			randomVertexList.add(vertexList.get(ra));
		}
		int sum1=0, sum2=0, sum3=0, sum4=0; int k=0;
		for(int i=0; i<randomVertexList.size(); i+=2)
		{
			Vertex s = randomVertexList.get(i); 
			Vertex d = randomVertexList.get(i+1);
			SuperVertex sv1 = superVertexList.get(i);
			SuperVertex sv2 = superVertexList.get(i+1);
			String ser = cn2.calculateShortestPath(superEdgeList, superVertexList, sv1, sv2);
			//System.out.println("ser:"+ser);
			
			//case21: 两个超点之间不可达，直接返回null ---应该不会运行到这里，从一开始就已经判断了
			if(ser==null)
			{
				continue;
			}
			k++;
			System.out.print("s:"+s.getId() + ", d:"+d.getId()+ "===");

		    /*Vertex s = vertexList.get(908); 
		    Vertex d = vertexList.get(1539);*/
		    //普通Dijkstra
			/*Date start = new Date();
			String str = cn.calculateShortestPathforWholeGraph2(vertexList, s, d);  //使用普通Dijkstra算法
			Date end = new Date();
			sum1 += end.getTime() - start.getTime();
			System.out.print((end.getTime() - start.getTime()) + ", "); 
			//System.out.println("Dijkstra计算两点之间最短路径："+ (end.getTime() - start.getTime()) + " total milliseconds"); */
			
			//双向Dijkstra
		    /*Date start2 = new Date();
			str = cn.calculateShortestPathforWholeGraph(vertexList, s, d);  //使用双向Dijkstra算法
			Date end2 = new Date();
			sum2 += end2.getTime() - start2.getTime();
			System.out.print((end2.getTime() - start2.getTime()) + ", "); 
			//System.out.println("双向Dijkstra计算两点之间最短路径："+ (end2.getTime() - start2.getTime()) + " total milliseconds"); 
			
			//OurApproach
			//String[] strs = str.split(":");
			//double cost = Double.parseDouble(strs[1]);
			int time = ourApproach.shortestPathQueryNewTime(superVertexList, vertexList, s, d, 0);
			sum3 += time;
			System.out.print(time+ ", "); 
			//System.out.println("Our计算两点之间最短路径："+ time + " total milliseconds"); 
			
			//ALT2005
			/*Date start4 = new Date();
			ALT2005.ALT2(vertexList, s, d, vectorbyCb);
			Date end4 = new Date();
			sum4 += end4.getTime() - start4.getTime();
			System.out.println((end4.getTime() - start4.getTime()) + "  str: " + str); 
			//System.out.println("ALT2005计算两点之间最短路径："+ (end4.getTime() - start4.getTime()) + " total milliseconds"); 
		}
		System.out.println("sum1="+sum1+", "+"sum2="+sum2+", "+"sum3="+sum3+", "+"sum4="+sum4);
		System.out.println("1="+sum1*1.0/k*1.0+", "+"2="+sum2*1.0/k*1.0+", "+"3="+sum3*1.0/k*1.0+", "+"4="+sum4*1.0/k*1.0);
	}*/
	
	public static void main(String[] args) throws SQLException, ParseException, IOException  //for all
	{			
		//预处理
		common cn = new common();
		shortestPathPre();   //OurApproach
		shortestPathPreSixStep();
		//ArrayList<ArrayList<Double>> vectorbyCb = ALT2005.ALTPre(vertexList);
		//WriteSpaceSPBOC();
		//WriteSpaceSPCD();
		Date startt = new Date();
		HashSet<Vertex> GL = LLS2012.LLSPre(vertexList);   //LLSh
		ArrayList<ArrayList<Integer>> traceAll = LLS2012.gettraceAll();
		ArrayList<ArrayList<Integer>> LAll = LLS2012.getLAll();
		ArrayList<HashMap<Integer,Integer>> stampAll = LLS2012.getstampAll();
		Date endd = new Date();
		System.out.println("LLS预处理时间："+ (endd.getTime() - startt.getTime()) + " total milliseconds"); 
		//LLS2012.WriteSpace(GL, traceAll, LAll, stampAll);
		int N=200;
		
		//测试数据
		ArrayList<Vertex> randomVertexList = new ArrayList<Vertex>();
		int vertexSize = vertexList.size()-1;
		for(int i=0; i<N*2; i++)    //随机选取n对节点
		{
			int ra = (int)(Math.random()*(vertexSize-0+1)); 
			randomVertexList.add(vertexList.get(ra));
		} 
		int sum1=0, sum2=0, sum3=0, sum4=0, spboc_=0, lls=0;   int k=0; int perfect=0; int allow=0; double worst= 0, worst1=0, worst2=0;
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

		    //Vertex s = vertexList.get(839); 
		    //Vertex d = vertexList.get(797);
		    //普通Dijkstra
			Date start = new Date();
			String str = cn.calculateShortestPathforWholeGraph2(vertexList, s, d);  //使用普通Dijkstra算法
			Date end = new Date();
			sum1 += end.getTime() - start.getTime();
			System.out.print((end.getTime() - start.getTime()) + ", "); 
			//System.out.println("Dijkstra计算两点之间最短路径："+ (end.getTime() - start.getTime()) + " total milliseconds"); 
			
			//双向Dijkstra
		    Date start2 = new Date();
			cn.calculateShortestPathforWholeGraph(vertexList, s, d);  //使用双向Dijkstra算法
			Date end2 = new Date();
			sum2 += end2.getTime() - start2.getTime();
			System.out.print((end2.getTime() - start2.getTime()) + ", "); 
			//System.out.println("双向Dijkstra计算两点之间最短路径："+ (end2.getTime() - start2.getTime()) + " total milliseconds");
			
			//OurApproach SPBOC
			if(str == null)  continue;
			String[] strs = str.split(":");
			//System.out.println("str: " + str);
			double cost = Double.parseDouble(strs[1]);
			//int time = ourApproach.shortestPathQueryNewTimeWithoutPrune(superEdgeList, superVertexList, vertexList, s, d, 0);
			int time = ourApproach.shortestPathQueryNewTime(superEdgeList, superVertexList, vertexList, s, d, 0);
			double error = ourApproach.shortestPathQueryNewError(superEdgeList, superVertexList, vertexList, s, d, cost);
			if(error > 10 || error<0) 
			{
				continue;
			}
			Terror += error;
			sum3 += time;
			System.out.print(time+ ", "); 
			//System.out.println("Our计算两点之间最短路径："+ time + " total milliseconds"); 
			
			//OurApproach SPBOC' Error2
			//double error2 = ourApproach.shortestDistanceQuery2Error(superEdgeList, superVertexList, vertexList, s, d, cost);
			//time = ourApproach.shortestDistanceQuery2Time(superEdgeList, superVertexList, vertexList, s, d);
			//if(error2 > 10) 
			//{
				//Terror -= error;
				//continue;
			//}
			//Terror3 += error2;
			//if(worst2<error2) worst2=error2;
			//spboc_ += time;
			
			//LLS 估算距离
			Date start3 = new Date();
			double appDis = LLS2012.LLSQuery(GL, traceAll, LAll, stampAll, s, d);
			double error3 = Math.abs((appDis-cost)/cost);
			Date end3 = new Date();
			lls += end3.getTime() - start3.getTime();
			System.out.println((end3.getTime() - start3.getTime()) + ", "); 
			Terror4 += error3;
			
			//ALT2005
			/*Date start4 = new Date();
			ALT2005.ALT2(vertexList, s, d, vectorbyCb);
			Date end4 = new Date();
			sum4 += end4.getTime() - start4.getTime();
			System.out.println((end4.getTime() - start4.getTime())); 
			//System.out.println("ALT2005计算两点之间最短路径："+ (end4.getTime() - start4.getTime()) + " total milliseconds"); */
			
			
		}
	    System.out.println("sum1="+sum1+", "+"sum2="+sum2+", "+"sum3="+sum3+", "+"sum4="+sum4+ ", "+"lls="+lls);
	    System.out.println("Dij="+sum1*1.0/k*1.0+", "+"biDij="+sum2*1.0/k*1.0+", "+"SPBOC="+sum3*1.0/k*1.0+", "+"ALT="+sum4*1.0/k*1.0+", "+"lls="+lls*1.0/k*1.0);
	    System.out.println("k="+k+",  SpbocError:"+Terror*1.0/N + ",  llsError:"+ Terror4*1.0/N);
	}
	
	/*public static void main(String[] args) throws SQLException, ParseException, IOException  
	{			
		//预处理
		common cn = new common();
		shortestPathPre();   //OurApproach
        shortestPathPreSixStep();
		int N=100;
		
		//测试数据
		ArrayList<Vertex> randomVertexList = new ArrayList<Vertex>();
		int vertexSize = vertexList.size()-1;
		for(int i=0; i<N*2; i++)    //随机选取n对节点
		{
			int ra = (int)(Math.random()*(vertexSize-0+1)); 
			randomVertexList.add(vertexList.get(ra));
		} 
		int sum1=0, sum2=0, sum3=0, sum4=0, spboc_=0, lls=0;   int k=0; int perfect=0; int allow=0; double worst= 0, worst1=0, worst2=0;
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

		    //Vertex s = vertexList.get(839); 
		    //Vertex d = vertexList.get(797);
			
			//普通Dijkstra
			Date start = new Date();
			String str = cn.calculateShortestPathforWholeGraph2(vertexList, s, d);  //使用普通Dijkstra算法
			Date end = new Date();
			sum1 += end.getTime() - start.getTime();
			System.out.print((end.getTime() - start.getTime()) + ", "); 
			
			//OurApproach SPBOC
			if(str == null) continue;
			String[] strs = str.split(":");
			//System.out.println("str: " + str);
			double cost = Double.parseDouble(strs[1]);
			//int time = ourApproach.shortestPathQueryNewTimeWithoutPrune(superEdgeList, superVertexList, vertexList, s, d, 0);
			int time = ourApproach.shortestPathQueryNewTime(superEdgeList, superVertexList, vertexList, s, d, 0);
			//double error = ourApproach.shortestPathQueryNewError(superEdgeList, superVertexList, vertexList, s, d, cost);
			//if(error > 10 || error<0) 
			{
				//continue;
			}
			//Terror += error;
			//if(error<=0.15) allow++;
			//if(error<=0.05) perfect++;
	        //if(worst<error) worst=error;
			sum3 += time;
			System.out.println(time+ ", "); 
			//System.out.println("Our计算两点之间最短路径："+ time + " total milliseconds"); 
			
		}
	    System.out.println("sum1="+sum1+", "+"sum2="+sum2+", "+"sum3="+sum3+", "+"sum4="+sum4+ ", "+"spboc'="+spboc_ + ", "+"lls="+lls);
	    System.out.println("1="+sum1*1.0/k*1.0+", "+"2="+sum2*1.0/k*1.0+", "+"3="+sum3*1.0/k*1.0+", "+"4="+sum4*1.0/k*1.0+", "+"spboc'="+spboc_*1.0/k*1.0+", "+"lls="+lls*1.0/k*1.0);
	    System.out.println("k="+k+",  error:"+Terror*1.0/N + ", worst:"+ worst + 
	    		            ",  spboc':"+Terror3*1.0/N +  ", worst2':"+ worst2 + 
	    		            ",  lls:"+ Terror4*1.0/N);
	}*/
}

package graph;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import database.dataAccess;
import database.dataAccessBase;

public class MakeGraph 
{
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //顶点集合
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //边集合
	public static int Vertex_Id = 0; //顶点ID
	public static int Edge_Id = 0;   //边ID
	public static Map<Integer, String> NameId = new HashMap<Integer, String>(); //邮箱名和ID的对应关系
	
	public MakeGraph()
	{
	}
	
	public static int containVertex(String newV) //顶点集合中是否包含
	{
		for(Vertex v:vertexList)
		{
			if(v.getName().equals(newV))
			{
				return v.getId();
			}
		}
		return -1;
	}
	
	public static int addVertex(String Name)  //增加顶点
	{
		Vertex v = new Vertex(Vertex_Id, Name);
		vertexList.add(v);
		NameId.put(Vertex_Id, Name);
		Vertex_Id++;
		return Vertex_Id-1;
	}
	
	public static Edge containEdge(int from, int to)  //边集合中是否包含
	{
		for(Edge e:edgeList)
		{
			if((e.nid1 == from && e.nid2 == to) || (e.nid2 == from && e.nid1 == to))
			{
				return e;
			}
		}
		return null;
	}
	
	public static Edge addEdge(int from, int to)  //增加边
	{
		Edge e = new Edge(Edge_Id, from, to);
		edgeList.add(e);
		Edge_Id++;
		return e;
	}
	
	public static int computeDate(String time)   //计算邮件距离今天的时间
	{
		SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int day = 0;
		try {
			Date date = format.parse(time);
			day = (int) ((System.currentTimeMillis()-date.getTime())/86400000); //一天86400秒，这个单位是毫秒
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		//System.out.println("day = "+day);
		return day;
	}
	
	public void makeGraph() throws SQLException, ParseException  //将邮件信息转化为图
	{
		//Date start = new Date(); //计时
		dataAccess db = new dataAccess();
		ResultSet record = db.getRecord();
	
		while(record.next()) //遍历数据库的每一行，加入社会网络图中
		{
			if(record.getString("m_from") == null || record.getString("m_from").equals("") || record.getString("m_from").equals(";") || record.getString("m_to") == null || 
					record.getString("m_to").equals("") || record.getString("m_to").equals(";"))
			{
				continue;
			}
			String from = record.getString("m_from").trim(); //发件人
			ArrayList<String> to = new ArrayList<String>();  //收件人
			String[] tempto = record.getString("m_to").trim().split(";"); //收件人，收件人可能有多个，以分号间隔（在Enron数据集）
			for(String t:tempto)
			{
				if(t.contains("@") && t!=null && !t.trim().equals(from))
				{
					to.add(t.trim());
					//System.out.println(t);
				}
			}
			if(to.size()==0)
			{
				continue;
			}
			//System.out.println(to.size());
			ArrayList<String> cc = new ArrayList<String>();  //cc、bcc一视同仁为cc
			if(record.getString("cc")!=null)
			{
				String[] c = record.getString("cc").trim().split(";"); //抄送方
				for(int i=0;i<c.length;i++)
				{
					if(!(c[i].equals("") || c[i]==null || c[i].equals(from)))
					{
						cc.add(c[i].trim());
					}
				}
			}
			if(record.getString("bcc")!=null)
			{
				String[] bc = record.getString("bcc").trim().split(";"); //抄送方
				for(int i=0;i<bc.length;i++)
				{
					if(!(bc[i].equals("") || bc[i]==null || bc[i].equals(from)))
					{
						cc.add(bc[i].trim());
					}
				}
			}
			
			//***************每封邮件构建一个mail*********************
			Mail mail = new Mail();
            mail.mail_Id = Integer.valueOf(record.getString("id")); //ID	    
//			mail.batch = record.getString("batch");  //批次
//			mail.time = computeDate(record.getString("date"));  //时间
//			mail.domain = record.getString("domain"); //领域
			//System.out.println(mail.mail_Id+":"+mail.time);
			
			//*******************构建图顶点**********************
			int fromId;
			int[] toId = new int[to.size()];
			int[] ccId = new int[cc.size()];
			if((fromId=containVertex(from))==-1) //将未出现过的加入顶点集合 
			{
				fromId = addVertex(from);  
				//System.out.println("fromId:"+fromId+"  "+NameId.get(fromId));
			}
			int toIdlen=0, ccIdlen = 0;
			for(String t:to)
			{
				int tid;
				if((tid=containVertex(t))==-1)
				{
					tid = addVertex(t);
					//System.out.println("toId:"+tid+"  "+NameId.get(tid));
				}
				toId[toIdlen] = tid;
				toIdlen++;
			}
			for(String t:cc)
			{
				int tid; int i =0;
				if((tid=containVertex(t))==-1)
				{
					tid = addVertex(t); 
				}
				//System.out.println("ccId:"+tid+"  "+NameId.get(tid));
				ccId[ccIdlen] = tid;
				ccIdlen++;
			}
			
			//********************构建图的边*********************
			for(int i=0;i<toId.length;i++)
			{
				Edge e;
				if((e=containEdge(fromId, toId[i]))==null)
				{
					e = addEdge(fromId, toId[i]);
					vertexList.get(fromId).edge.add(e);  //建立点和边之间的关系
					vertexList.get(fromId).neighborIdType1.add(toId[i]);
					vertexList.get(fromId).neighborIdTypeALL.add(toId[i]);
					vertexList.get(toId[i]).edge.add(e);
					vertexList.get(toId[i]).neighborIdType1.add(fromId);
					vertexList.get(toId[i]).neighborIdTypeALL.add(fromId);
				}
				if(e.nid1 == fromId)
				{
					e.nid1Tonid2.add(mail);
					e.nid1Withnid2.add(mail);
				}
				else
				{
					e.nid2Tonid1.add(mail);
					e.nid1Withnid2.add(mail);
				}
			}
			for(int i=0;i<ccId.length;i++)
			{
				Edge e;
				if((e=containEdge(fromId, ccId[i]))==null)
				{
					e = addEdge(fromId, ccId[i]);
					vertexList.get(fromId).edge.add(e);  //建立点和边之间的关系
					vertexList.get(fromId).neighborIdType1.add(ccId[i]);
					vertexList.get(fromId).neighborIdTypeALL.add(ccId[i]);
					vertexList.get(ccId[i]).edge.add(e);
					vertexList.get(ccId[i]).neighborIdType1.add(fromId);
					vertexList.get(ccId[i]).neighborIdTypeALL.add(fromId);
				}
				if(e.nid1 == fromId)
				{
					e.nid1Tonid2byCC.add(mail);
					e.nid1Withnid2.add(mail);
				}
				else
				{
					e.nid2Tonid1byCC.add(mail);
					e.nid1Withnid2.add(mail);
				}
			}
			if(toId.length>1)  //共同收件人之间的隐含边
			{
				for(int i=0; i<toId.length-1;i++)
				{
					for(int j=i+1; j<toId.length; j++)
					{
						if(toId[i]==toId[j])  continue;
						Edge e;
						if((e=containEdge(toId[i], toId[j]))==null)
						{
							e =addEdge(toId[i], toId[j]);
							vertexList.get(toId[i]).edge.add(e);  //建立点和边之间的关系
							vertexList.get(toId[i]).neighborIdTypeALL.add(toId[j]);
							vertexList.get(toId[j]).edge.add(e);
							vertexList.get(toId[j]).neighborIdTypeALL.add(toId[i]);
						}
						e.Co_recipient.add(mail);
					}
				}
				if(ccId.length>1)  //一个在收件人列表，一个人抄送人列表
				{
					for(int i=0; i<toId.length;i++)
					{
						for(int j=0; j<ccId.length; j++)
						{
							if(toId[i]==ccId[j])  continue;
							Edge e;
							if((e=containEdge(toId[i], ccId[j]))==null)
							{
								e = addEdge(toId[i], ccId[j]);
								vertexList.get(toId[i]).edge.add(e);  //建立点和边之间的关系
								vertexList.get(toId[i]).neighborIdTypeALL.add(ccId[j]);
								vertexList.get(ccId[j]).edge.add(e);
								vertexList.get(ccId[j]).neighborIdTypeALL.add(toId[i]);
							}
							e.Co_recipient.add(mail);
						}
					}
				}
			}
			if(ccId.length>1)  //共同抄送人之间的隐含边
			{
				for(int i=0; i<ccId.length-1;i++)
				{
					for(int j=i+1; j<ccId.length; j++)
					{
						if(ccId[i]==ccId[j])  continue;
						Edge e;
						if((e=containEdge(ccId[i], ccId[j]))==null)
						{
							e = addEdge(ccId[i], ccId[j]);
							vertexList.get(ccId[i]).edge.add(e);  //建立点和边之间的关系
							vertexList.get(ccId[i]).neighborIdTypeALL.add(ccId[j]);
							vertexList.get(ccId[j]).edge.add(e);
							vertexList.get(ccId[j]).neighborIdTypeALL.add(ccId[i]);
						}
						e.Co_recipient.add(mail);
					}
				}
			}
		}
		//Date end = new Date();
		//System.out.println(end.getTime() - start.getTime() + " total milliseconds"); 
	}
	
	public void computeWeightType1()  //计算第一型边的权重
	{ 
		Iterator<Vertex> iter1 = vertexList.iterator();
		while(iter1.hasNext())
		{
			Vertex v = iter1.next();//计算顶点中心性   
			v.centrality = v.edge.size();
			v.ed = v.edge.size() - 1;   //effective_degree
			for(int vid:v.neighborIdTypeALL)
			{
				v.neighborIdTypeALLbySort.add(vid);
			}
			Collections.sort(v.neighborIdTypeALLbySort);  
		}
		
		double arf = 2000;  //认为一年之内的邮件比较有参考价值
		Iterator<Edge> iter = edgeList.iterator();
		while(iter.hasNext())
		{
			Edge e = iter.next();			
			//*******************************计算第一型边的权重*************************************
			if(e.nid1Withnid2.size()==0)
			{
				continue;
			}
			int lg = e.nid1Withnid2.get(0).time;   //日期最远距离现在的天数
			int rec = e.nid1Withnid2.get(0).time;  //日期最近距离现在的天数
			for(Mail m:e.nid1Withnid2)  //找到最早的一封邮件和最晚的一封邮件
			{
				if(m.time > lg)
				{
					lg = m.time;
				}
				if(m.time < rec)
				{
					rec = m.time;
				}
			}
			e.longevity = lg;
			e.recency = rec;
			e.frequency = (e.nid1Withnid2.size()*1.0)/(lg-rec+1);  //频率
			//互惠度
			e.reciprocity = 1- Math.abs(e.nid1Tonid2.size() + e.nid1Tonid2byCC.size() - e.nid2Tonid1.size()- e.nid2Tonid1byCC.size())/(e.nid1Withnid2.size()*1.0);
			e.centrality = Math.sqrt(vertexList.get(e.nid1).centrality*vertexList.get(e.nid2).centrality);
			
			for(Mail m:e.nid1Tonid2)
			{
				double index = -(m.time*1.0/arf);
				e.availablityTo += Math.pow(Math.E, index);  //可用性
			}
			for(Mail m:e.nid2Tonid1)
			{
				double index = -(m.time*1.0/arf);
				e.availablityTo += Math.pow(Math.E, index);  //可用性
			}
			for(Mail m:e.nid1Tonid2byCC)
			{
				double index = -(m.time*1.0/arf);
				e.availablityCc += Math.pow(Math.E, index);  //可用性
			}
			for(Mail m:e.nid2Tonid1byCC)
			{
				double index = -(m.time*1.0/arf);
				e.availablityCc += Math.pow(Math.E, index);  //可用性
			}
			if(rec<0)
			{
				rec = 0;
			}
			double ee = Math.pow(Math.E, -rec)*Math.pow(Math.E, -(1.0/(lg-rec+1)));
			if(Double.isInfinite(ee))
			{
				System.out.print("WHAt");
			}
			e.weight += e.frequency+e.availablityTo +e.reciprocity + 0.5*(ee+e.availablityCc+e.centrality);
			//System.out.println(e.weight);
		}
	}
	
	public void computeWeightType2()  
	{
		//*******************************计算第二型边的权重*************************************
		Iterator<Edge> iter = edgeList.iterator();
		double max = 0; double min = 10000;
		while(iter.hasNext())
		{
			Edge e = iter.next();	
			Vertex v1 = vertexList.get(e.nid1);
			Vertex v2 = vertexList.get(e.nid2);
			if(e.Co_recipient.size()>0)//计算共现率
			{
				for(int xixi:v1.neighborIdType1)
				{
					for(int haha:v2.neighborIdType1)
					{
						if(xixi==haha)
						{
							e.co_occurrence ++;
						}
					}
				}
			}
			if(e.weight == 0)
			{
				e.weight += 0.5*e.centrality;
			}
			e.weight += 1.0*e.Co_recipient.size() + 0.7*e.co_occurrence;
			
			v1.weight += e.weight;
			v2.weight += e.weight;    //计算weight之和
			
			if(Double.isInfinite(e.weight))
			{
				System.out.print("WHAt");
			}
			/*if(e.weight>max)
			{
				max= e.weight;
			}
			if(e.weight<min)
			{
				min = e.weight;
			}*/
		}
		//System.out.println("max="+max+",  min="+min);
	}
	
	public ArrayList<Vertex> getVertex() throws SQLException, ParseException
	{
		return vertexList;
	}
	
	public ArrayList<Edge> getEdge() throws SQLException, ParseException
	{
		return edgeList;
	}
		
	public static void main(String[] args) throws SQLException, IOException, ParseException
	{
		MakeGraph mg = new MakeGraph();
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		dataAccess db = new dataAccess();
		db.writeVertex(vertexList);
		db.writeEdge(edgeList);
	}
}

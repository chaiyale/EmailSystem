package dataProcess;

import graph.Edge;
import graph.Vertex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import database.dataAccess;

public class proDatasetWeighted2 
{//预处理数据
	
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //顶点集合
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //边集合
	public static HashMap<Integer, HashMap<Integer,Float>> array = new HashMap<Integer, HashMap<Integer,Float>>();  //<点, <邻居，权重> >
	public static HashSet<Integer> vertexId = new HashSet<Integer>();    //顶点id集合
	public static int Edge_Id = 0;   //边ID
	public static Map<Integer, Integer> NameId = new HashMap<Integer, Integer>(); //原点ID和新点ID的对应关系
	public static int Vertex_Id = 0; //顶点ID
	
	/*public static int containVertex(int id) //顶点集合中是否包含
	{
		for(Vertex v:vertexList)
		{
			if(NameId.get(v.getId()) == id)
			{
				return v.getId();
			}
		}
		return -1;
	}*/
	
	public static void addVertex(int id)  //增加顶点
	{
		Vertex v = new Vertex(Vertex_Id);
		vertexList.add(v);
		NameId.put(id, Vertex_Id);
		Vertex_Id++;
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
	
	public void readTxtFile(String filePath)
	{
		try 
        {
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists())
            { //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    while((lineTxt = bufferedReader.readLine()) != null)
                    {
                    	//User.add(lineTxt);
                    	String[] mm = lineTxt.split("\t");
                    	int m0 = Integer.parseInt(mm[0]);
                    	int m1 = Integer.parseInt(mm[1]);
                    	float rate = Float.parseFloat(mm[2]);
                    	rate += 10;   rate*=2;
                    	HashMap<Integer,Float> Id2 = new HashMap<Integer,Float>();   
                    	if(array.containsKey(m0))
                    	{
                    		Id2 = array.get(m0);
                    		Id2.put(m1,rate);
                    	}
                    	else
                    	{  
                    		Id2.put(m1,rate);
                    	}
                    	array.put(m0, Id2);
                    	vertexId.add(m0);
                    	vertexId.add(m1);
                    }
                    read.close(); 
        }else{
            System.out.println("找不到指定的文件");
        }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
    }
	
	public void makeGraph()   //将将fromId,toId转化成边
	{
		ArrayList<Integer> vertexId2 = new ArrayList<Integer>();    //顶点id集合
		for(int vid:vertexId)
		{
			vertexId2.add(vid);
		}
		Collections.sort(vertexId2);  
		
		for(int vid:vertexId2)
		{
			addVertex(vid);
		}
		
		for(int vid:vertexId2)
		{
			if(array.containsKey(vid))
			{
				int realvid = NameId.get(vid);
				HashMap<Integer,Float> toids = array.get(vid);
				for(Map.Entry<Integer,Float> toidW:toids.entrySet())
				{
					int toid = toidW.getKey();
					float weight = toidW.getValue();
					int realvid2 = NameId.get(toid);
					Edge e;
					if((e=containEdge(realvid, realvid2))==null)
					{
						e = addEdge(realvid, realvid2);
						e.weight = weight;
						vertexList.get(realvid).edge.add(e);  //建立点和边之间的关系
						vertexList.get(realvid).neighborIdType1.add(realvid2);
						vertexList.get(realvid).neighborIdTypeALL.add(realvid2);
						vertexList.get(realvid2).edge.add(e);
						vertexList.get(realvid2).neighborIdType1.add(realvid);
						vertexList.get(realvid2).neighborIdTypeALL.add(realvid);
					}
				}
			}
		}
		
		for(Vertex v:vertexList)
		{
			v.centrality = v.edge.size();
			v.ed = v.edge.size() - 1;   //effective_degree
			for(int vid:v.neighborIdTypeALL)
			{
				v.neighborIdTypeALLbySort.add(vid);
			}
			Collections.sort(v.neighborIdTypeALLbySort);  
		}
	}
	
	public ArrayList<Vertex> getVertex() throws SQLException, ParseException
	{
		return vertexList;
	}
	
	public ArrayList<Edge> getEdge() throws SQLException, ParseException
	{
		return edgeList;
	}

	public static void main(String[] args) throws SQLException
	{
		String filePath = "./com-amazon.ungraph.txt";
		proDataset pd = new proDataset();
		pd.readTxtFile(filePath);
		pd.makeGraph();
		
		dataAccess db = new dataAccess();
		db.writeVertex(vertexList);
		db.writeEdge(edgeList);
	}
}


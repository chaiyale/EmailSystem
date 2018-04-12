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

public class proDataset 
{//预处理数据
	
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //顶点集合
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //边集合
	public static HashMap<Integer, ArrayList<Integer>> array = new HashMap<Integer, ArrayList<Integer>>();  //所有点之间的集合
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
                    	//m0--; m1--;
                    	/*if(m0>20000)
                    	{
                    		break;
                    	}
                    	else if(m1>20000)
                    	{
                    		continue;
                    	}*/
                    	ArrayList<Integer> Id2 = new ArrayList<Integer>();   
                    	if(array.containsKey(m0))
                    	{
                    		Id2 = array.get(m0);
                    		Id2.add(m1);
                    	}
                    	else
                    	{  
                    		Id2.add(m1);
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
				ArrayList<Integer> toids = array.get(vid);
				for(int toid:toids)
				{
					int realvid2 = NameId.get(toid);
					Edge e;
					if((e=containEdge(realvid, realvid2))==null)
					{
						e = addEdge(realvid, realvid2);
						//Random random = new Random();
						float rand = (float) (Math.random()*10);
						e.weight = rand;    //随机赋予边的值
						vertexList.get(realvid).edge.add(e);  //建立点和边之间的关系
						vertexList.get(realvid).neighborIdType1.add(realvid2);
						vertexList.get(realvid).neighborIdTypeALL.add(realvid2);
						vertexList.get(realvid2).edge.add(e);
						vertexList.get(realvid2).neighborIdType1.add(realvid);
						vertexList.get(realvid2).neighborIdTypeALL.add(realvid);
					}
					else
					{
						e.weight *= 2;
					}
				}
				//System.out.println(vid);
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

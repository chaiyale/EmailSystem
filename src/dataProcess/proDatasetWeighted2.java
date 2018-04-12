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
{//Ԥ��������
	
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //���㼯��
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //�߼���
	public static HashMap<Integer, HashMap<Integer,Float>> array = new HashMap<Integer, HashMap<Integer,Float>>();  //<��, <�ھӣ�Ȩ��> >
	public static HashSet<Integer> vertexId = new HashSet<Integer>();    //����id����
	public static int Edge_Id = 0;   //��ID
	public static Map<Integer, Integer> NameId = new HashMap<Integer, Integer>(); //ԭ��ID���µ�ID�Ķ�Ӧ��ϵ
	public static int Vertex_Id = 0; //����ID
	
	/*public static int containVertex(int id) //���㼯�����Ƿ����
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
	
	public static void addVertex(int id)  //���Ӷ���
	{
		Vertex v = new Vertex(Vertex_Id);
		vertexList.add(v);
		NameId.put(id, Vertex_Id);
		Vertex_Id++;
	}
	
	public static Edge containEdge(int from, int to)  //�߼������Ƿ����
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
	
	public static Edge addEdge(int from, int to)  //���ӱ�
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
            { //�ж��ļ��Ƿ����
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//���ǵ������ʽ
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
            System.out.println("�Ҳ���ָ�����ļ�");
        }
        } catch (Exception e) {
            System.out.println("��ȡ�ļ����ݳ���");
            e.printStackTrace();
        }
    }
	
	public void makeGraph()   //����fromId,toIdת���ɱ�
	{
		ArrayList<Integer> vertexId2 = new ArrayList<Integer>();    //����id����
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
						vertexList.get(realvid).edge.add(e);  //������ͱ�֮��Ĺ�ϵ
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


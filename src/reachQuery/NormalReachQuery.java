package reachQuery;

import graph.Edge;
import graph.MakeGraph;
import graph.Vertex;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import SuperGraph.SuperEdge;

public class NormalReachQuery 
{
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //ԭ���㼯��
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //�߼���
	
	public static void calculateReachId()    //������ͨ�ڵ�Ĵ��ݱհ�
	{
	   boolean arr[][] = new boolean[vertexList.size()][vertexList.size()];
	   Iterator<Edge> itEdge = edgeList.iterator();
       while(itEdge.hasNext()) 
       {  //��ʼ���ڽӾ���
           Edge se = itEdge.next();
           arr[se.nid1][se.nid2] = true;
           arr[se.nid2][se.nid1] = true;
       }
       for(int i = 0;i < vertexList.size();i++) { //��
           for(int j = 0;j < vertexList.size();j++) {  //��
               for(int k = 0;k < vertexList.size();k++) {  //ÿ���е���
                   arr[j][k] = arr[j][k] || (arr[j][i] && arr[i][k]);
               }
           }
       }
       for(int i = 0;i < vertexList.size();i++) 
       {
           for (int j = 0;j < vertexList.size();j++) 
           {
        	   if(arr[i][j] == true && j!= i) 
        	   {
        		   vertexList.get(i).reachId.add(j);
        	   }
           }
       }
	}
	
	public static void Normal(ArrayList<Vertex> vList,  ArrayList<Edge> eList)
	{
		vertexList = vList;
		edgeList = eList;
		calculateReachId();
	}
	
	public static void main(String[] args) throws SQLException, ParseException
	{
		Date start = new Date(); //��ʱ
		MakeGraph mg = new MakeGraph();   
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		vertexList = mg.getVertex();
		edgeList = mg.getEdge();
		Date end = new Date();
		System.out.println("���ʼ����ݼ�ת��Ϊͼ��"+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		start = new Date(); //��ʱ
		calculateReachId();
		end = new Date();
		System.out.println("Normal�����Դ��ݱհ���"+ (end.getTime() - start.getTime()) + " total milliseconds"); 
	}
}

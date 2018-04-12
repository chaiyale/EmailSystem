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
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //原顶点集合
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //边集合
	
	public static void calculateReachId()    //计算普通节点的传递闭包
	{
	   boolean arr[][] = new boolean[vertexList.size()][vertexList.size()];
	   Iterator<Edge> itEdge = edgeList.iterator();
       while(itEdge.hasNext()) 
       {  //初始化邻接矩阵
           Edge se = itEdge.next();
           arr[se.nid1][se.nid2] = true;
           arr[se.nid2][se.nid1] = true;
       }
       for(int i = 0;i < vertexList.size();i++) { //列
           for(int j = 0;j < vertexList.size();j++) {  //行
               for(int k = 0;k < vertexList.size();k++) {  //每行中的列
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
		Date start = new Date(); //计时
		MakeGraph mg = new MakeGraph();   
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		vertexList = mg.getVertex();
		edgeList = mg.getEdge();
		Date end = new Date();
		System.out.println("将邮件数据集转化为图："+ (end.getTime() - start.getTime()) + " total milliseconds"); 
		
		start = new Date(); //计时
		calculateReachId();
		end = new Date();
		System.out.println("Normal计算点对传递闭包："+ (end.getTime() - start.getTime()) + " total milliseconds"); 
	}
}

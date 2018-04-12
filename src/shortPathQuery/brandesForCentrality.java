package shortPathQuery;

import graph.Edge;
import graph.MakeGraph;
import graph.Vertex;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.Vector;
import java.util.Map.Entry;

public class brandesForCentrality 
{
	//public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //原顶点集合
	//public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //边集合
	
	public static double[] brande(ArrayList<Vertex> vertexList)    //计算中心性
	{
		int nodeSize = vertexList.size();
		double[] Cb = new double[nodeSize];  //中心性
		int[] sigma = new int[nodeSize];  
		int[] d = new int[nodeSize];  
		double[] delta = new double[nodeSize];
		
		Stack<Integer> S = new Stack<Integer>();
		Queue<Integer> Q = new LinkedList<Integer>();
		Vector<Vector<Integer>> P = new Vector<Vector<Integer>>();
		
		for(int i=0; i<nodeSize; i++)
		{
			Cb[i] = 0.0;
			P.add(new Vector<Integer>());
		}
		
		for(int s=0; s <nodeSize; s++)
		{//s is the index of the current node
			
	        while(!(S.empty()))
	        {
	            S.pop();  //empty stack
	        }	             
	        for(int i=0; i < nodeSize; i++) 
	        {
	            while(!(P.get(i).isEmpty())) //empty list, for all the nodes in the graph
	            {
	                P.get(i).clear();
	            }
	            sigma[i] = 0;
	            d[i] = -1;
	        }
	        sigma[s] = 1;
	        d[s] = 0;	        
	        while(!(Q.isEmpty()))//empty queue
	        {
	            Q.clear();
	        }
	        
	        
	        Q.add(s);   //start by enqueue the node s in Q
	        while(!(Q.isEmpty()))
	        {
	        	//dequeue v from Q
	            int v = Q.peek();
	            Q.remove();
	            //push v in S
	            S.push(v);
	            //for node w of neighbor of node v
	            //get the first item, then iterates until the last (node==NULL)
	            Vertex Vv = vertexList.get(v);
	            for(int w: Vv.neighborIdTypeALL)
	            {
	            	if(d[w] < 0)
	            	{
	                    //enqueue w in Q
	                    Q.add(w);
	                    d[w] = d[v] + 1;
	                }
	                //shortest path to w via v?
	                if(d[w] == d[v] + 1){
	                    sigma[w] += sigma[v];
	                    //append V in P[w]
	                    P.get(w).add(v);
	                }
	            }
	        }
	        //delta[v] = 0, for each node in graph
	        for(int i=0; i < nodeSize; i++){
	            delta[i] = 0;
	        }
	        //S retuns vertices in order of non-increaning distance from s
	        while(!(S.empty()))
	        {
	            //pop w from S
	            int w = S.pop();
	            //for all nodes in P[w]
	            for(int j=0; j != P.get(w).size(); j++)
	            {
	            	//System.out.println(sigma[P.get(w).get(j)] + "," + sigma[w]);
	            	delta[P.get(w).get(j)] += ((1.0 * sigma[P.get(w).get(j)]/sigma[w]) * (1 + delta[w]));
	            	Cb[P.get(w).get(j)] += delta[P.get(w).get(j)];
	            	//System.out.println(delta[P.get(w).get(j)] );
	            }
	        }
		}
		return Cb;
	}
	
	public static ArrayList<Vertex> TopCb(ArrayList<Vertex> vertexList)
	{
		ArrayList<Vertex> vertexListTop = new ArrayList<Vertex>();  
		double[] Cb = brande(vertexList);
		for(int i=0; i<vertexList.size(); i++)
		{
			vertexList.get(i).centralityBrandes = Cb[i];
		}
		List<Vertex> Centlist = new ArrayList<Vertex>(vertexList);  //这里将map.entrySet()转换成list
        //然后通过比较器来实现排序
        Collections.sort(Centlist,new Comparator<Vertex>() {
            //升序排序
            public int compare(Vertex v1, Vertex v2) {
                return (int) (v2.centralityBrandes-v1.centralityBrandes);
            }           
        });
		for(int i=0; i<20; i++)
		{
			vertexListTop.add(Centlist.get(i));
		}
		return vertexListTop;
	}
	
	/*public static void main(String[] args) throws SQLException, ParseException 
	{
		MakeGraph mg = new MakeGraph();   //邮件数据集
		Date start = new Date(); //计时
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		ArrayList<Vertex> vertexList = mg.getVertex();
		//edgeList = mg.getEdge();
		Date end = new Date();
		System.out.println("将邮件数据集转化为图："+ (end.getTime() - start.getTime()) + " total milliseconds");
		
		//common cn = new common();
		//cn.initialCost(vertexList);
		//ArrayList<Vertex> Top = TopCb(vertexList);
		//System.out.println();
		 //按照花费从小到大排序
		/*List<Vertex> Degreelist = new ArrayList<Vertex>(vertexList);  //这里将map.entrySet()转换成list
        //然后通过比较器来实现排序
        Collections.sort(Degreelist,new Comparator<Vertex>() {
            //升序排序
            public int compare(Vertex v1,
                    Vertex v2) {
                return v2.neighborIdTypeALL.size()-v1.neighborIdTypeALL.size();
            }           
        });
		for(int i=0; i<Degreelist.size(); i++)
		{
			System.out.print(Degreelist.get(i).getId() + ",");
		}
		System.out.println();
		
		for(int i=0; i<vertexList.size(); i++)
		{
			vertexList.get(i).centralityBrandes = Cb[i];
		}
		List<Vertex> Centlist = new ArrayList<Vertex>(vertexList);  //这里将map.entrySet()转换成list
        //然后通过比较器来实现排序
        Collections.sort(Centlist,new Comparator<Vertex>() {
            //升序排序
            public int compare(Vertex v1,
                    Vertex v2) {
                return (int) (v2.centralityBrandes-v1.centralityBrandes);
            }           
        });
		for(int i=0; i<Centlist.size(); i++)
		{
			System.out.print(Centlist.get(i).getId() + ",");
		}
	}*/
}

package shortPathQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import SuperGraph.SuperEdge;
import SuperGraph.SuperVertex;

public class allPath 
{ //找到两点之间的全部路径
	
	public static Stack<Integer> stack = new Stack<Integer>();  //临时保存路径节点的栈 
	public static Map<String, Double> map = new HashMap<String, Double>(); //路径，花费
	public static double matrix[][];		
	
	public void initial(ArrayList<SuperVertex> superVertexList)
	{
		matrix = new double[superVertexList.size()][superVertexList.size()];		//邻接矩阵的初始化
	    for(SuperVertex sv:superVertexList)
	    {
	    	if(sv.edge != null && sv.edge.size()!=0)
	    	{
	    		for(SuperEdge se:sv.edge)
		    	{
		    		matrix[se.cid1][se.cid2] = se.cost;
		    		matrix[se.cid2][se.cid1] = se.cost;
		    	}
	    	}
	    }
	}
	
	/* 此时栈中的节点组成一条所求路径，转储并打印输出 */  
    public void showAndSavePath()  
    {
    	Object[] o = stack.toArray();  
    	String tmp = "";
    	double cost = 0;
        for (int i = 0; i < o.length-1; i++) 
        {   
            tmp += o[i] + ",";  
            cost += matrix[(int) o[i]][(int) o[i+1]];
        }  
        tmp += o[o.length-1];
        map.put(tmp, cost);
        //sers.add(tmp);    /* 转储 */    
        //System.out.println(tmp+":"+cost);  
    }
    
	/* cNode: 当前的起始节点currentNode 
     * pNode: 当前起始节点的上一节点previousNode 
     * sNode: 最初的起始节点startNode 
     * eNode: 终点endNode 
     */
	public boolean getPaths(SuperVertex cNode, SuperVertex pNode, SuperVertex sNode, SuperVertex eNode, ArrayList<SuperVertex> superVertexList) 
	{
		if (cNode != null && pNode != null && cNode == pNode)  
            return false;  
		
		SuperVertex nNode = null;   //与cNode相邻的节点
		if (cNode != null) 
		{
			int i = 0;   
			stack.push(cNode.cid);  //起始节点入栈 
			
			if (cNode == eNode)    //如果该起始节点就是终点，说明找到一条路径
            {  
                /* 转储并打印输出该路径，返回true */  
                showAndSavePath();  
                return true;  
            }  
			else    //如果不是,继续寻路
			{
				Iterator<SuperEdge> iter = cNode.edge.iterator();
				SuperEdge se = iter.next();
				int nNodeId = (se.cid1==cNode.cid)?se.cid2:se.cid1;
				nNode = superVertexList.get(nNodeId);  //从与当前起始节点cNode有连接关系的节点集中按顺序遍历得到一个节点,作为下一次递归寻路时的起始节点  
				while (nNode != null) 
				{
					//如果nNode是最初的起始节点或者nNode就是cNode的上一节点或者nNode已经在栈中，说明产生环路 ，应重新在与当前起始节点有连接关系的节点集中寻找nNode 
					if ((pNode != null  && (nNode == sNode || nNode == pNode || stack.contains(nNode.cid))))
					{
						i++;  
                        if (i >= cNode.edge.size())  
                            nNode = null;  
                        else  
                        {
                        	se = iter.next();
            				nNodeId = (se.cid1==cNode.cid)?se.cid2:se.cid1;
            				nNode = superVertexList.get(nNodeId);  
                        }
                        continue; 
					}
					
					//以nNode为新的起始节点，当前起始节点cNode为上一节点，递归调用寻路方法
					if (getPaths(nNode, cNode, sNode, eNode, superVertexList))  //递归调用 
					{
						 stack.pop();  //如果找到一条路径，则弹出栈顶节点
					}
					
					i++;  
					if (i >= cNode.edge.size())  
                        nNode = null;  
                    else  
                    {
                    	se = iter.next();
        				nNodeId = (se.cid1==cNode.cid)?se.cid2:se.cid1;
        				nNode = superVertexList.get(nNodeId);  
                    }
				}
				stack.pop();  
                return false;  
			}
		}
		return false;
	}
	
	public static ArrayList<String> findAllPath(int cNode, int sNode, int eNode, ArrayList<SuperVertex> superVertexList, int K)
	{
		ArrayList<String> sers = new ArrayList<String>();   //存储路径的集合，只存储top3
		map.clear();
		allPath al = new allPath();
		al.initial(superVertexList);
	    al.getPaths(superVertexList.get(cNode), null, superVertexList.get(sNode), superVertexList.get(eNode), superVertexList);
		//al.getPaths(superVertexList.get(43), null, superVertexList.get(43), superVertexList.get(0), superVertexList);
		
        //按照花费从小到大排序
		List<Entry<String, Double>> list = new ArrayList<Map.Entry<String,Double>>(map.entrySet());  //这里将map.entrySet()转换成list
        //然后通过比较器来实现排序
        Collections.sort(list,new Comparator<Map.Entry<String,Double>>() {
            //升序排序
            public int compare(Entry<String, Double> o1,
                    Entry<String, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }           
        });
        int i = 0;
        for(Map.Entry<String,Double> mapping:list)
        { 
            //System.out.println(mapping.getKey()+":"+mapping.getValue()); 
            sers.add(mapping.getKey());
            if(mapping.getKey().isEmpty() || mapping.getKey() == null)
            {
            	break;
            }
            if(++i >= K)
            {
            	break;
            }
        }
		return sers;
	}
	
	public static ArrayList<String> findAllPath(int cNode, int sNode, int eNode, ArrayList<SuperVertex> superVertexList)  //返回所有的路径
	{
		ArrayList<String> sers = new ArrayList<String>();   //存储路径的集合，只存储top3
		map.clear();
		allPath al = new allPath();
		al.initial(superVertexList);
	    al.getPaths(superVertexList.get(cNode), null, superVertexList.get(sNode), superVertexList.get(eNode), superVertexList);
		//al.getPaths(superVertexList.get(43), null, superVertexList.get(43), superVertexList.get(0), superVertexList);
		
        //按照花费从小到大排序
		List<Entry<String, Double>> list = new ArrayList<Map.Entry<String,Double>>(map.entrySet());  //这里将map.entrySet()转换成list
        //然后通过比较器来实现排序
        Collections.sort(list,new Comparator<Map.Entry<String,Double>>() {
            //升序排序
            public int compare(Entry<String, Double> o1,
                    Entry<String, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }           
        });
        int i = 0;
        for(Map.Entry<String,Double> mapping:list)
        { 
            //System.out.println(mapping.getKey()+":"+mapping.getValue()); 
            sers.add(mapping.getKey());
            if(mapping.getKey().isEmpty() || mapping.getKey() == null)
            {
            	break;
            }
        }
		return sers;
	}
}

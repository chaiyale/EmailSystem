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
{ //�ҵ�����֮���ȫ��·��
	
	public static Stack<Integer> stack = new Stack<Integer>();  //��ʱ����·���ڵ��ջ 
	public static Map<String, Double> map = new HashMap<String, Double>(); //·��������
	public static double matrix[][];		
	
	public void initial(ArrayList<SuperVertex> superVertexList)
	{
		matrix = new double[superVertexList.size()][superVertexList.size()];		//�ڽӾ���ĳ�ʼ��
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
	
	/* ��ʱջ�еĽڵ����һ������·����ת������ӡ��� */  
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
        //sers.add(tmp);    /* ת�� */    
        //System.out.println(tmp+":"+cost);  
    }
    
	/* cNode: ��ǰ����ʼ�ڵ�currentNode 
     * pNode: ��ǰ��ʼ�ڵ����һ�ڵ�previousNode 
     * sNode: �������ʼ�ڵ�startNode 
     * eNode: �յ�endNode 
     */
	public boolean getPaths(SuperVertex cNode, SuperVertex pNode, SuperVertex sNode, SuperVertex eNode, ArrayList<SuperVertex> superVertexList) 
	{
		if (cNode != null && pNode != null && cNode == pNode)  
            return false;  
		
		SuperVertex nNode = null;   //��cNode���ڵĽڵ�
		if (cNode != null) 
		{
			int i = 0;   
			stack.push(cNode.cid);  //��ʼ�ڵ���ջ 
			
			if (cNode == eNode)    //�������ʼ�ڵ�����յ㣬˵���ҵ�һ��·��
            {  
                /* ת������ӡ�����·��������true */  
                showAndSavePath();  
                return true;  
            }  
			else    //�������,����Ѱ·
			{
				Iterator<SuperEdge> iter = cNode.edge.iterator();
				SuperEdge se = iter.next();
				int nNodeId = (se.cid1==cNode.cid)?se.cid2:se.cid1;
				nNode = superVertexList.get(nNodeId);  //���뵱ǰ��ʼ�ڵ�cNode�����ӹ�ϵ�Ľڵ㼯�а�˳������õ�һ���ڵ�,��Ϊ��һ�εݹ�Ѱ·ʱ����ʼ�ڵ�  
				while (nNode != null) 
				{
					//���nNode���������ʼ�ڵ����nNode����cNode����һ�ڵ����nNode�Ѿ���ջ�У�˵��������· ��Ӧ�������뵱ǰ��ʼ�ڵ������ӹ�ϵ�Ľڵ㼯��Ѱ��nNode 
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
					
					//��nNodeΪ�µ���ʼ�ڵ㣬��ǰ��ʼ�ڵ�cNodeΪ��һ�ڵ㣬�ݹ����Ѱ·����
					if (getPaths(nNode, cNode, sNode, eNode, superVertexList))  //�ݹ���� 
					{
						 stack.pop();  //����ҵ�һ��·�����򵯳�ջ���ڵ�
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
		ArrayList<String> sers = new ArrayList<String>();   //�洢·���ļ��ϣ�ֻ�洢top3
		map.clear();
		allPath al = new allPath();
		al.initial(superVertexList);
	    al.getPaths(superVertexList.get(cNode), null, superVertexList.get(sNode), superVertexList.get(eNode), superVertexList);
		//al.getPaths(superVertexList.get(43), null, superVertexList.get(43), superVertexList.get(0), superVertexList);
		
        //���ջ��Ѵ�С��������
		List<Entry<String, Double>> list = new ArrayList<Map.Entry<String,Double>>(map.entrySet());  //���ｫmap.entrySet()ת����list
        //Ȼ��ͨ���Ƚ�����ʵ������
        Collections.sort(list,new Comparator<Map.Entry<String,Double>>() {
            //��������
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
	
	public static ArrayList<String> findAllPath(int cNode, int sNode, int eNode, ArrayList<SuperVertex> superVertexList)  //�������е�·��
	{
		ArrayList<String> sers = new ArrayList<String>();   //�洢·���ļ��ϣ�ֻ�洢top3
		map.clear();
		allPath al = new allPath();
		al.initial(superVertexList);
	    al.getPaths(superVertexList.get(cNode), null, superVertexList.get(sNode), superVertexList.get(eNode), superVertexList);
		//al.getPaths(superVertexList.get(43), null, superVertexList.get(43), superVertexList.get(0), superVertexList);
		
        //���ջ��Ѵ�С��������
		List<Entry<String, Double>> list = new ArrayList<Map.Entry<String,Double>>(map.entrySet());  //���ｫmap.entrySet()ת����list
        //Ȼ��ͨ���Ƚ�����ʵ������
        Collections.sort(list,new Comparator<Map.Entry<String,Double>>() {
            //��������
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

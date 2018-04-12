package SuperGraph;

import graph.Edge;
import graph.Vertex;

import java.util.ArrayList;
import java.util.HashSet;

public class SuperEdge 
{
	public int ceid;   //������
	public int cid1 = 0;  //�׷����
	public int cid2 = 0;  //�ҷ����
	
	public HashSet<Edge> cid1Withcid2 = new HashSet<Edge>(); 
	public HashSet<Integer> Incid1 = new HashSet<Integer>();    //��cid1�ڲ��Ľڵ�
	public HashSet<Integer> Incid2 = new HashSet<Integer>();    //��cid2�ڲ��Ľڵ�
	public double weight = 0.0;  //Ȩ��
	public double cost;  //�ķ�
	
	public SuperEdge(int ceid ,int cid1, int cid2) //���캯��
	{
		this.ceid = ceid;
		this.cid1 = cid1;
		this.cid2 = cid2;
	}
}

package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Vertex 
{
	private int id = 0; //������
	private String name = ""; //�ʼ���ַ
	public HashSet<Edge> edge = new HashSet<Edge>(); //�߼���
	public HashSet<Integer> neighborIdType1 = new HashSet<Integer>();  //ʵ�������ھӽڵ�Id
	public HashSet<Integer> neighborIdTypeALL = new HashSet<Integer>();  //���������ھӽڵ�Id
	public ArrayList<Integer> neighborIdTypeALLbySort = new ArrayList<Integer>();  //���������ھӽڵ�Id
	public HashSet<Integer> neighborIdSPT = new HashSet<Integer>();  //SPT���ھӽڵ�Id
	
	public int centrality;  //������
	public double centralityBrandes;  //������
	public HashMap<Integer, Integer> communityDistribution = new HashMap<Integer, Integer>(); //��ǩ��keyΪ��ǩid,valueΪ�ñ�ǩ�ڴ˵��������ֵĴ���
	public HashSet<Integer> communityId = new HashSet<Integer>();  //����������id
	public HashSet<Integer> triangles = new HashSet<Integer>();//�ýڵ�����Щ��������	
	public HashSet<Integer> SuperVId = new HashSet<Integer>();  //�����ĳ����ڵ�id
	
	public int sd = 0; //similar_degree
	public int ed; //effective_degree
	public double weight = 0;  //edge��weight֮��
	public int type = 0;  //typeΪ0��һ��ڵ㣬typeΪ-2��outlier��typeΪ-1��hub
	public HashSet<Integer> reachId = new HashSet<Integer>();  //�����ھӽڵ�Id
	
	public HashMap<Integer,String> shortestPath = new HashMap<Integer,String>();   //�������ڵ�����·��
	
	public Vertex(int Id) //���캯��
	{
		this.id = Id;
	}
	
	public Vertex(int Id, String Name) //���캯��
	{
		this.id = Id;
		this.name = Name;
	}
	
	public int getId() //����ID
	{
		return this.id;
	}
	
	public String getName() //�����ʼ���ַ������������
	{
		return this.name;
	}
}

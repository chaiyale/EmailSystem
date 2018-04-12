package graph;

import java.util.ArrayList;
import java.util.HashSet;

public class Edge 
{
	public int eid;   //������
	public int nid1 = 0;  //�׷����
	public int nid2 = 0;  //�ҷ����
	
	public ArrayList<Mail> nid1Tonid2 = new ArrayList<Mail>();  //�׷�����
	public ArrayList<Mail> nid1Tonid2byCC = new ArrayList<Mail>();  //�׳��͸���
	public ArrayList<Mail> nid2Tonid1 = new ArrayList<Mail>();  //�ҷ�����
	public ArrayList<Mail> nid2Tonid1byCC = new ArrayList<Mail>();  //�ҳ��͸���
	public ArrayList<Mail> nid1Withnid2 = new ArrayList<Mail>();  //����֮������������ʼ�
	public ArrayList<Mail> Co_recipient = new ArrayList<Mail>();  //��ͬ�ռ��ˣ�1.���ռ����б���ͬʱ����  2.�ڳ������б���ͬʱ����  3.һ�����ռ����б�һ���˳������б�
	
	public boolean visited = false;
	
	public int longevity;    //������Զ�������ڵ�����
	public int recency;   //��������������ڵ�����
	public double frequency;  //�����ʼ���Ƶ��=����/ʱ����
	public double reciprocity;  //���ݶ�
	public double availablityTo = 0.0;  //������
	public double availablityCc = 0.0;  //������
	public double centrality;   //���Ķ�
	public int co_occurrence = 0;
	public float weight;  //Ȩ��
	public float cost;  //�ķѣ���Ȩ�سɷ���
	
	public HashSet<Integer> triangles = new HashSet<Integer>();//�ñ�����Щ��������
	
	//public int min_cn;
	
	public Edge(int eid ,int nid1, int nid2) //���캯��
	{
		this.eid = eid;
		this.nid1 = nid1;
		this.nid2 = nid2;
	}
}

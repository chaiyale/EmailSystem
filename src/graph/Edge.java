package graph;

import java.util.ArrayList;
import java.util.HashSet;

public class Edge 
{
	public int eid;   //顶点编号
	public int nid1 = 0;  //甲方编号
	public int nid2 = 0;  //乙方编号
	
	public ArrayList<Mail> nid1Tonid2 = new ArrayList<Mail>();  //甲发给乙
	public ArrayList<Mail> nid1Tonid2byCC = new ArrayList<Mail>();  //甲抄送给乙
	public ArrayList<Mail> nid2Tonid1 = new ArrayList<Mail>();  //乙发给甲
	public ArrayList<Mail> nid2Tonid1byCC = new ArrayList<Mail>();  //乙抄送给甲
	public ArrayList<Mail> nid1Withnid2 = new ArrayList<Mail>();  //甲乙之间的所有往来邮件
	public ArrayList<Mail> Co_recipient = new ArrayList<Mail>();  //共同收件人，1.在收件人列表中同时出现  2.在抄送人列表中同时出现  3.一个在收件人列表，一个人抄送人列表
	
	public boolean visited = false;
	
	public int longevity;    //日期最远距离现在的天数
	public int recency;   //日期最近距离现在的天数
	public double frequency;  //发送邮件的频率=次数/时间间隔
	public double reciprocity;  //互惠度
	public double availablityTo = 0.0;  //可用性
	public double availablityCc = 0.0;  //可用性
	public double centrality;   //中心度
	public int co_occurrence = 0;
	public float weight;  //权重
	public float cost;  //耗费，与权重成反比
	
	public HashSet<Integer> triangles = new HashSet<Integer>();//该边在哪些三角形中
	
	//public int min_cn;
	
	public Edge(int eid ,int nid1, int nid2) //构造函数
	{
		this.eid = eid;
		this.nid1 = nid1;
		this.nid2 = nid2;
	}
}

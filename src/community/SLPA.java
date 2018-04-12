package community;

import graph.*;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import SuperGraph.SuperVertex;

public class SLPA 
{//���õ��Ľ������ÿ���ڵ�������Щ����
	public static double threshold  = 0.3; //[0, 1]
	static int labelNum = 0; 
	
	public static void init(ArrayList<Vertex> vertexList)   //��ʼ�����ýڵ��idΪ�ýڵ������
	{
		Iterator<Vertex> iter1 = vertexList.iterator();
		while(iter1.hasNext())
		{
			Vertex v = iter1.next();
			v.communityId.removeAll(v.communityId);
			if(v.communityId.size()==0)
			{
				v.communityDistribution.put(labelNum++, 1);
			}
			else
			{
				v.communityDistribution.put(v.communityId.iterator().next(), 1);
			}
		}
	}
	
	public static void init2(ArrayList<Vertex> vertexList, ArrayList<Integer> svs)   //��ʼ�����ýڵ��idΪ�ýڵ������
	{
		Iterator<Vertex> iter1 = vertexList.iterator();
		while(iter1.hasNext())
		{
			Vertex v = iter1.next();
			if(svs.contains(v.getId()))
			{
				v.communityId.clear();
				v.communityDistribution.clear();
				v.communityDistribution.put(labelNum++, 1);
				System.out.print("");
			}
		}
	}
	
    public static void updateCommunityDistribution(Vertex v, int votedCommunity, int voteIncrement)  //���¶����ǩ
    {	
		if (v.communityDistribution.containsKey(votedCommunity) ) 
		{
			voteIncrement += v.communityDistribution.get(votedCommunity);
		}		
		v.communityDistribution.put(votedCommunity, voteIncrement);
		
	}
	
	public static int speakerVote(Vertex v)  //speak rule������ӱ�ǩs��ѡ��һ��
	{
		//����map�е�ÿ��Ԫ�أ��Դ����ۻ��ֲ�
		Set<Integer> communityIds = v.communityDistribution.keySet();
		ArrayList<Integer> communities = new ArrayList<Integer>();
		ArrayList<Integer> cumulativeCounts = new ArrayList<Integer>();
				
		int sum=-1;
		for (Integer comm: communityIds) 
		{
			sum += v.communityDistribution.get(comm);
			communities.add(comm);
			cumulativeCounts.add(sum);
		}
		Random random = new Random();
		int rand = random.nextInt(sum+1);
		int i=0;  			
		for (i=0; i<cumulativeCounts.size(); i++)   //��cumulativeCounts���ҵ�����rand�ĵ�һ��ֵ������
		{
			if (cumulativeCounts.get(i)>=rand) 
			{
				break;
			}
		}
		return communities.get(i);
	}
	
	public static void updateLabels(ArrayList<Vertex> vertexList, Vertex v)   //���¶���ı�ǩ
	{
		HashMap<Integer, Double> incomingVotes = new HashMap<Integer, Double>();  //ͳ�����бߴ��ݹ�����<����id,���ݹ����Ĵ���>
		for(Edge e:v.edge) //�����ھ�
		{
			int listenId = v.getId();
			int nid1 = e.nid1;
			int nid2 = e.nid2;
			int speakerId = (listenId == nid1)?nid2:nid1;
			Vertex speakerNode = vertexList.get(speakerId);
			int votedCommunity = speakerVote(speakerNode);
			double votedCommunitycount = Math.sqrt(e.weight);
			//double votedCommunitycount = e.weight;
			if ( incomingVotes.containsKey(votedCommunity)) 
			{
				votedCommunitycount += incomingVotes.get(votedCommunity);
			}		
			incomingVotes.put(votedCommunity, votedCommunitycount);
		}
		
		//�ҳ����������ִ�����������id
		Iterator<Entry<Integer, Double>> it = incomingVotes.entrySet().iterator();
		int popularCommunity=-1;
		Double popularCommunityCount=0.0;
		while (it.hasNext()) 
		{
			Entry<Integer, Double> entry = it.next();
			if ( entry.getValue() > popularCommunityCount ) 
			{
				popularCommunity = entry.getKey();
				popularCommunityCount = entry.getValue();
			}
		}
		updateCommunityDistribution(v,popularCommunity,1);
	}
	
	public static void updateLabels2(ArrayList<Vertex> vertexList, Vertex v, ArrayList<Integer> svs)   //���¶���ı�ǩ
	{
		HashMap<Integer, Double> incomingVotes = new HashMap<Integer, Double>();  //ͳ�����бߴ��ݹ�����<����id,���ݹ����Ĵ���>
		for(Edge e:v.edge) //�����ھ�
		{
			int listenId = v.getId();
			int nid1 = e.nid1;
			int nid2 = e.nid2;
			int speakerId = (listenId == nid1)?nid2:nid1;
			if(!svs.contains(speakerId))
			{
				continue;
			}
			Vertex speakerNode = vertexList.get(speakerId);
			int votedCommunity = speakerVote(speakerNode);
			double votedCommunitycount = Math.sqrt(e.weight);
			//double votedCommunitycount = e.weight;
			if ( incomingVotes.containsKey(votedCommunity)) 
			{
				votedCommunitycount += incomingVotes.get(votedCommunity);
			}		
			incomingVotes.put(votedCommunity, votedCommunitycount);
		}
		
		//�ҳ����������ִ�����������id
		Iterator<Entry<Integer, Double>> it = incomingVotes.entrySet().iterator();
		int popularCommunity=-1;
		Double popularCommunityCount=0.0;
		while (it.hasNext()) 
		{
			Entry<Integer, Double> entry = it.next();
			if ( entry.getValue() > popularCommunityCount ) 
			{
				popularCommunity = entry.getKey();
				popularCommunityCount = entry.getValue();
			}
		}
		updateCommunityDistribution(v,popularCommunity,1);
	}
	
	public static void printLabel(Vertex v)  //��ӡ�ڵ�ı�ǩ
	{
		Iterator<Entry<Integer, Integer>> it = v.communityDistribution.entrySet().iterator();
		while (it.hasNext()) 
		{
			Entry<Integer, Integer> entry = it.next();
			System.out.print(entry.getKey()+": "+entry.getValue()+"  ;");
		}
		System.out.println();
	}
	
	public static void chooseCommunity(Vertex v)  //ѡ��ڵ������
	{
		//���ڵ�ı�ǩ��Ϣת��Ϊ���ʷֲ�
		Iterator<Entry<Integer, Integer>> it = v.communityDistribution.entrySet().iterator();
		int sum = 0;
		while (it.hasNext()) 
		{
			Entry<Integer, Integer> entry = it.next();
			sum += entry.getValue();
		}
		
		Iterator<Entry<Integer, Integer>> it2 = v.communityDistribution.entrySet().iterator();
		//int max = -1; int maxId = -1;
		while (it2.hasNext()) 
		{
			Entry<Integer, Integer> entry = it2.next();
			if(entry.getValue()*1.0/sum >= threshold)
			{
				v.communityId.add(entry.getKey());
			}
			/*if(entry.getValue() > max)
			{
				max = entry.getValue();
				maxId = entry.getKey();
			}*/
		}
		//v.communityId.add(maxId);
	}
	
	public static void printCommunity(Vertex v)  //��ӡ�ڵ������
	{
		System.out.print(v.getId()+":  ");
		for(int id:v.communityId)
		{
			System.out.print(id+", ");
		}
		System.out.println();
	}
	
	public static ArrayList<Vertex> getVertex(ArrayList<Vertex> vertexList, int startNum) throws ParseException
	{
		/*findTruss fu = new findTruss();  //new
    	labelNum = fu.getFindTruss(5);
    	vertexList = fu.getVertex();*/
   
		labelNum = startNum;  //���ŵ���ʼid
		init(vertexList);
		
		int iterations = 30;  //��������
		for (int i=0; i<iterations; i++)
		{
			//System.out.println("******************************   "+(i+1)+"   **********************************");
			for(Vertex v:vertexList)
			{
				//printLabel(v);
				updateLabels(vertexList, v);
			}
		}
		for(Vertex v:vertexList)  //��������
		{
			chooseCommunity(v);
			//printCommunity(v);
		}
		return vertexList;
	}
	
	public static ArrayList<Vertex> getVertex2(ArrayList<Vertex> vertexList, int startNum, SuperVertex SV) throws ParseException
	{
		/*findTruss fu = new findTruss();  //new
    	labelNum = fu.getFindTruss(5);
    	vertexList = fu.getVertex();*/
		
		ArrayList<Integer> svs = new ArrayList<Integer>();
		for(int s:SV.containVertex)
		{
			svs.add(s);
		}
		
		labelNum = startNum;  //���ŵ���ʼid
		init2(vertexList, svs);
		
		int iterations = 50;  //��������
		for (int i=0; i<iterations; i++)
		{
			//System.out.println("******************************   "+(i+1)+"   **********************************");
			for(Vertex v:vertexList)
			{
				//printLabel(v);
				if(svs.contains(v.getId()))
				{
					updateLabels2(vertexList, v, svs);
				}
			}
		}
		for(Vertex v:vertexList)  //��������
		{
			if(svs.contains(v.getId()))
			{
				chooseCommunity(v);
				//printCommunity(v);
			}
		}
		return vertexList;
	}
	
    /*public static void main(String[] args) throws SQLException, IOException, ParseException
	{
    	findTruss fu = new findTruss();  //new
    	labelNum = fu.getFindTruss(5);
    	vertexList = fu.getVertex();
    	    	
		init();
		
		int iterations = 50;  //��������
		for (int i=0; i<iterations; i++)
		{
			//System.out.println("******************************   "+(i+1)+"   **********************************");
			for(Vertex v:vertexList)
			{
				//printLabel(v);
				updateLabels(v);
			}
		}
		for(Vertex v:vertexList)  //��������
		{
			chooseCommunity(v);
			printCommunity(v);
		}
	}*/
}

package reachQuery;

import java.sql.SQLException;
import java.util.HashSet;

import community.findTruss;
import database.dataAccess;

public class reachQuery 
{//�ɴ��ѯ
	/**
	 * 
	 * @param ad1 ��һ�������ַ
	 * @param ad2 �ڶ��������ַ
	 * @return ��������֮���Ƿ�ɴ�
	 * @throws SQLException 
	 */
	public static boolean ReachQuery(String ad1, String ad2) throws SQLException  //�ɴ��ѯ
	{
		boolean isReach = false;
		
		//Step1 �ֱ��ҵ����������ַ��Ӧ�Ľڵ�
		//Step2 �ڵ����ڵĳ����ڵ�
		//Step3 �����ڵ�Ĵ��ݱհ�
		dataAccess db = new dataAccess();
		HashSet<Integer> parentId = db.getReachId(ad1);  //���ݱհ�
		HashSet<Integer> parentId2 = db.getReachId(ad2);
		
		for(int i: parentId)
		{
			for(int j: parentId2)
			{
				if(i==j)
				{
					return true;
				}
			}
		}
		
		return isReach;
	}
	
	public static boolean ReachQuery(int ad1, int ad2) throws SQLException  //�ɴ��ѯ
	{
		boolean isReach = false;
		
		//Step1 �ֱ��ҵ����������ַ��Ӧ�Ľڵ�
		//Step2 �ڵ����ڵĳ����ڵ�
		//Step3 �����ڵ�Ĵ��ݱհ�
		dataAccess db = new dataAccess();
		HashSet<Integer> parentId = db.getReachId(ad1);  //���ݱհ�
		HashSet<Integer> parentId2 = db.getReachId(ad2);
		
		for(int i: parentId)
		{
			for(int j: parentId2)
			{
				if(i==j)
				{
					return true;
				}
			}
		}
		
		return isReach;
	}
	
	public static boolean ReachQueryNormal()  //��ͨ�ķ�����ѯ�ɴ���
	{
		boolean isReach = false;
		return isReach;
	}
	
	public static void main(String[] args) throws SQLException
	{
		boolean flag = ReachQuery("andrea.richards@enron.com", "Andrea Richards/NA/Enron@ENRON");
		if(flag == true)
		{
			System.out.println("�ɴ�");
		}
		else
		{
			System.out.println("���ɴ�");
		}
	}
}

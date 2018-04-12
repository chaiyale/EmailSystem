package reachQuery;

import java.sql.SQLException;
import java.util.HashSet;

import community.findTruss;
import database.dataAccess;

public class reachQuery 
{//可达查询
	/**
	 * 
	 * @param ad1 第一个邮箱地址
	 * @param ad2 第二个邮箱地址
	 * @return 两个邮箱之间是否可达
	 * @throws SQLException 
	 */
	public static boolean ReachQuery(String ad1, String ad2) throws SQLException  //可达查询
	{
		boolean isReach = false;
		
		//Step1 分别找到两个邮箱地址对应的节点
		//Step2 节点所在的超级节点
		//Step3 超级节点的传递闭包
		dataAccess db = new dataAccess();
		HashSet<Integer> parentId = db.getReachId(ad1);  //传递闭包
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
	
	public static boolean ReachQuery(int ad1, int ad2) throws SQLException  //可达查询
	{
		boolean isReach = false;
		
		//Step1 分别找到两个邮箱地址对应的节点
		//Step2 节点所在的超级节点
		//Step3 超级节点的传递闭包
		dataAccess db = new dataAccess();
		HashSet<Integer> parentId = db.getReachId(ad1);  //传递闭包
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
	
	public static boolean ReachQueryNormal()  //普通的方法查询可达性
	{
		boolean isReach = false;
		return isReach;
	}
	
	public static void main(String[] args) throws SQLException
	{
		boolean flag = ReachQuery("andrea.richards@enron.com", "Andrea Richards/NA/Enron@ENRON");
		if(flag == true)
		{
			System.out.println("可达");
		}
		else
		{
			System.out.println("不可达");
		}
	}
}

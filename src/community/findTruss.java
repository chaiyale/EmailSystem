package community;

import graph.Edge;
import graph.MakeGraph;
import graph.Vertex;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;

public class findTruss 
{
	public static int triangleNum = 0 ;//三角形编号从0开始
	public static HashSet<ArrayList<Vertex>> triple; //记录三角形顶点的三元组，ArrayList<Vertex>是三元组
	public static int nodeNum = 0; //图中节点总数
	public static ArrayList<Vertex> vertexList;
	public static ArrayList<Edge> edgeList;
	public static ArrayList<HashSet<Integer>> label = new ArrayList<HashSet<Integer>>(); 
	static int labelNum = 0; 
	int iter = 0;
	
	public findTruss() {} 
	
	//根据k-truss定义去掉不符合条件的边，即如果某边所在三角形数量小于k-2，则去掉它
	/*public void findKTruss(int k)
	{
		boolean deleteFlag = false; //记录是否删除了边
		for(Edge e:edgeList)
		{
			if(e.triangles.size()!=0 && e.triangles.size()<k-2)
			{
				deleteFlag = true;				
				
				int nid1 = e.nid1;
				Vertex v1 = vertexList.get(nid1);
				int nid2 = e.nid2;
				Vertex v2 = vertexList.get(nid2);
				int tripleId = getVertex(nid1, nid2);  //删除第三条边的，不仅仅是一个。。。。。
				for(int i:e.triangles)
				{
					int j = 0;
					if((j = findSit(v1,i))!=-1)  //去除掉连接边的两个节点的三角形
					{
						v1.triangles.remove(j);
					}
					if((j = findSit(v2,i))!=-1)  //去除掉连接边的两个节点的三角形
					{
						v2.triangles.remove(j);
					}
				}
				for(Edge v1e:v1.edge)
				{
					int j = 0;
					if((j = findSitforEdge(v1e,tripleId))!=-1)  //去除三角形其他两个边的
					{
						v1e.triangles.remove(j);
					}
				}
				for(Edge v2e:v2.edge)
				{
					int j = 0;
					if((j = findSitforEdge(v2e,tripleId))!=-1)  
					{
						v2e.triangles.remove(j);
					}
				}
			}
		}
		if(deleteFlag == true)
		{
			System.out.print("删除");
			findKTruss(k);
		}
	}*/
	
	public void findLabel(Vertex v)
	{
		if(v.triangles.size()>0 && v.communityId.size()==0)
		{
			boolean isExist = false;
			for(int i = 0; i< label.size(); i++)
			{
				if(isExist == true)
				{
					break;
				}
				HashSet<Integer> hi = label.get(i);
				for(int vtri :v.triangles)
				{
					if(hi.contains(vtri))
					{
						v.communityId.add(i);
						for(int vv :v.triangles)
						{
							hi.add(vv);
						}
						isExist = true;
						break;
					}
				}
			}
			if(isExist == false)  //不存在，新增
			{
				HashSet<Integer> hi = new HashSet<Integer>();
				for(int vtri :v.triangles)
				{
					hi.add(vtri);
				}
				v.communityId.add(labelNum);
				labelNum ++;
				label.add(hi);
			}
		}
	}
	
	public void findKTruss(int k)
	{
		findAllTriangles();
		boolean deleteFlag = false; //记录是否删除了边
		//System.out.println(edgeList.size());
		ArrayList<Edge> deledgeList = new ArrayList<Edge>();
		for(Edge e:edgeList)
		{
			if(e.triangles.size()!=0 && e.triangles.size()<k-2)
			{
                deleteFlag = true;				
				
				int nid1 = e.nid1;
				int nid2 = e.nid2;
				Vertex v1 = vertexList.get(nid1);
				v1.edge.remove(e);
				v1.neighborIdTypeALL.remove(nid2);
				Vertex v2 = vertexList.get(nid2);
				v2.edge.remove(e);
				v2.neighborIdTypeALL.remove(nid1);
				deledgeList.add(e);
			}
		}
		for(Edge e:deledgeList)
		{
			edgeList.remove(e);
		}
		if(deleteFlag == true)
		{
			iter++;
			//System.out.println("删除");
			findKTruss(k);
		}
		else
		{
			for(Vertex v:vertexList)
			{
				findLabel(v);
			}
		}
	}
	
	//找到图中所有三角形
	public void findAllTriangles() 
	{
		for(Vertex v:vertexList) 
		{
			v.triangles.removeAll(v.triangles);
		}
		for(Edge e:edgeList) 
		{
			e.triangles.removeAll(e.triangles);
		}
		triangleNum = 0;
		triple = new HashSet<ArrayList<Vertex>>();
		
		for(Vertex v:vertexList) //按度从小到大的顺序遍历全部节点
		{
			ArrayList<Integer> neighbor = new ArrayList<Integer>();
			//System.out.print(v.getId()+":   ");
			for(int i:v.neighborIdTypeALL)  //放入数组中方便有序取出
			{
				neighbor.add(i);
			}
			//System.out.println();
			
			for(int i=0; i<neighbor.size()-1;i++)
			{
				Vertex v1 = vertexList.get(neighbor.get(i));
				for(int j=i+1; j<neighbor.size();j++)
				{
					Vertex v2 = vertexList.get(neighbor.get(j));
					if(!intersect(v.triangles, v1.triangles, v2.triangles))  //如果不存在包含这三个点的三角形
					{
						if(isNeighbor(v1,v2))  //如果v1,v2相连，加入三角形
						{
							//三个节点的三角形加入
							v.triangles.add(triangleNum);
							v1.triangles.add(triangleNum);
							v2.triangles.add(triangleNum);
							//三条边的三角形加入
							getEdge(v,v1).triangles.add(triangleNum);
							getEdge(v1,v2).triangles.add(triangleNum);
							getEdge(v,v2).triangles.add(triangleNum);
							++triangleNum;
							//加入总的三角形
							ArrayList<Vertex> a = new ArrayList<Vertex>();
							a.add(v);
							a.add(v1);
							a.add(v2);
							triple.add(a);
						}
					}
				}
			}
		}
		System.out.println("triplesize: "+triple.size());
	}
	
	/*public int getVertex(int v1, int v2)  //返回与Vertex v1, Vertex v2属于同一triple的点
	{
		int i,j=0;
		Vertex v = null;
		for(i=0;i<triple.size();i++)
		{
			ArrayList<Vertex> tri = triple.get(i);
			if(v1 == tri.get(0).getId())
			{
				if(v2 == tri.get(1).getId())
				{
					v= tri.get(2);
					break;
				}
				else if(v2 == tri.get(2).getId())
				{
					v= tri.get(1);
					break;
				}
			}
			else if(v1 == tri.get(1).getId())
			{
				if(v2 == tri.get(0).getId())
				{
					v = tri.get(2);
					break;
				}
				else if(v2 == tri.get(2).getId())
				{
					v= tri.get(0);
					break;
				}
			}
			else if(v1 == tri.get(2).getId())
			{
				if(v2 == tri.get(0).getId())
				{
					v = tri.get(1);
					break;
				}
				else if(v2 == tri.get(1).getId())
				{
					v = tri.get(0);
					break;
				}
			}
		}
		Vertex vv = vertexList.get(v.getId());
		if((j = findSit(vv,i))!=-1)  //去除掉连接边的两个节点的三角形
		{
			vv.triangles.remove(j);
		}
		//删除Triple
		return i;
	}*/
	
	/*public int findSit(Vertex v, int value)
	{
		for(int j = 0; j< v.triangles.size(); j++)
		{
			if(v.triangles.get(j) == value)
			{
				return j;
			}
		}
		return -1;
	}
	
	public int findSitforEdge(Edge e, int value)
	{
		for(int j = 0; j< e.triangles.size(); j++)
		{
			if(e.triangles.get(j) == value)
			{
				return j;
			}
		}
		return -1;
	}*/
	
	public Edge getEdge(Vertex v1, Vertex v2)   //返回连接两个节点的边
	{
		Edge ee = null;
		int nid1 = v1.getId();
		int nid2 = v2.getId();
		if(v1.edge.size()>v2.edge.size())
		{
			for(Edge e:v2.edge)
			{
				if((e.nid1==nid2 && e.nid2 == nid1) || (e.nid1==nid1 && e.nid2 == nid2))
				{
					return e;
				}
			}
		}
		else
		{
			for(Edge e:v1.edge)
			{
				if((e.nid1==nid2 && e.nid2 == nid1) || (e.nid1==nid1 && e.nid2 == nid2))
				{
					return e;
				}
			}
		}
		return ee;
	}
	
	public boolean isNeighbor(Vertex v1, Vertex v2)    //判断两个节点是否相连
	{
		if(v1.neighborIdTypeALL.size() > v2.neighborIdTypeALL.size())
		{
			for(int i:v2.neighborIdTypeALL)
			{
				if(i==v1.getId())
				{
					return true;
				}
			}
		}
		else
		{
			for(int i:v1.neighborIdTypeALL)
			{
				if(i==v2.getId())
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean intersect(HashSet<Integer> list1,HashSet<Integer> list2,HashSet<Integer> list3) //如果有交集，则返回true
	{
		HashSet<Integer> a1 = new HashSet<Integer>(list1);
		HashSet<Integer> a2 = new HashSet<Integer>(list2);
		HashSet<Integer> a3 = new HashSet<Integer>(list3);
		boolean flag = false;
		if(!(a1.isEmpty()&& a2.isEmpty()&& a3.isEmpty()))//全不为空
		{
			a1.retainAll(a2);
			if(!a1.isEmpty())
			{
				a1.retainAll(a3);
				if(!a1.isEmpty())
				{
					flag = true;
				}
			}
		}
		return flag;
	}
	
	public boolean intersect2(HashSet<Integer> list1,HashSet<Integer> list2) //如果有交集，则返回true
	{
		HashSet<Integer> a1 = new HashSet<Integer>(list1);
		HashSet<Integer> a2 = new HashSet<Integer>(list2);
		boolean flag = false;
		if(!(a1.isEmpty()&& a2.isEmpty()))//全不为空
		{
			a1.retainAll(a2);
			if(!a1.isEmpty())
			{
				flag = true;
			}
		}
		return flag;
	}
	
	public HashSet<Integer> AddAll(HashSet<Integer> list1,HashSet<Integer> list2)
	{
		HashSet<Integer> a1 = new HashSet<Integer>(list1);
		HashSet<Integer> a2 = new HashSet<Integer>(list2);
		a1.addAll(a2);
		return a1;
	}
	
	public int getFindTruss(int KK) throws SQLException, ParseException
	{
		MakeGraph mg = new MakeGraph();
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
    	vertexList = mg.getVertex();
		edgeList = mg.getEdge();
		
		findKTruss(KK); 
		int k = 0;
		int[] labelVertex = new int[label.size()];
		for(int i=0;i<label.size();i++)
		{
			labelVertex[i] = 0;
		}
		ArrayList<Integer> dellabel = new ArrayList<Integer>();
		for(int i=0; i<label.size()-1; i++)
		{
			for(int j=1; j<label.size(); j++)
			{
				if(intersect2(label.get(i),label.get(j)))
				{
					//合并
					HashSet<Integer> a1 = new HashSet<Integer>();
					a1 = AddAll(label.get(i),label.get(j));
					for(Vertex v:vertexList)
					{
						if(v.communityId.size()!=0 && v.communityId.iterator().next()==j)
						{
							v.communityId.removeAll(v.communityId);
							v.communityId.add(i);
						}
					}
					label.get(j).removeAll(label.get(j));
				}
			}
		}
		for(Vertex v:vertexList)
		{
			if(v.communityId.size()!=0)
			    labelVertex[v.communityId.iterator().next()]++;
		}
		int del = 0;
		for(int i=0;i<labelNum;i++)
		{
			if(labelVertex[i] == 0)
			{
				for(int j = i ; j<labelNum-1; j++)
				{
					labelVertex[j] = labelVertex[j+1];					
					for(Vertex v:vertexList)
					{
						if(v.communityId.size()!=0 && v.communityId.iterator().next()==j+1)
						{
							v.communityId.removeAll(v.communityId);
							v.communityId.add(j);
						}
					}
				}
				labelVertex[labelNum-1] = 0;
			}
		}
		for(int i=0;i<labelNum;i++)
		{
			if(labelVertex[i] == 0)
			{
				for(int j = i ; j<labelNum-1; j++)
				{
					labelVertex[j] = labelVertex[j+1];					
					for(Vertex v:vertexList)
					{
						if(v.communityId.size()!=0 && v.communityId.iterator().next()==j+1)
						{
							v.communityId.removeAll(v.communityId);
							v.communityId.add(j);
						}
					}
				}
				labelVertex[labelNum-1] = 0;
			}
		}
		for(int i=0;i<labelNum;i++)
		{
			if(labelVertex[i] == 0)
				del++;
		}
		/*for(int i=0;i<labelNum-del;i++)
		{
			System.out.print(labelVertex[i]+"   ");
		}
		System.out.println();
		System.out.println(label.size()+" "+(labelNum- del));*/
		return labelNum- del;
	}
	
	public ArrayList<Vertex> getVertex() throws SQLException, ParseException
	{		
		MakeGraph mg = new MakeGraph();
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		ArrayList<Vertex> vertexList2 = mg.getVertex();
		for(Vertex v2:vertexList2)
		{
			Vertex tmp = vertexList.get(v2.getId());
			if(tmp.communityId.size()!=0)
			{
				v2.communityId.add(tmp.communityId.iterator().next());
			}
		}
		
		return vertexList2;
	}
	
	public static void main(String[] args) throws SQLException, IOException, ParseException
	{
		findTruss fu = new findTruss();
		fu.getFindTruss(5);
	}
}

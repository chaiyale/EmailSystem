package community;

import graph.Edge;
import graph.MakeGraph;
import graph.Vertex;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import dataProcess.proDataset;
import SuperGraph.SuperVertex;

public class pSCAN 
{//按照结构信息进行聚类的方法
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //原顶点集合
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //边集合
	public static int[] min_cn;  //为判断每个边设置三个参数：0,-1,-2.分别代表：未设置值，（经判断）结构相似，（经判断）结构不相似
	public static int[] pa;   //标记parent节点
	public static int[] rank; //union合并时，谁的rank大，合并到该社团下，即pa标记为rank大的节点
	public static int[] cid;  //社团id
	public static int[] cores;  //核心节点
	public static boolean[] visited;  //是否访问
	public static int cores_n = 0;
	public static int n;  //顶点个数
	public static int m;  //边的个数
	public static int miu;   //点的结构相似度个数大于miu，认定为核心节点
	public static double eps;   //点和邻居节点的临近相似度大于eps，认定为结构相似
	
	public pSCAN()
	{}
	
	public void pSCAN_main()
	{
		//初始化每个节点的sd.ed
		//prune_and_cross_link();  //初始化每个边的min_cn
		
		pa = new int[n];           //************************暂时不知道这部分是干什么的？？？？？？？？？？？？
		rank = new int[n];
		cores = new int[n];  //核心节点
		cid = new int[n];
		visited = new boolean[n];
		for(int j = 0;j < n;j ++) 
		{
			pa[j] = j;
			rank[j] = 0;
			visited[j] = false;
		}
		
		int[] bin_head = new int[n];    //*************使用单链表维护，节点度为i的在bin[i]中
		int[] bin_next = new int[n];
		for(int i = 0;i < n;i ++) 
			bin_head[i] = -1;

		int max_ed = 0;  int i =0;
		for(Vertex v:vertexList)
		{
			int ed = v.ed;
			if(ed > max_ed) max_ed = ed;
			bin_next[i] = bin_head[ed];
			bin_head[ed] = i;
			i++;
		}
		
		while(true)     
		{
			int u = -1;            
			if(cores_n!=0)          //************************找到u.按照ed不增长的顺序依次判别
		    {
				u = cores[-- cores_n]; //从核心节点中取出来一个
		    }
			else 
			{
				while(max_ed >= miu && u == -1) 
				{
					for(int x = bin_head[max_ed]; x != -1;) 
					{
						int tmp = bin_next[x];
						int ed = vertexList.get(x).ed;
						if(ed == max_ed) 
						{
							u = x;
							bin_head[max_ed] = bin_next[x];
							break;
						}
						else if(ed >= miu) 
						{
							bin_next[x] = bin_head[ed];
							bin_head[ed] = x;
						}
						x = tmp;
					}
					if(u == -1) 
					{
						bin_head[max_ed] = -1;
						-- max_ed;
					}
				}
			}
			if(u == -1) break;			
			Vertex uV = vertexList.get(u);
			
			ArrayList<Integer> edge_buf = new ArrayList<Integer>(); //*********************减少了与邻居节点结构相似度的计算量，WHY???????????
			ArrayList<Integer> Nei_buf = new ArrayList<Integer>();
			int edge_buf_n = 0;
			for(Edge e:uV.edge) 
			{
				int j = e.eid;
				int an = (u == e.nid1)?e.nid2:e.nid1;
				if(min_cn[j] == -2) continue;
				if(uV.sd < miu || find_root(u) != find_root(an)) 
				{
					edge_buf.add(j);
					edge_buf_n ++;
					Nei_buf.add(an);
				}
			}
			
			int i1 = 0;                  
			while(uV.sd < miu && uV.ed >= miu && i1 < edge_buf_n)  //*********************判断u是否是核心节点
			{
				int idx = edge_buf.get(i1);
				if(min_cn[idx] != -1) 
				{
					int v = Nei_buf.get(i1);
					Vertex vV = vertexList.get(v);

					min_cn[idx] = similar_check_OP(uV, vV, idx);   //计算节点的结构相似度
					
					if(min_cn[idx] == -1)
					{
						++ uV.sd;
					}
					else
					{
						-- uV.ed;
					}
					
					if(vV.ed >= 0)   //v没有被探索过
					{
						if(min_cn[idx] == -1)
						{
							++ vV.sd;
							if(vV.sd >= miu) cores[cores_n ++] = v;  //v加入核心节点
						}
						else
						{
							-- vV.ed;
						}
					}
				}
				++ i1;
			}
			
			uV.ed = -1; 
			if(uV.sd < miu) continue; 
			
			cluster_core_vertices(uV, edge_buf_n, edge_buf, Nei_buf, i1);   //将u的邻居核心节点与u进行聚类 
		}
		//将非核心节点进行聚类
		//cluster_noncore_vertices2();   //可达查询
		cluster_noncore_vertices();   //最短路径查询
	}
	
	public void cluster_core_vertices(Vertex uV, int edge_buf_n, ArrayList<Integer> edge_buf, ArrayList<Integer> Nei_buf, int i1)   //核心节点聚类
	{
		int u = uV.getId();
		for(int j=0; j < edge_buf_n; j++)   
		{
			int idx = edge_buf.get(j);
			int v = Nei_buf.get(j);
			Vertex vV = vertexList.get(v);
			
			if(min_cn[idx] == -1 && vV.sd>=miu)
			{
				my_union(u, v);
			}
		}
		
		while(i1 < edge_buf_n)   //对于尚未判断的邻居是核心节点的
		{
			int idx = edge_buf.get(i1);
			int v = Nei_buf.get(i1);
			Vertex vV = vertexList.get(v);
			if(min_cn[idx] < 0 || vV.sd<miu || find_root(u) == find_root(v))
			{
				++i1;
				continue;
			}
			
			min_cn[idx] = similar_check_OP(uV, vV, idx);   //计算节点的结构相似度
			if(vV.ed >= 0)   //v没有被探索过
			{
				if(min_cn[idx] == -1)
				{
					++ vV.sd;
					if(vV.sd >= miu) cores[cores_n ++] = v;  //v加入核心节点
				}
				else
				{
					-- vV.ed;
				}
			}
			if(min_cn[idx] == -1) 
			{
				my_union(u, v);
			}
			++i1;
		}
	}
	
	public void cluster_noncore_vertices()    //非核心节点聚类
	{
		for(int i=0;i<n;i++)
		{
			cid[i] = n;
		}
		for(int i=0;i<n;i++)
		{
			Vertex u = vertexList.get(i);
			if(u.sd>=miu)             //核心节点
			{
				for(Edge e:u.edge) 
				{
					int j = e.eid;
					int an = (u.getId() == e.nid1)?e.nid2:e.nid1;
					Vertex v = vertexList.get(an);
					if(v.sd<miu)   //核心节点的非核心节点邻居
					{
						if(min_cn[j]>=0)
						{
							min_cn[j] = similar_check_OP(u,v,j);
						    if(min_cn[j] == -1)
						    {
							    u.sd++;
							    v.sd++;
						    }
						}
						if(min_cn[j] == -1)
						{
							if(pa[an]==an)
							{
								pa[an] = u.getId();   //------------考虑重叠社区
							}
						}						
					}
				}
			}
		}
		for(int i=0;i<n;i++)
		{
			Vertex v = vertexList.get(i);
			int x = find_root(i);
			if(i < cid[x])
			{
				cid[x] = i;
			}
			v.communityId.add(cid[x]);
		}
	}
	
	public void cluster_noncore_vertices2()    //非核心节点聚类，把所有剩下的节点都找到归属
	{
		for(int i=0;i<n;i++)
		{
			cid[i] = n;
		}
		for(int i=0;i<n;i++)
		{
			Vertex u = vertexList.get(i);
			if(u.sd>=miu)             //核心节点
			{
				visited[i] = true;
				for(Edge e:u.edge) 
				{
					int j = e.eid;
					int an = (u.getId() == e.nid1)?e.nid2:e.nid1;
					Vertex v = vertexList.get(an);
					if(v.sd<miu)   //核心节点的非核心节点邻居
					{
						if(pa[an]==an)
						{
							visited[an] = true;
							pa[an] = u.getId();
						}						
					}
				}
			}
		}
		for(int i=0;i<n;i++)
		{
			if(visited[i] == false)
			{
				Vertex u = vertexList.get(i);
				dfs(u);
			}
		}
		
		for(int i=0;i<n;i++)
		{
			Vertex v = vertexList.get(i);
			int x = find_root(i);
			if(i < cid[x])
			{
				cid[x] = i;
			}
			v.communityId.add(cid[x]);
		}
	}
	
	public void dfs(Vertex u)   //深度优先遍历---递归
	{
		visited[u.getId()] = true;  
		//System.out.println(u.getId()+":  "+superVertex_Id);
		for(int vid:u.neighborIdTypeALLbySort)
		{
			if(visited[vid] && pa[vid]!=vid)
			{
				my_union(u.getId(),vid);
			}
			if(!visited[vid])
			{
				Vertex v = vertexList.get(vid);
				my_union(u.getId(),vid);
				dfs(v);
			}
		}
	}
	
	/*public void prune_and_cross_link()   //剪枝和预处理
	{
		//剪枝，这个在本公式下用不上了
		for(Edge e:edgeList)
		{
			Vertex v1 = vertexList.get(e.nid1);
			Vertex v2 = vertexList.get(e.nid2);
			int c = (int) (eps*(v1.weight + v2.weight));   //满足结构相似的分子的下界
			min_cn[e.eid] = c;  
		}
		
		for(int i=0;i<m;i++)
		{
			if(min_cn[i]==0)
			{
				System.out.print("");
			}
		}
	}*/
	
	public int find_root(int u)   //找到所在社团的根节点。从这个节点开始不断扩展
	{
		int x = u;
		while(pa[x] != x)
	    {
			x = pa[x];
	    }

		while(pa[u] != x) 
		{
			int tmp = pa[u];
			pa[u] = x;
			u = tmp;
		}

		return x;
	}
	
	public void my_union(int u, int v)  //合并u,v两个节点
	{
		int ru = find_root(u);
		int rv = find_root(v);

		if(ru == rv) return ;

		if(rank[ru] < rank[rv]) pa[ru] = rv;
		else if(rank[ru] > rank[rv]) pa[rv] = ru;
		else {
			pa[rv] = ru;
			++ rank[ru];
		}
	}
	
	public int similar_check_OP(Vertex u, Vertex v, int idx)   //判断节点之间的结构相似性
	{
		if(min_cn[idx] == 0)
		{
			//int c = (int) (eps*(u.weight + v.weight));   //满足结构相似的分子的下界
			int c= (int) (eps*Math.sqrt(u.centrality*v.centrality));
			if(c*c < (double) (eps*Math.sqrt(u.centrality*v.centrality)))
			{
				c++;
			}
			if(u.centrality < c || v.centrality < c)
			{
				return -2;
			}
			if(c<=2)
			{
				return -1;
			}
			min_cn[idx] = c;
		}
		return check_common_neighbor(u,v,min_cn[idx]);
	}
	
	public int check_common_neighbor(Vertex u, Vertex v, int c1) 
	{
		int cn = 2; 
		int i = 0 , j = 0;
		int du = u.centrality+1, dv = v.centrality+1;
		while(i<u.centrality && j<v.centrality && cn<c1 && du>=c1 && dv>=c1)
		{
			int nei_U = u.neighborIdTypeALLbySort.get(i);
			int nei_V = v.neighborIdTypeALLbySort.get(j);
			if(nei_U<nei_V)
			{
				--du;
				++i;
			}
			else if(nei_U>nei_V)
			{
				--dv;
				++j;
			}
			else
			{
				++cn;
				++i;
				++j;
			}
		}
		
		if(cn>=c1)
		{
			return -1;
		}
		return -2;
	}
	
	public void print()   //打印结果
	{
		int count = 0;
		for(Vertex v:vertexList)
		{
			if(v.communityId.iterator().hasNext())
			{
				//System.out.println(v.communityId.iterator().next());
				count++;
			}			    
		}
		System.out.println(count);
	}
	
	public static ArrayList<Vertex> getVertex(ArrayList<Vertex> vList,  ArrayList<Edge> eList) throws SQLException, ParseException
	//public static ArrayList<Vertex> getVertex() throws SQLException, ParseException
	{
		vertexList = vList;
		edgeList = eList;
		
		/*MakeGraph mg = new MakeGraph();   //邮件数据集
		Date start = new Date(); //计时
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		vertexList = mg.getVertex();
		edgeList = mg.getEdge();*/
		
		n = vertexList.size();
		m = edgeList.size();
		miu = 3; //miu赋值，这个参数怎么确定
		eps = 0.3; //esps赋值，这个参数怎么确定
		min_cn = new int[m];
		for(int i=0; i<m; i++)
		{
			min_cn[i] = 0;
		}
		
		pSCAN ps = new pSCAN();
		ps.pSCAN_main();
		//ps.print();
		
		/*for(Vertex v:vertexList)
		{
			if(v.communityId.size()==0)
			{
				v.communityId.add(n++);   //对于没有所属社团的进行操作
			}			    
		}*/
	
		return vertexList;
	}
	
	public static void main(String[] args) throws SQLException, ParseException
	{
		/*MakeGraph mg = new MakeGraph();   //邮件数据集
		Date start = new Date(); //计时
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		vertexList = mg.getVertex();
		edgeList = mg.getEdge();
		System.out.println(vertexList.size());
		getVertex(vertexList,edgeList);*/
		
		proDataset pd = new proDataset();  //测试数据集
		String filePath = "./CA-GrQc.txt";
		pd.readTxtFile(filePath);
		pd.makeGraph();
		vertexList = pd.getVertex();
		edgeList = pd.getEdge();
		getVertex(vertexList,edgeList);
	}
}

package community;

import graph.Edge;
import graph.MakeGraph;
import graph.Vertex;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

public class wSCAN 
{//wSCAN: weighted-structural clustering algorithm for networks
	
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //原顶点集合
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //边集合
	public static int[] min_cn;  //为判断每个边设置三个参数：0,-1,-2.分别代表：未设置值，（经判断）结构相似，（经判断）结构不相似
	public static int[] pa;   //标记parent节点
	public static int[] rank; //union合并时，谁的rank大，合并到该社团下，即pa标记为rank大的节点
	public static int[] cid;  //社团id
	public static int[] cores;  //核心节点
	public static int cores_n = 0;
	public static int n;  //顶点个数
	public static int m;  //边的个数
	public static int miu;   //点的结构相似度个数大于miu，认定为核心节点
	public static double eps1;   //点和邻居节点的临近相似度大于eps，认定为结构相似
	public static double eps2;   //点和邻居节点的临近相似度大于eps，认定为结构相似
	
	public wSCAN()
	{}
	
	public void wSCAN_main()
	{
		//初始化每个节点的sd.ed
		//prune_and_cross_link();  //初始化每个边的min_cn
		
		pa = new int[n];           //************************暂时不知道这部分是干什么的？？？？？？？？？？？？
		rank = new int[n];
		cores = new int[n];  //核心节点
		cid = new int[n];
		for(int j = 0;j < n;j ++) 
		{
			pa[j] = j;
			rank[j] = 0;
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
		cluster_noncore_vertices();   //将非核心节点进行聚类
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
			Vertex v = vertexList.get(i);
			if(v.sd>=miu)
			{
				int x = find_root(i);
				if(i < cid[x])
				{
					cid[x] = i;
				}
				v.communityId.add(cid[x]);
			}
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
								pa[an] = u.getId();
							}
						}						
					}
				}
			}
		}
	}
	
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
			int c1= (int) (eps1*Math.sqrt(u.centrality*v.centrality));
			if(c1*c1 < (double) (eps1*Math.sqrt(u.centrality*v.centrality)))
			{
				c1++;
			}
			if(u.centrality < c1 || v.centrality < c1)
			{
				return -2;
			}
			if(c1<=2)
			{
				return -1;
			}
			min_cn[idx] = c1;
		}
		return check_common_neighbor(u,v,min_cn[idx]);
	}
	
	public int check_common_neighbor(Vertex u, Vertex v, int c1) 
	{
		int cn = 2;  int cn2 = 1;
		int i = 0 , j = 0;
		int du = u.centrality+1, dv = v.centrality+1;
		int c2 = (int) (eps2*(u.weight + v.weight));
		while(i<u.centrality && j<v.centrality && (cn<c1 || cn2<c2) && du>=c1 && dv>=c1)
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
				Vertex w = vertexList.get(nei_V);   //第三个点
				double w1 = getEdgeWeight(u,w);
				double w2 = getEdgeWeight(v,w);
				double recip = 1- Math.abs((w1-w2)/(w1+w2));
				cn2 += recip*(w1+w2);
			}
		}
		
		if(cn2>=c2 && cn>=c1)
		{
			return -1;
		}
		return -2;
	}
	
	public double getEdgeWeight(Vertex u, Vertex v)
	{
		Vertex small = u, big = v;
		if(u.centrality>v.centrality)
		{
			small = v;
			big = u;
		}
		
		for(Edge e:small.edge)
		{
			int nid2 = (small.getId()==e.nid1)?e.nid2:e.nid1;
			if(nid2 == big.getId())
			{
				return e.weight;
			}
		}
		return 0;
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
	{
		vertexList = vList;
		edgeList = eList;
		
		/*MakeGraph mg = new MakeGraph();   
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		vertexList = mg.getVertex();
		edgeList = mg.getEdge();*/
		
		n = vertexList.size();
		m = edgeList.size();
		miu = 3; //miu赋值，这个参数怎么确定
		eps1 = 0.25; //esps赋值，这个参数怎么确定
		eps2 = 0.06;
		min_cn = new int[m];
		for(int i=0; i<m; i++)
		{
			min_cn[i] = 0;
		}
		
		wSCAN ws = new wSCAN();
		ws.wSCAN_main();
		ws.print();
		
		for(Vertex v:vertexList)
		{
			if(v.communityId.size()==0)
			{
				v.communityId.add(n++);   //对于没有所属社团的进行操作
			}			    
		}
	
		return vertexList;
	}
}

package community;

import graph.Edge;
import graph.MakeGraph;
import graph.Vertex;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

public class wSCAN 
{//wSCAN: weighted-structural clustering algorithm for networks
	
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //ԭ���㼯��
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //�߼���
	public static int[] min_cn;  //Ϊ�ж�ÿ������������������0,-1,-2.�ֱ����δ����ֵ�������жϣ��ṹ���ƣ������жϣ��ṹ������
	public static int[] pa;   //���parent�ڵ�
	public static int[] rank; //union�ϲ�ʱ��˭��rank�󣬺ϲ����������£���pa���Ϊrank��Ľڵ�
	public static int[] cid;  //����id
	public static int[] cores;  //���Ľڵ�
	public static int cores_n = 0;
	public static int n;  //�������
	public static int m;  //�ߵĸ���
	public static int miu;   //��Ľṹ���ƶȸ�������miu���϶�Ϊ���Ľڵ�
	public static double eps1;   //����ھӽڵ���ٽ����ƶȴ���eps���϶�Ϊ�ṹ����
	public static double eps2;   //����ھӽڵ���ٽ����ƶȴ���eps���϶�Ϊ�ṹ����
	
	public wSCAN()
	{}
	
	public void wSCAN_main()
	{
		//��ʼ��ÿ���ڵ��sd.ed
		//prune_and_cross_link();  //��ʼ��ÿ���ߵ�min_cn
		
		pa = new int[n];           //************************��ʱ��֪���ⲿ���Ǹ�ʲô�ģ�����������������������
		rank = new int[n];
		cores = new int[n];  //���Ľڵ�
		cid = new int[n];
		for(int j = 0;j < n;j ++) 
		{
			pa[j] = j;
			rank[j] = 0;
		}
		
		int[] bin_head = new int[n];    //*************ʹ�õ�����ά�����ڵ��Ϊi����bin[i]��
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
			if(cores_n!=0)          //************************�ҵ�u.����ed��������˳�������б�
		    {
				u = cores[-- cores_n]; //�Ӻ��Ľڵ���ȡ����һ��
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
			
			ArrayList<Integer> edge_buf = new ArrayList<Integer>(); //*********************���������ھӽڵ�ṹ���ƶȵļ�������WHY???????????
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
			while(uV.sd < miu && uV.ed >= miu && i1 < edge_buf_n)  //*********************�ж�u�Ƿ��Ǻ��Ľڵ�
			{
				int idx = edge_buf.get(i1);
				if(min_cn[idx] != -1) 
				{
					int v = Nei_buf.get(i1);
					Vertex vV = vertexList.get(v);

					min_cn[idx] = similar_check_OP(uV, vV, idx);   //����ڵ�Ľṹ���ƶ�
					
					if(min_cn[idx] == -1)
					{
						++ uV.sd;
					}
					else
					{
						-- uV.ed;
					}
					
					if(vV.ed >= 0)   //vû�б�̽����
					{
						if(min_cn[idx] == -1)
						{
							++ vV.sd;
							if(vV.sd >= miu) cores[cores_n ++] = v;  //v������Ľڵ�
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
			
			cluster_core_vertices(uV, edge_buf_n, edge_buf, Nei_buf, i1);   //��u���ھӺ��Ľڵ���u���о��� 
		}
		cluster_noncore_vertices();   //���Ǻ��Ľڵ���о���
	}
	
	public void cluster_core_vertices(Vertex uV, int edge_buf_n, ArrayList<Integer> edge_buf, ArrayList<Integer> Nei_buf, int i1)   //���Ľڵ����
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
		
		while(i1 < edge_buf_n)   //������δ�жϵ��ھ��Ǻ��Ľڵ��
		{
			int idx = edge_buf.get(i1);
			int v = Nei_buf.get(i1);
			Vertex vV = vertexList.get(v);
			if(min_cn[idx] < 0 || vV.sd<miu || find_root(u) == find_root(v))
			{
				++i1;
				continue;
			}
			
			min_cn[idx] = similar_check_OP(uV, vV, idx);   //����ڵ�Ľṹ���ƶ�
			if(vV.ed >= 0)   //vû�б�̽����
			{
				if(min_cn[idx] == -1)
				{
					++ vV.sd;
					if(vV.sd >= miu) cores[cores_n ++] = v;  //v������Ľڵ�
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
	
	public void cluster_noncore_vertices()    //�Ǻ��Ľڵ����
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
			if(u.sd>=miu)             //���Ľڵ�
			{
				for(Edge e:u.edge) 
				{
					int j = e.eid;
					int an = (u.getId() == e.nid1)?e.nid2:e.nid1;
					Vertex v = vertexList.get(an);
					if(v.sd<miu)   //���Ľڵ�ķǺ��Ľڵ��ھ�
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
	
	public int find_root(int u)   //�ҵ��������ŵĸ��ڵ㡣������ڵ㿪ʼ������չ
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
	
	public void my_union(int u, int v)  //�ϲ�u,v�����ڵ�
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
	
	public int similar_check_OP(Vertex u, Vertex v, int idx)   //�жϽڵ�֮��Ľṹ������
	{
		if(min_cn[idx] == 0)
		{
			//int c = (int) (eps*(u.weight + v.weight));   //����ṹ���Ƶķ��ӵ��½�
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
				Vertex w = vertexList.get(nei_V);   //��������
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
	
	public void print()   //��ӡ���
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
		miu = 3; //miu��ֵ�����������ôȷ��
		eps1 = 0.25; //esps��ֵ�����������ôȷ��
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
				v.communityId.add(n++);   //����û���������ŵĽ��в���
			}			    
		}
	
		return vertexList;
	}
}

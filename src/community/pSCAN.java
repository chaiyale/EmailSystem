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
{//���սṹ��Ϣ���о���ķ���
	public static ArrayList<Vertex> vertexList = new ArrayList<Vertex>();  //ԭ���㼯��
	public static ArrayList<Edge> edgeList = new ArrayList<Edge>();  //�߼���
	public static int[] min_cn;  //Ϊ�ж�ÿ������������������0,-1,-2.�ֱ����δ����ֵ�������жϣ��ṹ���ƣ������жϣ��ṹ������
	public static int[] pa;   //���parent�ڵ�
	public static int[] rank; //union�ϲ�ʱ��˭��rank�󣬺ϲ����������£���pa���Ϊrank��Ľڵ�
	public static int[] cid;  //����id
	public static int[] cores;  //���Ľڵ�
	public static boolean[] visited;  //�Ƿ����
	public static int cores_n = 0;
	public static int n;  //�������
	public static int m;  //�ߵĸ���
	public static int miu;   //��Ľṹ���ƶȸ�������miu���϶�Ϊ���Ľڵ�
	public static double eps;   //����ھӽڵ���ٽ����ƶȴ���eps���϶�Ϊ�ṹ����
	
	public pSCAN()
	{}
	
	public void pSCAN_main()
	{
		//��ʼ��ÿ���ڵ��sd.ed
		//prune_and_cross_link();  //��ʼ��ÿ���ߵ�min_cn
		
		pa = new int[n];           //************************��ʱ��֪���ⲿ���Ǹ�ʲô�ģ�����������������������
		rank = new int[n];
		cores = new int[n];  //���Ľڵ�
		cid = new int[n];
		visited = new boolean[n];
		for(int j = 0;j < n;j ++) 
		{
			pa[j] = j;
			rank[j] = 0;
			visited[j] = false;
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
		//���Ǻ��Ľڵ���о���
		//cluster_noncore_vertices2();   //�ɴ��ѯ
		cluster_noncore_vertices();   //���·����ѯ
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
								pa[an] = u.getId();   //------------�����ص�����
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
	
	public void cluster_noncore_vertices2()    //�Ǻ��Ľڵ���࣬������ʣ�µĽڵ㶼�ҵ�����
	{
		for(int i=0;i<n;i++)
		{
			cid[i] = n;
		}
		for(int i=0;i<n;i++)
		{
			Vertex u = vertexList.get(i);
			if(u.sd>=miu)             //���Ľڵ�
			{
				visited[i] = true;
				for(Edge e:u.edge) 
				{
					int j = e.eid;
					int an = (u.getId() == e.nid1)?e.nid2:e.nid1;
					Vertex v = vertexList.get(an);
					if(v.sd<miu)   //���Ľڵ�ķǺ��Ľڵ��ھ�
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
	
	public void dfs(Vertex u)   //������ȱ���---�ݹ�
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
	
	/*public void prune_and_cross_link()   //��֦��Ԥ����
	{
		//��֦������ڱ���ʽ���ò�����
		for(Edge e:edgeList)
		{
			Vertex v1 = vertexList.get(e.nid1);
			Vertex v2 = vertexList.get(e.nid2);
			int c = (int) (eps*(v1.weight + v2.weight));   //����ṹ���Ƶķ��ӵ��½�
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
	//public static ArrayList<Vertex> getVertex() throws SQLException, ParseException
	{
		vertexList = vList;
		edgeList = eList;
		
		/*MakeGraph mg = new MakeGraph();   //�ʼ����ݼ�
		Date start = new Date(); //��ʱ
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		vertexList = mg.getVertex();
		edgeList = mg.getEdge();*/
		
		n = vertexList.size();
		m = edgeList.size();
		miu = 3; //miu��ֵ�����������ôȷ��
		eps = 0.3; //esps��ֵ�����������ôȷ��
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
				v.communityId.add(n++);   //����û���������ŵĽ��в���
			}			    
		}*/
	
		return vertexList;
	}
	
	public static void main(String[] args) throws SQLException, ParseException
	{
		/*MakeGraph mg = new MakeGraph();   //�ʼ����ݼ�
		Date start = new Date(); //��ʱ
		mg.makeGraph();
		mg.computeWeightType1();
		mg.computeWeightType2();
		vertexList = mg.getVertex();
		edgeList = mg.getEdge();
		System.out.println(vertexList.size());
		getVertex(vertexList,edgeList);*/
		
		proDataset pd = new proDataset();  //�������ݼ�
		String filePath = "./CA-GrQc.txt";
		pd.readTxtFile(filePath);
		pd.makeGraph();
		vertexList = pd.getVertex();
		edgeList = pd.getEdge();
		getVertex(vertexList,edgeList);
	}
}

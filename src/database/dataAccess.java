package database;

import graph.Edge;
import graph.Vertex;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import SuperGraph.SuperEdge;
import SuperGraph.SuperVertex;

public class dataAccess {
	
	private dataAccessBase db = new dataAccessBase();
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public dataAccess()
	{
	}
	
	public int getAllTableNum() 
	{
		String sql = "show tables;";
		ResultSet rs = db.executeQuery(sql);
		int sum=0;
		try {
			while(rs.next())
			{
				sum++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("s="+sum);
		return sum;
	}
	
	public String[] getAllTableName2() 
	{
		int j = getAllTableNum();
	    String s[] = new String[j];
		String sql = "show tables;";
	    System.out.println(sql);/////////////////////
	    ResultSet rs = db.executeQuery(sql);
	    int i=0;
	    try {
			while(rs.next())
			{
				s[i] = rs.getString(1);
				//System.out.println(s[i]);
				i++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}
	
	public String[] getAllAttribute(String tablename)
	{
		String sql = "select * from "+ tablename + " limit 1;";
	    //System.out.println(sql);
	    ResultSet rs = db.executeQuery(sql);
	    ResultSetMetaData rsmd = null;
	    int colcount = 0;
	    try {
			rsmd = rs.getMetaData();
			colcount = rsmd.getColumnCount(); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] s = new String[colcount]; 
		for(int i=0;i<colcount;i++)
		{
			try {
				s[i]= rsmd.getColumnName(i+1);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	    return s;
	}
	public void index_log_init()
	{
		String sql="update glaucus_mail_log set status=5";
		db.executeUpdate(sql);
	}
	public void index_log_begin()
	{
		String sql="update glaucus_mail_log set status=2 where status=1 or status=4";
		db.executeUpdate(sql);
	}
	public void index_log_end()
	{
		String sql="update glaucus_mail_log set status=3 where status=2";
		db.executeUpdate(sql);
	}
	public void index_log_init_end()
	{
		String sql="update glaucus_mail_log set status=6 where status=5";
		db.executeUpdate(sql);
	}
	public void attach_log_begin()
	{
		String sql="insert into glaucus_attachment_log ('status','inserttime') values ('0','"+df.format(new Date())+"')";
		db.executeUpdate(sql);
	}
	public void attach_log_end()
	{
		String sql="update glaucus_attachment_log set status=1 where status=0";
		db.executeUpdate(sql);
	}
		
	public ResultSet getRecord() 
	{
		//String sql = "select * from glaucus_mail_information1 where id<60000;"; //Test
		String sql = "select * from glaucus_mail_information1;";
		ResultSet rs = db.executeQuery(sql);
		return rs;
	}
	
	public ResultSet getRecord2(int id) 
	{
		String sql = "select * from glaucus_mail_information where id="+id+";";
		//System.out.println(sql);
		ResultSet rs = db.executeQuery(sql);
		return rs;
	}
	
	public ResultSet getAddRecord(String id)  //鑾峰緱鏂板姞鍏ユ暟鎹簱鐨勬暟鎹�
	{
		String sql = "select * from glaucus_mail_information where id>"+id+";";
		ResultSet rs = db.executeQuery(sql);
		return rs;
	}
	
	public ResultSet getUpdateRecord(String id)  //鑾峰緱鏂板姞鍏ユ暟鎹簱鐨勬暟鎹�
	{
		String sql = "select * from glaucus_mail_information where id="+id+";";
		ResultSet rs = db.executeQuery(sql);
		return rs;
	}
	/***
	 * 向数据库加入数据
	 */
	public void UpdateRecord(String sql)
	{
		db.executeUpdate(sql);
	}
	/***
	 * 查询jsonpack表
	 */
	public boolean QueryRecord(int user_id,String type) throws SQLException
	{
		boolean bool = false;
		String judge = "select * from glaucus_jsonpack where user_id=" +user_id +" and graph_type =" +"'" + type +"'" ;
		ResultSet rs = db.executeQuery(judge);
		if(rs.next())
		{
			bool = true;
		}
		return bool;
	}
	public void close() { 
		db.close();
	}
	
	public void writeVertex(ArrayList<Vertex> vertexList) throws SQLException  //将顶点集写入到数据库中
	{
		String sqll = "delete from Vertex";  //先清空原来的
		db.executeUpdate(sqll);
		
		Connection conn = db.getConn();
		Boolean edgeflag ;
	    Boolean SVflag ;
	    Boolean neighborIdflag;
	    for(int i = 0;i < vertexList.size();i++) 
	    {
	        Vertex v = vertexList.get(i);
	        //将边集合转换为字符串
	        Iterator<Edge> itedge = v.edge.iterator(); 
	        StringBuffer vedge = new StringBuffer();
	        edgeflag = false;
	        while(itedge.hasNext()) {  
	            if(edgeflag == false) {
	                vedge.append(itedge.next().eid);
	                edgeflag = true;
	            }
	            else {
	                vedge.append("," + itedge.next().eid);
	            }
	        }
	        //将超级节点集合转换为字符串
	        SVflag = false;
            Iterator<Integer> itSVId = v.SuperVId.iterator(); 
            StringBuffer vSVId = new StringBuffer();
            while(itSVId.hasNext()) {  
                if(SVflag == false) {
                    vSVId.append(itSVId.next());
                    SVflag = true;
                }
                else {
                    vSVId.append("," + itSVId.next());
                }
            }
            //将所有相邻节点Id转换为字符串
            neighborIdflag = false;
            Iterator<Integer> itneighborId = v.neighborIdTypeALLbySort.iterator();
            StringBuffer vneighborId = new StringBuffer();
            while(itneighborId.hasNext()) {
                if(neighborIdflag == false) {
                    vneighborId.append(itneighborId.next());
                    neighborIdflag = true;
                }
                else {
                    vneighborId.append("," + itneighborId.next());
                }
            }
            //写入到数据库中
	        String sql = "insert into Vertex(vid, name, edge, SuperVId, centrality, neighborIdTypeALL) values (?, ?, ?, ?, ?, ?)";
	        PreparedStatement pst = conn.prepareStatement(sql);
	        pst.setInt(1, v.getId());
	        pst.setString(2, v.getName());
	        pst.setString(3, vedge.toString());
	        pst.setString(4,  vSVId.toString());
	        pst.setInt(5, v.centrality);
	        pst.setString(6, vneighborId.toString());
	        pst.execute();
	    }
	}
	
	public void writeEdge(ArrayList<Edge> edgeList) throws SQLException   //将边的集合写入到数据库中
	{
		String sqll = "delete from Edge";
		db.executeUpdate(sqll);
		
		Connection conn = db.getConn();
        for(int i = 0;i < edgeList.size();i++) 
        {
            Edge e = edgeList.get(i);
            String sql = "insert into Edge(eid, nid1, nid2, weight) values (?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, e.eid);
            pst.setInt(2, e.nid1);
            pst.setInt(3, e.nid2);
            pst.setFloat(4, e.weight);
            pst.execute();
        }
	}
	
	public ArrayList<Vertex> readVertex() throws SQLException   //从数据库中读出顶点集合信息
	{
		dataAccessBase db1 = new dataAccessBase();
	    ResultSet rs = db.executeQuery("select * from Vertex");
	    ArrayList<Vertex> vl = new ArrayList<Vertex>();
	    //int iter =0;
	    while(rs.next()) 
	    {
	        //System.out.println(iter++);
	    	Vertex v = new Vertex(rs.getInt(1),rs.getString(2));
	        //为每个点构建其边集合
	        String[] edges = rs.getString(3).split(",");
	        HashSet<Edge> EdgeSet = new HashSet<Edge>();
	        for(int i = 0;i < edges.length;i++) {
	            ResultSet rsedge = db1.executeQuery("select * from Edge where eid = " + edges[i]);
	            if(rsedge.next()) {
	                Edge e = new Edge(rsedge.getInt(1), rsedge.getInt(2), rsedge.getInt(3));
	                e.weight = rsedge.getFloat(4);
	                EdgeSet.add(e);
	            }
	        }
	        //为每个点构建其所在超级节点集合
	        String[] supervs = rs.getString(4).split(",");
	        HashSet<Integer> superVId = new HashSet<Integer>();
	        for(int i = 0;i < supervs.length;i++) 
	        {
	            if(!supervs[i].equals(""))
	        	    superVId.add(Integer.parseInt(supervs[i]));
	        }
	        //为每个点构建其所有相邻节点Id
	        String[] neighborIds = rs.getString(6).split(",");
	        HashSet<Integer> neighborIdTypeALL = new HashSet<Integer>();
	        for(int i = 0;i < neighborIds.length;i++) {
	            if(!neighborIds[i].equals("")) {
	                neighborIdTypeALL.add(Integer.parseInt(neighborIds[i]));
	            }
	        }
	        //赋值
	        v.edge = EdgeSet;
	        v.SuperVId = superVId;
	        v.centrality = rs.getInt(5);
	        v.neighborIdTypeALL = neighborIdTypeALL;
	        vl.add(v);
	    }
	    return vl;
	}
	
	public ArrayList<Edge> readEdge() throws SQLException    //从数据库中读出边集合信息
	{
        ResultSet rs = db.executeQuery("select * from Edge");
        ArrayList<Edge> el = new ArrayList<Edge>();
        while(rs.next()) {
            Edge e = new Edge(rs.getInt(1), rs.getInt(2), rs.getInt(3));
            e.weight = rs.getFloat(4);
            el.add(e);
        }
        return el;
	}
	
	/**
	 * 更新Vertex表中的SuperVId
	 * @param vertexList Vertex类型的数组
	 */
	public void updateSuperVId(ArrayList<Vertex> vertexList) 
	{
	    Boolean flag = false;
	    for(int i = 0;i < vertexList.size();i++) {
	        flag = false;
	        StringBuffer svids = new StringBuffer();
	        Iterator<Integer> it = vertexList.get(i).SuperVId.iterator();
	        while(it.hasNext()) {
	            if(flag == false) {
	                svids.append(it.next());
	                flag = true;
	            } else {
	                svids.append("," + it.next());
	            }
	        }
	        db.executeUpdate("update Vertex set SuperVId = \"" + svids + "\" where vid = " + vertexList.get(i).getId());
	    }
	}
	
	public void writeSuperVertex(ArrayList<SuperVertex> superVertexList) throws SQLException  //写入超级节点信息到数据库
	{
		String sqll = "delete from SuperVertex";
		db.executeUpdate(sqll);
		
		Connection conn = db.getConn();
        Boolean edgeflag;
        Boolean CVflag;
        Boolean RVflag;
        for(int i = 0;i < superVertexList.size();i++) 
        {
            SuperVertex sv = superVertexList.get(i);
            //将边集合转换为字符串
            Iterator<SuperEdge> itedge = sv.edge.iterator(); 
            StringBuffer vedge = new StringBuffer();
            edgeflag = false;
            while(itedge.hasNext()) {  
                if(edgeflag == false) {
                    vedge.append(itedge.next().ceid);
                    edgeflag = true;
                }
                else {
                    vedge.append("," + itedge.next().ceid);
                }
            }  
            //将包含的点集合转换为字符串
            Iterator<Integer> itCV = sv.containVertex.iterator(); 
            StringBuffer vCV= new StringBuffer();
            CVflag = false;
            while(itCV.hasNext()) {  
                if(CVflag == false) {
                    vCV.append(itCV.next());
                    CVflag = true;
                }
                else {
                    vCV.append("," + itCV.next());
                }
            }
            //将外围的点集合转换为字符串
            Iterator<Integer> itCV2 = sv.outVertex.iterator(); 
            StringBuffer vCV2= new StringBuffer();
            CVflag = false;
            while(itCV2.hasNext()) {  
                if(CVflag == false) {
                    vCV2.append(itCV2.next());
                    CVflag = true;
                }
                else {
                    vCV2.append("," + itCV2.next());
                }
            }
            //将到其他节点的最长边-最短边转换为字符串
            /*Iterator<Entry<Integer, Double>> itRV = sv.Diff.entrySet().iterator();
            StringBuffer vRV= new StringBuffer();
            RVflag = false;
            while(itRV.hasNext()) {  
                if(RVflag == false) {
                    vRV.append(itRV.next());
                    RVflag = true;
                }
                else {
                    vRV.append("," + itRV.next().getKey()+":"+itRV.next().getValue());
                }
            }*/
            StringBuffer vRV= new StringBuffer();
            for(Map.Entry<Integer, Double> entry: sv.Diff.entrySet())
            {
            	vRV.append("," + entry.getKey()+":"+entry.getValue());
            }
            
            //将邻居节点转化为字符串
            Iterator<Integer> neis = sv.neighborId.iterator(); 
            StringBuffer str = new StringBuffer();
            CVflag = false;
            while(neis.hasNext()) {  
                if(CVflag == false) {
                    str.append(neis.next());
                    CVflag = true;
                }
                else {
                    str.append("," + neis.next());
                }
            }
            //写入到数据库中
            String sql = "insert into SuperVertex(cid, kid, edge, containVertex, outVertex, parentId, diff, neibor) values (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, sv.cid);
            pst.setInt(2, sv.kid);
            pst.setString(3, vedge.toString());
            pst.setString(4, vCV.toString());
            pst.setString(5, vCV2.toString());
            pst.setInt(6, sv.parentId);
            pst.setString(7, vRV.toString());
            pst.setString(8, str.toString());
            pst.execute();
        }
	}
	
	public void writeSuperEdge(ArrayList<SuperEdge> superEdgeList) throws SQLException  //写入超级节点之间的边信息到数据库
	{
		String sqll = "delete from SuperEdge";
		db.executeUpdate(sqll);
		
		Connection conn = db.getConn();
        for(int i = 0;i < superEdgeList.size();i++) 
        {
            SuperEdge se = superEdgeList.get(i);
            
            //将Incid1转换为字符串
            Iterator<Integer> itCV1 = se.Incid1.iterator();
            StringBuffer vCV1= new StringBuffer();
            Boolean CVflag = false;
            while(itCV1.hasNext()) {  
                if(CVflag == false) {
                    vCV1.append(itCV1.next());
                    CVflag = true;
                }
                else {
                    vCV1.append("," + itCV1.next());
                }
            }
            
            //将Incid1转换为字符串
            Iterator<Integer> itCV2 = se.Incid2.iterator();
            StringBuffer vCV2= new StringBuffer();
            CVflag = false;
            while(itCV2.hasNext()) {  
                if(CVflag == false) {
                    vCV2.append(itCV2.next());
                    CVflag = true;
                }
                else {
                    vCV2.append("," + itCV2.next());
                }
            }
            
            String sql = "insert into SuperEdge(ceid, cid1, cid2, weight, Incid1, Incid2) values (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, se.ceid);
            pst.setInt(2, se.cid1);
            pst.setInt(3, se.cid2);
            pst.setDouble(4, se.weight);
            pst.setString(5, vCV1.toString());
            pst.setString(6, vCV2.toString());
            pst.execute();
        }
	}

	public ArrayList<SuperVertex> readSuperVertex() throws SQLException   //从数据库读出超级节点信息
	{
	    dataAccessBase db1 = new dataAccessBase();
        ResultSet rs = db.executeQuery("select * from SuperVertex");
        ArrayList<SuperVertex> vl = new ArrayList<SuperVertex>();
        while(rs.next()) {
            SuperVertex v = new SuperVertex(rs.getInt(1));
          //为每个超级节点构建其包含的节点集合
            String[] containvs = rs.getString(4).split(",");
            HashSet<Integer> CVId = new HashSet<Integer>();
            for(int i = 0;i < containvs.length;i++) {
                CVId.add(Integer.parseInt(containvs[i]));
            }
            //为每个超级节点构建其外部的节点集合
            HashSet<Integer> CVId2 = new HashSet<Integer>();
            if(rs.getString(5) == null ||rs.getString(5).equals(""))
            {
            	CVId2 = null;
            }
            else
            {
            	String[] containvs2 = rs.getString(5).split(",");
            	for(int i = 0;i < containvs2.length;i++) {
                    CVId2.add(Integer.parseInt(containvs2[i]));
                }
            }
      
            /*//为每个超级节点构建其可达的超级节点集合
            String[] reachvs = rs.getString(5).split(",");
            HashSet<Integer> RVId = new HashSet<Integer>();
            for(int i = 0;i < reachvs.length;i++) {
                RVId.add(Integer.parseInt(reachvs[i]));
            }*/
            String pi = rs.getString(6).toString();
            int parentId = Integer.parseInt(pi);
            //为每个点构建其边集合
            HashSet<SuperEdge> EdgeSet = new HashSet<SuperEdge>();
            if(rs.getString(3) == null ||rs.getString(3).equals(""))
            {
            	EdgeSet = null;
            }
            else
            {
            	String[] edges = rs.getString(3).split(",");
            	for(int i = 0;i < edges.length;i++) 
            	{
            		ResultSet rsedge = db1.executeQuery("select * from SuperEdge where ceid = " + edges[i]);
                    if(rsedge.next()) {
                    	//System.out.println(rsedge.getInt(1)+", "+rsedge.getInt(2)+", "+rsedge.getInt(3));
                        SuperEdge e = new SuperEdge(rsedge.getInt(1), rsedge.getInt(2), rsedge.getInt(3));
                        e.weight = rsedge.getFloat(4);
                        //为每个超级边构建其Incid1集合
                        String[] containvs1 = rsedge.getString(5).split(",");
                        HashSet<Integer> CVId1 = new HashSet<Integer>();
                        for(int i1 = 0;i1 < containvs1.length;i1++) {
                            CVId1.add(Integer.parseInt(containvs1[i1]));
                        }
                        
                        //为每个超级边构建其Incid2集合
                        String[] containvs2 = rsedge.getString(6).split(",");
                        HashSet<Integer> CVId21 = new HashSet<Integer>();
                        for(int i1 = 0;i1 < containvs2.length;i1++) {
                            CVId21.add(Integer.parseInt(containvs2[i1]));
                        }
                        e.Incid1 = CVId1;
                        e.Incid2 = CVId21;
                        EdgeSet.add(e);
                    }
                }
            }
            
            HashMap<Integer,Double> Diff = new HashMap<Integer,Double>();   //到其他节点的最长边-最短边
            if(rs.getString(7) == null ||rs.getString(7).equals(""))
            {
            	Diff = null;
            }
            else
            {
            	String[] strs = rs.getString(7).split(",");
            	for(String str: strs)
            	{
            		if(str.equals("")) continue;
            		String[] sss = str.split(":");
            		int id = Integer.parseInt(sss[0]);
            		Double rate = Double.parseDouble(sss[1]);
            		Diff.put(id, rate);
            	}
            }
            
            //为每个超级节点构建其neibor
            HashSet<Integer> Neibor = new HashSet<Integer>();
            if(rs.getString(8) != null && !rs.getString(8).equals(""))
            {
            	String[] containvs2 = rs.getString(8).split(",");
            	for(int i = 0;i < containvs2.length;i++) {
            		Neibor.add(Integer.parseInt(containvs2[i]));
                }
            }
            //赋值
            v.edge = EdgeSet;
            v.containVertex = CVId;
            v.outVertex = CVId2;
            //v.reachId = RVId;
            v.parentId = parentId;
            v.Diff = Diff;
            v.neighborId = Neibor;
            vl.add(v);
        }
        return vl;
	}

	public ArrayList<SuperEdge> readSuperEdge() throws SQLException  //从数据库中读出超级节点之间边的信息
	{
        ResultSet rs = db.executeQuery("select * from SuperEdge");
        ArrayList<SuperEdge> el = new ArrayList<SuperEdge>();
        while(rs.next()) 
        {
            SuperEdge e = new SuperEdge(rs.getInt(1), rs.getInt(2), rs.getInt(3));
            
            //为每个超级边构建其Incid1集合
            String[] containvs = rs.getString(5).split(",");
            HashSet<Integer> CVId = new HashSet<Integer>();
            for(int i = 0;i < containvs.length;i++) {
                CVId.add(Integer.parseInt(containvs[i]));
            }
            
            //为每个超级边构建其Incid2集合
            String[] containvs2 = rs.getString(6).split(",");
            HashSet<Integer> CVId2 = new HashSet<Integer>();
            for(int i = 0;i < containvs2.length;i++) {
                CVId2.add(Integer.parseInt(containvs2[i]));
            }
            
            e.weight = rs.getDouble(4);
            e.Incid1 = CVId;
            e.Incid2 = CVId2;
            el.add(e);
        }
        return el;
	}
	
	public HashSet<Integer> getReachId(String ad) throws SQLException  //给定邮箱地址，返回传递闭包
	{
		dataAccessBase db1 = new dataAccessBase();
		HashSet<Integer> RVId = new HashSet<Integer>();
		String sql = "select SuperVId from vertex where name= \"" + ad + "\"";
		ResultSet rs = db.executeQuery(sql);
		while(rs.next())
		{
			String[] reachvs = rs.getString(1).split(",");
			for(int i = 0;i < reachvs.length;i++) 
			{
	             int superId = Integer.parseInt(reachvs[i]);
	             String sql2 = "select parentId from supervertex where cid = " + superId;
	             ResultSet rs2 = db1.executeQuery(sql2);
	     		 while(rs2.next())
	     		 {
	     			RVId.add(Integer.parseInt(rs2.getString(1)));
	     		 }
	        }
		}
		return RVId;
	}
	
	public HashSet<Integer> getReachId(int ad) throws SQLException  //给定邮箱地址，返回传递闭包
	{
		dataAccessBase db1 = new dataAccessBase();
		HashSet<Integer> RVId = new HashSet<Integer>();
		String sql = "select SuperVId from vertex where vid= \"" + ad + "\"";
		ResultSet rs = db.executeQuery(sql);
		while(rs.next())
		{
			String[] reachvs = rs.getString(1).split(",");
			for(int i = 0;i < reachvs.length;i++) 
			{
	            if(reachvs[i].equals(""))
	            {
	            	System.out.println("");
	            }
	            if(reachvs[i].equals("")) return RVId;
				int superId = Integer.parseInt(reachvs[i]);
	             String sql2 = "select parentId from supervertex where cid = " + superId;
	             ResultSet rs2 = db1.executeQuery(sql2);
	     		 while(rs2.next())
	     		 {
	     			RVId.add(Integer.parseInt(rs2.getString(1)));
	     		 }
	        }
		}
		return RVId;
	}
	
	public HashSet<Integer> getSuperVId(String ad) throws SQLException  //给定邮箱地址，返回SuperId
	{
		HashSet<Integer> RVId = new HashSet<Integer>();
		String sql = "select SuperVId from vertex where name= \"" + ad + "\"";
		ResultSet rs = db.executeQuery(sql);
		while(rs.next())
		{
			String[] reachvs = rs.getString(1).split(",");
			for(int i = 0;i < reachvs.length;i++) 
			{
	             RVId.add(Integer.parseInt(reachvs[i]));
	        }
		}
		return RVId;
	}
	
	public int getVId(String ad) throws SQLException  //给定邮箱地址，返回顶点id
	{
		int RVId = 0;
		String sql = "select vid from vertex where name= \"" + ad + "\"";
		ResultSet rs = db.executeQuery(sql);
		while(rs.next())
		{
			RVId = Integer.parseInt(rs.getString(1));
		}
		return RVId;
	}
 	
	/*public static void main(String[] args) throws Exception 
	{
		dataAccess db = new dataAccess();
		String[] attrs = db.getAllAttribute("glaucus_mail_information"); 
		int attrnum = attrs.length; //鐎涙顔岄惃鍕嚋閺侊拷
		//ResultSet record = db.getRecord();
		ResultSet record = db.getAddRecord("92299");
		while(record.next())
		{
			System.out.println(record.getString(1));
		}
		db.close();
	}*/
}

package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class dataAccessBase {
	
	private Connection conn = null;
	private Statement st = null;
	private ResultSet rs = null;

	public Connection getConn() {
        return conn;
    }
	
	public ResultSet getRs() {
		return rs;
	}

	public void setRs(ResultSet rs) {
		this.rs = rs;
	}

	public dataAccessBase() {
		final String driverName = "com.mysql.jdbc.Driver";
		final String dbURL = "jdbc:mysql://192.168.190.206:3306/glaucus?characterEncoding=utf-8";
		final String userName = "root";
		final String userPassWord = "root";
		try {
			Class.forName(driverName);
			conn = DriverManager.getConnection(dbURL, userName, userPassWord);//Connection
			st = conn.createStatement();
		} catch (Exception e) {
			System.out.println("ִAccessFail!");
			e.printStackTrace();
		}
	}

	public ResultSet executeQuery(String sql) { 
		try {
			rs = st.executeQuery(sql);
		} catch (Exception e) {
			System.out.println("ִexecuteQueryFail!");
			e.printStackTrace();
		}
		return rs;
	}

	public int executeUpdate(String sql) { 
		int ret = 0;
		try {
			ret = st.executeUpdate(sql);
		} catch (SQLException e) {
			System.out.println("ִexecuteUpdateFail");
			e.printStackTrace();
		}
		return ret;
	}

	public void close() { 
		try {
			if (rs != null) {
				rs.close();
			}
			if (st != null) {
				st.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (Exception e) {
			System.out.println("ִcloseFail!");
			e.printStackTrace();
		}
	}

}

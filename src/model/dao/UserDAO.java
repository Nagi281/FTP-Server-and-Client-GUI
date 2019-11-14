package model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import utility.DBConnectionUtil;
import model.bean.User;

public class UserDAO {
	private Connection conn;
	private Statement st;
	private ResultSet rs;
	private PreparedStatement pst;
	protected String table;
	
	public UserDAO() {
        this.table = "users";
        conn = DBConnectionUtil.getConnection();
	}

	public ArrayList<User> getItems() {
		ArrayList<User> listAdmin = new ArrayList<>();
		String sql = "SELECT * FROM user ORDER BY id DESC";
		conn = DBConnectionUtil.getConnection();
		try {
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			while(rs.next()){
				User objUser = new User(rs.getInt("id"), rs.getString("username"), 
						rs.getString("password"), rs.getString("fullname"), rs.getString("path") , rs.getInt("role"));
				listAdmin.add(objUser);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBConnectionUtil.close(rs, st, conn);
		}
		return listAdmin;
	}
	public int add(User objUser) {
		int result = 0;
		conn = DBConnectionUtil.getConnection();
		String sql = "INSERT INTO user(username, password, fullname, position) VALUES(?,?,?,?)";
		try {
			pst = conn.prepareStatement(sql);
			pst.setString(1, objUser.getUsername());
			pst.setString(2, objUser.getPassword());
			pst.setString(3, objUser.getFullname());
			pst.setInt(4, objUser.getRole());
			result = pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBConnectionUtil.close(pst, conn);
		}
		return result;
	}
	public int del(int id) {
		int result = 0;
		conn = DBConnectionUtil.getConnection();
		String sql = "DELETE FROM user WHERE id = ?";
		try {
			pst = conn.prepareStatement(sql);
			pst.setInt(1, id);
			result = pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBConnectionUtil.close(pst, conn);
		}
		return result;
	}
	public User getItem(int id) {
		User objUser = null;
		conn = DBConnectionUtil.getConnection();
		String sql = "SELECT * FROM user WHERE id = ?";
		try {
			pst = conn.prepareStatement(sql);
			pst.setInt(1, id);
			rs = pst.executeQuery();
			if(rs.next()){
				objUser = new User(rs.getInt("id"), rs.getString("username"), 
						rs.getString("password"), rs.getString("fullname"), rs.getString("path") , rs.getInt("role"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBConnectionUtil.close(rs, pst, conn);
		}
		return objUser;
	}
	public int edit(User objUser, int id) {
		int result = 0;
		conn = DBConnectionUtil.getConnection();
		String sql = "UPDATE user SET username = ?, password = ?, fullname = ?, position = ? WHERE id = ?";
		try {
			pst = conn.prepareStatement(sql);
			pst.setString(1, objUser.getUsername());
			pst.setString(2, objUser.getPassword());
			pst.setString(3, objUser.getFullname());
			pst.setInt(4, objUser.getRole());
			pst.setInt(5, id);
			result = pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBConnectionUtil.close(pst, conn);
		}
		return result;
	}
	public int countItems() {
		int count = 0;
		conn = DBConnectionUtil.getConnection();
		String sql = "SELECT COUNT(*) as count FROM user";
		try {
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			if(rs.next()){
				count = rs.getInt("count");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}
	public User getItemByUsernamePassword(String username, String password) {
		User objUser = null;
		conn = DBConnectionUtil.getConnection();
		String sql = "SELECT * FROM user WHERE username = ? AND password = ?";
		try {
			pst = conn.prepareStatement(sql);
			pst.setString(1, username);
			pst.setString(2, password);
			rs = pst.executeQuery();
			if(rs.next()){
				objUser = new User(rs.getInt("id"), rs.getString("username"), 
						rs.getString("password"), rs.getString("fullname"), rs.getString("path") , rs.getInt("role"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBConnectionUtil.close(rs, pst, conn);
		}
		return objUser;
	}
	public ArrayList<User> searchUser(String search) {
		ArrayList<User> listUser = new ArrayList<>();
		String sql = "SELECT * FROM user WHERE fullname LIKE ? ORDER BY id DESC";
		conn = DBConnectionUtil.getConnection();
		try {
			pst = conn.prepareStatement(sql);
			pst.setString(1, "%"+search+"%");
			rs = pst.executeQuery();
			while(rs.next()) {
				User objUser = new User(rs.getInt("id"), rs.getString("username"), 
						rs.getString("password"), rs.getString("fullname"), rs.getString("path") , rs.getInt("role"));;
				listUser.add(objUser);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBConnectionUtil.close(rs, pst, conn);
		}
		return listUser;
	}
	
	public ResultSet excute(String sql) throws SQLException {
        PreparedStatement pstm = conn.prepareStatement(sql);
        return pstm.executeQuery();
    }
    
    public ResultSet get(String fields, String lConditions, String[] rConditions) throws SQLException {
        ResultSet rs = null;
        String sql = "SELECT " + fields + " FROM `" + table + "`";
        if (rConditions.length > 0 && lConditions.equals("") == false) {
        	sql += " WHERE " + lConditions; 
        }
        PreparedStatement pstm = conn.prepareStatement(sql);

        for (int i=1; i<=rConditions.length; i++) {
        	pstm.setString(i, rConditions[i-1]); 
        }
        rs = (ResultSet) pstm.executeQuery();
        
        return rs;
    }
    
    public int insert(String[] fields, String[] values) throws SQLException {
        if (fields.length != values.length) {
        	return -1; 
        }
        String sql = "INSERT INTO " + table + " (";
        String val = "VALUES (";
        for (String s : fields) {
        	sql += s + ","; val += "?,"; 
        }
        sql = sql.substring(0, sql.length() - 1);
        val = val.substring(0, val.length() - 1);
        sql = sql + ") " + val + ")";
        
        PreparedStatement pstm = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        for (int i=1; i<=values.length; i++) {
        	pstm.setString(i, values[i-1]); 
        }
        return pstm.executeUpdate();
    }
    
    public int update(int id, String[] fields, String[] values) throws SQLException {
        if (fields.length != values.length || fields.length == 0) {
        	return -1; 
        }
        String sql = "UPDATE " + table + " SET ";
        for (int i=0; i<fields.length; i++) {
        	sql += fields[i] + "=?,"; 
        }
        sql = sql.substring(0, sql.length() - 1);
        sql += " WHERE id_" + table + "=?";
        
        PreparedStatement pstm = conn.prepareStatement(sql); int i = 1;
        for (i=1; i<=values.length; i++) {
        	pstm.setString(i, values[i-1]); 
        }
        pstm.setString(i, id + "");
        return pstm.executeUpdate();
    }
	
}

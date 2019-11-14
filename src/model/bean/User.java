package model.bean;

public class User {
	private int id;
	private String username;
	private String password;
	private String fullname;
	private String path;
	private int role;
	
	public User(int id, String username, String password, String fullname, String path, int role) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.fullname = fullname;
		this.path = path;
		this.role = role;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getRole() {
		return role;
	}
	public void setRole(int role) {
		this.role = role;
	}
}

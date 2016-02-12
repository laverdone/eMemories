package com.glm.bean;

public class Bookmark {
	private int id;
	private int user;
	private int note;
	private int page;
	private java.util.Date creation_date;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public java.util.Date getCreation_date() {
		return creation_date;
	}
	public void setCreation_date(java.util.Date creation_date) {
		this.creation_date = creation_date;
	}
	public int getUser() {
		return user;
	}
	public void setUser(int user) {
		this.user = user;
	}
	public int getNote() {
		return note;
	}
	public void setNote(int note) {
		this.note = note;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	
}

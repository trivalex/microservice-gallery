package com.github.tvdtb.mediaresource.auth.model;

import java.util.List;

public class Group {
	String name;
	List<String> members;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getMembers() {
		return members;
	}

	public void setMembers(List<String> members) {
		this.members = members;
	}
}

package com.github.tvdtb.mediaresource.config.model;

import java.util.List;

import com.github.tvdtb.mediaresource.auth.model.Group;
import com.github.tvdtb.mediaresource.auth.model.User;

public class SeurityConfig {
	List<Group> groups;
	List<User> users;
	String jwtKey;

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public String getJwtKey() {
		return jwtKey;
	}

	public void setJwtKey(String jwtKey) {
		this.jwtKey = jwtKey;
	}
}

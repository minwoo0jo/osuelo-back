package com.osuelo.osuelo.helper;

import com.osuelo.osuelo.models.User;

//This class is a user object with everything except name and id stripped away
//Any other data can be recalculated on submission
public class SimpleUser {
	
	private long userId;
	

	private String userName;
	
	public SimpleUser(User user) {
		userId = user.getUserId();
		userName = user.getUserName();
	}
	
	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}

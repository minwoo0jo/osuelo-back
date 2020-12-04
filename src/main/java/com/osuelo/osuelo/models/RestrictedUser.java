package com.osuelo.osuelo.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/*
 * Entity representing old userids of restricted users.
 * Necessary because osu API does not make access of restricted user data possible
 * Entry is deleted upon unrestriction of user
 */
@Entity
public class RestrictedUser{
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long restrictedId;
	
	//Stores the user id of the user to make sure a new entry is not created if this user is unrestricted
	private long userId;
	
	//Used on creation to allow the entity to be created and saved without any data
	//Once linked to the correct data, this will be set true and not used
	private boolean saved;
	
	//Constructor
	public RestrictedUser() {
		
	}

	//Getters and setters
	public long getRestrictedId() {
		return restrictedId;
	}

	public void setRestrictedId(long restrictedId) {
		this.restrictedId = restrictedId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	public boolean isSaved() {
		return saved;
	}
	
	public void setSaved(boolean saved) {
		this.saved = saved;
	}
	
}

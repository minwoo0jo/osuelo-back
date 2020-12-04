package com.osuelo.osuelo.models;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/*
 * Entity made to store old usernames of users.
 * Necessary because challonge is the source of old data, which does not store updated usernames.
 * Must be linked to an existing current user to be created.
 * Entry is deleted upon name revert
 */
@Entity
public class OldUser {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long oldUserId;
	
	//Stores the old to new user mapping.
	//Uses the User object to keep track of the user even after another name change.
	@NotNull
	@Size(min = 3, max = 19)
	private String oldUserName;
	@NotNull
	@ManyToOne
	private User currentUser;
	
	//Constructors
	public OldUser() {}
	
	//Getters and Setters
	public long getOldUserId() {
		return oldUserId;
	}

	public void setOldUserId(long oldUserId) {
		this.oldUserId = oldUserId;
	}

	public String getOldUserName() {
		return oldUserName;
	}

	public void setOldUserName(String oldUserName) {
		this.oldUserName = oldUserName;
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}
	
}

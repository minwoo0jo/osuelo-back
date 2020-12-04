package com.osuelo.osuelo.helper;

import java.util.Map;

/*
 * Helper class to represent a challonge object
 * The TournamentService class will convert Challonge objects to Tournament objects
 */
public class Challonge {
	//Name of tournament
	private String challongeName;
	//List of names that are no longer valid due to outdated challonge data
	private Map<String, String> nameChanges;
	private String link;
	private String forum = "";
	private String winner = "";
	private String shortName = "";
	//open means not regional
	private boolean open = false;
	
	//Optional field and can be null. However, if it's null, startDate will be extracted from challonge API
	private String startDate;
	
	//Constructors
	public Challonge() {
		
	}
	public Challonge(String challongeName, String link, String startDate) {
		super();
		this.challongeName = challongeName;
		this.link = link;
		this.startDate = startDate;
	}
	public Challonge(String challongeName, String link, String startDate, String shortName, String forum) {
		super();
		this.challongeName = challongeName;
		this.link = link;
		this.startDate = startDate;
		this.forum = forum;
		this.shortName = shortName;
	}

	//Getters and Setters
	public String getChallongeName() {
		return challongeName;
	}

	public void setChallongeName(String challongeName) {
		this.challongeName = challongeName;
	}

	public Map<String, String> getNameChanges() {
		return nameChanges;
	}
	public void setNameChanges(Map<String, String> nameChanges) {
		this.nameChanges = nameChanges;
	}
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getForum() {
		return forum;
	}
	public void setForum(String forum) {
		this.forum = forum;
	}
	public String getWinner() {
		return winner;
	}
	public void setWinner(String winner) {
		this.winner = winner;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public boolean isOpen() {
		return open;
	}
	public void setOpen(boolean open) {
		this.open = open;
	}
}

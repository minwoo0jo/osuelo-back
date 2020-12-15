package com.osuelo.osuelo.helper;

import java.util.Map;

/*
 * Helper class to represent a challonge object
 * The TournamentService class will convert Challonge objects to Tournament objects
 */
public class Challonge {
	//Name of tournament
	private String tournamentName;
	//List of names that are no longer valid due to outdated challonge data
	private Map<String, String> nameChanges;
	private String challonge;
	private String forum = "";
	private String winner = "";
	private String shortName = "";
	//open means not regional
	private boolean open = false;
	private String region = "";
	private String regionType = "";
	private String rankRestrict = "";
	private String dateAdded = "";
	
	//Optional field and can be null. However, if it's null, startDate will be extracted from challonge API
	private String startDate;
	
	//Constructors
	public Challonge() {
		
	}
	public Challonge(String tournamentName, String challonge, String startDate) {
		super();
		this.tournamentName = tournamentName;
		this.challonge = challonge;
		this.startDate = startDate;
	}
	public Challonge(String tournamentName, String challonge, String startDate, String shortName, String forum) {
		super();
		this.tournamentName = tournamentName;
		this.challonge = challonge;
		this.startDate = startDate;
		this.forum = forum;
		this.shortName = shortName;
	}

	//Getters and Setters
	public String getTournamentName() {
		return tournamentName;
	}

	public void setTournamentName(String tournamentName) {
		this.tournamentName = tournamentName;
	}

	public Map<String, String> getNameChanges() {
		return nameChanges;
	}
	public void setNameChanges(Map<String, String> nameChanges) {
		this.nameChanges = nameChanges;
	}
	public String getChallonge() {
		return challonge;
	}

	public void setChallonge(String challonge) {
		this.challonge = challonge;
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
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getRegionType() {
		return regionType;
	}
	public void setRegionType(String regionType) {
		this.regionType = regionType;
	}
	public String getRankRestrict() {
		return rankRestrict;
	}
	public void setRankRestrict(String rankRestrict) {
		this.rankRestrict = rankRestrict;
	}
	public String getDateAdded() {
		return dateAdded;
	}
	public void setDateAdded(String dateAdded) {
		this.dateAdded = dateAdded;
	}
}

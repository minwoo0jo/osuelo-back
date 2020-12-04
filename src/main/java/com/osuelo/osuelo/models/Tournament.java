package com.osuelo.osuelo.models;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.validation.constraints.NotNull;


/*
 * Entity that represents the actual tournament.
 * General procedure to an update is done by adding a tournament.
 * As all matches need to be associated with a tournament, adding a tournament is the only way to add a match
 */
@Entity
public class Tournament implements Comparable<Tournament>{
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long tournamentId;
	
	//Separated from tournamentId to show that tournamentSequence is chronologically ordered by startDate
	@NotNull
	private long tournamentSequence;
	
	@NotNull
	private String tournamentName;
	
	private String shortName;
	
	private String challonge;
	
	private String forum;
	
	//open indicates whether this tournament is region locked or not
	private boolean open;
	
	@ManyToOne(cascade = CascadeType.PERSIST)
	private User tournamentWinner;
	
	//If null, there is an attempt to find the startDate from the challonge link
	@NotNull
	private Date startDate;
	
	//Every tournament has multiple users, and every user can play multiple tournaments
	@ManyToMany(fetch = FetchType.EAGER, mappedBy = "tournamentsParticipated")
	private List<User> tournamentUsers;
	
	//Every tournament has multiple matches
	@OneToMany(mappedBy = "tournament", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private List<Match> tournamentMatches;
	
	@OrderColumn
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private List<User> ranksNow;
	
	//Constructors
	public Tournament() {
		ranksNow = new ArrayList<User>();
	}

	public Tournament(String tournamentName, Date startDate, List<User> tournamentUsers,
			List<Match> tournamentMatches) {
		super();
		this.tournamentName = tournamentName;
		this.startDate = startDate;
		this.tournamentUsers = tournamentUsers;
		this.tournamentMatches = tournamentMatches;
	}

	//Getters and Setters
	public long getTournamentId() {
		return tournamentId;
	}

	public void setTournamentId(long tournamentId) {
		this.tournamentId = tournamentId;
	}
	
	public long getTournamentSequence() {
		return tournamentSequence;
	}

	public void setTournamentSequence(long tournamentSequence) {
		this.tournamentSequence = tournamentSequence;
	}

	public String getTournamentName() {
		return tournamentName;
	}

	public void setTournamentName(String tournamentName) {
		this.tournamentName = tournamentName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getChallonge() {
		return challonge;
	}

	public void setChallonge(String challonge) {
		this.challonge = challonge;
	}

	public String getForum() {
		return forum;
	}

	public void setForum(String forum) {
		this.forum = forum;
	}
	
	public boolean isOpen() {
		return open;
	}
	
	public void setOpen(boolean open) {
		this.open = open;
	}
	
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public User getTournamentWinner() {
		return tournamentWinner;
	}
	
	public void setTournamentWinner(User tournamentWinner) {
		this.tournamentWinner = tournamentWinner;
	}

	//Getter is made non generic so Spring does not automatically fetch this data
	public List<User> getTournamentUsers(boolean getU) {
		if(!getU)
			return null;
		Collections.sort(tournamentUsers);
		return tournamentUsers;
	}

	public void setTournamentUsers(List<User> tournamentUsers) {
		this.tournamentUsers = tournamentUsers;
	}

	//Getter is made non generic so Spring does not automatically fetch this data
	public List<Match> getTournamentMatches(boolean getM) {
		if(!getM)
			return null;
		Collections.sort(tournamentMatches);
		return tournamentMatches;
	}

	public void setTournamentMatches(List<Match> tournamentMatches) {
		this.tournamentMatches = tournamentMatches;
	}
	
	public List<User> getRanksNow(boolean getR) {
		if(!getR)
			return null;
		return ranksNow;
	}
	
	public void setRanksNow(List<User> ranksNow) {
		this.ranksNow = ranksNow;
	}
	
	//Make sorting easier by using tournament sequence in compareTo
	@Override
	public int compareTo(Tournament t) {
		return ((Long)this.tournamentSequence).compareTo(t.getTournamentSequence());
	}
	
	//Override in order to use contains method on a list of tournaments
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null || obj.getClass() != this.getClass())
			return false;
		return ((Tournament) obj).getTournamentId() == this.tournamentId;
	}
	
	@Override
	public int hashCode() {
		return (int) this.tournamentId;
	}
}

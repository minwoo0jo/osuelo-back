package com.osuelo.osuelo.models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/*
 * Entity representing Tournament Matches.
 * Database representation of this entity is explicitly renamed to Matches to avoid using the reserved word match.
 */
@Entity
@Table(name = "\"Matches\"")
public class Match implements Comparable<Match>{
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long matchId;
	
	//Separated from matchId to show that matchSequence is chronologically ordered (date stored in Tournament)
	@NotNull
	private long matchSequence;
	
	//Elo1 and Elo2 are the elo values of the players at the time that this match took place.
	//These are saved before any calculation for this match is done.
	//Required for backtracking and inserting a match in the middle chronologically
	@NotNull
	private double elo1;
	@NotNull
	private double elo2;
	
	//EloChange saves the amount the elo changed as the result of this match
	@NotNull
	private double eloChange1;
	@NotNull
	private double eloChange2;
	
	//Winner is only stored as a string for convenience.
	//In the case that there is a name change or restriction, player1Id is always the winner's userId
	@NotNull
	private String winner;
	
	//userId is stored alongside userName in case there is a name change or restriction
	//placement status of each player at the time of the match is saved
	@NotNull
	private String player1;
	@NotNull
	private long player1Id;
	@NotNull
	private int player1Placements;
	@NotNull
	private String player2;
	@NotNull
	private long player2Id;
	@NotNull
	private int player2Placements;
	
	@NotNull
	private boolean open;
	
	//Every match is part of a tournament, and every tournament has multiple matches
	@ManyToOne
	private Tournament tournament;
	
	//Every match has 2 users, and every user can play multiple matches
	//Warning: database does not store the order that the List originally stores them in and can be swapped
	@ManyToMany(mappedBy = "matchesParticipated")
	private List<User> matchUsers;
	
	//Constructors
	public Match() {
		
	}

	public Match(double elo1, double elo2, String winner) {
		super();
		this.elo1 = elo1;
		this.elo2 = elo2;
		this.winner = winner;
	}

	
	//Getters and Setters
	public long getMatchId() {
		return matchId;
	}
	
	public long getMatchSequence() {
		return matchSequence;
	}

	public void setMatchSequence(long matchSequence) {
		this.matchSequence = matchSequence;
	}

	public void setMatchId(long matchId) {
		this.matchId = matchId;
	}

	public double getElo1() {
		return elo1;
	}

	public void setElo1(double elo1) {
		this.elo1 = elo1;
	}

	public double getElo2() {
		return elo2;
	}

	public void setElo2(double elo2) {
		this.elo2 = elo2;
	}
	
	public double getEloChange1() {
		return eloChange1;
	}
	
	public void setEloChange1(double eloChange1) {
		this.eloChange1 = eloChange1;
	}
	
	public double getEloChange2() {
		return eloChange2;
	}
	
	public void setEloChange2(double eloChange2) {
		this.eloChange2 = eloChange2;
	}

	public String getWinner() {
		return winner;
	}

	public void setWinner(String winner) {
		this.winner = winner;
	}

	public String getPlayer1() {
		return player1;
	}

	public void setPlayer1(String player1) {
		this.player1 = player1;
	}
	
	public int getPlayer1Placements() {
		return player1Placements;
	}
	
	public void setPlayer1Placements(int player1Placements) {
		this.player1Placements = player1Placements;
	}
	
	public int getPlayer2Placements() {
		return player2Placements;
	}
	
	public void setPlayer2Placements(int player2Placements) {
		this.player2Placements = player2Placements;
	}

	public String getPlayer2() {
		return player2;
	}

	public void setPlayer2(String player2) {
		this.player2 = player2;
	}
	
	public boolean isOpen() {
		return open;
	}
	
	public void setOpen(boolean open) {
		this.open = open;
	}
	
	public Tournament getTournament() {
		return tournament;
	}

	public void setTournament(Tournament tournament) {
		this.tournament = tournament;
	}
	
	//Getter is made non generic so Spring does not automatically fetch this data
	public List<User> getMatchUsers(boolean getU) {
		return getU?matchUsers:null;
	}

	public void setMatchUsers(List<User> matchUsers) {
		this.matchUsers = matchUsers;
	}

	public long getPlayer1Id() {
		return player1Id;
	}

	public void setPlayer1Id(long player1Id) {
		this.player1Id = player1Id;
	}

	public long getPlayer2Id() {
		return player2Id;
	}

	public void setPlayer2Id(long player2Id) {
		this.player2Id = player2Id;
	}
	
	//Make sorting easier by using match sequence in compareTo
	@Override
	public int compareTo(Match m) {
		return ((Long)this.matchSequence).compareTo(m.getMatchSequence());
	}

}

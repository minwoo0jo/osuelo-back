package com.osuelo.osuelo.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/*
 * Entity representing each user.
 * Special Case: all restricted users have the username @RU{id}, with id being the absolute value of the userId.
 * Special Case: all users representing a bye in a tournament match have the name @BYE, and will not be processed during elo changes.
 */
@Entity
public class User implements Comparable<User>{
	
	//Primary key that correlates to the userId of the user on osu.ppy.sh/
	//Restricted users have a userId of -10 and below, added in the order of insertion
	//Byes are represented by a user with userId of -9
	//userId of -1 indicates error
	@Id
	@NotNull
	private long userId;
	
	//Username of the user if the user is valid.
	//Restricted users and byes are not considered valid.
	//Warning: May not always be up to date, and userId should be used in every case where possible
	@NotNull
	@Size(min = 3, max = 19)
	private String userName;
	
	//Country of the player if the user is valid.
	//Warning: May not always be up to date, as users can change their country
	//Restricted users will have an empty string as their country
	//Warning: A bug on the osu API can result in a false positive for restricted users and not all restricted users may have an empty country.
	@NotNull
	private String country;
	
	//Current elo of the player at this time.
	//Elo history can be seen through matchesParticipated.
	@NotNull
	private double elo;
	
	private long countryRank;
	
	//Current rank of the player at this time
	private long rank = 0;
	
	private int numMatches;
	
	private int numPlacements;
	
	private int numWins;
	
	private int numLosses;
	
	private int numTournamentWins;
	
	private double winRate;
	
	private boolean placed;
	
	private long placedMatch;
	
	@OneToMany(mappedBy = "tournamentWinner", fetch = FetchType.LAZY)
	private List<Tournament> tournamentsWon;
	@ManyToMany(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	private List<Tournament> tournamentsParticipated;
	@ManyToMany(fetch = FetchType.LAZY)
	private List<Match> matchesParticipated;
	
	//Constructors
	public User() {
		matchesParticipated = new ArrayList<Match>();
		tournamentsParticipated = new ArrayList<Tournament>();
		tournamentsWon = new ArrayList<Tournament>();
	}
	
	public User(long userId, String userName, String country, double elo) {
		super();
		this.userId = userId;
		this.userName = userName;
		this.country = country;
		this.elo = elo;
	}
	
	//Getters and Setters
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

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public long getCountryRank() {
		return countryRank;
	}

	public void setCountryRank(long countryRank) {
		this.countryRank = countryRank;
	}

	public double getElo() {
		return elo;
	}

	public void setElo(double elo) {
		this.elo = elo;
	}

	public long getRank() {
		return rank;
	}

	public void setRank(long rank) {
		this.rank = rank;
	}

	public int getNumMatches() {
		return numMatches;
	}

	public void setNumMatches(int numMatches) {
		this.numMatches = numMatches;
		setWinRate((numWins * 100.0)/numMatches);
	}
	
	public void incrementNumMatches() {
		numMatches++;
		setWinRate((numWins * 100.0)/numMatches);
	}
	
	public int getNumPlacements() {
		return numPlacements;
	}

	public void setNumPlacements(int numPlacements) {
		this.numPlacements = numPlacements;
	}
	
	public void incrementNumPlacements() {
		numPlacements++;
	}

	public int getNumWins() {
		return numWins;
	}

	public void setNumWins(int numWins) {
		this.numWins = numWins;
	}
	
	public void incrementNumWins() {
		numWins++;
	}
	
	public int getNumLosses() {
		return numLosses;
	}

	public void setNumLosses(int numLosses) {
		this.numLosses = numLosses;
	}
	
	public void incrementNumLosses() {
		numLosses++;
	}
	
	public int getNumTournamentWins() {
		return numTournamentWins;
	}
	
	public void incrementNumTournamentWins() {
		numTournamentWins = tournamentsWon.size();
	}
	
	public void setNumTournamentWins(int numTournamentWins) {
		this.numTournamentWins = numTournamentWins;
	}

	public double getWinRate() {
		return winRate;
	}

	public void setWinRate(double winRate) {
		this.winRate = winRate;
	}
	
	public boolean isPlaced() {
		return placed;
	}
	
	//checks if the user was placed at the time of the match given in the parameters
	//current placement status is not taken into consideration
	//users are placed once they play 10 matches in an open tournament or against placed opponents
	public boolean isPlaced(long ms) {
		Collections.sort(matchesParticipated);
		int i = 0;
		for(Match m : matchesParticipated) {
			if(m.getMatchSequence() >= ms) {
				return i >= 10;
			}
			if(m.getTournament().isOpen())
				i++;
			else {
				List<User> playerList = m.getMatchUsers(true);
				if(playerList.get(0).getUserId() != userId && playerList.get(0).placedMatch <= m.getMatchSequence() && playerList.get(0).placedMatch > 0)
					i++;
				else if(playerList.get(1).getUserId() != userId && playerList.get(1).placedMatch <= m.getMatchSequence() && playerList.get(1).placedMatch > 0)
					i++;
/*				if(m.getPlayer2Id() != userId && m.getPlayer2Placements() > 9)
					i++;
				else if(m.getPlayer1Id() != userId && m.getPlayer1Placements() > 9)
					i++;
*/			}
		}
		return i >= 10;
	}

	public void setPlaced(boolean placed) {
		this.placed = placed;
	}
	
	public long getPlacedMatch() {
		return placedMatch;
	}
	
	public void setPlacedMatch(long placedMatch) {
		this.placedMatch = placedMatch;
	}
	
	//Getter is made non generic so Spring does not automatically fetch this data
	public List<Tournament> getTournamentsWon(boolean getT) {
		if(!getT)
			return null;
		Collections.sort(tournamentsWon);
		return tournamentsWon;
	}
	
	public void addTournamentWon(Tournament tournament) {
		for(Tournament t : tournamentsWon) {
			if(t.getTournamentId() == tournament.getTournamentId())
				return;
		}
		tournamentsWon.add(tournament);
	}
	public void removeTournamentWon(Tournament tournament) {
		tournamentsWon.remove(tournament);
	}
	
	//Getter is made non generic so Spring does not automatically fetch this data
	public List<Tournament> getTournamentsParticipated(boolean getT) {
		if(!getT)
			return null;
		Collections.sort(tournamentsParticipated);
		return tournamentsParticipated;
	}

	public void addTournament(Tournament tournament) {
		tournamentsParticipated.add(tournament);
	}

	//Getter is made non generic so Spring does not automatically fetch this data
	public List<Match> getMatchesParticipated(boolean getM) {
		if(!getM)
			return null;
		Collections.sort(matchesParticipated);
		return matchesParticipated;
	}
	
	//Only returns matches with a lower matchSequence
	public List<Match> getMatchesParticipatedUpTo(long ms) {
		List<Match> matchesUpTo = new ArrayList<Match>();
		for(Match m : matchesParticipated) {
			if(m.getMatchSequence() >= ms)
				continue;
			matchesUpTo.add(m);
		}
		return matchesUpTo;
	}
	
	//Only returns matches with a lower matchSequence that count toward placements
	public List<Match> getOpenMatchesParticipatedUpTo(long ms) {
		List<Match> matchesUpTo = new ArrayList<Match>();
		for(Match m : matchesParticipated) {
			if(m.getMatchSequence() >= ms)
				continue;
			if(m.getTournament().isOpen())
				matchesUpTo.add(m);
			else {
				List<User> mUsers = m.getMatchUsers(true);
				for(User mU : mUsers) {
					if(mU.getUserId() != userId && mU.isPlaced(m.getMatchSequence()))
						matchesUpTo.add(m);
					else if(mU.getUserId() != userId && !mU.isPlaced(m.getMatchSequence()))
						break;
				}
			}
		}
		return matchesUpTo;
	}
	
	//Only add matches to the current set
	public void addMatch(Match match) {
		matchesParticipated.add(match);
	}
	
	public void removeMatch(Match match) {
		matchesParticipated.remove(match);
	}
	
	//Completely resets the user of all match and tournament data
	public void resetUser() {
		elo = 1200;
		numMatches = 0;
		numPlacements = 0;
		numWins = 0;
		numLosses = 0;
		numTournamentWins = 0;
		winRate = 0;
		placed = false;
		placedMatch = 0;
		tournamentsParticipated = new ArrayList<Tournament>();
		matchesParticipated = new ArrayList<Match>();
		tournamentsWon = new ArrayList<Tournament>();
	}
	
	//Override in order to use contains method on a list of users
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null || obj.getClass() != this.getClass())
			return false;
		return ((User) obj).getUserId() == this.userId;
	}
	
	@Override
	public int hashCode() {
		return (int) this.userId;
	}
	
	//Make sorting easier by using elo and rank in compareTo
	@Override
	public int compareTo(User u) {
		int comp = ((Double)this.elo).compareTo(u.getElo()) * -1;
		if(comp == 0)
			comp = ((Long)this.rank).compareTo(u.getRank());
		return comp;
	}
}

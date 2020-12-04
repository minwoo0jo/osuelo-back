package com.osuelo.osuelo.helper;

import java.util.ArrayList;
import java.util.List;

import com.osuelo.osuelo.models.Match;
import com.osuelo.osuelo.models.OldUser;
import com.osuelo.osuelo.models.RestrictedUser;
import com.osuelo.osuelo.models.Tournament;
import com.osuelo.osuelo.models.User;

//Wrapper class to store additional information about the user
public class UserWrapper {
	private User user;
	private List<Tournament> tournaments;
	private List<Match> matches;
	private List<Double> eloHistory;
	private double peakElo;
	private List<String> pastNames;
	private long oldId;
	
	//Constructor
	public UserWrapper(User user, List<Tournament> tournaments, List<Match> matches) {
		super();
		this.user = user;
		this.tournaments = tournaments;
		this.matches = matches;
		eloHistory = new ArrayList<Double>();
		pastNames = new ArrayList<String>();
	}
	//Fill in data about the user from related data objects
	public void initialize(List<OldUser> oldUsers, RestrictedUser rUser) {
		saveEloHistory();
		if(oldUsers.size() > 0)
			savePastNames(oldUsers);
		if(rUser != null)
			saveOldId(rUser);
	}
	//Iterate through matches to create a list of past elos
	private void saveEloHistory() {
		double peak = user.getElo();
		for(Match m : matches) {
			if(m.getPlayer1Id() == user.getUserId()) {
				if(m.getElo1() > peak)
					peak = m.getElo1();
				eloHistory.add(m.getElo1());
			}
			else {
				if(m.getElo2() > peak)
					peak = m.getElo2();
				eloHistory.add(m.getElo2());
			}
		}
		peakElo = peak;
	}
	
	//Iterate through olduser objects to create a list of old usernames
	private void savePastNames(List<OldUser> oldUsers) {
		for(OldUser ou : oldUsers) {
			pastNames.add(ou.getOldUserName());
		}
	}
	
	//If currently restricted, save actual osu user id
	private void saveOldId(RestrictedUser rUser) {
		if(rUser != null) 
			oldId = rUser.getUserId();
	}
	
	//Getters and setters
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public List<Tournament> getTournaments() {
		return tournaments;
	}
	public void setTournaments(List<Tournament> tournaments) {
		this.tournaments = tournaments;
	}
	public List<Match> getMatches() {
		return matches;
	}
	public void setMatches(List<Match> matches) {
		this.matches = matches;
	}

	public List<Double> getEloHistory() {
		return eloHistory;
	}

	public void setEloHistory(List<Double> eloHistory) {
		this.eloHistory = eloHistory;
	}

	public double getPeakElo() {
		return peakElo;
	}

	public void setPeakElo(double peakElo) {
		this.peakElo = peakElo;
	}

	public List<String> getPastNames() {
		return pastNames;
	}

	public void setPastNames(List<String> pastNames) {
		this.pastNames = pastNames;
	}
	
	public long getOldId() {
		return oldId;
	}
	
	public void setOldId(long oldId) {
		this.oldId = oldId;
	}
	
}

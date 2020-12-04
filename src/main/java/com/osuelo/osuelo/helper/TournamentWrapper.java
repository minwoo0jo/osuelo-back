package com.osuelo.osuelo.helper;

import java.util.List;

import com.osuelo.osuelo.models.Match;
import com.osuelo.osuelo.models.Tournament;
import com.osuelo.osuelo.models.User;

//Wrapper class to store tournament data as user and match data is invisible to spring in the tournament class
public class TournamentWrapper {
	
	private Tournament tournament;
	private List<User> users;
	private List<Match> matches;
	
	//Constructor
	public TournamentWrapper(Tournament tournament, List<Match> matches, List<User> users) {
		super();
		this.tournament = tournament;
		this.matches = matches;
		this.users = users;
	}
	
	//Getters and Setters
	public Tournament getTournament() {
		return tournament;
	}
	public void setTournament(Tournament tournament) {
		this.tournament = tournament;
	}
	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
	public List<Match> getMatches() {
		return matches;
	}
	public void setMatches(List<Match> matches) {
		this.matches = matches;
	}
	
}

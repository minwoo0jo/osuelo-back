package com.osuelo.osuelo.helper;

import java.util.ArrayList;
import java.util.List;
import com.osuelo.osuelo.models.Match;
import com.osuelo.osuelo.models.Tournament;
import com.osuelo.osuelo.models.User;

//This class is a tournament class with most data stripped away that would be added in calculation
//Leave behind only what is necessary to rebuild the rest of the tournament object on submission
public class SimpleTournamentWrapper {
	
	private String tournamentName;
	
	private String shortName;
	
	private String challonge;
	
	private String forum;
	
	private boolean open;
	
	private SimpleUser tournamentWinner;
	
	private Long startDate;
	
	private List<SimpleUser> tournamentUsers;
	
	private List<SimpleMatch> tournamentMatches;
	
	public SimpleTournamentWrapper(Tournament tournament) {
		tournamentName = tournament.getTournamentName();
		shortName = tournament.getShortName();
		challonge = tournament.getChallonge();
		forum = tournament.getForum();
		open = tournament.isOpen();
		tournamentWinner = new SimpleUser(tournament.getTournamentWinner());
		startDate = tournament.getStartDate().getTime();
		convertUserList(tournament.getTournamentUsers(true));
		convertMatchList(tournament.getTournamentMatches(true));
	}
	
	private void convertUserList(List<User> userList) {
		tournamentUsers = new ArrayList<SimpleUser>();
		for(User u : userList) {
			tournamentUsers.add(new SimpleUser(u));
		}
	}
	private void convertMatchList(List<Match> matchList) {
		tournamentMatches = new ArrayList<SimpleMatch>();
		for(Match m : matchList) {
			tournamentMatches.add(new SimpleMatch(m));
		}
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

	public SimpleUser getTournamentWinner() {
		return tournamentWinner;
	}

	public void setTournamentWinner(SimpleUser tournamentWinner) {
		this.tournamentWinner = tournamentWinner;
	}

	public Long getStartDate() {
		return startDate;
	}

	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}

	public List<SimpleUser> getTournamentUsers() {
		return tournamentUsers;
	}

	public void setTournamentUsers(List<SimpleUser> tournamentUsers) {
		this.tournamentUsers = tournamentUsers;
	}

	public List<SimpleMatch> getTournamentMatches() {
		return tournamentMatches;
	}

	public void setTournamentMatches(List<SimpleMatch> tournamentMatches) {
		this.tournamentMatches = tournamentMatches;
	}
}

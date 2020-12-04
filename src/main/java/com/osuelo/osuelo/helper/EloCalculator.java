package com.osuelo.osuelo.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.osuelo.osuelo.models.Match;
import com.osuelo.osuelo.models.User;

/*
 * Helper class for calculating elo changes for each match.
 * Does not actually save any data and only returns the results to the caller
 */
public class EloCalculator {
	
	//Given a match, calculate the change in elo for both the winner and the loser
	public static List<User> calculateEloChange(Match match) {
		System.out.println(match.getPlayer1() + ": " + Math.round(match.getElo1()) + 
				", " + match.getPlayer2() + ": " + Math.round(match.getElo2()));
		double elo1 = match.getElo1();
		double elo2 = match.getElo2();
		User user1 = match.getMatchUsers(true).get(0);
		User user2 = match.getMatchUsers(true).get(1);
		//userId of -9 indicates a bye and the winner should not gain elo
		if((user1.getUserId() != -9 && user2.getUserId() != -9) && (user1.getUserId() != user2.getUserId())) {
			//This only reflects the number of matches played up to this point.
			//Necessary for the case of out of order tournament submissions
			int numMatchesPlayed1 = user1.getOpenMatchesParticipatedUpTo(match.getMatchSequence()).size();
			int numMatchesPlayed2 = user2.getOpenMatchesParticipatedUpTo(match.getMatchSequence()).size();
			//Maximum elo change is 90 until the 10th match, 60 until 30th match, then 30
			//Only open tournament matches or matches against placed players count toward this number
			//This is done so new players can quickly find their true elo rating
			double k1 = 90;
			if(numMatchesPlayed1 >= 10)
				k1 = 60;
			if(numMatchesPlayed1 >= 30)
				k1 = 30;
			double k2 = 90;
			if(numMatchesPlayed2 >= 10)
				k2 = 60;
			if(numMatchesPlayed2 >= 30)
				k2 = 30;
			//Maximum elo change vs unplaced players is divided by a factor
			int unplacedPenalty = match.getTournament().isOpen() ? 3 : 6;
			if(!user1.isPlaced(match.getMatchSequence()))
				k2 /= unplacedPenalty;
			if(!user2.isPlaced(match.getMatchSequence()))
				k1 /= unplacedPenalty;
			//If this is the 10th valid placement match, set this player to placed
			if(numMatchesPlayed1 == 9 && (user2.isPlaced(match.getMatchSequence()) || match.getTournament().isOpen())) {
				user1.setPlaced(true);
				user1.setPlacedMatch(match.getMatchSequence());
			}
			if(numMatchesPlayed2 == 9 && (user1.isPlaced(match.getMatchSequence()) || match.getTournament().isOpen())) {
				user2.setPlaced(true);
				user2.setPlacedMatch(match.getMatchSequence());
			}
			double expectedWin1 = 1 / (1 + Math.pow(10, (elo2 - elo1) / 400));
			double expectedWin2 = 1 / (1 + Math.pow(10, (elo1 - elo2) / 400));
			double newElo1 = elo1 + k1 * (1 - expectedWin1);
			double newElo2 = elo2 + k2 * (0 - expectedWin2);
			user1.setElo(newElo1);
			user2.setElo(newElo2);
		}
		//Do not save here, simply send off the User objects with the new elos set to the caller function
		List<User> usersAfterMatch = new ArrayList<User>();
		usersAfterMatch.add(user1);
		usersAfterMatch.add(user2);
		return usersAfterMatch;
	}
	//Given a list of matches and users, recalculate the elos of matches and players that are affected
	//Used in the case of out of order tournament submission
	public static List<List<? extends Object>> recalculateElos(List<Match> matches, List<User> users) {
		//Resets all affected users to their elos at the point at which the tournament is inserted
		users = rewindPlayerElos(matches, users);
		//Resets all matches to an elo of 0 to show that this needs to be reset with the proper elo ratings
		matches = rewindMatchElos(matches);
		List<Match> recalculatedMatches = new ArrayList<Match>();
		List<User> recalculatedUsers = new ArrayList<User>();
		//nested for loop should run faster than expected since matches and users are both pre-sorted
		for(int i = 0; i < matches.size(); i ++) {
			List<User> matchUsers = matches.get(i).getMatchUsers(true);
			if(matches.get(i).getPlayer1Id() != matchUsers.get(0).getUserId()) {
				User temp = matchUsers.get(0);
				matchUsers.set(0, matchUsers.get(1));
				matchUsers.set(1, temp);
			}
			//Fill this list with null values to make sure the list can be set later on
			List<User> newUserList = new ArrayList<User>();
			User p1 = null;
			User p2 = null;
			int index1 = 0;
			int index2 = 0;
			for(int j = 0; j < users.size(); j++) {
				//If the correct user for the match is found, set the match elo to the correct value
				if(users.get(j).equals(matchUsers.get(0))) {
					matches.get(i).setElo1(users.get(j).getElo());
					p1 = users.get(j);
					index1 = j;
				}
				if(users.get(j).equals(matchUsers.get(1))) {
					matches.get(i).setElo2(users.get(j).getElo());
					p2 = users.get(j);
					index2 = j;
				}
				//Do not go past this if statement if the match has not been correctly initialized yet
				if(matches.get(i).getElo1() == 0 || matches.get(i).getElo2() == 0 ||
						p1 == null || p2 == null) {
					continue;
				}
				//Once the match is initialized properly, calculate the elos and move to the next match
				newUserList.add(p1);
				newUserList.add(p2);
				matches.get(i).setMatchUsers(newUserList);
				newUserList = calculateEloChange(matches.get(i));
				recalculatedUsers.addAll(newUserList);
				//Fill in placement status and elo change for the match after calculation
				Match m = matches.get(i);
				User u1 = newUserList.get(0);
				User u2 = newUserList.get(1);
				m.setPlayer1Placements(u1.getOpenMatchesParticipatedUpTo(m.getMatchSequence()).size());
				m.setPlayer2Placements(u2.getOpenMatchesParticipatedUpTo(m.getMatchSequence()).size());
				m.setEloChange1(u1.getElo() - m.getElo1());
				m.setEloChange2(u2.getElo() - m.getElo2());
				users.set(index1, u1);
				users.set(index2, u2);
				recalculatedMatches.add(matches.get(i));
				break;
			}
		}
		//if the match elo is still 0 for some reason, throw an error.
		//Otherwise, return the users and matches together to be saved in a service class
		for(Match m : recalculatedMatches)
			if(m.getElo1() == 0 || m.getElo2() == 0)
				throw new ArithmeticException("Match ID: " + m.getMatchId() + " still zero elo after recalculation");
		return Arrays.asList(recalculatedUsers, recalculatedMatches);
	}
	
	//Rewind all the player elos to the values they were at the point of match insertion
	private static List<User> rewindPlayerElos(List<Match> matches, List<User> users) {
		List<User> resetUserElo = new ArrayList<User>();
		for(User u : users) {
			//Break out after 2 matches because there are only 2 users for each match
			boolean found = false;
			for(Match m : matches) {
				if(m.getElo1() == 0) {
					continue;
				}
				if(m.getPlayer1Id() == u.getUserId()) {
					u.setElo(m.getElo1());
					resetUserElo.add(u);
					found = true;
					break;
				}
				if(m.getPlayer2Id() == u.getUserId()) {
					u.setElo(m.getElo2());
					resetUserElo.add(u);
					found = true;
					break;
				}
			}
			if(!found)
				resetUserElo.add(u);
		}
		return resetUserElo;
	}
	
	//Resets all the match elo ratings to 0 temporarily to be processed later on.
	//Warning: matchUsers is not always in the correct order as the database does not store order
	private static List<Match> rewindMatchElos(List<Match> matches) {
		for(int i = 0; i < matches.size(); i++) {
			matches.get(i).setElo1(0);
			matches.get(i).setElo2(0);
		}
		//If the list of users is out of order, switch them
		for(Match m : matches) {
			List<User> users = m.getMatchUsers(true);
			if(users.get(0).getUserId() != m.getPlayer1Id()) {
				User temp = users.get(0);
				users.set(0, users.get(1));
				users.set(1, temp);
			}
		}
		return matches;
	}
}

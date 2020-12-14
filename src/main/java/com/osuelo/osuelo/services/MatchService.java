package com.osuelo.osuelo.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.osuelo.osuelo.models.Match;
import com.osuelo.osuelo.models.Tournament;
import com.osuelo.osuelo.models.User;
import com.osuelo.osuelo.repositories.MatchRepository;

/*
 * Service class for the match model
 */
@Service
public class MatchService {
	
	@Autowired
	private MatchRepository matchRepository;
	
	@Autowired
	private UserService userService;
	
	//Not for use by controller classes
	public List<Match> listAllMatches() {
		return (List<Match>) matchRepository.findAll(Sort.by("matchSequence").ascending());
	}
	
	//Return all matches from a given tournament
	public List<Match> listTournamentMatches(Tournament tournament) {
		return matchRepository.findByTournament(tournament);
	}
	
	public Match listMatchById(long id) {
		return matchRepository.findById(id).get();
	}
	
	//Save a list of matches all at once, usually for adding tournaments
	public void populateMatches(List<Match> matchList) {
		matchList.forEach(matchRepository::save);
	}
	
	//Save one match
	public void addMatch(Match match) {
		matchRepository.save(match);
	}
	
	//Delete all matches that were part of a tournament
	public void deleteMatchesByTournament(Tournament tournament) {
		matchRepository.deleteAll(listTournamentMatches(tournament));
	}
	
	//When a user changes their name or gets restricted/unrestricted, the match data must be changed
	public boolean changeNamesForUserMatches(User user) {
		try {
			List<Match> userMatches = user.getMatchesParticipated(true);
			long userId = user.getUserId();
			String newName = user.getUserName();
			for(Match m : userMatches) {
				if(m.getPlayer1Id() == userId) {
					m.setPlayer1(newName);
					m.setWinner(newName);
				}
				else {
					m.setPlayer2(newName);
				}
			}
			userMatches.forEach(matchRepository::save);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/*
	public boolean updateAllMatches() {
		try {
			Iterable<Match> allMatches = matchRepository.findAll();
			for(Match m : allMatches) {
				String player1 = userService.getUserById(m.getPlayer1Id()).getUserName();
				String player2 = userService.getUserById(m.getPlayer2Id()).getUserName();
				boolean diff = false;
				if(!m.getPlayer1().equals(player1)) {
					m.setPlayer1(player1);
					m.setWinner(player1);
					diff = true;
				}
				if(!m.getPlayer2().equals(player2)) {
					m.setPlayer2(player2);
					diff = true;
				}
				if(diff)
					matchRepository.save(m);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}*/
	
	//Find matches where the user did not get their elo adjusted by their last match
	public List<Match> findBad(User user) {
		return matchRepository.findBadMatches(user.getElo(), user.getUserId());
	}
	
	//Find matches where the user did not get their elo adjusted by looking through their match history
	public List<Match> findBad2(User user) {
		List<Match> userMatches = user.getMatchesParticipated(true);
		List<Match> badMatches = new ArrayList<Match>();
		for(int i = 0; i < userMatches.size() - 1; i++) {
			//These matches should not be included in a bad match list
			if(userMatches.get(i).getPlayer1().equals("@BYE")||userMatches.get(i).getPlayer2().equals("@BYE"))
				continue;
			double elo1 = 0;
			double elo2 = 0;
			if(userMatches.get(i).getPlayer1Id() == user.getUserId())
				elo1 = userMatches.get(i).getElo1();
			else
				elo1 = userMatches.get(i).getElo2();
			if(userMatches.get(i + 1).getPlayer1Id() == user.getUserId())
				elo2 = userMatches.get(i + 1).getElo1();
			else
				elo2 = userMatches.get(i + 1).getElo2();
			if(elo1 == elo2)
				badMatches.add(userMatches.get(i));
		}
		return badMatches;
	}
	
	/*public boolean editMatch(Match newM) {
		Match oldM = matchRepository.findById(newM.getMatchId()).get();
		if(oldM.getPlayer1Id() != newM.getPlayer1Id() || oldM.getPlayer2Id() != newM.getPlayer2Id()) {
			//players are different
			return false;
		}
		return true;
	}*/
	
	/*public boolean fillMatchR() {
		List<Match> allMatches = listAllMatches();
		List<Match> allMatchesDone = new ArrayList<Match>();
		List<User> allUsersDone = new ArrayList<User>();
		for(Match m : allMatches) {
			if(m.getMatchSequence() % 100 == 0)
				System.out.println(m.getMatchSequence());
			User u1 = null;
			User u2 = null;
			if(m.getMatchUsers(true).size() == 1) {
				List<User> lU = new ArrayList<User>();
				u1 = userService.getUserById(m.getPlayer1Id());
				u2 = userService.getUserById(m.getPlayer2Id());
				lU.add(u1);
				lU.add(u2);
				m.setMatchUsers(lU);
			}
			else {
				u1 = m.getMatchUsers(true).get(0);
				u2 = m.getMatchUsers(true).get(1);
			}
			if(u1.getUserId() != m.getPlayer1Id()) {
				User temp = u1;
				u1 = u2;
				u2 = temp;
			}
			List<Match> u1Matches = u1.getMatchesParticipated(true);
			List<Match> u2Matches = u2.getMatchesParticipated(true);
			Collections.sort(u1Matches);
			Collections.sort(u2Matches);
			Match lM1 = null;
			Match lM2 = null;
			for(Match m1 : u1Matches) {
				if(m1.getMatchSequence() > m.getMatchSequence()) {
					lM1 = m1;
					break;
				}
			}
			for(Match m2 : u2Matches) {
				if(m2.getMatchSequence() > m.getMatchSequence()) {
					lM2 = m2;
					break;
				}
			}
			double newElo1 = lM1 == null ? u1.getElo() : (lM1.getPlayer1Id() == u1.getUserId() ? lM1.getElo1() : lM1.getElo2());
			double newElo2 = lM2 == null ? u2.getElo() : (lM2.getPlayer1Id() == u2.getUserId() ? lM2.getElo1() : lM2.getElo2());
			m.setEloChange1(newElo1 - m.getElo1());
			m.setEloChange2(newElo2 - m.getElo2());
			m.setOpen(m.getTournament().isOpen());
			m.setPlayer1Placements(u1.getOpenMatchesParticipatedUpTo(m.getMatchSequence()).size());
			m.setPlayer2Placements(u2.getOpenMatchesParticipatedUpTo(m.getMatchSequence()).size());
			if(lM1 == null) {
				u1.setNumPlacements(m.getPlayer1Placements() + (m.isOpen() ? 1 : 0));
				if(!allUsersDone.contains(u1))
					allUsersDone.add(u1);
			}
			if(lM2 == null) {
				u2.setNumPlacements(m.getPlayer2Placements() + (m.isOpen() ? 1 : 0));
				if(!allUsersDone.contains(u2))
					allUsersDone.add(u2);
			}
			allMatchesDone.add(m);
		}
		System.out.println("saving");
		matchRepository.saveAll(allMatchesDone);
		allUsersDone.forEach(userService::addUser);
		return true;
	}*/
}

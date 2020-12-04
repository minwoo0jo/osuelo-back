package com.osuelo.osuelo.services;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osuelo.osuelo.config.Keys;
import com.osuelo.osuelo.helper.Challonge;
import com.osuelo.osuelo.helper.DataScraper;
import com.osuelo.osuelo.helper.EloCalculator;
import com.osuelo.osuelo.helper.RequestWrapper;
import com.osuelo.osuelo.helper.SimpleTournamentWrapper;
import com.osuelo.osuelo.helper.TournamentWrapper;
import com.osuelo.osuelo.models.Match;
import com.osuelo.osuelo.models.RestrictedUser;
import com.osuelo.osuelo.models.Tournament;
import com.osuelo.osuelo.models.User;
import com.osuelo.osuelo.repositories.TournamentRepository;

/*
 * Service class for Tournament.
 * As adding a tournament adds multiple matches and potentially multiple users, this class has access to most service classes
 */
@Service
public class TournamentService {
	
	@Autowired
	private TournamentRepository tournamentRepository;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private MatchService matchService;
	
	@Autowired
	private OldUserService oldUserService;
	
	@Autowired
	private RestrictedUserService restrictedUserService;
	
	//Lists all tournaments in the database. Only used internally and is a private method.
	//Users will not have access to all tournaments at once, only pages of 50 at a time
	//Sorted by startDate as tournamentSequence may not have been set properly yet
	//If startDate is the same, sort by tournamentId
	public List<Tournament> listAllTournaments() {
		List<Tournament> partialSort = (List<Tournament>) tournamentRepository.findAll(Sort.by("startDate").descending());
		for(int i = 0; i < partialSort.size() - 1; i++) {
			Tournament t = partialSort.get(i);
			Tournament t2 = partialSort.get(i + 1);
			if(t.getStartDate().equals(t2.getStartDate()) && t.getTournamentId() < t2.getTournamentId()) {
				partialSort.set(i, t2);
				partialSort.set(i + 1, t);
			}
		}
		return partialSort;
	}
	
	public long numTournaments() {
		return tournamentRepository.count();
	}
	
	//Find the last tournament which changed the rankings of placed users
	//Skip the tournament that resulted in the current rankings as the goal is to find the previous version
	public Tournament findLatestTournamentRankingChange() {
		long numT = numTournaments() - 1;
		Tournament lastT = null;
		int pass = 0;
		while(numT >= 0) {
			lastT = tournamentRepository.findByTournamentSequence(numT).get(0);
			if(lastT.getRanksNow(true) != null && lastT.getRanksNow(true).size() > 0) {
				if(pass == 0)
					pass = 1;
				else
					return lastT;
			}
			numT -= 1;
		}
		return lastT;
	}

	/*
	public Tournament findLatestTournamentRankingChange(long numT) {
		Tournament lastT = null;
		while(numT >= 0) {
			lastT = tournamentRepository.findByTournamentSequence(numT).get(0);
			if(lastT.getRanksNow(true) != null && lastT.getRanksNow(true).size() > 0) {
				return lastT;
			}
			numT -= 1;
		}
		return lastT;
	}*/
		
	/*public Tournament findRankingsByDate(Date date) {
		List<Tournament> sortByDate = (List<Tournament>) tournamentRepository.findAll(Sort.by("startDate").descending());
		for(Tournament t : sortByDate) {
			if(t.getStartDate().before(date))
				return findLatestTournamentRankingChange(t.getTournamentSequence());
		}
		return null;
	}*/
	
	//Lists pages of 50 tournaments sorted by startDate
	public List<Object> list50Tournaments(int page) {
		if(page < 0)
			return null;
		Pageable pageable = PageRequest.of(page, 50, Sort.by("startDate").descending());
		List<Tournament> pageOfTournaments = tournamentRepository.findAll(pageable).getContent();
		List<Object> listTournaments = new ArrayList<Object>();
		listTournaments.add(new Long(numTournaments()));
		listTournaments.add(pageOfTournaments);
		return listTournaments;
	}
	
	//Checks for tournaments by the given name
	//Returns null if no match
	public Tournament getTournamentByName(String name) {
		List<Tournament> results = tournamentRepository.findByTournamentName(name);
		if(results.size() == 1)
			return results.get(0);
		return null;
	}
	
	//If the tournament with the given id exists, return it
	public Tournament getTournamentById(long id) {
		try {
			return tournamentRepository.findById(id).get();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	//If the tournament with the given sequence exists, return it
	public Tournament getTournamentBySequence(long ts) {
		try {
			return tournamentRepository.findByTournamentSequence(ts).get(0);
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	//Save the data for the challonge and tournament into a file for later manual review and submission
	private void saveChallonge(List<Tournament> toSave, List<Challonge> challonges) {
		for(Tournament t : toSave) {
			System.out.println("saving " + t.getTournamentName());
			try {
				TournamentWrapper tw = new TournamentWrapper(t, t.getTournamentMatches(true), t.getTournamentUsers(true));
				FileWriter fstream = new FileWriter("tournaments/" + Long.toString(System.currentTimeMillis(), 32) + "." + t.getTournamentName() + ".json");
				Challonge chall = null;
				for(Challonge c : challonges) {
					if(c.getNameChanges() == null || c.getNameChanges().isEmpty())
						continue;
					if(c.getChallongeName().equals(t.getTournamentName())) {
						chall = c;
						break;
					}
				}
				if(chall != null) {
					FileWriter fstream2 = new FileWriter("tournaments/" + Long.toString(System.currentTimeMillis(), 32) + "." + t.getTournamentName() + ".names.json");
					BufferedWriter out2 = new BufferedWriter(fstream2);
					ObjectMapper mapper2 = new ObjectMapper();
					String json2 = mapper2.writeValueAsString(chall.getNameChanges());
					out2.write(json2);
					out2.close();
				}
		        BufferedWriter out = new BufferedWriter(fstream);
		        ObjectMapper mapper = new ObjectMapper();
		        String json = mapper.writeValueAsString(tw);
		        out.write(json);
		        out.close();
			} catch(Exception e) {
				System.err.println("Error: " + e.getMessage());
			}
		}
	}
	
	//Take the RequestWrapper object, print out the submitter name, and pass the challonges on
	public List<List<String>> processSubmitData(RequestWrapper requestObj, boolean override, boolean overrideUnlikely) {
		System.out.println("Submitted by: " + requestObj.getSubmitter());
		return convertChallongesToTournaments(requestObj.getChallonges(), override, overrideUnlikely);
	}
	
	//Given a list of Challonge objects, convert them into Tournament objects to be saved
	//Returns a list of successful conversions and a list of failed conversions
	//Will not actually save unless the correct flag is triggered
	public List<List<String>> convertChallongesToTournaments(List<Challonge> challonges, boolean override, boolean overrideUnlikely) {
		List<Tournament> convertedChallonges = new ArrayList<Tournament>();
		List<String> success = new ArrayList<String>();
		success.add("SUCCCESS:");
		List<String> failure = new ArrayList<String>();
		failure.add("FAILURE:");
		List<String> invalid = new ArrayList<String>();
		invalid.add("INVALID USERNAMES:");
		List<String> unlikely = new ArrayList<String>();
		unlikely.add("UNLIKELY USERNAMES:");
		for(Challonge c : challonges) {
			Tournament tournament = convertChallongeToTournament(c, override || overrideUnlikely);
			if(tournament == null) {
				failure.add(c.getChallongeName());
				continue;
			}
			if(tournament.getTournamentName().startsWith("invalid: ")) {
				failure.add(c.getChallongeName());
				String[] names = tournament.getChallonge().substring(1, tournament.getChallonge().length() - 1).split(",");
				int invalidSize = Integer.parseInt(tournament.getTournamentName().substring(9).split(",")[0].trim());
				for(int i = 0; i < names.length; i++) {
					if(i < invalidSize)
						invalid.add(names[i].trim());
					else
						unlikely.add(names[i].trim());
				}
				continue;
			}
			success.add(c.getChallongeName());
			convertedChallonges.add(tournament);
		}
		//If the return of addTournament is not an empty list, the save has failed
		for(Tournament t : convertedChallonges) {
			List<User> invalidUsers = addTournament(t, false);
			if(invalidUsers.size() > 0) {
				success.remove(t.getTournamentName());
				failure.add(t.getTournamentName());
				for(User u : invalidUsers)
					invalid.add(u.getUserName());
			}
			else if(override)
				addTournament(t, true);
		}
		
		if(!override && invalid.size() == 1 && (unlikely.size() == 1 || overrideUnlikely)) {
			saveChallonge(convertedChallonges, challonges);
		}
		List<List<String>> combineResult = Arrays.asList(success, failure, invalid, unlikely);
		return combineResult;
	}
	
	//Use the Challonge API to gain the needed user and match data to convert a challonge object into a tournament object
	//Two endpoints are needed to get all the data from a challonge link
	//Returns the converted Tournament if successful and null if not
	public Tournament convertChallongeToTournament(Challonge challonge, boolean override) {
		List<String> namesNotFound = new ArrayList<String>();
		List<String> unlikelyNames = new ArrayList<String>();
		String requestString = "https://api.challonge.com/v1/tournaments/";
		//Challonge links are not case sensitive, so all links are sent in lower case for convenience
		String tournamentName = challonge.getLink().toLowerCase();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("api_key", Keys.API_KEY_CHALLONGE);
		try {
			//Valid challonge links can be in many forms
			if(tournamentName.startsWith("https://challonge.com/"))
				tournamentName = tournamentName.substring(22);
			else if(tournamentName.startsWith("http://challonge.com/"))
				tournamentName = tournamentName.substring(21);
			else if(tournamentName.startsWith("challonge.com/"))
				tournamentName = tournamentName.substring(14);
			//Some challonge brackets are under a subdomain, and must be properly parsed to perform a correct API call
			else if(tournamentName.startsWith("https://")) {
				String subdomain = tournamentName.substring(8, tournamentName.indexOf(".challonge.com/"));
				tournamentName = subdomain + "-" + tournamentName.substring(23 + subdomain.length());
			}
			else if(tournamentName.startsWith("http://")) {
				String subdomain = tournamentName.substring(7, tournamentName.indexOf(".challonge.com/"));
				tournamentName = subdomain + "-" + tournamentName.substring(23 + subdomain.length());
			}
			else {
				String subdomain = tournamentName.substring(0, tournamentName.indexOf(".challonge.com/"));
				tournamentName = subdomain + "-" + tournamentName.substring(15 + subdomain.length());
			}
			if(tournamentName.length() > 3 && tournamentName.charAt(2) == '/')
				tournamentName = tournamentName.substring(3);
		} catch (IndexOutOfBoundsException e) {
			System.out.println("Invalid link: " + challonge.getLink());
			return null;
		}
		if(tournamentName.charAt(tournamentName.length() -1) == '/')
			tournamentName = tournamentName.substring(0, tournamentName.length() - 1);
		System.out.println(tournamentName);
		String endpoint = requestString + tournamentName + "/participants.json";
		JsonNode fullResponse = DataScraper.performGetRequest(endpoint, parameters);
		if(fullResponse == null || fullResponse.get(0) == null ||
				fullResponse.get(0).asText().equals("errors")) {
			System.out.println("Tournament " + challonge.getLink() +
					" is invalid. Please check that the link is correct.");
			return null;
		}
		
		//Challonge uses their own system of IDs for users, and these can vary across group stage and bracket stage
		//These lists are used to keep track of the user before a full User object can be formed
		List<JsonNode> listOfParticipants = new ArrayList<JsonNode>();
		List<String> listOfParticipantNames = new ArrayList<String>();
		List<Integer> listOfParticipantIds = new ArrayList<Integer>();
		List<Integer> listOfParticipantGIds = new ArrayList<Integer>();
		fullResponse.forEach(listOfParticipants::add);
		//Create the new tournament object here and save simple metadata into it to start
		Tournament tournament = new Tournament();
		tournament.setChallonge(challonge.getLink());
		tournament.setForum(challonge.getForum());
		tournament.setShortName(challonge.getShortName());
	    tournament.setTournamentName(challonge.getChallongeName());
	    tournament.setOpen(challonge.isOpen());
		String startDate = challonge.getStartDate();
		//If the startDate not given explicitly, the startDate will be determined from the challonge API
		//As tournament brackets can be created before the tournament, the creation date of the first participant is used instead
		//This guarantees that the registrations of the tournament has at least been finalized, leading to a more accurate startDate
		if(startDate != null && startDate.length() >= 10 &&
				startDate.charAt(4) == '-' && startDate.charAt(7) == '-') {
			//If the given date is invalid, then we move on to the challonge API to get the startDate
			try {
				Integer.parseInt(startDate.substring(0, 4));
				Integer.parseInt(startDate.substring(5, 7));
				Integer.parseInt(startDate.substring(8, 10));
				startDate = startDate.substring(0, 10);
			} catch (NumberFormatException e) {
				startDate = listOfParticipants.get(0).get("participant").get("created_at").asText();
			}
		}
		else
			startDate = listOfParticipants.get(0).get("participant").get("created_at").asText();
	    startDate = startDate.substring(0, 10);
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    //If even the challonge API fails to give the startDate, then the current time will be used to set the tournament startDate
	    try {
	    	if(challonge.getStartDate() != null)
	    		tournament.setStartDate(new Date(formatter.parse(challonge.getStartDate()).getTime()));
	    	else
	    		tournament.setStartDate(new Date(formatter.parse(startDate).getTime()));
		} catch (ParseException e1) {
			e1.printStackTrace();
			System.out.println("Tournament " + challonge.getLink() +
					"does not have a valid creation date. Setting start date to current time by default");
			tournament.setStartDate(new Date(System.currentTimeMillis()));
		}
	    List<User> users = new ArrayList<User>();
	    //First check if the user exists, then if the old username to user mapping exists, then create a new user
	    for(JsonNode j : listOfParticipants) {
	    	User user = new User();
	    	String userName = j.get("participant").get("name").asText();
	    	if(challonge.getNameChanges() != null && challonge.getNameChanges().containsKey(userName)) {
	    		userName = challonge.getNameChanges().get(userName);
	    	}
	    	User tempUser = userService.getUserByName(userName);
	    	if(userName.startsWith("@R")) {
	    		try {
	    			long rId = Long.parseLong(userName.substring(2));
	    			RestrictedUser rUser = restrictedUserService.getRestrictedUserByUserId(rId);
	    			tempUser = userService.getUserById(rId);
	    			if(rUser != null) {
	    				tempUser = userService.getUserById((rUser.getRestrictedId() + 10) * -1);
	    			}
	    			else if(tempUser != null) {
	    				userName = tempUser.getUserName();
	    			}
	    			else if(userService.getNameUsingId(rId).equals("")){
	    				tempUser = userService.getUserById(-8);
	    				tempUser.setRank(rId);
	    			}
	    			else {
	    				tempUser = null;
	    			}
	    		} catch(NumberFormatException e) {
	    		}
	    	}
	    	if(tempUser == null) {
	    		tempUser = oldUserService.getUserByOldUserName(userName);
	    		if(tempUser == null) {
	    			long idAttempt = userService.getIdUsingName(userName, override);
			    	if(idAttempt == -1) {
			    		User oldUser = oldUserService.getUserByOldUserName(userName);
			    		if(oldUser != null)
			    			userName = oldUser.getUserName();
			    		else {
			    			//At this point, there is no existing record of this username anywhere, including the osu API, and this username is considered invalid
			    			namesNotFound.add(userName);
			    			continue;
			    		}
			    	}
			    	else if(idAttempt == -2) {
			    		User oldUser = oldUserService.getUserByOldUserName(userName);
			    		if(oldUser != null)
			    			userName = oldUser.getUserName();
			    		else {
			    			unlikelyNames.add(userName);
			    			continue;
			    		}
			    	}
			    	user.setUserId(idAttempt);
	    		}
	    		else {
	    			user = tempUser;
	    			userName = user.getUserName();
	    		}
	    	}
	    	else {
	    		user = tempUser;
	    		userName = user.getUserName();
	    	}
	    	//Check if this player was in the records as a restricted player and unrestrict if necessary
	    	long uId = user.getUserId();
	    	if(uId != 0) {
	    		RestrictedUser rUser = restrictedUserService.getRestrictedUserByUserId(uId);
	    		if(rUser != null) {
	    			User unupdatedUser = userService.getUserById((rUser.getRestrictedId() + 10) * -1);
	    			User updatedUser = new User();
	    			updatedUser = userService.copyUserData(unupdatedUser, updatedUser);
	    			updatedUser.setUserId(uId);
	    			updatedUser.setUserName(user.getUserName());
	    			userService.addUser(updatedUser);
	    			changeUserList(unupdatedUser, updatedUser);
	    			matchService.changeNamesForUserMatches(unupdatedUser, updatedUser);
	    			oldUserService.updateRestriction(unupdatedUser, updatedUser);
	    			userService.deleteUser(unupdatedUser);
	    			restrictedUserService.deleteRU(rUser);
	    			user = updatedUser;
	    		}
	    	}
	    	if(userName.equals(challonge.getWinner()))
	    		tournament.setTournamentWinner(user);
	    	users.add(user);
	    	listOfParticipantNames.add(userName);
	    	listOfParticipantIds.add(j.get("participant").get("id").asInt());
	    	if(j.get("participant").get("group_player_ids").size() > 0) {
	    		listOfParticipantGIds.add(j.get("participant").get("group_player_ids").get(0).asInt());
	    	}
	    }
	    //If the name used in the submission data is no longer in use, find the correct user
	    if(tournament.getTournamentWinner() == null) {
	    	User tournamentWinnerOld = oldUserService.getUserByOldUserName(challonge.getWinner());
	    	if(tournamentWinnerOld != null)
	    		tournament.setTournamentWinner(tournamentWinnerOld);
	    }
	    //If any names are invalid, the tournament cannot be processed and the challonge to tournament conversion has failed
	    if(namesNotFound.size() > 0 || unlikelyNames.size() > 0) {
	    	namesNotFound.forEach(System.out::println);
	    	System.out.println();
	    	unlikelyNames.forEach(System.out::println);
	    	System.out.println("Failed to add tournament: " + challonge.getChallongeName());
	    	Tournament invalid = new Tournament();
	    	invalid.setTournamentName("invalid: " + namesNotFound.size() + ", " + unlikelyNames.size());
	    	namesNotFound.addAll(unlikelyNames);
	    	invalid.setChallonge(namesNotFound.toString());
	    	return invalid;
	    }
	    tournament.setTournamentUsers(users);
	    //This is the second half of the method used to fill in the match data
	    endpoint = requestString + tournamentName + "/matches.json";
	    fullResponse = DataScraper.performGetRequest(endpoint, parameters);
	    if(fullResponse == null || fullResponse.get(0) == null ||
				fullResponse.get(0).asText().equals("errors")) {
			System.out.println("Tournament " + challonge.getLink() +
					" is invalid. Please check that all of the matches have completed.");
			return null;
		}
	    List<JsonNode> listOfMatches = new ArrayList<JsonNode>();
	    for(JsonNode j : fullResponse) {
	    	if(j.get("match").get("state").asText().equals("complete"))
	    		listOfMatches.add(j);
	    }
	    List<Match> matches = new ArrayList<Match>();
	    //Use the previously filled in user to userid mappings to fill in the match data
	    //If the match does not have a valid list of players or winner, the match is considered unfinished and will skip
	    for(JsonNode j : listOfMatches) {
	    	Match match = new Match();
	    	int id1 = j.get("match").get("player1_id").asInt();
	    	int id2 = j.get("match").get("player2_id").asInt();
	    	int index1 = listOfParticipantIds.indexOf(id1);
	    	int index2 = listOfParticipantIds.indexOf(id2);
	    	if(index1 == -1 || index2 == -1) {
	    		index1 = listOfParticipantGIds.indexOf(id1);
	    		index2 = listOfParticipantGIds.indexOf(id2);
	    	}
	    	if(index1 == -1 || index2 == -1)
	    		continue;
	    	match.setPlayer1(listOfParticipantNames.get(index1));
	    	match.setPlayer2(listOfParticipantNames.get(index2));
	    	if(match.getPlayer1().equals("@PLACEHOLDER")) {
	    		match.setPlayer1Id(users.get(index1).getUserId());
	    	}
	    	if(match.getPlayer2().equals("@PLACEHOLDER")) {
	    		match.setPlayer2Id(users.get(index2).getUserId());
	    	}
	    	if(j.get("match").get("winner_id").asInt() == (id1))
	    		match.setWinner(listOfParticipantNames.get(index1));
	    	else
	    		match.setWinner(listOfParticipantNames.get(index2));
	    	matches.add(match);
	    }
	    tournament.setTournamentMatches(matches);
	    return tournament;
	}
	
	//Checks each tournament for invalid names, and then saves the tournaments
	//Maps invalid users to the tournament that attempted to add them, and then returns the map
	public Map<User, Tournament> addTournamentList(List<Tournament> tournamentList) {
		Map<User, Tournament> failedUsers = new HashMap<User, Tournament>();
		List<User> nameCheck;
		for(Tournament t : tournamentList) {
			nameCheck = addTournament(t, false);
			if(nameCheck.size() > 0) {
				for(User u : nameCheck)
					failedUsers.put(u, t);
				continue;
			}
			nameCheck = replaceWithExistingUsers(t);
			for(User u : nameCheck)
				failedUsers.put(u, t);
		}
		return failedUsers;
	}
	
	//Adds the tournament to the database, along with the list of given users and matches
	//Although users can be added separately, matches can only be added through tournaments, as matches cannot exist without a tournament
	//If any of the users are invalid, the save will fail and the return will be a list of invalid users
	//If there are no issues, the return is an empty list
	public List<User> addTournament(Tournament tournament, boolean save) {
		System.out.println(tournament.getTournamentName());
		//First make sure all the users are valid before proceeding
		List<User> failures = userService.populateUsers(tournament.getTournamentUsers(true), save);
		if(failures.size() > 0 || !save)
			return failures;
		if(tournamentRepository.findByTournamentName(tournament.getTournamentName()).size() > 0) {
			System.out.println("Already exists");
			return failures;
		}
		//Save the tournament itself so it exists in the database as subsequent functions require the tournament
		tournamentRepository.save(tournament);
		//Sort the tournaments by startDate after it's saved
		setCorrectSequence();
		//Make sure the tournament exists to the user so an extra lookup is not needed
		addTournamentToUsers(tournament.getTournamentUsers(true), tournament);
		//Fill in all the extra details to the match itself and set the match sequence
		fillMatch(tournament.getTournamentMatches(true), tournament);
		//If the tournament is entered out of sequence chronologically, then current elos for users and matches must be recalculated
		if(tournament.getTournamentSequence() != tournamentRepository.count() - 1) {
			recalculateEloFrom(tournament.getTournamentMatches(true).get(0).getMatchSequence());
		}
		else {
			//Save the rankings after every tournament in case there was a change
			saveRankings(tournament);
		}
		System.out.println("Done adding");
		return failures;
	}
	
	//Make sure all users contain the reference to this tournament
	//Add the tournament to the list of wins if the tournament winner is found
	public void addTournamentToUsers(List<User> users, Tournament tournament) {
		for(User user : users) {
			User savedUser = userService.getUserById(user.getUserId());
			savedUser.addTournament(tournament);
			if(tournament.getTournamentWinner() != null &&
					tournament.getTournamentWinner().getUserName().equals(user.getUserName())) {
				savedUser.addTournamentWon(tournament);
				savedUser.incrementNumTournamentWins();
			}
			userService.addUser(savedUser);
		}
	}
	
	//Fills in the missing fields for the match object
	//Only the player names and winner must be given and the rest will be automatically entered
	public void fillMatch(List<Match> matches, Tournament tournament) {
		List<Double> initElos = new ArrayList<Double>();
		for(User u : tournament.getTournamentUsers(true))
			initElos.add(u.getElo());
		long tournamentSequence = tournament.getTournamentSequence();
		long matchSequence;
		boolean insertMiddle = tournamentRepository.count() - 1 != tournamentSequence;
		//If the tournament itself was inserted out of order, then the matches must have also been added out of order
		if(tournamentSequence > 0) {
			Tournament lastTournament = tournamentRepository.findByTournamentSequence(
					tournamentSequence - 1).get(0);
			List<Match> lastMatches = lastTournament.getTournamentMatches(true);
			matchSequence = lastMatches.get(lastMatches.size() - 1).getMatchSequence() + 1;
		}
		else
			matchSequence = 0;
		for(int i = 0; i < matches.size(); i++) {
			Match match = matches.get(i);
			try {
				match.setMatchSequence(matchSequence++);
				match.setTournament(tournament);
				match.setOpen(tournament.isOpen());
				List<User> users;
				User user1, user2;
				if(match.getPlayer1Id() != 0) 
					user1 = userService.getUserById(match.getPlayer1Id());
				else
					user1 = userService.getUserByName(match.getPlayer1());
				if(match.getPlayer2Id() != 0)
					user2 = userService.getUserById((match.getPlayer2Id()));
				else
					user2 = userService.getUserByName(match.getPlayer2());
				//Player 1 is always the winner and player 2 is always the loser
				//After saving the match and discarding the object, the list of users will not maintain the same order
				//In that case, the userId is stored alongside each player name to make it possible to find the users later on
				if(match.getWinner().toLowerCase().equals(user1.getUserName().toLowerCase())) {
					user1.incrementNumWins();
					user2.incrementNumLosses();
					users = new ArrayList<User>(Arrays.asList(user1, user2));
					match.setPlayer1Id(user1.getUserId());
					match.setPlayer2Id(user2.getUserId());
				}
				else {
					user1.incrementNumLosses();
					user2.incrementNumWins();
					users = new ArrayList<User>(Arrays.asList(user2, user1));
					match.setPlayer2(match.getPlayer1());
					match.setPlayer1(match.getWinner());
					match.setPlayer1Id(user2.getUserId());
					match.setPlayer2Id(user1.getUserId());
				}
				user1.incrementNumMatches();
				user2.incrementNumMatches();
				match.setMatchUsers(users);
				//If the match is not inserted in chronological order, keep the elos to 0 for now and recalculate after all the matches for this tournament have been added
				if(!insertMiddle) {
					match.setElo1(users.get(0).getElo());
					match.setElo2(users.get(1).getElo());
					match.setPlayer1Placements(users.get(0).getNumPlacements());
					match.setPlayer2Placements(users.get(1).getNumPlacements());
					match = userService.calculateEloChange(match);
				}
				if(tournament.isOpen() || user1.isPlaced(match.getMatchSequence()))
					user2.incrementNumPlacements();
				if(tournament.isOpen() || user2.isPlaced(match.getMatchSequence()))
					user1.incrementNumPlacements();
				users.get(0).addMatch(match);
				users.get(1).addMatch(match);
				userService.addUser(users.get(0));
				userService.addUser(users.get(1));
			} catch (NullPointerException e) {
				e.printStackTrace();
				deleteTournament(tournament, initElos);
			}
			matchService.addMatch(match);
		}
		setMatchSequence(tournamentSequence, matchSequence);
	}
	
	//non-inclusive deletion
	//Delete all tournaments with a greater tournament sequence
	//Currently bugged: does not delete match data and must be manually removed from db
	public void resetToTournament(long tournamentSequence) {
		List<Tournament> allT = listAllTournaments();
		for(Tournament t : allT) {
			if(t.getTournamentSequence() == tournamentSequence) {
				userService.updateAllRanks();
				break;
			}
			deleteTournament(t);
		}
		userService.updateAllRanks();
	}
	
	//Delete a specific tournament
	//Currently bugged: Does not properly delete matches
	private void deleteTournament(Tournament t) {
		//remove win from winner
		User tWinner = t.getTournamentWinner();
		tWinner.removeTournamentWon(t);
		tWinner.setNumTournamentWins(tWinner.getNumTournamentWins() - 1);
		userService.addUser(tWinner);
		//remove matches one by one
		t.setTournamentWinner(null);
		List<Match> tMatches = t.getTournamentMatches(true);
		Collections.reverse(tMatches);
		for(Match m : tMatches) {
			List<User> mUsers = m.getMatchUsers(true);
			User p1 = mUsers.get(0);
			User p2 = mUsers.get(1);
			if(p1.getUserId() == m.getPlayer2Id()) {
				User temp = p1;
				p1 = p2;
				p2 = temp;
			}
			if(p1.getNumMatches() == 1)
				p1.resetUser();
			else {
				p1.setElo(m.getElo1());
				p1.setNumMatches(p1.getNumMatches() - 1);
				p1.setNumWins(p1.getNumWins() - 1);
				if(p2.isPlaced() || t.isOpen()) {
					p1.setNumPlacements(p1.getNumPlacements() - 1);
				}
				if(p1.getPlacedMatch() == m.getMatchSequence()) {
					p1.setPlaced(false);
					p1.setPlacedMatch(0);
				}
				p1.removeMatch(m);
			}
			if(p2.getNumMatches() == 1)
				p2.resetUser();
			else {
				p2.setElo(m.getElo2());
				p2.setNumMatches(p2.getNumMatches() - 1);
				p2.setNumLosses(p2.getNumLosses() - 1);				
				if(p1.isPlaced() || t.isOpen()) {
					p2.setNumPlacements(p2.getNumPlacements() - 1);
				}
				if(p2.getPlacedMatch() == m.getMatchSequence()) {
					p2.setPlaced(false);
					p2.setPlacedMatch(0);
				}
				p2.removeMatch(m);
			}
			m.setMatchUsers(null);
			userService.addUser(p1);
			userService.addUser(p2);
			matchService.addMatch(m);
		}
			//revert elos using recorded past elo on the match
			//update user stats
				//elo
				//nummatches, wins, losses, winrate
				//tournament wins (if user won tournament)
				//remove tournament from tournamentswon
				//remove match from user
			//remove match from user
			//remove match from tournament
		matchService.deleteMatchesByTournament(t);
		tournamentRepository.delete(t);
		//remove tournament
	}
	
	//If tournament addition fails midway, the tournament is deleted and elos are reverted to initial values
	private void deleteTournament(Tournament tournament, List<Double> initElos) {
		List<User> userList = tournament.getTournamentUsers(true);
		for(int i = 0; i < userList.size(); i++) {
			User u = userList.get(i);
			u.setElo(initElos.get(i));
			userService.addUser(u);
		}
		matchService.deleteMatchesByTournament(tournament);
		tournamentRepository.delete(tournament);
	}
	
	//Set the correct tournamentSequence for each tournament
	//tournamentSequence does not reflect order of insertion, like tournamentId
	private void setCorrectSequence() {
		List<Tournament> allTournamentList = listAllTournaments();
		if(allTournamentList.size() < 2)
			return;
		for(int i = 0; i < allTournamentList.size(); i++) {
			Tournament tournament = allTournamentList.get(i);
			long sequence = tournament.getTournamentSequence();
			tournament.setTournamentSequence(allTournamentList.size() - 1 - i);
			tournamentRepository.save(tournament);
			if(sequence == 0)
				return;
		}
	}
	
	//Set the correct matchSequence for each tournament
	//matchSequence does not reflect order of insertion, like matchId
	private void setMatchSequence(long ts, long ms) {
		List<Tournament> allTournamentList = listAllTournaments();
		for(long i = ts + 1; i < allTournamentList.size(); i++) {
			Tournament tournament = allTournamentList.get(allTournamentList.size() - 1 - (int)i);
			for(Match m : tournament.getTournamentMatches(true)) {
				m.setMatchSequence(ms++);
				matchService.addMatch(m);
			}
		}
	}
	
	//Given a matchSequence, recalculate all the elos from this point
	@SuppressWarnings("unchecked")
	private void recalculateEloFrom(long ms) {
		List<Match> allMatches = matchService.listAllMatches();
		List<Match> matchesAfterChange = allMatches.subList((int) ms, allMatches.size());
		//Not all matches are always affected, as there can be isolated matches without any involved players
		List<List<? extends Object>> affectedUsersAndMatches = findAffectedMatches(matchesAfterChange);
		if(affectedUsersAndMatches.size() == 0)
			return;
		List<User> affectedUsers = (List<User>) affectedUsersAndMatches.get(0);
		List<Match> affectedMatches = (List<Match>) affectedUsersAndMatches.get(1);
		//Attempts to recalculate elos through the helper class EloCalculator
		//Since the helper class does not save the data, the list of users and matches is returned and all the changes are saved at once
		List<List<? extends Object>> recalculatedUsersAndMatches =
				EloCalculator.recalculateElos(affectedMatches, affectedUsers);
		affectedUsers = (List<User>) recalculatedUsersAndMatches.get(0);
		affectedMatches = (List<Match>) recalculatedUsersAndMatches.get(1);
		affectedUsers.forEach(userService::addUser);
		affectedMatches.forEach(matchService::addMatch);
	}
	
	//Given a list of all matches after a certain point, return a list of users and matches that are affected by the out of order matches that have been inserted
	//Isolated matches without any involvement with affected users are not included
	private List<List<? extends Object>> findAffectedMatches(List<Match> matchesAfterChange) {
		List<User> affectedUsers = new ArrayList<User>();
		List<Long> affectedUserIds = new ArrayList<Long>();
		List<Match> affectedMatches = new ArrayList<Match>();
		int startIndex = 0;
		//Find out where the previously saved matches start as they have valid elo ratings
		for(int i = 0; i < matchesAfterChange.size(); i++) {
			Match m = matchesAfterChange.get(i);
			if(m.getElo1() != 0 && m.getElo2() != 0) {
				startIndex = i;
				break;
			}
			affectedUsers.add(m.getMatchUsers(true).get(0));
			affectedUsers.add(m.getMatchUsers(true).get(1));
			affectedUserIds.add(m.getPlayer1Id());
			affectedUserIds.add(m.getPlayer2Id());
			affectedMatches.add(m);
		}
		//Add user and match only if it includes a user that has been previously affected by this insertion
		for(int i = startIndex; i < matchesAfterChange.size(); i++) {
			long id1 = matchesAfterChange.get(i).getPlayer1Id();
			long id2 = matchesAfterChange.get(i).getPlayer2Id();
			boolean affected1 = affectedUserIds.contains(id1);
			boolean affected2 = affectedUserIds.contains(id2);
			if(!affected1 && !affected2)
				continue;
			affectedMatches.add(matchesAfterChange.get(i));
			List<User> findUser = matchesAfterChange.get(i).getMatchUsers(true);
			if(!affected1) {
				affectedUserIds.add(id1);
				affectedUsers.add(findUser.get(0).getUserId() == id1?
						findUser.get(0):findUser.get(1));
			}
			else if(!affected2) {
				affectedUserIds.add(id2);
				affectedUsers.add(findUser.get(0).getUserId() == id2?
						findUser.get(0):findUser.get(1));
			}
		}
		return Arrays.asList(affectedUsers, affectedMatches);
	}
	
	//When a user gets restricted/unrestricted, the id changes, so the old id must be manually switched with the new one
	public void changeUserList(User oldU, User newU) {
		List<Tournament> userTournaments = oldU.getTournamentsParticipated(true);
		for(Tournament t : userTournaments) {
			List<User> tournamentUsers = t.getTournamentUsers(true);
			tournamentUsers.remove(oldU);
			tournamentUsers.add(newU);
			newU.addTournament(t);
			t.setTournamentUsers(tournamentUsers);
			//To prevent issues with java object to db row mapping, retrieve the user from the db first
			if(t.getTournamentWinner() == oldU) {
				newU.addTournamentWon(t);
				userService.addUser(newU);
				User retrieve = userService.getUserById(newU.getUserId());
				t.setTournamentWinner(retrieve);
			}
			userService.addUser(newU);
			tournamentRepository.save(t);
		}
	}
	
	//Delete all tournaments
	public void deleteAll() {
		Iterator<Tournament> allTournaments = tournamentRepository.findAll().iterator();
		while(allTournaments.hasNext()) {
			Tournament t = allTournaments.next();
			matchService.deleteMatchesByTournament(t);
			tournamentRepository.delete(t);
		}
	}
	
	//Delete all tournaments in a given list
/*	public void deleteAll(List<Tournament> listT) {
		for(Tournament t : listT) {
			matchService.deleteMatchesByTournament(t);
			tournamentRepository.delete(t);
		}
	}*/
	
	//Entry point for search queries; this is where the other search methods are called if required
	public List<Object> searchAll(String query, String type) {
		//Split multi word searches into multiple single word searches
		String[] queries = query.split(" ");
		Arrays.sort(queries, Comparator.comparingInt(String::length).reversed());
		List<Object> fullResults = new ArrayList<Object>();
		List<Object> resultList = new ArrayList<Object>();
		List<Tournament> tResults = new ArrayList<Tournament>();
		List<User> uResults = new ArrayList<User>();
		if(type.equals("a") || type.equals("t")) {
			tResults = tournamentRepository.searchTournamentByName(queries[0]);
			for(int i = 0; i < queries.length; i++) {
				if(i == 0)
					continue;
				String q = queries[i];
				List<Tournament> tR = tournamentRepository.searchTournamentByName(q);
				tResults.retainAll(tR);
			}
			LinkedHashSet<Tournament> tHashSet = new LinkedHashSet<>(tResults);
			tResults = new ArrayList<>(tHashSet);
			resultList.addAll(tResults);
		}
		if(type.equals("a") || type.equals("u")) {
			uResults = userService.search(queries[0]);
			uResults.addAll(oldUserService.search(queries[0]));
			for(int i = 0; i < queries.length; i++) {
				if(i == 0)
					continue;
				String q = queries[i];
				List<User> uR = userService.search(q);
				uR.addAll(oldUserService.search(q));
				uResults.retainAll(uR);	
			}
			LinkedHashSet<User> uHashSet = new LinkedHashSet<>(uResults);
			uResults = new ArrayList<>(uHashSet);
			resultList.addAll(uResults);
		}
		Integer resultLen = new Integer(resultList.size());
		fullResults.add(new Integer(resultLen));
		fullResults.add(resultList);
		return fullResults;
	}
	
	public void update(Tournament t) {
		tournamentRepository.save(t);
	}
	
	//Take the submitted tournament, and replace users that exist already with the db version
	//Names are temporary, so the userId will be used
	public List<User> replaceWithExistingUsers(Tournament t) {
		Tournament submit = new Tournament();
		submit.setTournamentName(t.getTournamentName());
		submit.setChallonge(t.getChallonge());
		submit.setForum(t.getForum());
		submit.setOpen(t.isOpen());
		submit.setShortName(t.getShortName());
		submit.setStartDate(new Date(t.getStartDate().getTime()));
		submit.setTournamentMatches(t.getTournamentMatches(true));
		List<User> userListOfT = t.getTournamentUsers(true);
		List<User> newList = new ArrayList<User>();
		//Find the user by ID, then use that instead
		for(User u : userListOfT) {
			User existing = userService.getUserById(u.getUserId());
			if(existing == null) {
				RestrictedUser rUser = restrictedUserService.getRestrictedUserByUserId(u.getUserId());
				if(rUser != null)
					existing = userService.getUserById((rUser.getRestrictedId() + 10) * -1);
			}
			if(existing == null)
				newList.add(u);
			else
				newList.add(existing);
			if(u.getUserId() == t.getTournamentWinner().getUserId()) {
				if(existing == null)
					submit.setTournamentWinner(u);
				else
					submit.setTournamentWinner(existing);
			}
		}
		submit.setTournamentUsers(newList);
		return addTournament(submit, true);
	}
	
	//If the rankings change after adding this tournament, save the rankings list into the tournament
	public void saveRankings(Tournament t) {
		List<User> getPrevRankings = userService.getRankingsByRank();
		if(userService.updateAllRanks()) {
			List<User> getRankings = userService.getRankingsByElo();
			if(getPrevRankings.size() == getRankings.size()) {
				for(int i = 0; i < getRankings.size(); i++) {
					if(!getRankings.get(i).equals(getPrevRankings.get(i))) {
						t.setRanksNow(getRankings);
						tournamentRepository.save(t);
						return;
					}
				}
			}
			else {
				t.setRanksNow(getRankings);
				tournamentRepository.save(t);
			}
		}
	}
	
	/*public boolean fixStuff() {
		String endpoint = "https://osu.ppy.sh/oauth/authorize";
		Map<String, String> params = new HashMap<String, String>();
		params.put("client_id", "****");
		params.put("redirect_url", "https://osuelo.com");
		params.put("response_type", "****");
		params.put("scope", "****");
		DataScraper.performGetRequest(endpoint, params);
		return true;
	}*/
	
	//Exports all tournaments in a json format
	//Simplified as much as possible to reduce size
	public String exportAllTournaments() {
		List<Tournament> allTournaments = listAllTournaments();
		Collections.reverse(allTournaments);
		ObjectMapper mapper = new ObjectMapper();
		//List<String> tournamentJson = new ArrayList<String>();
		String jsonString = "[";
		for(Tournament t : allTournaments) {
			try {
				jsonString += mapper.writeValueAsString(new SimpleTournamentWrapper(t)) + ",";
				//tournamentJson.add(jsonString);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		jsonString = jsonString.substring(0, jsonString.length() - 1) + "]";
		return jsonString;
	}
	
	//Export a specific tournament
	//Simplified as much as possible to reduce size
	public String exportTournament(long tournamentId) {
		Tournament t = getTournamentById(tournamentId);
		if(t == null)
			return "Sorry, that tournament does not exist!";
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = "";
		try {
			jsonString = mapper.writeValueAsString(new SimpleTournamentWrapper(t));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonString;
	}
}

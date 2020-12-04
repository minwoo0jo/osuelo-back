package com.osuelo.osuelo.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.osuelo.osuelo.config.Keys;
import com.osuelo.osuelo.helper.DataScraper;
import com.osuelo.osuelo.helper.EloCalculator;
import com.osuelo.osuelo.models.Country;
import com.osuelo.osuelo.models.Match;
import com.osuelo.osuelo.models.RestrictedUser;
import com.osuelo.osuelo.models.Tournament;
import com.osuelo.osuelo.models.User;
import com.osuelo.osuelo.repositories.UserRepository;

/*
 * Service class for the User entity
 */
@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private OldUserService oldUserService;
	
	@Autowired
	private MatchService matchService;
	
	@Autowired
	private RestrictedUserService restrictedUserService;
	
	@Autowired
	private TournamentService tournamentService;
	
	@Autowired
	private CountryService countryService;
	
	//Number of users who have completed their placements
	public long numUsers() {
		return userRepository.countByPlaced(true);
	}
	
	//Number of users who have completed their placements
	public long numUsersAll() {
		return userRepository.count();
	}
	
	//Number of users from a country, placed or not
	public long numUsers(String country) {
		return userRepository.countByCountry(country);
	}
	
	//Get a list of placed users, sort, then return a page of 50
	//sort condition can be specified
	//First entry in the list is the number of users in total
	public List<Object> list50Users(String sort, int page) {
		if(page < 0)
			return null;
		Pageable pageable = PageRequest.of(page, 50);
		List<User> pageOfUsers;
		switch(sort) {
		case "rank":
			pageOfUsers = userRepository.findByPlacedOrderByEloDescRankAsc(true, pageable);
			break;
		case "matches":
			pageOfUsers = userRepository.findByPlacedOrderByNumMatchesDescRankAsc(true, pageable);
			break;
		case "win":
			pageOfUsers = userRepository.findByPlacedOrderByWinRateDescRankAsc(true, pageable);
			break;
		case "tournamentWin":
			pageOfUsers = userRepository.findByPlacedOrderByNumTournamentWinsDescRankAsc(true, pageable);
			break;
		default:
			pageOfUsers = userRepository.findByPlacedOrderByEloDescRankAsc(true, pageable);
			break;
		}
		List<Object> listUsers = new ArrayList<Object>();
		listUsers.add(new Long(numUsers()));
		listUsers.add(pageOfUsers);
		Tournament t = tournamentService.findLatestTournamentRankingChange();
		listUsers.add(t.getRanksNow(true));
		return listUsers;
	}
	
	//Same as above, but for a specific country
	//Gives all users and not just placed users
	public List<Object> list50Users(String sort, int page, String country) {
		Pageable pageable = PageRequest.of(page, 50);
		List<User> pageOfUsers = userRepository.findByCountryOrderByEloDescRankAsc(country, pageable);
		switch(sort) {
		case "rank":
			pageOfUsers = userRepository.findByCountryOrderByEloDescRankAsc(country, pageable);
			break;
		case "matches":
			pageOfUsers = userRepository.findByCountryOrderByNumMatchesDescEloDescRankAsc(country, pageable);
			break;
		case "win":
			pageOfUsers = userRepository.findByCountryOrderByWinRateDescEloDescRankAsc(country, pageable);
			break;
		case "tournamentWin":
			pageOfUsers = userRepository.findByCountryOrderByNumTournamentWinsDescEloDescRankAsc(country, pageable);
			break;
		default:
			pageOfUsers = userRepository.findByCountryOrderByEloDescRankAsc(country, pageable);
			break;
		}
		List<Object> listUsers = new ArrayList<Object>();
		listUsers.add(new Long(numUsers(country)));
		listUsers.add(pageOfUsers);
		return listUsers;
	}
	
	//Get a list of users, sort, then return a page of 50
	//sort condition can be specified
	//First entry in the list is the number of users in total
	public List<Object> list50UsersAll(String sort, int page) {
		if(page < 0)
			return null;
		Pageable pageable = PageRequest.of(page, 50);
		List<User> pageOfUsers;
		switch(sort) {
		case "rank":
			pageOfUsers = userRepository.findAllByOrderByEloDescRankAsc(pageable);
			break;
		case "matches":
			pageOfUsers = userRepository.findAllByOrderByNumMatchesDescEloDescRankAsc(pageable);
			break;
		case "win":
			pageOfUsers = userRepository.findAllByOrderByWinRateDescEloDescRankAsc(pageable);
			break;
		case "tournamentWin":
			pageOfUsers = userRepository.findAllByOrderByNumTournamentWinsDescEloDescRankAsc(pageable);
			break;
		default:
			pageOfUsers = userRepository.findAllByOrderByEloDescRankAsc(pageable);
			break;
		}
		List<Object> listUsers = new ArrayList<Object>();
		listUsers.add(new Long(numUsersAll()));
		listUsers.add(pageOfUsers);
		return listUsers;
	}
	
	/*public List<Object> list50UsersHistory(int page, Date date) {
		if(page < 0)
			return null;
		List<Object> listUsers = new ArrayList<Object>();
		Tournament t;
		if(date == null)
			t = tournamentService.findLatestTournamentRankingChange(tournamentService.numTournaments() - 1);
		else
			t = tournamentService.findRankingsByDate(date);
		List<User> fullUsersRanking = t.getRanksNow(true);
		List<User> pageOfUsers = new ArrayList<User>();
		listUsers.add(new Long(fullUsersRanking.size()));
		for(int i = page * 50; i < fullUsersRanking.size(); i++) {
			if(i - page * 50 == 50)
				break;
			pageOfUsers.add(fullUsersRanking.get(i));
		}
		listUsers.add(pageOfUsers);
		List<Match> matches = t.getTournamentMatches(true);
		listUsers.add(matches.get(matches.size() - 1).getMatchSequence());
		listUsers.add(t.getStartDate().toString());
		return listUsers;
	}*/
		
	//Search the database for the given user. If the user does not exist, return null
	public User getUserByName(String name) {
		List<User> results = userRepository.findByUserName(name);
		if(results.size() == 1)
			return results.get(0);
		return null;
	}
	
	//Given a list of users, this method will process each user and attempt to save
	//If either userId or userName is missing, then a attempt will be made to fill in the missing data using the osu API
	//If the user already exists, will act as an update, only saving the country and username from the given data
	//If the user does not exist already, will set elo to the default (1200) and save
	//Returns a list of failed entries
	public List<User> populateUsers(List<User> userList, boolean save) {
		List<User> failure = new ArrayList<User>();
		for(User user : userList) {
			if(user.getUserId() == 0)
				System.out.println(user.getUserName());
			//If only the userId is given, use the osu API to fill in the userName
			if(user.getUserName() == null && user.getUserId() != 0) {
				String nameAttempt = getNameUsingId(user.getUserId());
				if(nameAttempt.equals("")) {
					System.out.println("There is no user with the userId: " + user.getUserId());
					failure.add(user);
					continue;
				}
				user.setUserName(nameAttempt);
			}
			//If only the userName if given, use the osu API to fill in the userId
			else if(user.getUserId() == 0 && user.getUserName() != null) {
				long idAttempt = getIdUsingName(user.getUserName(), save);
				user.setUserId(idAttempt);
				if(idAttempt == -1) {
					System.out.println("There is no user with the username: " + user.getUserName());
					failure.add(user);
					continue;
				}
				else if(idAttempt == -2) {
					failure.add(user);
					continue;
				}
			}
			//If neither are given, automatic failure to add user
			else if(user.getUserId() == 0 && user.getUserName() == null) {
				System.out.println("Please give either a userId or userName: " + user);
				failure.add(user);
				continue;
			}
			//If country is missing, use the osu API to fill in the country
			//At this point, it can be assumed that the user is valid
			if(user.getCountry() == null)
				user.setCountry(getCountryUsingId(user.getUserId()));
			//If the user already exists, do not save the user as important information is likely missing
			//Only save the updated country and username
			//Use addUser to completely overwrite an existing user
			if(userRepository.existsById(user.getUserId())) {
				User existingUser = userRepository.findById(user.getUserId()).get();
				existingUser.setUserName(user.getUserName());
				if(save) {
					if(!existingUser.getCountry().equals(user.getCountry())) {
						countryService.add1ToCountry(existingUser.getCountry(), true);
						existingUser.setCountry(user.getCountry());
						countryService.add1ToCountry(existingUser.getCountry(), false);
					}
					userRepository.save(existingUser);
				}
			}
			//All new users are unplaced and have the default elo value
			else {
				user.setElo(1200);
				user.setPlaced(false);
				if(save) {
					userRepository.save(user);
					countryService.add1ToCountry(user.getCountry(), false);
				}
			}
		}
		return failure;
	}
	
	public User getUserById(long id) {
		Optional<User> attempt = userRepository.findById(id);
		if(attempt.isPresent())
			return attempt.get();
		else
			return null;
	}
	
	//In general, it is best to use populateUsers instead when adding new users
	//Instead, should be used mostly as an update function as populateUsers does not update every field for existing Users
	//Immediate save as opposed to populateUsers which checks each user with the osu API
	public void addUser(User user) {
		try {
			userRepository.save(user);
		} catch (Exception e) {
			System.out.println("Error saving user: " + e.getMessage());
			System.out.println("" + user.getElo() + " " + user.getNumLosses() +" "+user.getNumWins()+" "+user.getNumMatches()+" "+user.getNumPlacements()+" "+user.getPlacedMatch()+" "+user.getMatchesParticipated(true).size());
		}
	}
	
	//Will call the helper class for calculating elo changes and then save the users that are returned
	public Match calculateEloChange(Match match) {
		List<User> newUsers = EloCalculator.calculateEloChange(match);
		double eloChange1 = newUsers.get(0).getElo() - match.getElo1();
		double eloChange2 = newUsers.get(1).getElo() - match.getElo2();
		userRepository.saveAll(newUsers);
		match.setEloChange1(eloChange1);
		match.setEloChange2(eloChange2);
		return match;
	}
	
	//Will attempt to get the username of the user through the osu API, assuming a valid userId
	//Returns empty string if the userId is invalid
	//Special Case: userId of -9 indicates a user representing a bye in a bracket, and return will be "@BYE"
	//Special Case: userId of less than -9 indicates a restricted user, and will return "@RU{id}" where id is the absolute value of (userId+9)
	public String getNameUsingId(long id) {
		if(id < -9) {
			id *= -1;
			return "@RU" + id;
		}
		if(id == -9) {
			return "@BYE";
		}
		if(id == -8) {
			return "@PLACEHOLDER";
		}
		String endpoint = "https://osu.ppy.sh/api/get_user";
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("k", Keys.API_KEY_OSU);
		parameters.put("type", "id");
		parameters.put("u", id + "");
		JsonNode fullResponse = DataScraper.performGetRequest(endpoint, parameters);
		if(fullResponse == null || fullResponse.get(0) == null) {
			return "";
		}
		if(id != fullResponse.get(0).get("user_id").asLong())
			return "";
		return fullResponse.get(0).get("username").asText();
	}
	
	//Will attempt to get the userId of the user through the osu API, assuming a valid userName
	//Returns -1 if the userName is invalid
	//Special Case: userName of "@BYE" indicates a user representing a bye in the bracket, and return will be -9
	//Special Case: userName of "@RU{id}" indicates a restricted user, and return will be the negative of (id+9)
	public long getIdUsingName(String name, boolean override) {
		if(name.startsWith("@RU")) {
			long convertToId = 0;
			try {
				convertToId = Integer.parseInt(name.substring(3));
				return -1 * (convertToId + 9);
			} catch (NumberFormatException e) {
				
			}
			return -1;
		}
		if(name.startsWith("@PLACEHOLDER")) {
			return -8;
		}
		if(name.startsWith("@BYE")) {
			return -9;
		}
		String endpoint = "https://osu.ppy.sh/api/get_user";
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("k", Keys.API_KEY_OSU);
		parameters.put("type", "string");
		parameters.put("u", name);
		JsonNode fullResponse = DataScraper.performGetRequest(endpoint, parameters);
		if(fullResponse == null || fullResponse.get(0) == null) {
			return -1;
		}
		//If the user has a playcount below 10000 or a rank below 100k, the match is considered a false positive unless an override flag is set
		if(fullResponse.get(0).get("username").asText().toLowerCase().equals(name.toLowerCase())) {
			if(!override && (fullResponse.get(0).get("playcount").asInt() < 10000 || fullResponse.get(0).get("pp_rank").asInt() > 100000 || fullResponse.get(0).get("pp_rank").asInt() < 1)) {
				System.out.println("probably the wrong person");
				return -2;
			}
			return fullResponse.get(0).get("user_id").asLong();
		}
		else
			return -1;
	}
	
	//Will attempt to get the country of the user through the osu API
	//Returns empty string if the userId is invalid
	private String getCountryUsingId(long id) {
		if(id < 0)
			return "";
		String endpoint = "https://osu.ppy.sh/api/get_user";
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("k", Keys.API_KEY_OSU);
		parameters.put("type", "id");
		parameters.put("u", id + "");
		JsonNode fullResponse = DataScraper.performGetRequest(endpoint, parameters);
		if(fullResponse == null || fullResponse.get(0) == null) {
			System.out.println("User id " + id +
					" is invalid. Please check if the player has gotten restricted.");
			return "";
		}
		if(id != fullResponse.get(0).get("user_id").asLong())
				return "";
		return fullResponse.get(0).get("country").asText();
	}
	
	//Updates all the global ranks of users
	public boolean updateAllRanks() {
		List<User> allUsers = userRepository.findByPlacedOrderByEloDesc(true);
		try {
			for(int i = 0; i < allUsers.size(); i++) {
				if(allUsers.get(i).getRank() != i + 1)
					allUsers.get(i).setRank(i + 1);;
			}
			allUsers.forEach(userRepository::save);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	//Updates all the country ranks of users
	public boolean updateAllCountryRanks() {
		List<Country> allCountries = countryService.listCountries();
		for(Country c : allCountries) {
			List<User> countryUsers = userRepository.findByCountryOrderByEloDescRankAsc(c.getAbbr());
			try {
				for(int i = 0; i < countryUsers.size(); i++) {
					if(countryUsers.get(i).getCountryRank() != i + 1)
						countryUsers.get(i).setCountryRank(i + 1);;
				}
				countryUsers.forEach(userRepository::save);
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}
	
	//Updates all the country ranks of a specific country's users
	public boolean updateCountryRanks(String abbr) {
		Country c = countryService.findCountryByAbbr(abbr);
		List<User> countryUsers = userRepository.findByCountryOrderByEloDescRankAsc(c.getAbbr());
		try {
			for(int i = 0; i < countryUsers.size(); i++) {
				if(countryUsers.get(i).getCountryRank() != i + 1)
					countryUsers.get(i).setCountryRank(i + 1);;
			}
			countryUsers.forEach(userRepository::save);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	//Checks every user for a new name by using the osu API
	//Also checks for restricted/unrestriction
	public boolean updateAllNames(int start) {
		List<User> allUsers = userRepository.findAllByOrderByEloDesc();
		List<User> changedUsers = new ArrayList<User>();
		try {
			for(int i = start; i < allUsers.size(); i++) {
				User u = allUsers.get(i);
				if(u.getUserId() < -9) {
					long rId = (u.getUserId() * -1) - 10;
					RestrictedUser rUser = restrictedUserService.getRestrictedUserById(rId);
					String userName = getNameUsingId(rUser.getUserId());
					if(!userName.equals("")) {
						//unrestrict
						changedUsers.add(unrestriction(u, userName, rUser));
					}
					continue;
				}
				String userName = getNameUsingId(u.getUserId());
				if(userName.equals("")) {
					changedUsers.add(restriction(u));
					continue;
					//process restriction here
					//make restricted user
					//point oldusers to restricted user
					//delete user
					//save new user
					//change match names and ids
				}
				if(u.getUserName().equals(userName))
					continue;
				System.out.println(u.getUserName() + " changed name to " + getNameUsingId(u.getUserId()));
				User unupdated = u;
				oldUserService.addOldUser(u.getUserName(), userName);
				u.setUserName(userName);
				oldUserService.deleteAfterRevert(u);
				matchService.changeNamesForUserMatches(unupdated, u);
				changedUsers.add(u);
			}
			changedUsers.forEach(userRepository::save);
			System.out.println("Finished updating names");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//Method to manually copy all relevant user data to another user when restriction/unrestriction takes place
	public User copyUserData(User oldU, User newU) {
		newU.setCountry(oldU.getCountry());
		newU.setElo(oldU.getElo());
		newU.setNumLosses(oldU.getNumLosses());
		newU.setNumMatches(oldU.getNumMatches());
		newU.setNumWins(oldU.getNumWins());
		newU.setRank(oldU.getRank());
		newU.setCountryRank(oldU.getCountryRank());
		newU.setWinRate(oldU.getWinRate());
		newU.setNumTournamentWins(oldU.getNumTournamentWins());
		newU.setNumPlacements(oldU.getNumPlacements());
		return newU;
	}
	
	public void deleteUser(User user) {
		userRepository.delete(user);
	}
	
	//Resets all users to their base state and deletes all matches and tournaments
	public void resetAll() {
		tournamentService.deleteAll();
		List<User> allUsers = userRepository.findAllByOrderByEloDesc();
		for(User u : allUsers) {
			u.resetUser();
		}
		userRepository.saveAll(allUsers);
	}
	
	/*public void resetTo(long ts) {
		List<Tournament> allT = tournamentService.listAllTournaments();
		Collections.sort(allT);
		for(Tournament t : allT) {
			if(t.getTournamentSequence() > ts)
				break;
			allT.remove(t);
		}
		tournamentService.deleteAll(allT);
		List<User> allUsers = userRepository.findAllByOrderByEloDesc();
		//finish this next time
	}*/
	
	//Fill countries with the user data manually
	public void fillCountries() {
		List<User> allUsers = userRepository.findAllByOrderByEloDesc();
		for(User u : allUsers) {
			if(u.getCountry() != null && (!u.getCountry().equals(""))) {
				countryService.add1ToCountry(u.getCountry(), false);
			}
		}
	}
	
	//Fill countries with elos to calculate the country's average elo
	public boolean fillCountryElos() {
		List<Country> allCountries = countryService.listCountries();
		for(Country c : allCountries) {
			List<User> countryUsers = userRepository.findByCountryOrderByEloDescRankAsc(c.getAbbr());
			try {
				double avg = 0;
				for(int i = 0; i < countryUsers.size(); i++) {
					avg += countryUsers.get(i).getElo();
				}
				avg /= countryUsers.size();
				c.setAverageElo(avg);
				countryService.saveCountry(c);
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}
	
	//search for users with a given query
	public List<User> search(String query) {
		List<User> results = userRepository.searchByName(query);
		return results;
	}
	
	//Replace the restricted user's data with a new user that has all the match and tournament info
	public User unrestriction(User user, String userName, RestrictedUser rUser) {
		if(user.getUserId() < -9) {
			System.out.println("player " + userName + " unrestricted");
			User unrestricted = new User();
			unrestricted = copyUserData(user, unrestricted);
			unrestricted.setUserName(userName);
			unrestricted.setUserId(rUser.getUserId());
			String country = getCountryUsingId(unrestricted.getUserId());
			addUser(unrestricted);
			if(!user.getCountry().equals(country)) {
				//dont forget to update country elos
				if(!user.getCountry().equals("")) {
					countryService.add1ToCountry(user.getCountry(), true);
					countryService.updateCountryFields(user.getCountry(), user.getElo(), false);
				}
				unrestricted.setCountry(country);
				countryService.add1ToCountry(country, false);
				countryService.updateCountryFields(country, unrestricted.getElo(), true);
				addUser(unrestricted);
				updateCountryRanks(unrestricted.getCountry());
			}
			matchService.changeNamesForUserMatches(user, unrestricted);
			tournamentService.changeUserList(user, unrestricted);
			oldUserService.updateRestriction(user, unrestricted);
			deleteUser(user);
			restrictedUserService.deleteRU(rUser);
			return unrestricted;
		}
		return user;
	}
	
	//Replace the user's data with a new restricted user that has all the match and tournament info
	public User restriction(User user) {
		if(user.getUserId() > 0) {
			System.out.println("player " + user.getUserName() + " restricted");
			oldUserService.addOldUser(user.getUserName(), "@RU");
			User unupdated = oldUserService.getUserByOldUserName(user.getUserName());
			User updated = unupdated;
			updated = copyUserData(user, updated);
			addUser(updated);
			matchService.changeNamesForUserMatches(user, updated);
			tournamentService.changeUserList(user, updated);
			restrictedUserService.addUserToRU((updated.getUserId() * -1) - 10, user.getUserId());
			oldUserService.updateRestriction(user, updated);
			deleteUser(user);
			return updated;
		}
		return user;
	}
	
	//Manually add a tournament win to a user in case it fails on initial insert (usually when the winner is restricted)
	public boolean addWinToUser(String tName, long id) {
		Tournament t = tournamentService.getTournamentByName(tName);
		User u = getUserById(id);
		//Check if tournament already has a winner or if user is nonexistent
		if(t.getTournamentWinner() != null && !t.getTournamentWinner().getUserName().equals(""))
			return false;
		if(u == null)
			return false;
		u.addTournamentWon(t);
		u.incrementNumTournamentWins();
		t.setTournamentWinner(u);
		addUser(u);
		tournamentService.update(t);
		return true;
	}
	/*//Find bad matches (matches that don't get the elos changed after calculation)
	public List<Match> findbad() {
		List<Match> allBad = new ArrayList<Match>();
		List<User> alluser = userRepository.findAllByOrderByEloDesc();
		for(User u:alluser) {
			List<Match> bads = (matchService.findBad(u));
			for(Match m : bads) {
				if(m.getPlayer1().equals("@BYE") || m.getPlayer2().equals("@BYE"))
					continue;
				allBad.add(m);
			}
			if(u.getNumMatches() > 1)
				allBad.addAll(matchService.findBad2(u));
		}
		return allBad;
	}*/
	
	//Returns users sorted by rank
	//Useful to call after elo has been updated, but not rank
	public List<User> getRankingsByRank() {
		return userRepository.findByPlacedOrderByRankAsc(true);
	}
	//Returns users sorted by elo
	public List<User> getRankingsByElo() {
		List<User> allUsers = userRepository.findByPlacedOrderByEloDesc(true);
		return allUsers;
	}
}

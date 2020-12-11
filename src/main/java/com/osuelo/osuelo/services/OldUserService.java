package com.osuelo.osuelo.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.osuelo.osuelo.models.OldUser;
import com.osuelo.osuelo.models.User;
import com.osuelo.osuelo.repositories.OldUserRepository;

/*
 * Service class for old usernames
 * old usernames are not given by the osu API, so they must be saved here for reference
 */
@Service
public class OldUserService {
	
	@Autowired
	private OldUserRepository oldUserRepository;
	
	@Autowired
	private UserService userService;
	
	public Map<String, String> exportAll() {
		List<OldUser> listOfOldUsers = StreamSupport.stream(oldUserRepository.findAll().spliterator(), false).collect(Collectors.toList());
		Map<String, String> nameMap = new HashMap<String, String>();
		for(OldUser u : listOfOldUsers)
			nameMap.put(u.getOldUserName(), u.getCurrentUser().getUserName());
		return nameMap;
	}
	public List<OldUser> getOldUsersByUser(User user) {
		return oldUserRepository.findAllByCurrentUser(user);
	}
	
	//Search the database by a user's old username
	//Return is null if the search fails
	public User getUserByOldUserName(String name) {
		List<OldUser> oldUsers = oldUserRepository.findByOldUserName(name);
		if(oldUsers.size() == 0) {
			return null;
		}
		//It's possible for multiple users to have had the same username in the past, and in that case, the search fails
		if(oldUsers.size() > 1) {
			System.out.println("It seems multiple users have had the names "
					+ name + " in the past. Please use a more current username.");
			return null;
		}
		return oldUsers.get(0).getCurrentUser();
	}
	
	//Adds an old to new username mapping into the database
	//Return is true if success and false if failure
	public boolean addOldUser(String oldName, String currentName) {
		OldUser oldUser = new OldUser();
		oldUser.setOldUserName(oldName);
		User user;
		long idAttempt = -1;
		List<OldUser> searchExistingOldUsers = oldUserRepository.findByOldUserName(oldName);
		if(currentName.startsWith("ID:")) {
			try {
				idAttempt = Integer.parseInt(currentName.substring(3));
				user = new User();
				user.setUserId(idAttempt);
				userService.populateUsers(Arrays.asList(user), true);
				if(user.getUserId() == -1)
					return false;
				user = userService.getUserById(idAttempt);
			} catch (NumberFormatException e) {
				return false;
			}
		}
		else {
			user = userService.getUserByName(currentName);
			//If the user does not exist already, a new user will be inserted before saving the old username
			if(user == null) {/*
				if(currentName.equals("@RU")) {
					long rId = restrictedUserService.addRestrictedUser();
					currentName += (rId + 1);
				}*/
				List<User> userList = new ArrayList<User>();
				User newUser = new User();
				newUser.setUserName(currentName);
				if(currentName.startsWith("@RU")) {
					long userId = Long.parseLong(currentName.substring(3));
					newUser.setUserId(userId);
					newUser.setRestricted(true);
				}
				userList.add(newUser);
				userService.populateUsers(userList, true);
				if(newUser.getUserId() == -1) {
					return false;
				}
				user = userService.getUserByName(currentName);
			}
		}
		if(searchExistingOldUsers.size() != 0){
			for(OldUser ou : searchExistingOldUsers) {
				if(ou.getCurrentUser().getUserName().equals(currentName)) {
					System.out.println("The old username " + oldName + " is already in our records.");
					return false;
				}
			}
		}
		oldUser.setCurrentUser(user);
		oldUserRepository.save(oldUser);
		return true;
	}
	
	//Add multiple old users at once
	//Return is a list of successful old name insertions followed by failed old name insertions
	public List<Object> addOldUsers(Map<String, String> namePairs) {
		Set<String> oldNames = namePairs.keySet();
		Map<String, String> success = new HashMap<String, String>();
		Map<String, String> failure = new HashMap<String, String>();
		for(String s : oldNames) {
			if(addOldUser(s, namePairs.get(s)))
				success.put(s, namePairs.get(s));
			else
				failure.put(s, namePairs.get(s));
		}
		List<Object> combinedResult = Arrays.asList("SUCCESS:", success,"FAILURE:", failure);
		return combinedResult;
	}
	
	/*//When a user gets restricted, their username becomes an old username
	//upon unrestriction, assuming the name is the same, that old username entry is deleted
	//userid gets changed on restriction/unrestriction, so the old usernames are updated according to the new id
	public void updateRestriction(User user, User newUser) {
		List<OldUser> oldUsers = oldUserRepository.findAllByCurrentUser(user);
		if(oldUsers.size() > 0) {
			for(OldUser ou : oldUsers) {
				if(ou.getOldUserName().equals(newUser.getUserName())) {
					oldUserRepository.delete(ou);
					continue;
				}
				ou.setCurrentUser(newUser);
				oldUserRepository.save(ou);
			}
		}
	}*/
	
	//If any user reverts to a previous username, that old username entry is deleted
	public void deleteAfterRevert() {
		Iterator<OldUser> oldUsers = oldUserRepository.findAll().iterator();
		while(oldUsers.hasNext()) {
			OldUser ou = oldUsers.next();
			if(ou.getCurrentUser().getUserName().equals(ou.getOldUserName()))
				oldUserRepository.delete(ou);
		}
	}
	
	//If a certain user reverts to a previous username, that old username entry is deleted
	public void deleteAfterRevert(User user) {
		List<OldUser> oldUsers = oldUserRepository.findAllByCurrentUser(user);
		for(OldUser ou : oldUsers) {
			if(ou.getOldUserName().equals(user.getUserName()))
				oldUserRepository.delete(ou);
		}
	}
	//Search for old usernames
	//Can search using partial words
	public List<User> search(String query) {
		List<OldUser> results = oldUserRepository.searchByName(query);
		List<User> uResults = new ArrayList<User>();
		for(OldUser ou : results)
			uResults.add(ou.getCurrentUser());
		return uResults;
	}
	
}

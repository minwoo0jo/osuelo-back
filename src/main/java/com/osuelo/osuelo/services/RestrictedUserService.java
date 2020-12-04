package com.osuelo.osuelo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.osuelo.osuelo.models.RestrictedUser;
import com.osuelo.osuelo.models.User;
import com.osuelo.osuelo.repositories.RestrictedUserRepository;

/*
 * Service class for restricted users
 */
@Service
public class RestrictedUserService {

	@Autowired
	private RestrictedUserRepository restrictedUserRepository;
	
	@Autowired OldUserService oldUserService;
	
	//Search by restricted user id (not the actual osu user id)
	public RestrictedUser getRestrictedUserById(long id) {
		try {
			RestrictedUser rUser = restrictedUserRepository.findById(id).get();
			return rUser;
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	//Search by actual osu user id before ban
	public RestrictedUser getRestrictedUserByUserId(long id) {
		List<RestrictedUser> rUser = restrictedUserRepository.findByUserId(id);
		if(rUser.size() == 1)
			return rUser.get(0);
		else return null;
	}
	
	//Only used on creation for convenience
	//Not available to controller classes
	private RestrictedUser getRestrictedUserBySaved() {
		try {
			return restrictedUserRepository.findBySaved(false).get(0);
		} catch (Exception e) {
			return null;
		}
	}
	
	//Create a new restricted user
	public long addRestrictedUser() {
		RestrictedUser rUser = new RestrictedUser();
		restrictedUserRepository.save(rUser);
		rUser = getRestrictedUserBySaved();
		if(rUser == null)
			return -1;
		long rId = rUser.getRestrictedId();
		rUser.setSaved(true);
		restrictedUserRepository.save(rUser);
		return rId;
	}
	
	//Add a user object reference to the restricted user
	public boolean addUserToRU(long rUserId, long user) {
		try {
			RestrictedUser rUser = getRestrictedUserById(rUserId);
			if(rUser == null)
				return false;
			rUser.setUserId(user);
			restrictedUserRepository.save(rUser);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	//Use the username mapping to link restricted users to users
	public List<String> addUsersToRU(Map<String, String> idPairs) {
		List<String> failures = new ArrayList<String>();
		failures.add("FAILURES:");
		Set<String> keys = idPairs.keySet();
		for(String s : keys) {
			User rUser = oldUserService.getUserByOldUserName(s);
			if(rUser == null) {
				failures.add(s);
				continue;
			}
			if(!addUserToRU(((rUser.getUserId() * -1) - 10), Long.parseLong(idPairs.get(s)))) {
				failures.add(s);
				continue;
			}
		}
		return failures;
	}
	
	public void deleteRU(RestrictedUser rUser) {
		restrictedUserRepository.delete(rUser);
	}
}

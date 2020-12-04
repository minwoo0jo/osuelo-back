package com.osuelo.osuelo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.osuelo.osuelo.helper.UserWrapper;
import com.osuelo.osuelo.models.OldUser;
import com.osuelo.osuelo.models.RestrictedUser;
import com.osuelo.osuelo.models.User;
import com.osuelo.osuelo.services.CountryService;
import com.osuelo.osuelo.services.OldUserService;
import com.osuelo.osuelo.services.RestrictedUserService;
import com.osuelo.osuelo.services.UserService;

@RestController
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private OldUserService oldUserService;
	
	@Autowired
	private CountryService countryService;
	
	@Autowired
	private RestrictedUserService restrictedUserService;
	
	//By default, not specifying a page number will return only the first page
	//Sorted by current rank, number of matches, or win rate
	@CrossOrigin()
	@RequestMapping("/users")
	public List<Object> listUsers(@RequestParam(value="sort", defaultValue="rank") String sort) {
		return userService.list50Users(sort, 0);
	}

	//Page number can be specified to get more users with lower ranks
	@CrossOrigin()
	@RequestMapping("/users/page/{page}")
	public List<Object> listUsersByPage(@PathVariable int page, @RequestParam(value="sort", defaultValue="rank") String sort) {
		return userService.list50Users(sort, page - 1);
	}
	
	//By default, not specifying a page number will return only the first page
	//Sorted by current rank, number of matches, or win rate
	@CrossOrigin()
	@RequestMapping("/users/complete")
	public List<Object> listUsersAll(@RequestParam(value="sort", defaultValue="rank") String sort) {
		return userService.list50UsersAll(sort, 0);
	}

	//Page number can be specified to get more users with lower ranks
	@CrossOrigin()
	@RequestMapping("/users/complete/page/{page}")
	public List<Object> listUsersAllByPage(@PathVariable int page, @RequestParam(value="sort", defaultValue="rank") String sort) {
		return userService.list50UsersAll(sort, page - 1);
	}
	
	/*@CrossOrigin()
	@RequestMapping("/users/past")
	public List<Object> listUsersHistorical(@RequestParam(value="date", defaultValue="") String date) {
		if(date.equals(""))
			return userService.list50UsersHistory(0, null);
		return userService.list50UsersHistory(0, Date.valueOf(date));
	}

	//Page number can be specified to get more users with lower ranks
	@CrossOrigin()
	@RequestMapping("/users/past/page/{page}")
	public List<Object> listUsersHistoricalByPage(@PathVariable int page, @RequestParam(value="date", defaultValue="") String date) {
		if(date.equals(""))
			return userService.list50UsersHistory(page - 1, null);
		return userService.list50UsersHistory(page - 1, Date.valueOf(date));
	}*/
	
	//Get the top 50 list of countries sorted by country elo
	//Page uses params here to differentiate from specific country user leaderboards
	@CrossOrigin()
	@RequestMapping("/users/country")
	public List<Object> listCountries(@RequestParam(value="p", defaultValue="1") int page) {
		return countryService.list50Countries(page - 1);
	}
	
	//Country can be specified to get country specific leaderboards
	@CrossOrigin()
	@RequestMapping("/users/country/{country}")
	public List<Object> listUsersByCountry(@PathVariable String country, @RequestParam(value="sort", defaultValue="rank") String sort) {
		return userService.list50Users(sort, 0, country);
	}
	
	//Page number can be specified to get more users with lower ranks
	@CrossOrigin()
	@RequestMapping("/users/country/{country}/{page}")
	public List<Object> listUsersByCountryAndPage(@PathVariable String country, @PathVariable int page, @RequestParam(value="sort", defaultValue="rank") String sort) {
		return userService.list50Users(sort, page - 1, country);
	}
	
	//Find the details of a specific user using the userName
	//Might not always have the up to date userName if the user had recently changed the userName
	@CrossOrigin()
	@RequestMapping("/users/{name}")
	public UserWrapper userPageByName(@PathVariable String name) {
		User u = userService.getUserByName(name.toLowerCase());
		if(u == null)
			return null;
		UserWrapper userObject = new UserWrapper(u, u.getTournamentsParticipated(true), u.getMatchesParticipated(true));
		List<OldUser> oldUsers = oldUserService.getOldUsersByUser(userObject.getUser());
		RestrictedUser rUser = null;
		long uId = userObject.getUser().getUserId();
		if(uId < -9)
			rUser = restrictedUserService.getRestrictedUserById((uId * -1) - 10);
		userObject.initialize(oldUsers, rUser);
		return userObject;
	}
	
	//Find the details of a specific user using the userId
	@CrossOrigin()
	@RequestMapping("/users/id/{id}")
	public UserWrapper userPageByTid(@PathVariable long id) {
		User u = userService.getUserById(id);
		if(u == null)
			return null;
		UserWrapper userObject = new UserWrapper(u, u.getTournamentsParticipated(true), u.getMatchesParticipated(true));
		List<OldUser> oldUsers = oldUserService.getOldUsersByUser(userObject.getUser());
		RestrictedUser rUser = null;
		long uId = userObject.getUser().getUserId();
		if(uId < -9)
			rUser = restrictedUserService.getRestrictedUserById((uId * -1) - 10);
		userObject.initialize(oldUsers, rUser);
		return userObject;
	}
}

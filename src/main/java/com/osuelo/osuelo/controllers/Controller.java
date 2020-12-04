package com.osuelo.osuelo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.osuelo.osuelo.config.APIHandler;
import com.osuelo.osuelo.services.TournamentService;
import com.osuelo.osuelo.services.UserService;


@RestController
public class Controller {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private TournamentService tournamentService;
	
	
	//Update ranks of every user
	@RequestMapping("/ranks")
	public boolean updateRanks(@RequestParam("k") String key) {
		if(!APIHandler.checkKey(key))
			return false;
		return userService.updateAllRanks();
	}
	
	//Update names of every user
	//Also handles restriction/unrestriction
	@RequestMapping("/names")
	public boolean updateNames(@RequestParam("k") String key, @RequestParam(value="s", defaultValue="0") String start) {
		if(!APIHandler.checkKey(key))
			return false;
		return userService.updateAllNames(Integer.parseInt(start));
	}
	
	//Reset all users to the base state
	@RequestMapping("/reset")
	public void resetAllUsers(@RequestParam("k") String key) {
		if(!APIHandler.checkKey(key))
			return;
		userService.resetAll();
	}
	
	//Fills country table with data from the user table
	@RequestMapping("/countryfill")
	public void fillCountries(@RequestParam("k") String key) {
		if(!APIHandler.checkKey(key))
			return;
		userService.fillCountries();
	}
	
	//Update country ranks of every user
	@RequestMapping("/countryranks")
	public boolean updateCountryRanks(@RequestParam("k") String key) {
		if(!APIHandler.checkKey(key))
			return false;
		return userService.updateAllCountryRanks();
	}
	
	//Update country elos of every country
	@RequestMapping("/countryelos")
	public boolean updateCountryElos(@RequestParam("k") String key) {
		if(!APIHandler.checkKey(key))
			return false;
		return userService.fillCountryElos();
	}
	
	//Delete later
	//If the tournament winner is restricted, use this to manually set the tournament winner
	@RequestMapping("/addWin")
	public boolean addWin(@RequestParam("k") String key, @RequestParam("t") String tName, @RequestParam("id") long id) {
		if(!APIHandler.checkKey(key))
			return false;
		return userService.addWinToUser(tName, id);
	}
	
	/*//Debugging tool to find some types of invalid matches
	@RequestMapping("/findbad")
	public List<Match> findb(@RequestParam("k") String key) {
		if(!APIHandler.checkKey(key))
			return null;
		return userService.findbad();
	}*/
	
	//Returns a list of matches for the search query
	//Can be narrowed down to search for a specific object type
	@CrossOrigin()
	@RequestMapping("/search")
	public List<Object> searchQuery(@RequestParam("q") String query, @RequestParam(value="t", defaultValue="a") String type) {
		List<Object> results = null;
		if(query.length() < 2)
			return null;
		results = tournamentService.searchAll(query, type);
		return results;
	}
	
	/*@RequestMapping("/fix")
	public boolean fixStuff() {
		return tournamentService.fixStuff();
	}*/
}
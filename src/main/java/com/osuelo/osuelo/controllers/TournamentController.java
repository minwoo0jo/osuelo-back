package com.osuelo.osuelo.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.osuelo.osuelo.config.APIHandler;
import com.osuelo.osuelo.helper.Challonge;
import com.osuelo.osuelo.helper.RequestWrapper;
import com.osuelo.osuelo.helper.TournamentWrapper;
import com.osuelo.osuelo.models.Tournament;
import com.osuelo.osuelo.models.User;
import com.osuelo.osuelo.services.TournamentService;

@RestController
public class TournamentController {
	
	@Autowired
	private TournamentService tournamentService;
	
	//By default, not specifying a page number will return only the first page
	//Sorted by chronological order of the start date
	@CrossOrigin()
	@RequestMapping("/tournaments")
	public List<Object> listTournaments() {
		return tournamentService.list50Tournaments(0);
	}
	
	//Page number can be specified to get older tournaments
	@CrossOrigin()
	@RequestMapping(value="/tournaments/page/{page}")
	public List<Object> listTournamentsByPage(@PathVariable int page) {
		return tournamentService.list50Tournaments(page - 1);
	}
	
	//Find the details of a specific tournament
	//Might not work if the tournament name has invalid characters
	@CrossOrigin()
	@RequestMapping("/tournaments/{name}")
	public TournamentWrapper tournamentByName(@PathVariable String name) {
		Tournament t = tournamentService.getTournamentByName(name.toLowerCase());
		if(t == null)
			return null;
		return new TournamentWrapper(t, t.getTournamentMatches(true), t.getTournamentUsers(true));
	}
	
	//Find the details of a specific tournament
	@CrossOrigin()
	@RequestMapping("/tournaments/id/{id}")
	public TournamentWrapper tournamentPageById(@PathVariable long id) {
		Tournament t = tournamentService.getTournamentById(id);
		if(t == null)
			return null;
		return new TournamentWrapper(t, t.getTournamentMatches(true), t.getTournamentUsers(true));
	}
	
	//Submit a tournament manually without using challonge
	//Each User requires a name or id and each Match requires a player1, player2, and winner
	@RequestMapping(method=RequestMethod.POST, value="/tournaments/submit")
	public List<User> addTournament(@RequestBody Tournament tournament, @RequestParam("k") String key) {
		if(!APIHandler.checkKey(key))
			return null;
		//false param indicates that the submission is attempted, but not actually saved
		List<User> nameCheck = tournamentService.addTournament(tournament, false);
		if(nameCheck.size() > 0)
			return nameCheck;
		
		return tournamentService.replaceWithExistingUsers(tournament);
	}
	
	//Submit a list of tournaments manually without using challonge
	//Each User requires a name or id and each Match requires a player1, player2, and winner
	@RequestMapping(method=RequestMethod.POST, value="/tournaments/submitList")
	public Map<User, Tournament> addTournamentList(@RequestBody List<Tournament> tournaments, @RequestParam("k") String key) {
		if(!APIHandler.checkKey(key))
			return null;
		return tournamentService.addTournamentList(tournaments);
	}
	
	
	//Submit a list of tournaments through a valid challonge link
	//startDate is optional, but highly recommended. Will attempt to find through challonge if null
	//Will not submit, but instead will save the data to a folder
	//o param is used to override unlikely name checking
	@RequestMapping(method=RequestMethod.POST, value="/tournaments/challonge")
	@CrossOrigin()
	public List<List<String>> addChallonge(@RequestBody List<Challonge> challonges, @RequestParam(value="o", defaultValue="false") String override) {
		return tournamentService.convertChallongesToTournaments(challonges, false, override.equals("true"));
	}
	
	//Submit a tournament through a valid challonge link
	//Identical to addChallonge, but includes additional data from the requestwrapper class
	@RequestMapping(method=RequestMethod.POST, value="/tournaments/challonge/test")
	@CrossOrigin()
	public List<List<String>> addChallonge2(@RequestBody RequestWrapper requestObj, @RequestParam(value="o", defaultValue="false") String override) {
		return tournamentService.processSubmitData(requestObj, false, override.equals("true"));
	}
	
	//Submit a list of tournaments
	//Requires API key as this will actually submit
	@RequestMapping(method=RequestMethod.POST, value="/tournaments/challonge/force")
	public List<List<String>> addChallongeOverride(@RequestBody List<Challonge> challonges, @RequestParam("k") String key) {
		if(!APIHandler.checkKey(key))
			return null;
		return tournamentService.convertChallongesToTournaments(challonges, true, true);
	}
	
	//Delete all tournaments with a higher tournament sequence value
	//Revert all data in other tables to the values they were at the given point in time
	//Currently bugged: matches are not deleted from the database and must be deleted manually
	@RequestMapping("tournaments/resetTo/{sequence}")
	public void resetTo(@PathVariable long sequence, @RequestParam("k") String key) {
		if(!APIHandler.checkKey(key))
			return;
		tournamentService.resetToTournament(sequence);
	}
	
	@RequestMapping("tournaments/export")
	public String exportAllTournaments(@RequestParam("k") String key) {
		if(!APIHandler.checkKey(key))
			return null;
		return tournamentService.exportAllTournaments();
	}
	@RequestMapping("tournaments/export/{id}")
	public String exportTournament(@PathVariable long id, @RequestParam("k") String key) {
		if(!APIHandler.checkKey(key))
			return null;
		return tournamentService.exportTournament(id);
	}
}

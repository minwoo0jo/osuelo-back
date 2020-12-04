package com.osuelo.osuelo.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.osuelo.osuelo.config.APIHandler;
import com.osuelo.osuelo.models.User;
import com.osuelo.osuelo.services.OldUserService;
import com.osuelo.osuelo.services.RestrictedUserService;

@RestController
public class OldUserController {
	
	@Autowired
	private OldUserService oldUserService;
	
	@Autowired
	private RestrictedUserService restrictedUserService;
	
	//Input: List of name pairs in an "old": "new" format
	//Output: List of all the valid mappings and all the invalid mappings
	@RequestMapping(method=RequestMethod.POST, value="/oldusername/add")
	public List<Object> addOldUserNames(@RequestBody Map<String, String> namePairs, @RequestParam("k") String key) {
		if(!APIHandler.checkKey(key))
			return null;
		return oldUserService.addOldUsers(namePairs);
	}
	
	@RequestMapping("/oldusernames/{oldName}")
	public User findUserByOldName(@PathVariable String oldName) {
		return oldUserService.getUserByOldUserName(oldName);
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/restrictedusers")
	public List<String> addIdToRU(@RequestBody Map<String, String> idPairs, @RequestParam("k") String key) {
		if(!APIHandler.checkKey(key))
			return null;
		return restrictedUserService.addUsersToRU(idPairs);
	}
}

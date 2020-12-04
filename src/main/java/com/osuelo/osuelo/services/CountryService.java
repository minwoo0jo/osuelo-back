package com.osuelo.osuelo.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.osuelo.osuelo.models.Country;
import com.osuelo.osuelo.repositories.CountryRepository;


/*
 * Service class for the country model
 * Most country data is updated periodically instead of with the other models, as any match could adjust many others' country ranks
 */
@Service
public class CountryService {
	
	@Autowired
	private CountryRepository countryRepository;
	
	//List top 50 countries, ordered by number of users in that country
	//First object in the list is the number of countries in total, second is the list of 50
	public List<Object> list50Countries(int page) {
		if(page < 0)
			return null;
		Pageable pageable = PageRequest.of(page, 50);
		List<Country> pageOfCountries = countryRepository.findAllByOrderByNumUsersDesc(pageable);
		List<Object> listCountries = new ArrayList<Object>();
		listCountries.add(new Long(countryRepository.count()));
		listCountries.add(pageOfCountries);
		return listCountries;
	}
	
	//Lists all countries
	//Not for use by a controller, but instead for other service classes
	public List<Country> listCountries() {
		return countryRepository.findAllByOrderByNumUsersDesc();
	}
	
	//Simply increments a country's user number count by 1
	//Creates the country first if the country did not previously exist
	public void add1ToCountry(String abbr, boolean remove) {
		if(abbr == null || abbr.equals("")) {
			System.out.println("Invalid country abbreviation");
			return;
		}
		List<Country> country = countryRepository.findByAbbr(abbr);
		if(country.size() == 0) {
			Country newC = new Country();
			newC.setAbbr(abbr);
			newC.setNumUsers(1);
			countryRepository.save(newC);
		}
		else {
			if(country.size() > 1) {
				System.out.println("Error: duplicate country");
			}
			Country oldC = country.get(0);
			oldC.setNumUsers(oldC.getNumUsers() + (remove? -1 : 1));
			countryRepository.save(oldC);
		}
	}
	
	//osu API only stores abbreviations, so the full name of the country must be set manually
	public void setFullName(String abbr, String fullName) {
		if(abbr == null || fullName == null || abbr.equals("") || fullName.equals("")) {
			System.out.println("Invalid country abbreviation or name");
			return;
		}
		List<Country> country = countryRepository.findByAbbr(abbr);
		if(country.size() == 0) {
			Country newC = new Country();
			newC.setAbbr(abbr);
			newC.setFullName(fullName);
			countryRepository.save(newC);
		}
		else {
			if(country.size() > 1) {
				System.out.println("Error: duplicate country");
			}
			Country oldC = country.get(0);
			oldC.setFullName(fullName);
			countryRepository.save(oldC);
		}
	}
	
	//Save function
	public boolean saveCountry(Country country) {
		try {
			countryRepository.save(country);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//returns the country with the given 2 letter abbreviation
	public Country findCountryByAbbr(String abbr) {
		if(abbr.length() == 0) 
			return null;
		try {
			List<Country> cList = countryRepository.findByAbbr(abbr);
			if(cList.size() == 0) {
				return null;
			}
			return cList.get(0);
		} catch(Exception e) {
			return null;
		}
	}
	
	//update the country's average elo
	//when add is true, a new user was added since last calculation
	//when add is false, an old user was removed since last calculation (flag change)
	public void updateCountryFields(String abbr, double elo, boolean add) {
		Country country = findCountryByAbbr(abbr);
		double avgElo = country.getAverageElo();
		if(add) {
			avgElo *= country.getNumUsers() - 1;
			avgElo += elo;
		}
		else {
			avgElo *= country.getNumUsers() + 1;
			avgElo -= elo;
		}
		avgElo /= country.getNumUsers();
		country.setAverageElo(avgElo);
		countryRepository.save(country);
	}

}

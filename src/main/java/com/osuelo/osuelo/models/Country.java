package com.osuelo.osuelo.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/*
 * Entity representing Countries.
 */
@Entity
public class Country {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long countryId;
	private String fullName;
	//Abbreviation based on osu's 2 letter country abbreviations
	private String abbr;
	private long numUsers;
	private double averageElo;
	
	//Constructors
	public Country() {
		fullName = "";
		abbr = "";
		numUsers = 0;
		averageElo = 1200;
	}
	
	public Country(long countryId, String fullName, String abbr, long numUsers) {
		super();
		this.countryId = countryId;
		this.fullName = fullName;
		this.abbr = abbr;
		this.numUsers = numUsers;
	}
	//Getters and setters
	public long getCountryId() {
		return countryId;
	}
	public void setCountryId(long countryId) {
		this.countryId = countryId;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getAbbr() {
		return abbr;
	}
	public void setAbbr(String abbr) {
		this.abbr = abbr;
	}
	public long getNumUsers() {
		return numUsers;
	}
	public void setNumUsers(long numUsers) {
		this.numUsers = numUsers;
	}

	public double getAverageElo() {
		return averageElo;
	}

	public void setAverageElo(double averageElo) {
		this.averageElo = averageElo;
	}
	
	
}

package com.osuelo.osuelo.helper;

import java.util.List;
import java.util.Map;

//This is a wrapper class for when a tournament is submitted through challonge on the website
public class RequestWrapper {
	private List<Challonge> challonges;
	private String submitter = "omit";
	private Map<String, String> nameChanges;
	public RequestWrapper(List<Challonge> challonges, String submitter, Map<String, String> nameChanges) {
		super();
		this.challonges = challonges;
		this.submitter = submitter;
		this.nameChanges = nameChanges;
	}
	public List<Challonge> getChallonges() {
		return challonges;
	}
	public void setChallonges(List<Challonge> challonges) {
		this.challonges = challonges;
	}
	public String getSubmitter() {
		return submitter;
	}
	public void setSubmitter(String submitter) {
		this.submitter = submitter;
	}
	public Map<String, String> getNameChanges() {
		return nameChanges;
	}
	public void setNameChanges(Map<String, String> nameChanges) {
		this.nameChanges = nameChanges;
	}
}

package com.osuelo.osuelo.helper;

import com.osuelo.osuelo.models.Match;

//This class is a match class with all of the calculation data stripped away
//Leave behind only the userdata and the winner to calculate everything else later
public class SimpleMatch {
	
	private String winner;
	private String player1;
	private long player1Id;
	private String player2;
	private long player2Id;
	public SimpleMatch(Match match) {
		winner = match.getWinner();
		player1 = match.getPlayer1();
		player2 = match.getPlayer2();
		player1Id = match.getPlayer1Id();
		player2Id = match.getPlayer2Id();
	}
	public String getWinner() {
		return winner;
	}
	public void setWinner(String winner) {
		this.winner = winner;
	}
	public String getPlayer1() {
		return player1;
	}
	public void setPlayer1(String player1) {
		this.player1 = player1;
	}
	public long getPlayer1Id() {
		return player1Id;
	}
	public void setPlayer1Id(long player1Id) {
		this.player1Id = player1Id;
	}
	public String getPlayer2() {
		return player2;
	}
	public void setPlayer2(String player2) {
		this.player2 = player2;
	}
	public long getPlayer2Id() {
		return player2Id;
	}
	public void setPlayer2Id(long player2Id) {
		this.player2Id = player2Id;
	}
}

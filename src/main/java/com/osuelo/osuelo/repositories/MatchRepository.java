package com.osuelo.osuelo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.osuelo.osuelo.models.Match;
import com.osuelo.osuelo.models.Tournament;

public interface MatchRepository extends PagingAndSortingRepository<Match, Long> {

	public List<Match> findByTournament(Tournament tournament);
	
	//Find matches where the players involved didn't get their elos adjusted
	@Query(value="select * from matches where (player1id=?2 and elo1=?1) or (player2id=?2 and elo2=?1)", nativeQuery=true)
	public List<Match> findBadMatches(double elo, long id);
}

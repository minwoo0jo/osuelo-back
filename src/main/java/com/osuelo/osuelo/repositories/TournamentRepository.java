package com.osuelo.osuelo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.osuelo.osuelo.models.Tournament;

public interface TournamentRepository extends PagingAndSortingRepository<Tournament, Long> {

	public List<Tournament> findByTournamentName(String name);
	
	public List<Tournament> findByTournamentSequence(long sequence);
	
	//Search by tournament name and sort by length to simulate relevance
	@Query(value = "Select * from tournament where tournament_name like %?1% or short_name like %?1% order by length(tournament_name)", nativeQuery = true)
	public List<Tournament> searchTournamentByName(String query);
	
}

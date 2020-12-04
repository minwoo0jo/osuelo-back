package com.osuelo.osuelo.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.osuelo.osuelo.models.User;

public interface UserRepository extends PagingAndSortingRepository<User, Long> {

	public List<User> findByUserName(String name);
		
	public List<User> findByCountryOrderByEloDescRankAsc(String country, Pageable pageable);
	
	public List<User> findByCountryOrderByEloDescRankAsc(String country);
	
	public List<User> findByCountryOrderByNumMatchesDescEloDescRankAsc(String country, Pageable pageable);

	public List<User> findByCountryOrderByWinRateDescEloDescRankAsc(String country, Pageable pageable);

	public List<User> findByCountryOrderByNumTournamentWinsDescEloDescRankAsc(String country, Pageable pageable);
	
	public List<User> findByPlacedOrderByEloDesc(boolean placed);

	public List<User> findByPlacedOrderByRankAsc(boolean placed);

	public List<User> findByPlacedOrderByNumMatchesDescRankAsc(boolean placed, Pageable pageable);
	
	public List<User> findByPlacedOrderByWinRateDescRankAsc(boolean placed, Pageable pageable);
	
	public List<User> findByPlacedOrderByNumTournamentWinsDescRankAsc(boolean placed, Pageable pageable);
	
	public List<User> findByPlacedOrderByEloDescRankAsc(boolean placed, Pageable pageable);
	
	public List<User> findAllByOrderByEloDesc();
	
	public List<User> findAllByOrderByNumMatchesDescEloDescRankAsc(Pageable pageable);
	
	public List<User> findAllByOrderByWinRateDescEloDescRankAsc(Pageable pageable);
	
	public List<User> findAllByOrderByNumTournamentWinsDescEloDescRankAsc(Pageable pageable);

	public List<User> findAllByOrderByEloDescRankAsc(Pageable pageable);

	public long countByCountry(String country);
	
	public long countByPlaced(boolean placed);
	
	//Search by username and sort by length to simulate relevance
	@Query(value="Select * from user where user_name like %?1% order by length(user_name)", nativeQuery=true)
	public List<User> searchByName(String query);
}

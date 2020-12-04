package com.osuelo.osuelo.repositories;


import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.osuelo.osuelo.models.Country;


public interface CountryRepository extends PagingAndSortingRepository<Country, Long> {
	public List<Country> findByAbbr(String abbr);
	public List<Country> findAllByOrderByNumUsersDesc(Pageable pageable);
	public List<Country> findAllByOrderByNumUsersDesc();
}

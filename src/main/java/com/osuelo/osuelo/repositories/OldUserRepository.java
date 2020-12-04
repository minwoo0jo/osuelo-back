package com.osuelo.osuelo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.osuelo.osuelo.models.OldUser;
import com.osuelo.osuelo.models.User;

public interface OldUserRepository extends CrudRepository<OldUser, Long> {

	public List<OldUser> findByOldUserName(String name);
	
	public List<OldUser> findAllByCurrentUser(User user);
	
	//Search by old username and sort by length to simulate relevance
	@Query(value="Select * from old_user where old_user_name like %?1% order by length(old_user_name)", nativeQuery=true)
	public List<OldUser> searchByName(String query);
		
}

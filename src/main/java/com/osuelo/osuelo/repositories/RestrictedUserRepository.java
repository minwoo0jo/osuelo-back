package com.osuelo.osuelo.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.osuelo.osuelo.models.RestrictedUser;

public interface RestrictedUserRepository extends CrudRepository<RestrictedUser, Long> {
	public List<RestrictedUser> findBySaved(boolean saved);
	public List<RestrictedUser> findByUserId(long userId);
}

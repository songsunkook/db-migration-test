package io.spring.dbmigration.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.spring.dbmigration.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

}

package com.taxflow.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u JOIN FETCH u.tenant WHERE LOWER(u.email) = LOWER(:email)")
    List<User> findAllByEmailIgnoreCase(@Param("email") String email);

    @Query("SELECT u FROM User u JOIN FETCH u.tenant WHERE u.id = :id")
    Optional<User> findByIdWithTenant(@Param("id") Long id);
}

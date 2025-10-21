package com.acme.vocatio.repository;

import com.acme.vocatio.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "favoriteCareers")
    Optional<User> findWithFavoriteCareersById(Long id);
}

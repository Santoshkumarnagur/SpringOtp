package com.kodnest.auth.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kodnest.auth.Entity.User;

public interface UserRepository
        extends JpaRepository<User, Integer> {

    User findByEmail(String email);
}
package com.example.SpringApi.Repository.CentralDatabase;

import com.example.SpringApi.DatabaseModels.CentralDatabase.GoogleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoogleUserRepository extends JpaRepository<GoogleUser, Long> {
    GoogleUser findByEmail(String email);
}

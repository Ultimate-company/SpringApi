package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.UserGridPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGridPreferenceRepository extends JpaRepository<UserGridPreference, Long> {
    UserGridPreference findUserGridPreferenceByUserIdAndGridId(long userId, int gridId);
}
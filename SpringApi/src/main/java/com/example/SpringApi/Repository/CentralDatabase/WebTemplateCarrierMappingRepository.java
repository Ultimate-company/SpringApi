package com.example.SpringApi.Repository.CentralDatabase;

import com.example.SpringApi.DatabaseModels.CentralDatabase.User;
import com.example.SpringApi.DatabaseModels.CentralDatabase.WebTemplateCarrierMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebTemplateCarrierMappingRepository extends JpaRepository<WebTemplateCarrierMapping, Long> {
    WebTemplateCarrierMapping findByWildCard(String wildCard);
    WebTemplateCarrierMapping findByCarrierId(long carrierId);
}

package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.WebTemplatesFontStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebTemplateFontStyleRepository extends JpaRepository<WebTemplatesFontStyle, Long> {
}

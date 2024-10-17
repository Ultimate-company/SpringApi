package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.WebTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WebTemplatesRepository extends JpaRepository<WebTemplate, Long> {
    @Query("select w, ch, cs, h from WebTemplate w join WebTemplatesFontStyle ch on ch.webTemplateFontStyleId = w.cardHeaderFontStyleId " +
            "join WebTemplatesFontStyle cs on cs.webTemplateFontStyleId = w.cardSubTextFontStyleId " +
            "join WebTemplatesFontStyle h on h.webTemplateFontStyleId = w.headerFontStyleId " +
            "where (:includeDeleted = true OR w.deleted = false) " +
            "and (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'Url' THEN CONCAT(w.url, '') " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END)) " +
            "order by w.webTemplateId asc ")
    Page<Object[]> findPaginatedWebTemplates(@Param("columnName") String columnName,
                                            @Param("condition") String condition,
                                            @Param("filterExpr") String filterExpr,
                                            @Param("includeDeleted") boolean includeDeleted,
                                            Pageable pageable);

    WebTemplate findByUrl(String url);
}

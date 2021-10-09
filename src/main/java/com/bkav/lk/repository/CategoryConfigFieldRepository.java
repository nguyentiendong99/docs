package com.bkav.lk.repository;

import com.bkav.lk.domain.CategoryConfigField;
import com.bkav.lk.repository.custom.CategoryConfigFieldRepositoryCustom;
import org.checkerframework.checker.nullness.Opt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * The interface Category config field repository.
 *
 * @author hieu.daominh
 */
@Repository
public interface CategoryConfigFieldRepository extends JpaRepository<CategoryConfigField, Long>, CategoryConfigFieldRepositoryCustom {

    /**
     * Find by id and status.
     *
     * @param id     the id of Field
     * @param status the status of Field
     * @return the Optional<CategoryConfigField>
     */
    Optional<CategoryConfigField> findByIdAndStatus(Long id, Integer status);

    Optional<CategoryConfigField> findByIdAndStatusIn(Long id, Integer[] status);

    /**
     * Find all by health facility id and status.
     *
     * @param healthFacilityId the health facility id
     * @param status           the status
     * @return the Optional<List<CategoryConfigField>>
     */
    @Query(value = "SELECT * FROM category_config_field WHERE health_facility_id = ?1 AND status = ?2 ", nativeQuery = true)
    Optional<List<CategoryConfigField>> findAllByHealthFacilityIdAndStatus(Long healthFacilityId, Integer status);

    /**
     * Find all by health facility id and status and config type optional.
     *
     * @param healthFacilityId the health facility id
     * @param status           the status
     * @param typeCode         the type code
     * @return the optional
     */
    @Query(value = "SELECT * FROM category_config_field WHERE health_facility_id = ?1 AND status = ?2 AND type like %?3% ", nativeQuery = true)
    Optional<List<CategoryConfigField>> findAllByHealthFacilityIdAndStatusAndConfigType(Long healthFacilityId, Integer status, String typeCode);

    @Query(value = "SELECT * FROM category_config_field WHERE health_facility_id = ?1 AND type like %?2% AND status IN (?3)", nativeQuery = true)
    Optional<List<CategoryConfigField>> findAllByHealthFacilityIdAndConfigTypeAndStatusIn(Long healthFacilityId, String typeCode, Integer[] status);

    /**
     * Find all by status.
     *
     * @param status the status
     * @return the optional
     */
    Optional<List<CategoryConfigField>> findAllByStatus(Integer status);

    /**
     * Exists by name ignore case boolean.
     *
     * @param name the name
     * @return the boolean
     */
    boolean existsByNameIgnoreCase(String name);


    /**
     * Exists by name ignore case and status not boolean.
     *
     * @param name   the name
     * @param status the status
     * @return the boolean
     */
    @Query(value = "select count(*) from category_config_field WHERE health_facility_id = ?1 AND name like %?2% AND type like %?3% AND status != ?4 ", nativeQuery = true)
    Integer existsByNameIgnoreCaseAndStatusNot(Long healthFacilityId, String name, String type, Integer status);

    /**
     * Exists by id and status boolean.
     *
     * @param id     the id
     * @param status the status
     * @return the boolean
     */
    boolean existsByIdAndStatusNot(Long id, Integer status);

    /**
     * Exists by name ignore case and id not boolean.
     *
     * @param name   the name
     * @param id     the id
     * @param status the status
     * @return the boolean
     */


//    boolean existsByNameIgnoreCaseAndIdNotAndStatusNot(String name, Long id, Integer status);

    @Query(value = "select count(*) from category_config_field " +
            " WHERE name = ?1 AND id != ?2 AND status != ?3 AND health_facility_id = ?4 AND type like %?5%"
            , nativeQuery = true)
    Integer existsByNameIgnoreCaseAndIdNotAndStatusNot(String name, Long id, Integer status, Long healthFacilityId, String type);

}

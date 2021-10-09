package com.bkav.lk.repository;

import com.bkav.lk.domain.Position;
import com.bkav.lk.repository.custom.PositionRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long>, PositionRepositoryCustom {

    Optional<Position> findOneByIdAndStatus(Long id, Integer status);

    @Query(
            value = "SELECT * FROM position WHERE status = 1 OR status =2 ORDER BY last_modified_date DESC",
            nativeQuery = true)
    List<Position> findAllOrderByLastModifiedDateDesc();

    boolean existsByCode(String code);

    Optional<Position> findByCodeAndStatus(String code, Integer status);

    Position findByIdAndStatus(Long id, Integer status);

    List<Position> findByStatus(Integer status);

    Position findTopByCode(String code);

    Optional<Position> findByCodeAndStatusIsNot(String code, Integer status);
}

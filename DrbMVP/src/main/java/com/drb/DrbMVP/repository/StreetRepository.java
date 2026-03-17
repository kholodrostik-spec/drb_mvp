package com.drb.DrbMVP.repository;


import com.drb.DrbMVP.entity.Street;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StreetRepository extends JpaRepository<Street, Long> {
    List<Street> findByName(String name);
    List<Street> findByHighway(String highway);
}

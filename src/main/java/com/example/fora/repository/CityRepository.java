package com.example.fora.repository;

import com.example.fora.model.Category;
import com.example.fora.model.City;
import com.example.fora.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface CityRepository extends JpaRepository<City,Integer> {
    boolean existsByUrlSuffix(String urlSuffix);

    @Query("select urlSuffix from City")
    List<String> getAllCities();
}

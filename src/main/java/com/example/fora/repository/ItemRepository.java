package com.example.fora.repository;

import com.example.fora.model.Category;
import com.example.fora.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item,Integer> {
    Optional<Item> findOneByExternalId(String externalId);

    @Modifying
    @Transactional
    @Query("update Item item set item.available=false where item.category=:category")
    void resetItemAvailability(Category category);
}

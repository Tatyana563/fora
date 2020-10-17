package com.example.fora.repository;

import com.example.fora.model.City;
import com.example.fora.model.Item;
import com.example.fora.model.ItemPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemPriceRepository extends JpaRepository<ItemPrice, Integer> {
    Optional<ItemPrice> findOneByItemAndCity(Item item, City city);
}

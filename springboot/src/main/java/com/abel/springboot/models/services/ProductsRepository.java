package com.abel.springboot.models.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.abel.springboot.models.Product;
           
//here we are making an interface and extending to JpaRepository
// Jpa respository needs two thinds. one in the model class and the datatype of the primary key
public interface ProductsRepository extends JpaRepository<Product,Integer> {
  
	
	  @Query("SELECT MAX(p.id) FROM Product p")
	    Optional<Integer> findMaxId();
	 
} 

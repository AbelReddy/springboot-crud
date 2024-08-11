package com.abel.springboot.controllers;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.abel.springboot.models.Product;
import com.abel.springboot.models.ProductDto;
import com.abel.springboot.models.services.ProductsRepository;

import jakarta.validation.Valid;

@Controller //
//In Java Spring framework, @Controller is an annotation used to mark a class as a Spring MVC controller.
//When you annotate a class with @Controller, it signals to the Spring framework that the class plays a role in handling HTTP requests and generating HTTP responses.
@RequestMapping("/products")
public class ProductsController {

	@Autowired
	private ProductsRepository repo;

	@GetMapping({ "", "/" })
	public String showProductList(Model model) {
		List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id")); // it will return as a list
		model.addAttribute("products", products);
		return "products/index";
	}

	@GetMapping("/create")
	public String showCreatePage(Model model) {
		ProductDto productDto = new ProductDto();

		model.addAttribute("productDto", productDto);
		return "products/createProduct";
	}

	@PostMapping("/create")
	public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result) {

		if (productDto.getImageFile().isEmpty()) {
			result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
		}

		if (result.hasErrors()) {
			return "products/createProduct";
		}

		// to save an image file
		MultipartFile image = productDto.getImageFile();
		Date createdAt = new Date();
		String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

		
		  try { String uploadDir = "public/images/"; 
		  Path uploadPath =Paths.get(uploadDir);
		  if(!Files.exists(uploadPath)) {
		  Files.createDirectories(uploadPath);
		  
		  } try (InputStream inputStream = image.getInputStream()) {
		  Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
		  StandardCopyOption.REPLACE_EXISTING);		  
		  }
		  
		  
		  
		  }catch (Exception e){ 
			  System.out.println("Exception: " + e.getMessage());
			  }
		  
	        int nextId = getNextProductId();

		  
		  Product product = new Product();
	      product.setId(nextId);
		  product.setName(productDto.getName());
		  product.setBrand(productDto.getBrand());
		  product.setCategory(productDto.getCategory());
		  product.setPrice(productDto.getPrice());
		  product.setDescription(productDto.getDescription());
		  product.setCreatedDate(createdAt);
		  product.setImageFileName(storageFileName);
		  
		  repo.save(product);
		  
		return "redirect:/products";
	}


	private int getNextProductId() {
        Optional<Integer> maxIdOptional = repo.findMaxId();
        
        return maxIdOptional.orElse(0) + 1;
    }
	
	  @GetMapping("/edit")
	  public String showEditPage(Model model, @RequestParam int id) {
		  
		  try {
			   Product product = repo.findById(id).get();
			   model.addAttribute("product", product);
			   
			   ProductDto productDto = new ProductDto();
			   productDto.setName(product.getName());
			   productDto.setBrand(product.getBrand());
			   productDto.setCategory(product.getCategory());
			   productDto.setPrice(product.getPrice());
			   productDto.setDescription(product.getDescription());
			   model.addAttribute("productDto", productDto);
			  
		  } catch (Exception e) {
			  System.out.println("Exception:" + e.getMessage());
			  return "redirect:/products";
		  }
		    
		  return "products/EditProduct";
	  }
	  
	  @PostMapping("/edit")
	  public String updateProduct(Model model, @RequestParam int id, @Valid  @ModelAttribute ProductDto productDto, BindingResult result ) {
		 
		    try{
		    	Product product = repo.findById(id).get();
		    	 model.addAttribute("product",product);
		    	 
		    	 if(result.hasErrors()) {
		    		 return "product/EditProduct";
		    	 }
		    	 //delete old image
		    	 if(!productDto.getImageFile().isEmpty()) {
		    		 String uploadDir = "public/images/";
		    		 Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());
		    	 
		    	 
		    	 try {
		    		 Files.delete(oldImagePath);
		    	 }catch(Exception e) {
		    		 System.out.println("Exception:" + e.getMessage());
		    	 }
		    	 
		    	 //save new image files
		    		MultipartFile image = productDto.getImageFile();
		    		Date createdAt = new Date();
		    		String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

		    		
		    		 try (InputStream inputStream = image.getInputStream()) {
		    		  Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
		    		  StandardCopyOption.REPLACE_EXISTING);
		    		  
		    		  }
		    		    
		    		  product.setImageFileName(storageFileName);
		    		  
		    		  }
		    	   product.setName(productDto.getName());
				   product.setBrand(productDto.getBrand());
				   product.setCategory(productDto.getCategory());
				   product.setPrice(productDto.getPrice());
				   product.setDescription(productDto.getDescription());
				   repo.save(product);
		    }
		    	      catch (Exception e){ 
		    			  System.out.println("Exception: " + e.getMessage());
		    			  }
		    	 
		    	 
		 
		  
		  return "redirect:/products";
	  }
	  
	  
	 @GetMapping("/delete")
	 public String  deleteProduct(@RequestParam int id) {
		    
		 try {
	            Optional<Product> productOptional = repo.findById(id);
	            
	            // Print the Optional<Product>
	            System.out.println("Optional<Product>: " + productOptional);
	            
	            // Print the Product details if present
	            if (productOptional.isPresent()) {
	                Product product = productOptional.get();
	               // System.out.println("Product found: " + product);
	                repo.deleteById(id);
	                
	                
	            } else {
	                System.out.println("Product not found with id: " + id);
	                
	            }
	        } catch (Exception e) {
	            System.out.println("Exception: " + e.getMessage());
	        }

	        return "redirect:/products";
	    }
	}



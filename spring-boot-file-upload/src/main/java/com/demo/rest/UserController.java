package com.demo.rest;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.demo.dto.UserDTO;
import com.demo.model.Authority;
import com.demo.model.User;
import com.demo.service.AuthorityServiceInterface;
import com.demo.service.UserServiceInterface;


@RestController
@RequestMapping(value = "api/users")
@CrossOrigin("*")
public class UserController {

//	@Autowired
//	private UserServiceInterface userService;
	
//	@Autowired
//	private AuthorityServiceInterface authorityService;
	
	//@Autowired
	//PasswordEncoder passwordEncoder;
	
	
	
	@PostMapping(value="/register", consumes="application/json")
	public ResponseEntity<UserDTO> register(@RequestBody UserDTO userDTO) {
		//Authority authority = authorityService.findByName("REGULAR");
		
		//User u = userService.findByEmail(userDTO.getEmail());
		/*User u = new User();
		if(u!=null) {
			return new ResponseEntity<UserDTO>(HttpStatus.FORBIDDEN);
		}
		*/
		User u = new User();
		System.out.println(userDTO.getEmail());
		u.setEmail(userDTO.getEmail());
		u.setPassword(userDTO.getPassword());
		u.setActive(false);
		//u.getUser_authorities().add(authority);
		System.out.println("User u " + u.getEmail());
		//u = userService.save(u);
		return new ResponseEntity<UserDTO>(new UserDTO(u),HttpStatus.OK);
		
		
	
	//	return null;
	}
	
	
}

package ib.project.rest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.security.cert.Certificate;
import ib.project.dto.UserDTO;
import ib.project.model.Authority;
import ib.project.model.User;
import ib.project.service.AuthorityServiceInterface;
import ib.project.service.UserServiceInterface;


@RestController
@RequestMapping(value = "api/users")
@CrossOrigin("*")
public class UserController {
	

	private UserServiceInterface userService;
	private AuthorityServiceInterface authorityService;
	PasswordEncoder passwordEncoder;

	public UserController(UserServiceInterface userService, AuthorityServiceInterface authorityService, PasswordEncoder passwordEncoder){
		this.userService = userService;
		this.authorityService = authorityService;
		this.passwordEncoder = passwordEncoder;
	}



	@GetMapping
	public List<User> getAll() {
        return this.userService.findAll();
    }


	@GetMapping(value = "/inactive")
	public ResponseEntity<List<UserDTO>>getInactive(){
		List<UserDTO> inactive = new ArrayList<>();
		List<User> users = userService.findByActiveFalse();
		for (User user : users) {
			inactive.add(new UserDTO(user));
		}
		return new ResponseEntity<>(inactive,HttpStatus.OK);
	}


	@GetMapping(value = "/active")
	public ResponseEntity<List<UserDTO>> getActive(){
		List<UserDTO> active = new ArrayList<>();
		List<User> users = userService.findByActiveTrue();
		for (User user : users) {
			active.add(new UserDTO(user));
		}
		return new ResponseEntity<>(active,HttpStatus.OK);
	}
	
	
	
	@PostMapping(value="/register", consumes="application/json")
	public ResponseEntity<UserDTO> register(@RequestBody UserDTO userDTO) {
		Authority authority = authorityService.findByName("REGULAR");
		
		User u = userService.findByEmail(userDTO.getEmail());
		if(u!=null) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		
		
		u = new User();
		u.setEmail(userDTO.getEmail());
		u.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		u.setActive(false);
		u.getUser_authorities().add(authority);
		

		u = userService.save(u);
		return new ResponseEntity<>(new UserDTO(u),HttpStatus.OK);
	}

	@PutMapping(value="/activate/{id}")
	public ResponseEntity<UserDTO> activateUser(@PathVariable("id") Long id){
		User user = userService.findById(id);
		if(user == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		user.setActive(true);
		user = userService.save(user);
		return new ResponseEntity<>(new UserDTO(user),HttpStatus.OK);
	}


	@RequestMapping("/whoami")
	public User user(Principal user) {
		return this.userService.findByEmail(user.getName());
	}

}

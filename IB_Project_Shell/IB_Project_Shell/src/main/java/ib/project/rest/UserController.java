package ib.project.rest;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import ib.project.dto.UserDTO;
import ib.project.model.Authority;
import ib.project.model.User;
import ib.project.service.AuthorityServiceInterface;
import ib.project.service.UserServiceInterface;


@RestController
@RequestMapping(value = "api/users")
@CrossOrigin("*")
public class UserController {
	
	@Autowired
	private UserServiceInterface userService;
	
	@Autowired
	private AuthorityServiceInterface authorityService;

	@Autowired
	PasswordEncoder passwordEncoder;

	@GetMapping
	public List<User> getAll() {
        return this.userService.findAll();
    }


	@GetMapping(value="/inactive")
	public ResponseEntity<List<UserDTO>>getInactive(){
		List<UserDTO> inactive = new ArrayList<>();
		List<User> users = userService.findByActiveFalse();
		for (User user : users) {
			inactive.add(new UserDTO(user));
		}
		return new ResponseEntity<List<UserDTO>>(inactive,HttpStatus.OK);
	}


	@GetMapping(value="/active")
	public ResponseEntity<List<UserDTO>> getActive(){
		List<UserDTO> active = new ArrayList<>();
		List<User> users = userService.findByActiveTrue();
		for (User user : users) {
			active.add(new UserDTO(user));
		}
		return new ResponseEntity<List<UserDTO>>(active,HttpStatus.OK);
	}
	
	
	
	@PostMapping(value="/register", consumes="application/json")
	public ResponseEntity<UserDTO> register(@RequestBody UserDTO userDTO) {
		Authority authority = authorityService.findByName("REGULAR");
		
		User u = userService.findByEmail(userDTO.getEmail());
		if(u!=null) {
			return new ResponseEntity<UserDTO>(HttpStatus.FORBIDDEN);
		}
		
		
		u = new User();
		u.setEmail(userDTO.getEmail());
		u.setPassword(userDTO.getPassword());
		u.setActive(false);
		u.getUser_authorities().add(authority);
		

		u = userService.save(u);
		return new ResponseEntity<UserDTO>(new UserDTO(u),HttpStatus.OK);
	}

	@PutMapping(value="/activate/{id}")
	public ResponseEntity<UserDTO> activateUser(@PathVariable("id") Long id){
		User user = userService.findById(id);
		if(user == null) {
			return new ResponseEntity<UserDTO>(HttpStatus.NOT_FOUND);
		}
		user.setActive(true);
		user = userService.save(user);
		return new ResponseEntity<UserDTO>(new UserDTO(user),HttpStatus.OK);
	}


	@RequestMapping("/whoami")
	public User user(Principal user) {
		return this.userService.findByEmail(user.getName());
	}
}

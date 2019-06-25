package ib.project.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	

	@GetMapping
	public List<User> getAll() {
        return this.userService.findAll();
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
		//u.getUser_authorities().add(authority);
		

		u = userService.save(u);
		return new ResponseEntity<UserDTO>(new UserDTO(u),HttpStatus.OK);
	}
}

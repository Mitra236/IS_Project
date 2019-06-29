package ib.project.rest;

import ib.project.model.User;
import ib.project.model.UserTokenState;
import ib.project.security.JwtAuthenticationRequest;
import ib.project.security.TokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/auth")
@CrossOrigin("*")
public class AuthenticationController {

	@Autowired
	TokenHelper tokenHelper;

	@Autowired
	private AuthenticationManager authenticationManager;


	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(
			@RequestBody JwtAuthenticationRequest authenticationRequest
	) throws AuthenticationException {

		final Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						authenticationRequest.getUsername(),
						authenticationRequest.getPassword()
				)
		);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		User user = (User)authentication.getPrincipal();
		String jws = tokenHelper.generateToken( user.getUsername());

		return ResponseEntity.ok(new UserTokenState(jws));
	}


}

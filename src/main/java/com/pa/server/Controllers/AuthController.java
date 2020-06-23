package com.pa.server.Controllers;

import com.pa.server.Authentication.JwtResponse;
import com.pa.server.Authentication.LoginForm;
import com.pa.server.Authentication.SignUpForm;
import com.pa.server.JwtProvider;
import com.pa.server.Models.Role;
import com.pa.server.Models.RoleName;
import com.pa.server.Models.User;
import com.pa.server.Repositories.RoleRepository;
import com.pa.server.Repositories.UserRepository;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtProvider jwtProvider;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm login) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                login.getUsername(),
                login.getPassword()
        );
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateJwtToken(authentication);
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpForm signUp) {
        JSONObject response = new JSONObject();
        if (userRepository.existsByUsername(signUp.getUsername())) {
            response.put("message", "Fail -> Username is already taken!");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(signUp.getEmail())) {
            response.put("message", "Fail -> Email is already in use!");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Creating user's account
        User user = new User(signUp.getName(), signUp.getUsername(),
                signUp.getEmail(), encoder.encode(signUp.getPassword()));

        Set<String> strRoles = signUp.getRole();
        Set<Role> roles = new HashSet<>();

        strRoles.forEach(role -> {
            switch (role) {
                case "admin":
                    Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
                    roles.add(adminRole);

                    break;
                case "artist":
                    Role artistRole = roleRepository.findByName(RoleName.ROLE_ARTIST)
                            .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
                    roles.add(artistRole);

                    break;
                default:
                    Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
                    roles.add(userRole);
            }
        });

        user.setRoles(roles);
        userRepository.save(user);
        response.put("message", "User registered successfully!");
        response.put("user", user);

        return ResponseEntity.ok(response);
    }
}

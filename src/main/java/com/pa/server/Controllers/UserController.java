package com.pa.server.Controllers;

import com.pa.server.Models.User;
import com.pa.server.Repositories.UserRepository;
import com.pa.server.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    UserRepository userRepository;

    @GetMapping("")
    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Optional<User>> getUserById(@PathVariable long userId) {
        Optional<User> user = userRepository.findById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    public User updateUser(@PathVariable Long userId, @Valid @RequestBody User user) {
        return userRepository.findById(userId)
                .map(userFound -> {
                    //TODO: add other params update if it's not null
                    userFound.setName(user.getName());
                    return userRepository.save(userFound);
                }).orElseThrow(() -> new ResourceNotFoundException("user not found with id " + userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok().build();
                }).orElseThrow(() -> new ResourceNotFoundException("user not found with id " + userId));
    }
}

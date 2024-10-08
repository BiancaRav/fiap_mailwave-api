package com.mailwave.api.modules.users;

import com.mailwave.api.exceptions.UserNotFoundException;
import com.mailwave.api.modules.users.dtos.UserResponse;
import com.mailwave.api.modules.users.dtos.UserCreateRequest;
import com.mailwave.api.modules.users.dtos.UserUpdateRequest;
import com.mailwave.api.modules.users.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository repository;

    public UserService(UserRepository userRepository) {
        this.repository = userRepository;
    }

    public User getByEmail(String email) {
        var user = repository.getByEmail(email);
        if (user == null)
            throw new UserNotFoundException(email);


        return user;
    }

    public UserResponse create(UserCreateRequest model) {
        var user = new User(
                null,
                model.email(),
                new BCryptPasswordEncoder().encode(model.password()),
                false,
                UserRole.USER,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        return new UserResponse(repository.save(user));
    }

    public UserResponse update(UserUpdateRequest model) {
        var user = repository.findById(model.id()).orElseThrow(() -> new UserNotFoundException(model.id()));
        user.setEmail(model.email());
        user.setPasswordHash(new BCryptPasswordEncoder().encode(model.password()));
        user.setUpdatedAt(LocalDateTime.now());

        return new UserResponse(repository.save(user));
    }

    public UserResponse activate(Long id) {
        var user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(true);
        return new UserResponse(repository.save(user));
    }

    public UserResponse deactivate(Long id) {
        var user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(false);
        return new UserResponse(repository.save(user));
    }

    public User getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public UserResponse upgradePermission(Long id) {
        var user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setRole(UserRole.ADMIN);
        return new UserResponse(repository.save(user));
    }

    public UserResponse downgradePermission(Long id) {
        var user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setRole(UserRole.USER);
        return new UserResponse(repository.save(user));
    }

    public Page<UserResponse> getAll(Pageable page) {
        return repository.findAll(page).map(UserResponse::new);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByEmail(username);
    }

}
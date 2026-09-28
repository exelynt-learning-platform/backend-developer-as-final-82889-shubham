package com.booking.config;

import com.booking.entity.User;
import com.booking.enums.Role;
import com.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) {

		seedAdmin();
		seedUser();
	}

	private void seedAdmin() {

		if (userRepository.existsByEmail("admin@booking.com")) {
			return;
		}

		User admin = User.builder().name("System Admin").email("admin@booking.com")
				.password(passwordEncoder.encode("admin123")).role(Role.ADMIN).build();

		userRepository.save(admin);

		System.out.println("Default ADMIN user created.");
	}

	private void seedUser() {

		if (userRepository.existsByEmail("user@booking.com")) {
			return;
		}

		User user = User.builder().name("Test User").email("user@booking.com")
				.password(passwordEncoder.encode("user123")).role(Role.USER).build();

		userRepository.save(user);

		System.out.println("Default USER created.");
	}
}
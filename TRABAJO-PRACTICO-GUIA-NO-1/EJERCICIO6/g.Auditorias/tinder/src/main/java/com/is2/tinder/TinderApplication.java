package com.is2.tinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.is2.tinder.services.UsuarioService;

@SpringBootApplication
public class TinderApplication {
	private final UsuarioService usuarioService;

	public TinderApplication(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	public static void main(String[] args) {
		SpringApplication.run(TinderApplication.class, args);
	}

	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(usuarioService).passwordEncoder(new BCryptPasswordEncoder());
	}

}

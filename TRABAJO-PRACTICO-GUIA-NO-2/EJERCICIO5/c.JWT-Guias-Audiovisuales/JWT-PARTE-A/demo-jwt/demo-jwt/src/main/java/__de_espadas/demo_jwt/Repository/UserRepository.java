package __de_espadas.demo_jwt.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import __de_espadas.demo_jwt.User.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}

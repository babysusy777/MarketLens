package it.unipi.MarketLens.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.MarketLens.model.User;
import it.unipi.MarketLens.repository.mongo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.InputStream;
import java.util.List;

@Configuration
public class UserSeeder {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository) {
        return args -> {
            // Controllo se il DB user è già pieno
            long count = userRepository.count();
            if (count > 0) {
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = TypeReference.class.getResourceAsStream("/users.json");

            if (inputStream == null) return;

            try {
                List<User> users = mapper.readValue(inputStream, new TypeReference<List<User>>() {});
                // Encoder locale a bassa intensità (4) -->perchè sennò impiegava troppo tempo
                BCryptPasswordEncoder fastEncoder = new BCryptPasswordEncoder(4);

                int counter = 0;
                for (User user : users) {
                    user.setPassword(fastEncoder.encode(user.getPassword()));
                }

                userRepository.saveAll(users);

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}
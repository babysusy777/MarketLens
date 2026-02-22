package it.unipi.MarketLens.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;


    @JsonProperty("full_name")
    private String fullName;


    @Indexed(unique = true)
    private String email;


    @Indexed(unique = true)
    private String username;

    private String password;

    private UserRole role;

    private String brandId;
}
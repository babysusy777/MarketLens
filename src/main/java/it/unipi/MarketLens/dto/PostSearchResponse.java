package it.unipi.MarketLens.dto;

import it.unipi.MarketLens.model.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostSearchResponse {
    private List<Post> posts;
    private int totalPosts;
    private Double totalEngagement;
}
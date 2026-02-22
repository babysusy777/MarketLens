package it.unipi.MarketLens.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class HashtagTrendDTO {
    private String hashtag;
    private long numPosts;
    private double totalEng;
    private double avgSentiment;
}

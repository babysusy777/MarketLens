package it.unipi.MarketLens.dto;


import lombok.Data;


/*
 * DTO utilizzato per rappresentare il ranking dei brand.
 * I campi riflettono le metriche materializzate nella collection 'brands'
 * e il valore calcolato dell'engagement medio.
 */
@Data
public class BrandRankingDTO {
    private String name;
    private Long totalPosts;
    private Double totalEngagement;
    private Double avgSentiment;
    private Double engagementPerPost;


}
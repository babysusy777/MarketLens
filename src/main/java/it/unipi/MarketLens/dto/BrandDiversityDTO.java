package it.unipi.MarketLens.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandDiversityDTO {
    private String brand;
    private Long topicDiversity;
}
package it.unipi.MarketLens.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicExclusivityDTO {
    private String topic;
    private Long brandCount;
    private String brandName;
}
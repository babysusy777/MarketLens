package it.unipi.MarketLens.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicTrendDTO {
    private String topic;
    private Long CoOccurences;
    private Long totalFrequency;
}
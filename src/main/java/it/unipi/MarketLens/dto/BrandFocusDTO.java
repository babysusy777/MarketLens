package it.unipi.MarketLens.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandFocusDTO {
    private String topic;
    private Long occurrences;
}
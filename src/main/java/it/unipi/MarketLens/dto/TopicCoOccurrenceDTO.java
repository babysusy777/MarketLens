package it.unipi.MarketLens.dto;

public class TopicCoOccurrenceDTO {

    private String topic1;
    private String topic2;
    private Long strength;

    public TopicCoOccurrenceDTO() {}

    public TopicCoOccurrenceDTO(String topic1, String topic2, Long strength) {
        this.topic1 = topic1;
        this.topic2 = topic2;
        this.strength = strength;
    }

    public String getTopic1() {
        return topic1;
    }

    public void setTopic1(String topic1) {
        this.topic1 = topic1;
    }

    public String getTopic2() {
        return topic2;
    }

    public void setTopic2(String topic2) {
        this.topic2 = topic2;
    }

    public Long getStrength() {
        return strength;
    }

    public void setStrength(Long strength) {
        this.strength = strength;
    }
}
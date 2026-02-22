package it.unipi.MarketLens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class MarketLensApplication {

    private static final Logger log = LoggerFactory.getLogger(MarketLensApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MarketLensApplication.class, args);
        log.info("MarketLens application STARTED");
        }
}


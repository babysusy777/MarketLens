package it.unipi.MarketLens.config;
import it.unipi.MarketLens.service.PostIngestionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//ingestion iniziale dei primi post tutti insieme
@Configuration
public class DataIngestionRunner {

    private static final int BOOTSTRAP_MAX = 20000;
    //Viene eseguito una sola volta all'avvio dell'applicazione

    @Bean
    CommandLineRunner runIngestion(PostIngestionService ingestionService) {
        return args -> {
            System.out.println("Starting BOOTSTRAP post ingestion...");
            //se riavviamo l'app, non duplichiamo i dati
            ingestionService.bootstrapIngestIfNeeded(BOOTSTRAP_MAX);
            System.out.println("BOOTSTRAP ingestion finished.");

        };
    }
}

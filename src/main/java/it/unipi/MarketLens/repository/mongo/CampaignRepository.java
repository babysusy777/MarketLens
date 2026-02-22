package it.unipi.MarketLens.repository.mongo;

import it.unipi.MarketLens.model.Campaign;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends MongoRepository<Campaign, String> {

    List<Campaign> findByAnalystUsername(String analystUsername);

    Optional<Campaign> findByNameAndAnalystUsername(String name, String analystUsername);

    boolean existsByNameAndAnalystUsername(String name, String analystUsername);


}

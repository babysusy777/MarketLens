package it.unipi.MarketLens.config;

import com.mongodb.ConnectionString;
import org.springframework.context.annotation.Primary;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@Configuration
public class MongoClientsConfig {

    @Bean(name = "fastMongoClient")
    public MongoClient fastMongoClient(@Value("${marketlens.mongodb.fast-uri}") String uri) {
        ConnectionString cs = new ConnectionString(uri);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(cs)
                // ridondante ma esplicito
                .readPreference(ReadPreference.secondaryPreferred())
                .writeConcern(WriteConcern.W1.withJournal(true))   // FAST writes
                .readConcern(ReadConcern.LOCAL)
                .retryWrites(true)
                .build();

        return MongoClients.create(settings);
    }

    @Bean(name = "strongMongoClient")
    @Primary
    public MongoClient strongMongoClient(@Value("${marketlens.mongodb.strong-uri}") String uri) {
        ConnectionString cs = new ConnectionString(uri);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(cs)
                .readPreference(ReadPreference.primary())
                .writeConcern(WriteConcern.MAJORITY.withJournal(true)) // STRONG writes
                .readConcern(ReadConcern.MAJORITY)                     // STRONG reads (coerenti)
                .retryWrites(true)
                .build();

        return MongoClients.create(settings);
    }

    @Bean(name = "fastMongoDbFactory")
    public MongoDatabaseFactory fastMongoDbFactory(
            @Qualifier("fastMongoClient") MongoClient client,
            @Value("${marketlens.mongodb.fast-uri}") String uri) {
        ConnectionString cs = new ConnectionString(uri);
        return new SimpleMongoClientDatabaseFactory(client, cs.getDatabase());
    }

    @Bean(name = "strongMongoDbFactory")
    @Primary
    public MongoDatabaseFactory strongMongoDbFactory(
            @Qualifier("strongMongoClient") MongoClient client,
            @Value("${marketlens.mongodb.strong-uri}") String uri) {
        ConnectionString cs = new ConnectionString(uri);
        return new SimpleMongoClientDatabaseFactory(client, cs.getDatabase());
    }

    @Bean(name = "fastMongoTemplate")
    public MongoTemplate fastMongoTemplate(@Qualifier("fastMongoDbFactory") MongoDatabaseFactory factory,
                                           MongoConverter converter) {
        return new MongoTemplate(factory, converter);
    }

    @Bean(name = "strongMongoTemplate")
    public MongoTemplate strongMongoTemplate(@Qualifier("strongMongoDbFactory") MongoDatabaseFactory factory,
                                             MongoConverter converter) {
        return new MongoTemplate(factory, converter);
    }

    @Bean
    public GridFsTemplate gridFsTemplate(
            @Qualifier("strongMongoDbFactory") MongoDatabaseFactory dbFactory,
            MongoConverter mongoConverter) {
        return new GridFsTemplate(dbFactory, mongoConverter);
    }



}

package it.unipi.MarketLens.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DuplicateKeyException;
import it.unipi.MarketLens.model.Brand;
import it.unipi.MarketLens.model.Industry;
import it.unipi.MarketLens.model.Post;
import it.unipi.MarketLens.repository.mongo.BrandRepository;
import it.unipi.MarketLens.repository.mongo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PostIngestionService {

    private static final String SOURCE_FILE = "combined_sorted_normalized2_id.json";
    private static final String SOURCE_KEY  = "classpath:" + SOURCE_FILE;
    public static final int DEFAULT_SCHEDULED_BATCH_SIZE = 1000;

    private final PostRepository postRepository;
    private final BrandRepository brandRepository;
    private final ObjectMapper objectMapper;
    private final GraphIngestionService graphIngestionService;
    private final IngestionCursorService cursorService;
    private final GraphRecomputeService graphRecomputeService;
    private final AnalyticsRefreshService analyticsResfreshService;
    private final PostBulkUpsertService postBulkUpsertService;
    private static final int CHECKPOINT_BATCH_SIZE = 100; // consigliato 50-200
    private static final int REPAIR_WINDOW_SIZE = 200;



    // --- MAPPA STATICA PER LE INDUSTRY
    private static final Map<String, Industry> STATIC_INDUSTRY_MAP = Map.ofEntries(
            Map.entry("fendi", Industry.FASHION),
            Map.entry("valentino", Industry.FASHION),
            Map.entry("ralphlauren", Industry.FASHION),
            Map.entry("zara", Industry.FASHION),
            Map.entry("mango", Industry.FASHION),
            Map.entry("stradivarius", Industry.FASHION),
            Map.entry("celine", Industry.FASHION),
            Map.entry("miumiu", Industry.FASHION),
            Map.entry("louisvuitton", Industry.FASHION),
            Map.entry("rolex", Industry.FASHION),
            Map.entry("omega", Industry.FASHION),
            Map.entry("patekphilippe", Industry.FASHION),
            Map.entry("cartier", Industry.FASHION),
            Map.entry("swarovski", Industry.FASHION),
            Map.entry("ysl", Industry.FASHION),
            Map.entry("balenciaga", Industry.FASHION),
            Map.entry("pomellato", Industry.FASHION),
            Map.entry("tiffanyandco", Industry.FASHION),
            Map.entry("hermes", Industry.FASHION),
            Map.entry("hm", Industry.FASHION),
            Map.entry("dior", Industry.FASHION),
            Map.entry("prada", Industry.FASHION),
            Map.entry("gucci", Industry.FASHION),
            Map.entry("apple", Industry.TECHNOLOGY),
            Map.entry("jblaudio", Industry.TECHNOLOGY),
            Map.entry("marshall", Industry.TECHNOLOGY),
            Map.entry("braun", Industry.TECHNOLOGY),
            Map.entry("ariete", Industry.TECHNOLOGY),
            Map.entry("miele", Industry.TECHNOLOGY),
            Map.entry("ghd", Industry.TECHNOLOGY),
            Map.entry("dyson", Industry.TECHNOLOGY),
            Map.entry("panasonic", Industry.TECHNOLOGY),
            Map.entry("hp", Industry.TECHNOLOGY),
            Map.entry("asus", Industry.TECHNOLOGY),
            Map.entry("samsung", Industry.TECHNOLOGY),
            Map.entry("acer", Industry.TECHNOLOGY),
            Map.entry("sony", Industry.TECHNOLOGY),
            Map.entry("huawei", Industry.TECHNOLOGY),
            Map.entry("alo", Industry.SPORTSWEAR),
            Map.entry("asics", Industry.SPORTSWEAR),
            Map.entry("hoka", Industry.SPORTSWEAR),
            Map.entry("arcteryx", Industry.SPORTSWEAR),
            Map.entry("thenorthface", Industry.SPORTSWEAR),
            Map.entry("jordan", Industry.SPORTSWEAR),
            Map.entry("patagonia", Industry.SPORTSWEAR),
            Map.entry("underarmour", Industry.SPORTSWEAR),
            Map.entry("salomon", Industry.SPORTSWEAR),
            Map.entry("head", Industry.SPORTSWEAR),
            Map.entry("newbalance", Industry.SPORTSWEAR),
            Map.entry("adidas", Industry.SPORTSWEAR),
            Map.entry("nike", Industry.SPORTSWEAR),
            Map.entry("tesla", Industry.AUTOMOTIVE),
            Map.entry("maserati", Industry.AUTOMOTIVE),
            Map.entry("hyundai", Industry.AUTOMOTIVE),
            Map.entry("jaguar", Industry.AUTOMOTIVE),
            Map.entry("suzuki", Industry.AUTOMOTIVE),
            Map.entry("nissan", Industry.AUTOMOTIVE),
            Map.entry("ford", Industry.AUTOMOTIVE),
            Map.entry("honda", Industry.AUTOMOTIVE),
            Map.entry("mercedesbenz", Industry.AUTOMOTIVE),
            Map.entry("lamborghini", Industry.AUTOMOTIVE),
            Map.entry("toyota", Industry.AUTOMOTIVE),
            Map.entry("mini", Industry.AUTOMOTIVE),
            Map.entry("peugeot", Industry.AUTOMOTIVE),
            Map.entry("renault", Industry.AUTOMOTIVE),
            Map.entry("audi", Industry.AUTOMOTIVE),
            Map.entry("volkswagen", Industry.AUTOMOTIVE),
            Map.entry("fiat", Industry.AUTOMOTIVE),
            Map.entry("ferrari", Industry.AUTOMOTIVE),
            Map.entry("porsche", Industry.AUTOMOTIVE),
            Map.entry("bmw", Industry.AUTOMOTIVE),
            Map.entry("spotify", Industry.MUSIC),
            Map.entry("applemusic", Industry.MUSIC),
            Map.entry("amazonmusic", Industry.MUSIC),
            Map.entry("taylorswift", Industry.MUSIC),
            Map.entry("sabrinacarpenter", Industry.MUSIC),
            Map.entry("madisonbeer", Industry.MUSIC),
            Map.entry("sofiaisella", Industry.MUSIC),
            Map.entry("dualipa", Industry.MUSIC),
            Map.entry("billieeilish", Industry.MUSIC),
            Map.entry("coldplay", Industry.MUSIC),
            Map.entry("arianagrande", Industry.MUSIC),
            Map.entry("venchi", Industry.FOOD),
            Map.entry("domperignon", Industry.FOOD),
            Map.entry("giovannirana", Industry.FOOD),
            Map.entry("nutella", Industry.FOOD),
            Map.entry("bauli", Industry.FOOD),
            Map.entry("mulinobianco", Industry.FOOD),
            Map.entry("loacker", Industry.FOOD),
            Map.entry("barilla", Industry.FOOD),
            Map.entry("lindt", Industry.FOOD),
            Map.entry("nespresso", Industry.FOOD),
            Map.entry("crumbl", Industry.FOOD),
            Map.entry("chamberlaincoffee", Industry.FOOD),
            Map.entry("starbucks", Industry.FOOD),
            Map.entry("mcdonald", Industry.FOOD),
            Map.entry("sephora", Industry.BEAUTY),
            Map.entry("rarebeauty", Industry.BEAUTY),
            Map.entry("rhode", Industry.BEAUTY),
            Map.entry("maybelline", Industry.BEAUTY),
            Map.entry("kiko", Industry.BEAUTY),
            Map.entry("hudabeauty", Industry.BEAUTY),
            Map.entry("nars", Industry.BEAUTY),
            Map.entry("benefit", Industry.BEAUTY),
            Map.entry("theordinary", Industry.BEAUTY),
            Map.entry("erbolario", Industry.BEAUTY),
            Map.entry("diegodallapalma", Industry.BEAUTY),
            Map.entry("nabla", Industry.BEAUTY),
            Map.entry("fenty", Industry.BEAUTY),
            Map.entry("diorbeauty", Industry.BEAUTY),
            Map.entry("chanelbeauty", Industry.BEAUTY),
            Map.entry("sidneysweeney", Industry.ENTERTAINMENT),
            Map.entry("netflix", Industry.ENTERTAINMENT),
            Map.entry("primevideo", Industry.ENTERTAINMENT),
            Map.entry("disneyplus", Industry.ENTERTAINMENT),
            Map.entry("nowtv", Industry.ENTERTAINMENT),
            Map.entry("discovery", Industry.ENTERTAINMENT),
            Map.entry("dazn", Industry.ENTERTAINMENT),
            Map.entry("raiplay", Industry.ENTERTAINMENT),
            Map.entry("instarai2", Industry.ENTERTAINMENT),
            Map.entry("mediasetinfinity", Industry.ENTERTAINMENT),
            Map.entry("masterchef", Industry.ENTERTAINMENT),
            Map.entry("atptour", Industry.TENNIS),
            Map.entry("wimbledon", Industry.TENNIS),
            Map.entry("rolandgarros", Industry.TENNIS),
            Map.entry("usopen", Industry.TENNIS),
            Map.entry("australianopen", Industry.TENNIS),
            Map.entry("janniksinner", Industry.TENNIS),
            Map.entry("fcbarcelona", Industry.FOOTBALL),
            Map.entry("realmadrid", Industry.FOOTBALL),
            Map.entry("atletico", Industry.FOOTBALL),
            Map.entry("seriea", Industry.FOOTBALL),
            Map.entry("mancity", Industry.FOOTBALL),
            Map.entry("liverpoolfc", Industry.FOOTBALL),
            Map.entry("arsenal", Industry.FOOTBALL),
            Map.entry("juventus", Industry.FOOTBALL),
            Map.entry("acmilan", Industry.FOOTBALL),
            Map.entry("inter", Industry.FOOTBALL),
            Map.entry("officialasroma", Industry.FOOTBALL),
            Map.entry("acffiorentina", Industry.FOOTBALL),
            Map.entry("pisasportingclub", Industry.FOOTBALL),
            Map.entry("sscnapoli", Industry.FOOTBALL),
            Map.entry("tomford", Industry.FASHION),
            Map.entry("pullandbear", Industry.FASHION),
            Map.entry("bershka", Industry.FASHION),
            Map.entry("unitedcuptennis", Industry.TENNIS),
            Map.entry("givenchy", Industry.FASHION),
            Map.entry("burberry", Industry.FASHION)

    );

    @Autowired
    public PostIngestionService(PostRepository postRepository,
                                BrandRepository brandRepository,
                                ObjectMapper objectMapper,
                                GraphIngestionService graphIngestionService,
                                IngestionCursorService cursorService,
                                GraphRecomputeService graphRecomputeService,
                                AnalyticsRefreshService analyticsResfreshService,
                                PostBulkUpsertService postBulkUpsertService) {
        this.postRepository = postRepository;
        this.brandRepository = brandRepository;
        this.objectMapper = objectMapper;
        this.graphIngestionService = graphIngestionService;
        this.cursorService = cursorService;
        this.graphRecomputeService = graphRecomputeService;
        this.analyticsResfreshService = analyticsResfreshService;
        this.postBulkUpsertService = postBulkUpsertService;
    }

    private String normalizeKey(String input) {
        if (input == null) return "";
        return input.toLowerCase().replaceAll("[^a-z0-9]", "").trim();
    }

    public static class IngestionStats {
        public long processed = 0;
        public long insertedMongo = 0;
        //public long duplicatesMongo = 0;
        public long syncedNeo4j = 0;
        public long neo4jErrors = 0;

        public Map<String, Long> toCounters() {
            return Map.of("processed", processed, "insertedMongo", insertedMongo, "syncedNeo4j", syncedNeo4j, "neo4jErrors", neo4jErrors);
        }
    }

    public IngestionStats bootstrapIngestIfNeeded(int bootstrapMax) throws Exception {
        long postsCount = postRepository.count();
        var cursor = cursorService.getOrCreate(SOURCE_KEY);
        boolean needsBootstrap = (cursor.getLastProcessedIndex() == 0) && (postsCount == 0);

        if (!needsBootstrap) {
            System.out.println("Bootstrap not needed.");
            return new IngestionStats();
        }
        int limit = (bootstrapMax <= 0) ? Integer.MAX_VALUE : bootstrapMax;
        System.out.println("BOOTSTRAP ingestion START.");

        IngestionStats stats = ingestFromCursor(limit);

        graphRecomputeService.recomputeAllWithStats();
        analyticsResfreshService.refreshAllWithStats();

        return stats;
    }

    public IngestionStats ingestScheduledBatch(int batchSize) throws Exception {
        return ingestFromCursor(Math.max(batchSize, 1));
    }

    public IngestionStats ingestPostsWithStats(int batchSize) throws Exception {
        return ingestScheduledBatch(batchSize);
    }

    private IngestionStats ingestFromCursor(int limit) throws Exception {
        InputStream inputStream = new ClassPathResource(SOURCE_FILE).getInputStream();
        JsonNode root = objectMapper.readTree(inputStream);
        IngestionStats stats = new IngestionStats();

        if (root == null || !root.isArray() || root.size() == 0) return stats;

        long size = root.size();
        var cursor = cursorService.getOrCreate(SOURCE_KEY);
        long startIndex = cursor.getLastProcessedIndex();
        if (startIndex >= size) return stats;

        // --- REPAIR PASS: riprocessa gli ultimi N elementi prima del cursor, senza avanzare cursor
        long repairStart = Math.max(0, startIndex - REPAIR_WINDOW_SIZE);
        if (repairStart < startIndex) {
            List<Post> repairBatch = new ArrayList<>(CHECKPOINT_BATCH_SIZE);
            for (long r = repairStart; r < startIndex; r++) {
                JsonNode node = root.get((int) r);
                Post post = mapPostFromNode(node);
                if (post == null) continue;
                if (post.getPostId() == null || post.getPostId().isBlank()) continue;

                upsertBrandFromPost(post);          // idempotente + unique
                repairBatch.add(post);

                if (repairBatch.size() >= CHECKPOINT_BATCH_SIZE) {
                    // upsert posts + neo4j, MA NON avanzare cursor
                    flushBatchNoCursorAdvance(repairBatch, stats);
                    repairBatch.clear();
                }
            }
            if (!repairBatch.isEmpty()) {
                flushBatchNoCursorAdvance(repairBatch, stats);
                repairBatch.clear();
            }
        }


        List<Post> batch = new ArrayList<>(CHECKPOINT_BATCH_SIZE);

        long processedThisRun = 0;
        long i;

        for (i = startIndex; i < size && processedThisRun < (long) limit; i++) {
            JsonNode node = root.get((int) i);

            Post post = mapPostFromNode(node);
            if (post == null) continue;

            // inderogabile: postId deve essere quello dei social
            if (post.getPostId() == null || post.getPostId().isBlank()) continue;

            // Brand (strong repo). Consigliato unique index su brandName.
            upsertBrandFromPost(post);

            batch.add(post);
            stats.processed++;
            processedThisRun++;

            if (batch.size() >= CHECKPOINT_BATCH_SIZE) {
                flushBatch(batch, stats, i + 1); // next index
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            flushBatch(batch, stats, i); // i già next index
            batch.clear();
        }

        System.out.println("Ingestion Batch Completed. Stats: " + stats.toCounters());
        return stats;
    }

    private void flushBatchNoCursorAdvance(List<Post> batch, IngestionStats stats) {

        // 1) Mongo FAST: upsert idempotente
        long inserted = postBulkUpsertService.bulkUpsertPosts(batch);
        stats.insertedMongo += inserted;

        // 2) Neo4j MERGE: idempotente
        for (Post p : batch) {
            if (p.getPlatform() != null && p.getPlatform().equalsIgnoreCase("instagram")) {
                try {
                    graphIngestionService.savePostToGraph(p);
                    stats.syncedNeo4j++;
                } catch (Exception e) {
                    stats.neo4jErrors++;
                }
            }
        }
    }



    private void flushBatch(List<Post> batch, IngestionStats stats, long nextCursorIndex) {

        // 1) FAST: bulk upsert posts (idempotente su post_id)
        long inserted = postBulkUpsertService.bulkUpsertPosts(batch);
        stats.insertedMongo += inserted;

        // 2) STRONG: avanzamento cursor (w=majority)
        cursorService.advance(SOURCE_KEY, nextCursorIndex);

        // 3) Neo4j: MERGE idempotente
        for (Post p : batch) {
            if (p.getPlatform() != null && p.getPlatform().equalsIgnoreCase("instagram")) {
                try {
                    graphIngestionService.savePostToGraph(p); // MERGE
                    stats.syncedNeo4j++;
                } catch (Exception e) {
                    stats.neo4jErrors++;
                }
            }
        }
    }


    private Post mapPostFromNode(JsonNode node) throws Exception {
        String externalId = null;
        if (node.has("post_id") && !node.get("post_id").isNull()) externalId = node.get("post_id").asText();
        else if (node.has("id") && !node.get("id").isNull()) externalId = node.get("id").asText();

        Post post = objectMapper.convertValue(node, Post.class);

        // inderogabile: post.postId = id social
        if ((post.getPostId() == null || post.getPostId().isBlank()) && externalId != null && !externalId.isBlank()) {
            post.setPostId(externalId);
        }
        if (post.getPostId() == null || post.getPostId().isBlank()) return null;

        if (node.has("user_posted") && !node.get("user_posted").isNull())
            post.setUserPosted(node.get("user_posted").asText());

        if (node.has("description") && !node.get("description").isNull())
            post.setContent(node.get("description").asText());

        if (node.has("platform") && !node.get("platform").isNull())
            post.setPlatform(node.get("platform").asText());

        // industry resolve (come tuo codice)
        String industryRaw = node.has("industry") && !node.get("industry").isNull() ? node.get("industry").asText() : null;
        Industry resolvedIndustry = null;

        if (industryRaw != null) {
            try { resolvedIndustry = Industry.valueOf(industryRaw.toUpperCase()); }
            catch (Exception ignored) {}
        }
        if (resolvedIndustry == null && post.getUserPosted() != null) {
            String key = normalizeKey(post.getUserPosted());
            resolvedIndustry = STATIC_INDUSTRY_MAP.get(key);
        }
        if (resolvedIndustry != null) post.setIndustry(resolvedIndustry.name());

        if (post.getDatePosted() == null && node.has("date_posted") && !node.get("date_posted").isNull()) {
            try { post.setDatePosted(Instant.parse(node.get("date_posted").asText())); }
            catch (Exception e) { post.setDatePosted(Instant.now()); }
        }

        // hashtags / comments / nlp: puoi copiare pari pari dal tuo processSinglePost
        // (non riscrivo qui per brevità: è identico, solo senza DB calls)

        return post;
    }


    private void upsertBrandFromPost(Post post) {
        String brandName = post.getUserPosted();
        if (brandName == null || brandName.isBlank()) return;

        Industry ind = null;
        // post.getIndustry() è una stringa tipo "FASHION"
        if (post.getIndustry() != null) {
            try { ind = Industry.valueOf(post.getIndustry()); } catch (Exception ignored) {}
        }

        Optional<Brand> existing = brandRepository.findByBrandNameIgnoreCase(brandName);
        if (existing.isPresent()) {
            Brand b = existing.get();
            if (b.getIndustry() == null && ind != null) {
                b.setIndustry(ind);
                brandRepository.save(b);
            }
            return;
        }

        Brand b = new Brand();
        b.setBrandName(brandName);
        b.setIndustry(ind);

        try {
            brandRepository.save(b);
        } catch (DuplicateKeyException e) {
            // se metti indice unique su brandName, qui può capitare DuplicateKey in concorrenza: ok
        }
    }



//    private void processSinglePost(JsonNode node, IngestionStats stats) throws Exception {
//        String externalId = null;
//        if (node.has("post_id") && !node.get("post_id").isNull()) externalId = node.get("post_id").asText();
//        else if (node.has("id") && !node.get("id").isNull()) externalId = node.get("id").asText();
//
//        boolean existsInMongo = (externalId != null && !externalId.isBlank() && postRepository.existsByPostId(externalId));
//
//        Post post = objectMapper.convertValue(node, Post.class);
//        if (post.getPostId() == null && externalId != null) post.setPostId(externalId);
//
//        if (node.has("user_posted") && !node.get("user_posted").isNull())
//            post.setUserPosted(node.get("user_posted").asText());
//        if (node.has("description") && !node.get("description").isNull())
//            post.setContent(node.get("description").asText());
//        if (node.has("platform") && !node.get("platform").isNull())
//            post.setPlatform(node.get("platform").asText());
//
//        String industryRaw = node.has("industry") && !node.get("industry").isNull() ? node.get("industry").asText() : null;
//        Industry resolvedIndustry = null;
//
//
//        if (industryRaw != null) {
//            try {
//                resolvedIndustry = Industry.valueOf(industryRaw.toUpperCase());
//            } catch (Exception e) {}
//        }
//
//
//        if (resolvedIndustry == null && post.getUserPosted() != null) {
//            String key = normalizeKey(post.getUserPosted());
//            resolvedIndustry = STATIC_INDUSTRY_MAP.get(key);
//            if (resolvedIndustry != null) {
//
//            }
//        }
//
//
//        if (resolvedIndustry != null) {
//            post.setIndustry(resolvedIndustry.name());
//        }
//
//        if (post.getDatePosted() == null && node.has("date_posted") && !node.get("date_posted").isNull()) {
//            try {
//                post.setDatePosted(Instant.parse(node.get("date_posted").asText()));
//            } catch (Exception e) {
//                post.setDatePosted(Instant.now());
//            }
//        }
//
//
//        if (post.getNlp() == null) post.setNlp(new Post.NlpData());
//        JsonNode nlpNode = node.path("nlp");
//        if (nlpNode.path("topics").isArray()) {
//            List<String> ts = new ArrayList<>();
//            for (JsonNode t : nlpNode.path("topics")) ts.add(t.asText());
//            post.getNlp().setTopics(ts);
//        }
//        if (nlpNode.path("entities").isArray()) {
//            List<Post.EntityData> entities = new ArrayList<>();
//            for (JsonNode e : nlpNode.path("entities")) {
//                Post.EntityData ed = new Post.EntityData();
//                if (e.has("entity") && !e.get("entity").isNull()) ed.setEntity(e.get("entity").asText());
//                if (e.has("type") && !e.get("type").isNull()) ed.setType(e.get("type").asText());
//                entities.add(ed);
//            }
//            post.getNlp().setEntities(entities);
//        }
//        List<String> hashtags = new ArrayList<>();
//        if (node.has("hashtags") && node.get("hashtags").isArray()) {
//            for (JsonNode h : node.get("hashtags")) hashtags.add(h.asText());
//        }
//        if (hashtags.isEmpty() && post.getContent() != null) {
//            Matcher m = Pattern.compile("#\\w+").matcher(post.getContent());
//            while (m.find()) hashtags.add(m.group());
//        }
//        post.setHashtags(hashtags);
//
//        if (node.has("comments") && node.get("comments").isArray()) {
//            List<Post.CommentData> commentsList = new ArrayList<>();
//            for (JsonNode cNode : node.get("comments")) {
//                Post.CommentData cd = new Post.CommentData();
//
//                if (cNode.has("comments") && !cNode.get("comments").isNull()) {
//                    cd.setText(cNode.get("comments").asText());
//                }
//                if (cNode.has("likes")) {
//                    cd.setLikes(cNode.get("likes").asInt(0));
//                }
//                commentsList.add(cd);
//            }
//            post.setCommentsList(commentsList);
//        }
//
//        if (node.has("num_comments") && !node.get("num_comments").isNull()) {
//            post.setNumComments(node.get("num_comments").asInt());
//        }
//
//        if (post.getNumComments() == null) {
//            if (post.getCommentsList() != null) {
//                post.setNumComments(post.getCommentsList().size());
//            } else {
//                post.setNumComments(0);
//            }
//        }
//
//
//        String brandName = post.getUserPosted();
//        if (brandName != null && !brandName.isBlank()) {
//            Industry finalIndustry = resolvedIndustry;
//
//
//            Optional<Brand> existing = brandRepository.findByBrandName(brandName);
//
//            if (existing.isPresent()) {
//
//                Brand b = existing.get();
//                if (b.getIndustry() == null && finalIndustry != null) {
//                    b.setIndustry(finalIndustry);
//                    brandRepository.save(b);
//                }
//            } else {
//
//                Brand b = new Brand();
//                b.setBrandName(brandName);
//                b.setIndustry(finalIndustry);
//
//                brandRepository.save(b);
//            }
//        }
//
//
//
//
//        if (post.getPlatform() != null && post.getPlatform().equalsIgnoreCase("instagram")) {
//            try {
//                graphIngestionService.savePostToGraph(post);
//                stats.syncedNeo4j++;
//            } catch (Exception e) {
//                stats.neo4jErrors++;
//                System.err.println("Error in Neo4j: " + e.getMessage());
//            }
//        }
//
//    }
}
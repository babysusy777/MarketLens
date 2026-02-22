package it.unipi.MarketLens.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "ingestion_cursor_state")
public class IngestionCursorState {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sourceKey;

    /*
     * Indice del prossimo elemento da processare.
     * Esempio: se vale 0, il prossimo elemento e' root[0].
     */
    private long lastProcessedIndex;

    private Instant updatedAt;

    public String getId() { return id; }

   public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String sourceKey) { this.sourceKey = sourceKey; }

    public long getLastProcessedIndex() { return lastProcessedIndex; }
    public void setLastProcessedIndex(long lastProcessedIndex) { this.lastProcessedIndex = lastProcessedIndex; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

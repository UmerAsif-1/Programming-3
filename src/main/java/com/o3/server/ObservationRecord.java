package com.o3.server;


import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class ObservationRecord {
    private String recordIdentifier;
    private String recordDescription;
    private String recordPayload;
    private String recordRightAscension;
    private String recordDeclination;
    private ZonedDateTime sent; 

    public ObservationRecord(String recordIdentifier, String recordDescription, String recordPayload, String recordRightAscension, String recordDeclination) {
        this.recordIdentifier = recordIdentifier;
        this.recordDescription = recordDescription;
        this.recordPayload = recordPayload;
        this.recordRightAscension = recordRightAscension;
        this.recordDeclination = recordDeclination;
        this.sent = ZonedDateTime.now(ZoneOffset.UTC);
    }

    // Getters (and setters if you need them later)
    public String getRecordIdentifier() { return recordIdentifier; }
    public String getRecordDescription() { return recordDescription; }
    public String getRecordPayload() { return recordPayload; }
    public String getRecordRightAscension() { return recordRightAscension; }
    public String getRecordDeclination() { return recordDeclination; }
    public ZonedDateTime getSent() { return sent; } // Getter for sent

    // Setters
    public void setRecordIdentifier(String recordIdentifier) { this.recordIdentifier = recordIdentifier; }
    public void setRecordDescription(String recordDescription) { this.recordDescription = recordDescription; }
    public void setRecordPayload(String recordPayload) { this.recordPayload = recordPayload; }
    public void setRecordRightAscension(String recordRightAscension) { this.recordRightAscension = recordRightAscension; }
    public void setRecordDeclination(String recordDeclination) { this.recordDeclination = recordDeclination; }
    public void setSent(ZonedDateTime sent) { this.sent = sent; } // Setter for sent

    // Helper functions for timestamp conversion
    public long dateAsInt() {
        return sent.toInstant().toEpochMilli(); // Convert to Unix time (milliseconds since epoch)
    }

    public void setSent(long epoch) {
        sent = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC); // Convert Unix time to ZonedDateTime
    }
}

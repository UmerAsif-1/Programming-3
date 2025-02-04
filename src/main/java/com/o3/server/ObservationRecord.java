package com.o3.server;

public class ObservationRecord {
    private String recordIdentifier;
    private String recordDescription;
    private String recordPayload;
    private String recordRightAscension;
    private String recordDeclination;

    public ObservationRecord(String recordIdentifier, String recordDescription, String recordPayload, 
                             String recordRightAscension, String recordDeclination) {
        this.recordIdentifier = recordIdentifier;
        this.recordDescription = recordDescription;
        this.recordPayload = recordPayload;
        this.recordRightAscension = recordRightAscension;
        this.recordDeclination = recordDeclination;
    }

    // Getters and setters
    public String getRecordIdentifier() { return recordIdentifier; }
    public void setRecordIdentifier(String recordIdentifier) { this.recordIdentifier = recordIdentifier; }
    public String getRecordDescription() { return recordDescription; }
    public void setRecordDescription(String recordDescription) { this.recordDescription = recordDescription; }
    public String getRecordPayload() { return recordPayload; }
    public void setRecordPayload(String recordPayload) { this.recordPayload = recordPayload; }
    public String getRecordRightAscension() { return recordRightAscension; }
    public void setRecordRightAscension(String recordRightAscension) { this.recordRightAscension = recordRightAscension; }
    public String getRecordDeclination() { return recordDeclination; }
    public void setRecordDeclination(String recordDeclination) { this.recordDeclination = recordDeclination; }
}

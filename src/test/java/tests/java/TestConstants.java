package tests.java;

import org.apache.commons.lang3.tuple.Pair;
import root.dto.ProcessedDataMessage;
import root.model.DataForProcessing;
import root.model.ProcessedData;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class TestConstants {

    public static final String DATA_FOR_PROCESSING_AS_STRING = "data-for-processing";
    public static final String ADDITIONAL_DATA_ID = "additional-data-id";
    public static final String ADDITIONAL_DATA = "additional-data";
    public static final DataForProcessing DATA_FOR_PROCESSING = new DataForProcessing(DATA_FOR_PROCESSING_AS_STRING, ADDITIONAL_DATA_ID);
    public static final String PROCESSED_DATA_ID = "processed-data-id";
    public static final String PROCESSED_DATA_AS_STRING = "DATA-FOR-PROCESSING | ADDITIONAL-DATA";
    public static final Instant PROCESSING_TIMESTAMP = Instant.ofEpochMilli(1655797427000L);
    public static final ProcessedData PROCESSED_DATA = ProcessedData.builder()
            .id(PROCESSED_DATA_ID)
            .dataForProcessing(DATA_FOR_PROCESSING_AS_STRING)
            .additionalDataId(ADDITIONAL_DATA_ID)
            .processingResult(PROCESSED_DATA_AS_STRING)
            .processingTimestamp(PROCESSING_TIMESTAMP)
            .build();
    public static final ProcessedDataMessage PROCESSED_DATA_MESSAGE_VALUE = new ProcessedDataMessage(PROCESSED_DATA_AS_STRING);
    public static final String PROCESSED_DATA_MESSAGE_VALUE_AS_JSON = """
        {
            "data": "%s"
        }
    """.formatted(PROCESSED_DATA_AS_STRING);
    public static final Pair<String, String> PROCESSED_DATA_MESSAGE = Pair.of(PROCESSED_DATA_ID, PROCESSED_DATA_MESSAGE_VALUE_AS_JSON);
    public static final String DATA_PROCESSING_URL = "/api/v1/processing";
    public static final String THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URL = "/additional-data/";
    public static final String THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URI = THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URL + ADDITIONAL_DATA_ID;
    public static final String THIRD_PARTY_SERVICE_RESPONSE_BODY = """
        {
            "additionalData": "%s"
        }
    """.formatted(ADDITIONAL_DATA);

    public static final long FETCH_TIMEOUT_MILLIS = 1000L;
    public static final Clock CLOCK = Clock.fixed(PROCESSING_TIMESTAMP, ZoneId.systemDefault());
}

import root.dto.ProcessedDataMessage
import root.model.DataForProcessing
import root.model.ProcessedData
import spock.util.concurrent.PollingConditions

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class TestConstants {

    public static final DATA_FOR_PROCESSING_AS_STRING = 'data-for-processing'
    public static final ADDITIONAL_DATA_ID = 'additional-data-id'
    public static final ADDITIONAL_DATA = 'additional-data'
    public static final DATA_FOR_PROCESSING = new DataForProcessing(DATA_FOR_PROCESSING_AS_STRING, ADDITIONAL_DATA_ID)
    public static final PROCESSED_DATA_ID = 'processed-data-id'
    public static final PROCESSED_DATA_AS_STRING = 'DATA-FOR-PROCESSING | ADDITIONAL-DATA'
    public static final PROCESSING_TIMESTAMP = Instant.ofEpochMilli(1655797427000L)
    public static final PROCESSED_DATA = new ProcessedData(PROCESSED_DATA_ID, PROCESSED_DATA_AS_STRING, PROCESSING_TIMESTAMP)
    public static final PROCESSED_DATA_MESSAGE = new ProcessedDataMessage(PROCESSED_DATA_AS_STRING)
    public static final PROCESSED_DATA_MESSAGE_AS_JSON = """
        {
            "data": "$PROCESSED_DATA_AS_STRING"
        }
    """
    public static final JSON_CONTENT_TYPE = 'application/json'
    public static final DATA_PROCESSING_URL = '/api/v1/processing'
    public static final THIRD_PARTY_SERVICE_ADDITIONAL_DATA_URI = "/additional-data/$ADDITIONAL_DATA_ID"
    public static final THIRD_PARTY_SERVICE_RESPONSE_BODY = """
        {
            "additionalData": "$ADDITIONAL_DATA"
        }
    """
    public static final POLLING_CONDITIONS = new PollingConditions(timeout: 10, delay: 1)
    public static final FETCH_TIMEOUT_MS = 1000
    public static final CLOCK = Clock.fixed(PROCESSING_TIMESTAMP, ZoneId.systemDefault())
}

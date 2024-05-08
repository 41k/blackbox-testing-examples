package root.dto;

import lombok.Value;
import root.model.DataForProcessing;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Value
public class DataForProcessingMessage {

    String data;
    String additionalDataId;

    public boolean isValid() {
        return isNotBlank(data) && isNotBlank(additionalDataId);
    }

    public DataForProcessing getDataForProcessing() {
        return new DataForProcessing(data, additionalDataId);
    }
}

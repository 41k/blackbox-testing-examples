package root.dto;

import lombok.Data;
import root.model.DataForProcessing;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Data
public class DataForProcessingMessage {

    private String data;
    private String additionalDataId;

    public boolean isValid() {
        return isNotBlank(data) && isNotBlank(additionalDataId);
    }

    public DataForProcessing getDataForProcessing() {
        return new DataForProcessing(data, additionalDataId);
    }
}

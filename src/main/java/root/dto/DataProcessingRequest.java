package root.dto;

import lombok.Value;
import root.model.DataForProcessing;

import javax.validation.constraints.NotBlank;

@Value
public class DataProcessingRequest {
    @NotBlank
    String data;
    @NotBlank
    String additionalDataId;

    public DataForProcessing getDataForProcessing() {
        return new DataForProcessing(data, additionalDataId);
    }
}

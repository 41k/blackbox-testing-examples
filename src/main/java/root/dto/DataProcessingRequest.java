package root.dto;

import lombok.Data;
import root.model.DataForProcessing;

import javax.validation.constraints.NotBlank;

@Data
public class DataProcessingRequest {
    @NotBlank
    private String data;
    @NotBlank
    private String additionalDataId;

    public DataForProcessing getDataForProcessing() {
        return new DataForProcessing(data, additionalDataId);
    }
}

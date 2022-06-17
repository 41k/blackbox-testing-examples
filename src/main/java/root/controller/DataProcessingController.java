package root.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.dto.DataProcessingRequest;
import root.service.DataProcessingService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/processing")
@RequiredArgsConstructor
public class DataProcessingController {

    private final DataProcessingService dataProcessingService;

    @PostMapping
    public void process(@RequestBody @Valid DataProcessingRequest request) {
        dataProcessingService.process(request.getDataForProcessing());
    }
}

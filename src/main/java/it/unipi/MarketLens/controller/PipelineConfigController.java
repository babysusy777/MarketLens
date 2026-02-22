package it.unipi.MarketLens.controller;

import it.unipi.MarketLens.config.PipelineConfig;
import it.unipi.MarketLens.dto.PipelineConfigUpdateRequest;
import it.unipi.MarketLens.service.PipelineConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/admin/pipeline-config")
public class PipelineConfigController {

    private final PipelineConfigService service;

    public PipelineConfigController(PipelineConfigService service) {
        this.service = service;
    }

    @GetMapping
    public PipelineConfig getConfig() {
        return service.getCurrentConfig();
    }

    @PutMapping
    public PipelineConfig updateConfig(
            @Valid @RequestBody PipelineConfigUpdateRequest request,
            Principal principal
    ) {
        String admin = (principal != null) ? principal.getName() : "admin-manual";
        return service.updateConfig(request, admin);
    }

}

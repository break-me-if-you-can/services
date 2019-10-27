package xyz.breakit.game.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.breakit.game.gateway.admin.HealthcheckService;

/**
 * REST controller for healthcheck.
 */
@RestController
public class HealthcheckController {

    private final HealthcheckService healthcheckService;

    @Autowired
    public HealthcheckController(HealthcheckService healthcheckService) {
        this.healthcheckService = healthcheckService;
    }

    @GetMapping(value = "/health", produces = "text/plain")
    public String healthCheck() throws Exception {
        return healthcheckService.healthCheck().get().toString();
    }

}

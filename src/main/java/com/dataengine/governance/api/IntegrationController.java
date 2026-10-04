package com.dataengine.governance.api;
import com.dataengine.governance.service.IntegrationCatalogService; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/v1/integrations") public class IntegrationController {private final IntegrationCatalogService service; public IntegrationController(IntegrationCatalogService s){service=s;} @GetMapping public List<IntegrationCatalogService.IntegrationStatus> list(){return service.statuses();}}

package com.dataengine.governance.api;
import com.dataengine.governance.service.GovernanceReportService; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/v1/reports") public class GovernanceReportController {private final GovernanceReportService service;public GovernanceReportController(GovernanceReportService s){service=s;}@GetMapping("/governance-overview")public GovernanceReportResponse overview(){return service.overview();}}

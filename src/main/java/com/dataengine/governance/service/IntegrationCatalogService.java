package com.dataengine.governance.service;
import com.dataengine.governance.config.IntegrationProperties; import org.springframework.stereotype.Service; import java.util.*;
@Service public class IntegrationCatalogService {
 private final IntegrationProperties props; public IntegrationCatalogService(IntegrationProperties p){props=p;}
 public List<IntegrationStatus> statuses(){return props.getEndpoints().entrySet().stream().map(e->{var c=e.getValue();return new IntegrationStatus(e.getKey(),c.isEnabled()?"CONFIGURED":"LOCAL_SAFE",c.isEnabled()?"READY_FOR_CONNECTIVITY_TEST":"NOT_CONNECTED",c.getBaseUrl(),c.getCredentialRef().isBlank()?"未配置":"已引用");}).toList();}
 public record IntegrationStatus(String name,String mode,String status,String baseUrl,String credentialStatus){}
}

package com.dataengine.governance.config;
import org.springframework.boot.context.properties.ConfigurationProperties; import java.util.*;
@ConfigurationProperties(prefix="governance.integrations") public class IntegrationProperties {
 private Map<String,Endpoint> endpoints=new LinkedHashMap<>(); public Map<String,Endpoint> getEndpoints(){return endpoints;} public void setEndpoints(Map<String,Endpoint> e){endpoints=e;}
 public static class Endpoint { private boolean enabled; private String baseUrl=""; private String credentialRef=""; public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;} public String getBaseUrl(){return baseUrl;} public void setBaseUrl(String v){baseUrl=v;} public String getCredentialRef(){return credentialRef;} public void setCredentialRef(String v){credentialRef=v;} }
}

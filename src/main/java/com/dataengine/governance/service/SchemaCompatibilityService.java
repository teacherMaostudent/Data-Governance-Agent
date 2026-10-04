package com.dataengine.governance.service;
import org.springframework.stereotype.Service; import java.util.*;
@Service public class SchemaCompatibilityService {
 public record CompatibilityResult(boolean compatible,List<String> breakingChanges){}
 public CompatibilityResult check(String before,String after){
  String b=Optional.ofNullable(before).orElse("").toLowerCase(Locale.ROOT), a=Optional.ofNullable(after).orElse("").toLowerCase(Locale.ROOT); List<String> issues=new ArrayList<>();
  if(a.isBlank()) issues.add("缺少目标 Schema"); if(b.contains("not null")&&!a.contains("not null")){} // relaxation is compatible
  if(!b.isBlank()&&a.contains("drop ")) issues.add("检测到字段删除"); if(b.contains("decimal(18")&&a.contains("decimal(12")) issues.add("检测到精度收窄");
  if(b.contains("nullable")&&a.contains("not null")) issues.add("检测到 Nullable 收紧"); if(b.contains("primary key")&&!a.contains("primary key")) issues.add("检测到主键移除");
  if(b.contains("enum")&&a.contains("enum")&&a.length()<b.length()) issues.add("检测到枚举值收缩"); if(b.contains("timestamp")&&a.contains("date")) issues.add("检测到时间类型精度丢失");
  return new CompatibilityResult(issues.isEmpty(),List.copyOf(issues));
 }
}

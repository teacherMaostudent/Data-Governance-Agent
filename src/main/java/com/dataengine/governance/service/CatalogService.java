package com.dataengine.governance.service;
import com.dataengine.governance.domain.*; import com.dataengine.governance.repository.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;
@Service @Transactional public class CatalogService {
 private final DataAssetRepository assets; private final LineageEdgeRepository edges; private final DataContractRepository contracts;
 public CatalogService(DataAssetRepository a,LineageEdgeRepository e,DataContractRepository c){assets=a;edges=e;contracts=c;}
 @Transactional(readOnly=true) public List<String> confirmedDownstream(String root){Set<String> visited=new LinkedHashSet<>();Deque<String> q=new ArrayDeque<>();q.add(root);while(!q.isEmpty()){for(LineageEdge e:edges.findBySourceAsset(q.remove()))if(e.getStatus()==LineageStatus.CONFIRMED&&visited.add(e.getTargetAsset()))q.add(e.getTargetAsset());}return List.copyOf(visited);}
 @Transactional(readOnly=true) public List<String> candidateDownstream(String root){return edges.findBySourceAsset(root).stream().filter(e->e.getStatus()==LineageStatus.CANDIDATE).map(LineageEdge::getTargetAsset).toList();}
 @Transactional(readOnly=true) public List<DataAsset> assets(){return assets.findAll();} @Transactional(readOnly=true) public List<LineageEdge> edges(){return edges.findAll();} @Transactional(readOnly=true) public List<DataContract> contracts(){return contracts.findAll();}
}

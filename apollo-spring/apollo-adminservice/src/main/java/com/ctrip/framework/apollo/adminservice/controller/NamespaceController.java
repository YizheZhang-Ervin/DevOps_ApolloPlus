package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.common.utils.InputValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Namespace Controller
 */
@RestController
public class NamespaceController {

    @Autowired
    private NamespaceService namespaceService;

    /**
     * 创建 Namespace
     *
     * @param appId App 编号
     * @param clusterName Cluster 名字
     * @param dto NamespaceDTO 对象
     * @return 创建成功的 NamespaceDTO 对象
     */
    @RequestMapping(path = "/apps/{appId}/clusters/{clusterName}/namespaces", method = RequestMethod.POST)
    public NamespaceDTO create(@PathVariable("appId") String appId,
                               @PathVariable("clusterName") String clusterName, @RequestBody NamespaceDTO dto) {
        // 校验 NamespaceDTO 的 `namespaceName` 格式正确。
        if (!InputValidator.isValidClusterNamespace(dto.getNamespaceName())) {
            throw new BadRequestException(String.format("Namespace格式错误: %s", InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE));
        }
        // 将 NamespaceDTO 转换成 Namespace 对象
        Namespace entity = BeanUtils.transfrom(Namespace.class, dto);
        // 判断 `name` 在 Cluster 下是否已经存在对应的 Namespace 对象。若已经存在，抛出 BadRequestException 异常。
        Namespace managedEntity = namespaceService.findOne(appId, clusterName, entity.getNamespaceName());
        if (managedEntity != null) {
            throw new BadRequestException("namespace already exist.");
        }
        // 保存 Namespace 对象
        entity = namespaceService.save(entity);
        // 将保存的 Namespace 对象转换成 NamespaceDTO
        dto = BeanUtils.transfrom(NamespaceDTO.class, entity);
        return dto;
    }

    @RequestMapping(path = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName:.+}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("appId") String appId,
                       @PathVariable("clusterName") String clusterName,
                       @PathVariable("namespaceName") String namespaceName, @RequestParam String operator) {
        Namespace entity = namespaceService.findOne(appId, clusterName, namespaceName);
        if (entity == null) throw new NotFoundException(
                String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));

        namespaceService.deleteNamespace(entity, operator);
    }

    @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces", method = RequestMethod.GET)
    public List<NamespaceDTO> find(@PathVariable("appId") String appId,
                                   @PathVariable("clusterName") String clusterName) {
        List<Namespace> groups = namespaceService.findNamespaces(appId, clusterName);
        return BeanUtils.batchTransform(NamespaceDTO.class, groups);
    }

    @RequestMapping(value = "/namespaces/{namespaceId}", method = RequestMethod.GET)
    public NamespaceDTO get(@PathVariable("namespaceId") Long namespaceId) {
        Namespace namespace = namespaceService.findOne(namespaceId);
        if (namespace == null)
            throw new NotFoundException(String.format("namespace not found for %s", namespaceId));
        return BeanUtils.transfrom(NamespaceDTO.class, namespace);
    }

    @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName:.+}", method = RequestMethod.GET)
    public NamespaceDTO get(@PathVariable("appId") String appId,
                            @PathVariable("clusterName") String clusterName,
                            @PathVariable("namespaceName") String namespaceName) {
        Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
        if (namespace == null) throw new NotFoundException(
                String.format("namespace not found for %s %s %s", appId, clusterName, namespaceName));
        return BeanUtils.transfrom(NamespaceDTO.class, namespace);
    }

    @RequestMapping(value = "/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/associated-public-namespace",
            method = RequestMethod.GET)
    public NamespaceDTO findPublicNamespaceForAssociatedNamespace(@PathVariable String appId,
                                                                  @PathVariable String clusterName,
                                                                  @PathVariable String namespaceName) {
        Namespace namespace = namespaceService.findPublicNamespaceForAssociatedNamespace(clusterName, namespaceName);

        if (namespace == null) {
            throw new NotFoundException(String.format("public namespace not found. namespace:%s", namespaceName));
        }

        return BeanUtils.transfrom(NamespaceDTO.class, namespace);
    }

    /**
     * cluster -> cluster has not published namespaces?
     */
    @RequestMapping(value = "/apps/{appId}/namespaces/publish_info", method = RequestMethod.GET)
    public Map<String, Boolean> namespacePublishInfo(@PathVariable String appId) {
        return namespaceService.namespacePublishInfo(appId);
    }


}

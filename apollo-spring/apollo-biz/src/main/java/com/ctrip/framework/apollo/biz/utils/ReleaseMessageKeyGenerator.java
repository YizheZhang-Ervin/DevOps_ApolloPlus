package com.ctrip.framework.apollo.biz.utils;


import com.ctrip.framework.apollo.core.ConfigConsts;
import com.google.common.base.Joiner;

/**
 * {@link com.ctrip.framework.apollo.biz.entity.ReleaseMessage#message} 生成器
 */
public class ReleaseMessageKeyGenerator {

    private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);

    public static String generate(String appId, String cluster, String namespace) {
        return STRING_JOINER.join(appId, cluster, namespace);
    }

}
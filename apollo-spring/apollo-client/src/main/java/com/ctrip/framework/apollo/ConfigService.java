package com.ctrip.framework.apollo;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.internals.ConfigManager;
import com.ctrip.framework.apollo.spi.ConfigFactory;
import com.ctrip.framework.apollo.spi.ConfigRegistry;

/**
 * Entry point for client config use
 * <p>
 * 客户端配置服务，作为配置使用的入口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigService {

    /**
     * 单例
     */
    private static final ConfigService s_instance = new ConfigService();

    private volatile ConfigManager m_configManager;
    private volatile ConfigRegistry m_configRegistry;

    private ConfigManager getManager() {
        // 若 ConfigManager 未初始化，进行获得
        if (m_configManager == null) {
            synchronized (this) {
                if (m_configManager == null) {
                    m_configManager = ApolloInjector.getInstance(ConfigManager.class);
                }
            }
        }
        // 返回 ConfigManager
        return m_configManager;
    }

    private ConfigRegistry getRegistry() {
        // 若 ConfigRegistry 未初始化，进行获得
        if (m_configRegistry == null) {
            synchronized (this) {
                if (m_configRegistry == null) {
                    m_configRegistry = ApolloInjector.getInstance(ConfigRegistry.class);
                }
            }
        }
        // 返回 ConfigRegistry
        return m_configRegistry;
    }

    /**
     * Get Application's config instance.
     *
     * @return config instance
     */
    public static Config getAppConfig() {
        return getConfig(ConfigConsts.NAMESPACE_APPLICATION);
    }

    /**
     * Get the config instance for the namespace.
     *
     * @param namespace the namespace of the config
     * @return config instance
     */
    public static Config getConfig(String namespace) {
        return s_instance.getManager().getConfig(namespace);
    }

    public static ConfigFile getConfigFile(String namespace, ConfigFileFormat configFileFormat) {
        return s_instance.getManager().getConfigFile(namespace, configFileFormat);
    }

    static void setConfig(Config config) {
        setConfig(ConfigConsts.NAMESPACE_APPLICATION, config);
    }

    /**
     * Manually set the config for the namespace specified, use with caution.
     *
     * @param namespace the namespace
     * @param config    the config instance
     */
    static void setConfig(String namespace, final Config config) {
        s_instance.getRegistry().register(namespace, new ConfigFactory() {

            @Override
            public Config create(String namespace) {
                return config;
            }

            @Override
            public ConfigFile createConfigFile(String namespace, ConfigFileFormat configFileFormat) {
                return null; // 空
            }

        });
    }

    static void setConfigFactory(ConfigFactory factory) {
        setConfigFactory(ConfigConsts.NAMESPACE_APPLICATION, factory);
    }

    /**
     * Manually set the config factory for the namespace specified, use with caution.
     *
     * @param namespace the namespace
     * @param factory   the factory instance
     */
    static void setConfigFactory(String namespace, ConfigFactory factory) {
        s_instance.getRegistry().register(namespace, factory);
    }

    // for test only
    static void reset() {
        synchronized (s_instance) {
            s_instance.m_configManager = null;
            s_instance.m_configRegistry = null;
        }
    }

}

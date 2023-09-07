package com.cloud.eurekanacos;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

/**
 * @author ywang
 */
@ConfigurationProperties("spring.cloud.nacos.discovery")
public class NacosDiscoveryProperties extends Properties {
}

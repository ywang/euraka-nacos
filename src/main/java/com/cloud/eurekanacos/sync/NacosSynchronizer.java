package com.cloud.eurekanacos.sync;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ywang
 */
@Component
public class NacosSynchronizer {
    private static final Logger log = LoggerFactory.getLogger(NacosSynchronizer.class);

    @Resource
    private NamingService namingService;
    @Resource
    private NacosEventListener listener;
    @Resource
    private PeerAwareInstanceRegistry peerAwareInstanceRegistry;

    @PostConstruct
    public void init() {
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, runnable -> {
            Thread t = new Thread(runnable);
            t.setName(NacosSynchronizer.class.getName());
            t.setDaemon(true);
            return t;
        });
        executor.scheduleWithFixedDelay(() -> {
            try {
                syncService();
            } catch (Throwable e) {
                log.error("synchronize service error", e);
            }
        }, 5, 10, TimeUnit.SECONDS);
    }

    public void syncService() throws Exception {
        ListView<String> serviceList = namingService.getServicesOfServer(1, 1000);

        for (String service : serviceList.getData()) {
            List<Instance> instances = namingService.getAllInstances(service);
            for (Instance instance : instances) {
                if (!isFromEureka(instance)) {
                    String instanceId = String.format("%s:%s:%s", service, instance.getIp(), instance.getPort());
                    peerAwareInstanceRegistry.renew(service.toUpperCase(), instanceId, false);
                }
            }

            List<ServiceInfo> list = namingService.getSubscribeServices();
            Optional<ServiceInfo> optional = list.stream().filter(serviceInfo -> serviceInfo.getName().equals(service)).findFirst();
            if (!optional.isPresent()) {
                namingService.subscribe(service, listener);
            }
        }

    }

    private boolean isFromEureka(Instance instance) {
        String discoveryClient = instance.getMetadata().get(ProxyConstants.METADATA_DISCOVERY_CLIENT);
        return !StringUtils.isEmpty(discoveryClient) && discoveryClient.equals(ProxyConstants.EUREKA_VALUE);
    }
}

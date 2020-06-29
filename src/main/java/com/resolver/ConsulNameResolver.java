package com.resolver;

/**
 * java grpc client集成consul服务发现
 * https://www.jianshu.com/p/997505834bf5
 */
public class ConsulNameResolver {
};
/*
@Slf4j
public class ConsulNameResolver extends NameResolver {
    private static final int DEFAULT_PAUSE_IN_SECONDS = 5;
    private URI uri;
    private String serviceName;
    private int pauseInSeconds;
    private boolean ignoreConsul;
    private List<String> hostPorts;
    private Listener listener;
    private List<ServiceDiscovery.ServiceNode> nodes;
    private ConnectionCheckTimer connectionCheckTimer;

    public ConsulNameResolver(URI uri, String serviceName, int pauseInSeconds, boolean ignoreConsul, List<String> hostPorts) {
        this.uri = uri;
        this.serviceName = serviceName;
        this.pauseInSeconds = pauseInSeconds;
        this.ignoreConsul = ignoreConsul;
        this.hostPorts = hostPorts;
        // run connection check timer.
        this.connectionCheckTimer = new ConnectionCheckTimer(this, this.pauseInSeconds);
        this.connectionCheckTimer.runTimer();
    }

    @Override
    public String getServiceAuthority() {
        return this.uri.getAuthority();
    }

    @Override
    public void start(Listener listener) {
        this.listener = listener;
    }

    private void loadServiceNodes() {
        List<EquivalentAddressGroup> addrs = new ArrayList<>();
        if (!this.ignoreConsul) {
            log.info("开始获取consul服务节点...");
            String consulHost = uri.getHost();
            int consulPort = uri.getPort();
            nodes = getServiceNodes(serviceName, consulHost, consulPort);
            if (nodes == null || nodes.size() == 0) {
                log.info("未找到服务[{}]节点", serviceName);
                return;
            }
            for (ServiceDiscovery.ServiceNode node : nodes) {
                String host = node.getHost();
                int port = node.getPort();
                log.info("serviceName: [" + serviceName + "], host: [" + host + "], port: [" + port + "]");
                List<SocketAddress> sockaddrsList = new ArrayList<SocketAddress>();
                sockaddrsList.add(new InetSocketAddress(host, port));
                addrs.add(new EquivalentAddressGroup(sockaddrsList));
            }
        } else {
            nodes = new ArrayList<>();
            for (String hostPort : this.hostPorts) {
                String[] tokens = hostPort.split(":");
                String host = tokens[0];
                int port = Integer.valueOf(tokens[1]);
                log.info("static host: [" + host + "], port: [" + port + "]");
                nodes.add(new ServiceDiscovery.ServiceNode("", host, port));
                List<SocketAddress> sockaddrsList = new ArrayList<SocketAddress>();
                sockaddrsList.add(new InetSocketAddress(host, port));
                addrs.add(new EquivalentAddressGroup(sockaddrsList));
            }
        }
        if (addrs.size() > 0) {
            if (listener != null) {
                this.listener.onAddresses(addrs, Attributes.EMPTY);
            }
        }
    }

    public List<ServiceDiscovery.ServiceNode> getNodes() {
        return this.nodes;
    }

    private List<ServiceDiscovery.ServiceNode> getServiceNodes(String serviceName, String consulHost, int consulPort) {
        ServiceDiscovery serviceDiscovery = ConsulServiceDiscovery.singleton(consulHost, consulPort);
        return serviceDiscovery.getHealthServices(serviceName);
    }

    @Override
    public void shutdown() {
    }

    private class ConnectionCheckTimer {
        private int delay = 3;
        private int pauseInSeconds;
        private ScheduledExecutorService executorService;
        private ConsulNameResolver consulNameResolver;

        public ConnectionCheckTimer(ConsulNameResolver consulNameResolver, int pauseInSeconds) {
            this.consulNameResolver = consulNameResolver;
            this.pauseInSeconds = pauseInSeconds;
            this.executorService = new ScheduledThreadPoolExecutor(1);
        }

        public void runTimer() {
            this.executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        consulNameResolver.loadServiceNodes();
                    } catch (Exception e) {
                        log.error("定时任务执行失败", e);
                    }
                }
            }, delay, pauseInSeconds, TimeUnit.SECONDS);
        }

    }
}*/

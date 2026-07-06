package com.example.ems.reports.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "reports")
public class ReportStorageProperties {

    private Storage storage = new Storage();
    private Async async = new Async();
    private Cache cache = new Cache();
    private Export export = new Export();

    public Storage getStorage() { return storage; }
    public void setStorage(Storage storage) { this.storage = storage; }

    public Async getAsync() { return async; }
    public void setAsync(Async async) { this.async = async; }

    public Cache getCache() { return cache; }
    public void setCache(Cache cache) { this.cache = cache; }

    public Export getExport() { return export; }
    public void setExport(Export export) { this.export = export; }

    public static class Storage {
        private String type = "local";
        private String localPath = "target/exports";

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getLocalPath() { return localPath; }
        public void setLocalPath(String localPath) { this.localPath = localPath; }
    }

    public static class Async {
        private int poolSize = 5;

        public int getPoolSize() { return poolSize; }
        public void setPoolSize(int poolSize) { this.poolSize = poolSize; }
    }

    public static class Cache {
        private int dashboardTtl = 300;

        public int getDashboardTtl() { return dashboardTtl; }
        public void setDashboardTtl(int dashboardTtl) { this.dashboardTtl = dashboardTtl; }
    }

    public static class Export {
        private int maxRows = 100000;

        public int getMaxRows() { return maxRows; }
        public void setMaxRows(int maxRows) { this.maxRows = maxRows; }
    }
}

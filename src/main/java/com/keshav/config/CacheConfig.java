package com.keshav.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * In-memory Spring Cache Manager for high-traffic read operations
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager mgr = new ConcurrentMapCacheManager();
        mgr.setCacheNames(List.of(
                "categories",
                "products",
                "productDetails",
                "supplierDashboard",
                "supplierProducts"
        ));
        return mgr;
    }

    /**
     * Shallow ETag filter: Automatically generates HTTP ETag headers for responses.
     * When clients make conditional requests (If-None-Match), Spring returns 304 Not Modified
     * with 0 body payload, drastically reducing server network bandwidth and latency.
     */
    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> shallowEtagHeaderFilter() {
        FilterRegistrationBean<ShallowEtagHeaderFilter> filterRegistrationBean =
                new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        filterRegistrationBean.addUrlPatterns("/api/*");
        filterRegistrationBean.setName("etagFilter");
        filterRegistrationBean.setOrder(1);
        return filterRegistrationBean;
    }
}

package com.example.ems.organization.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    @Bean
    public RazorpayClient razorpayClient(RazorpayProperties properties) throws RazorpayException {
        String keyId = properties.getKeyId();
        String keySecret = properties.getKeySecret();
        if (keyId == null || keyId.isEmpty() || keySecret == null || keySecret.isEmpty()) {
            keyId = "rzp_test_StUZupmMw4H4yc";
            keySecret = "uq9yq68G6Dydj3FJXgobDeB0";
        }
        return new RazorpayClient(keyId, keySecret);
    }
}

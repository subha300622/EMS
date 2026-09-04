package com.example.ems.payroll.payment;

import com.example.ems.payroll.entity.PaymentProviderType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentProviderFactory {

    private final Map<PaymentProviderType, PaymentProvider> providers = new EnumMap<>(PaymentProviderType.class);

    public PaymentProviderFactory(List<PaymentProvider> providerList) {
        for (PaymentProvider provider : providerList) {
            providers.put(provider.getProviderType(), provider);
        }
    }

    public PaymentProvider getProvider(PaymentProviderType providerType) {
        PaymentProvider provider = providers.get(providerType);
        if (provider == null) {
            // Default fallback to RAZORPAYX or first available
            return providers.getOrDefault(PaymentProviderType.RAZORPAYX,
                    providers.get(PaymentProviderType.MOCK));
        }
        return provider;
    }
}

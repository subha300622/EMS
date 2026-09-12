package com.example.ems.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class FlywayDependsOnBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] names = beanFactory.getBeanNamesForType(LocalContainerEntityManagerFactoryBean.class, false, false);
        for (String name : names) {
            String beanName = name.startsWith("&") ? name.substring(1) : name;
            BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
            String[] dependsOn = bd.getDependsOn();
            if (dependsOn == null) {
                bd.setDependsOn("flyway");
            } else {
                List<String> list = new ArrayList<>(Arrays.asList(dependsOn));
                if (!list.contains("flyway")) {
                    list.add("flyway");
                    bd.setDependsOn(list.toArray(new String[0]));
                }
            }
        }
    }
}

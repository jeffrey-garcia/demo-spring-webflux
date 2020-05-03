package com.jeffrey.example.demolib.eventstore.util;

import com.jeffrey.example.demoapp.dao.DemoDao;
import com.jeffrey.example.demoapp.dao.DemoRxDao;
import com.jeffrey.example.demolib.eventstore.annotation.EnableEventStore;
import com.jeffrey.example.demolib.eventstore.config.EventStoreConfig;
import com.jeffrey.example.demolib.eventstore.config.MongoDbConfig;
import com.jeffrey.example.demolib.eventstore.config.ReactiveMongoDbConfig;
import com.jeffrey.example.demolib.eventstore.config.ReactiveStoreConfig;
import org.springframework.cloud.commons.util.SpringFactoryImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnableEventStoreImportSelector extends SpringFactoryImportSelector<EnableEventStore> {

    public EnableEventStoreImportSelector() {}

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        if (!this.isEnabled()) {
            return new String[0];
        } else {
            String [] imports = super.selectImports(metadata);
            AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(this.getAnnotationClass().getName(), true));
            List<String> importsList = new ArrayList(Arrays.asList(imports));

            importsList.add(MongoDbConfig.class.getName());
            importsList.add(ReactiveMongoDbConfig.class.getName());
            importsList.add(DemoDao.class.getName());
            importsList.add(DemoRxDao.class.getName());
            importsList.add(EventStoreConfig.class.getName());
            importsList.add(ReactiveStoreConfig.class.getName());

            imports = importsList.toArray(new String[0]);
            return imports;
        }
    }

    @Override
    protected boolean isEnabled() {
        return this.getEnvironment().getProperty("com.jeffrey.example.eventstore.enabled", Boolean.class, Boolean.TRUE);
    }

    @Override
    protected boolean hasDefaultFactory() {
        return true;
    }

}

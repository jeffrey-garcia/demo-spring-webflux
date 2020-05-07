package com.jeffrey.example.demolib.eventstore.util;

import com.jeffrey.example.demolib.eventstore.annotation.EnableEventStore;
import com.jeffrey.example.demolib.eventstore.aop.aspect.EventStoreAspect;
import com.jeffrey.example.demolib.eventstore.aop.aspect.ReactiveEventStoreAspect;
import com.jeffrey.example.demolib.eventstore.config.*;
import com.jeffrey.example.demolib.eventstore.dao.MongoEventStoreDao;
import com.jeffrey.example.demolib.eventstore.service.EventStoreService;
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
            boolean useLegacy = attributes.getBoolean("useLegacy");

            List<String> importsList = new ArrayList<>(Arrays.asList(imports));

            // core dependencies for input/output channels integration
            importsList.add(ServiceActivatorConfig.class.getName());
            importsList.add(ChannelBindingAccessor.class.getName());

            // event store config
            // general configuration object that is globally accessible
            // by any components at the lower-level
            importsList.add(EventStoreConfig.class.getName());

            // event store database dependencies
            importsList.add(MongoDbConfig.class.getName());
            importsList.add(ReactiveMongoDbConfig.class.getName());
            importsList.add(MongoEventStoreDao.class.getName());

            // event store higher level dependencies
            // - event store specific logic
            // - bindings/functions aspect/advice
            importsList.add(EventStoreService.class.getName());
            importsList.add(EventStoreAspect.class.getName());

            /**
             * (useLegacy = true) imply the user explicitly request retain the use
             * annotation-based bindings configuration instead of the functional
             * mechanism provided by SCSt
             */
            if (!useLegacy) {
                // ONLY import the SCSt reactive functions mechanism support if useLegacy is FALSE (defaults TRUE)
                importsList.add(ReactiveEventStoreConfig.class.getName());
                importsList.add(ReactiveEventStoreAspect.class.getName());
            }

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

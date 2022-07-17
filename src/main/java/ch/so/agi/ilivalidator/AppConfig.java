package ch.so.agi.ilivalidator;

import org.jobrunr.jobs.mappers.JobMapper;
import org.jobrunr.storage.InMemoryStorageProvider;
import org.jobrunr.storage.StorageProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
public class AppConfig {
    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Bean
    void setPluginClasses() {
        System.setProperty("ch.ehi.iox-ili.pluginClasses",
                "ch.so.agi.ilivalidator.ext.AreaIoxPlugin,"
                        + "ch.so.agi.ilivalidator.ext.LengthIoxPlugin,"
                        + "ch.so.agi.ilivalidator.ext.IsHttpResourceIoxPlugin,"
                        + "ch.so.agi.ilivalidator.ext.IsValidDocumentsCycleIoxPlugin,"
                        + "ch.so.agi.ilivalidator.ext.RingSelfIntersectionIoxPlugin,"
                        + "ch.so.agi.ilivalidator.ext.TooFewPointsPolylineIoxPlugin");
    }
    
    // Falls eine Datasource vorhanden ist, wird diese als StorageProvider verwendent.
    // Weitere Konfiguration ist dann nicht mehr notwendig.
//    @Bean
//    StorageProvider storageProvider(JobMapper jobMapper) {
//        InMemoryStorageProvider storageProvider = new InMemoryStorageProvider();
//        storageProvider.setJobMapper(jobMapper);
//        return storageProvider;
//    }

}

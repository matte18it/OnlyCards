package unical.enterpriceapplication.onlycards.application.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableCaching
@EnableScheduling
@Slf4j
public class CacheConfig {

    public static final String CACHE_FOR_FEATURES = "FEATURES";
    public static final String CACHE_FOR_CART = "CART";

    @CacheEvict(value = CACHE_FOR_FEATURES, allEntries = true)
    @Scheduled(cron = "0 0 0 * * ?")  // Esegui ogni giorno a mezzanotte
    public void evictAllFeaturesCacheDaily() {
        // Questo metodo svuota la cache di tutte le features
    }

    @CacheEvict(value = CACHE_FOR_CART, allEntries = true)
    @Scheduled(cron = "0 0 1 * * *") // secondi minuti ore giorno mese giornoSettimana
    public void evictCart(){}


}

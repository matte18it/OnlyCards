package unical.enterpriceapplication.onlycards.application.data.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.FeatureProduct;
import unical.enterpriceapplication.onlycards.application.data.repository.FeatureProductRepository;

@Service
@RequiredArgsConstructor
public class FeatureProductServiceImpl implements FeatureProductService {
    private static final int MAX_FEATURES = 10;

    private final FeatureProductRepository featureProductRepository;


    @Override
    public void save(FeatureProduct featureProduct) {
        featureProductRepository.save(featureProduct);
    }


    @Override
    public int getMaxFeaturesForProduct() {
        return MAX_FEATURES;
    }

 
}

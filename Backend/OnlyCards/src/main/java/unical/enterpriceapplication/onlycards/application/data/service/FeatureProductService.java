package unical.enterpriceapplication.onlycards.application.data.service;

import unical.enterpriceapplication.onlycards.application.data.entities.cards.FeatureProduct;


public interface FeatureProductService {
    void save(FeatureProduct featureProduct);
    int getMaxFeaturesForProduct();
}

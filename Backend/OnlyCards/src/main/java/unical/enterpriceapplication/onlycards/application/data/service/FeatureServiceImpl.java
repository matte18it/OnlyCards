package unical.enterpriceapplication.onlycards.application.data.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import unical.enterpriceapplication.onlycards.application.data.repository.FeatureRepository;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Feature;

@Service
@RequiredArgsConstructor
public class FeatureServiceImpl implements FeatureService{
    private final FeatureRepository featureRepository;
    @Override
    public void save(Feature feature) {
        featureRepository.save(feature);
    }
}

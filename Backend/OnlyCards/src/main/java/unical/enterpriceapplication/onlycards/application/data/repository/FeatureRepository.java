package unical.enterpriceapplication.onlycards.application.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Feature;

import java.util.Optional;
import java.util.Set;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Long>{
    @Query("SELECT DISTINCT f FROM Feature f JOIN f.cards fc JOIN fc.productType ct WHERE ct.game = :game")
    public Set<Feature> findFeaturesByCardTypeGame(String game);

    Optional<Feature> findByName(String name);
}

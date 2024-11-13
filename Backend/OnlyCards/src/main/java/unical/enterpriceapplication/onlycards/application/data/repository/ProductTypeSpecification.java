package unical.enterpriceapplication.onlycards.application.data.repository;


import jakarta.persistence.criteria.*;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ProductTypeSpecification {
    private ProductTypeSpecification() {
    }

    public static Specification<ProductType> buildSpecification(Filter cardTypeFilter) {
        return new Specification<ProductType>() {
            @Override
            public Predicate toPredicate(Root<ProductType> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                // Gioco
                if (cardTypeFilter.getGame() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("game")), cardTypeFilter.getGame().toLowerCase()));
                }

                // Tipo
                if (cardTypeFilter.getType() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("type")), cardTypeFilter.getType().toLowerCase()));
                }

                // Lingua
                if (cardTypeFilter.getLanguage() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("language")), cardTypeFilter.getLanguage().toLowerCase()));
                }
                //nome
                if (cardTypeFilter.getName() != null && !cardTypeFilter.getName().trim().isEmpty()) {
                    String name = cardTypeFilter.getName().trim().toLowerCase();
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name + "%"));
                }
                //prezzo
                if (cardTypeFilter.getMinPrice() != null && cardTypeFilter.getMaxPrice() != null) {
                    predicates.add(criteriaBuilder.between(root.get("minPrice").get("amount"), cardTypeFilter.getMinPrice(), cardTypeFilter.getMaxPrice()));
                }else if (cardTypeFilter.getMinPrice() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("minPrice").get("amount"), cardTypeFilter.getMinPrice()));
                }else if (cardTypeFilter.getMaxPrice() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("minPrice").get("amount"), cardTypeFilter.getMaxPrice()));
                }
                // features
                if (cardTypeFilter.getFeatures() != null && !cardTypeFilter.getFeatures().isEmpty()) {

                    cardTypeFilter.getFeatures().forEach((key, value) -> {
                        // Crea un sottoquery per recuperare i valori delle feature
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(criteriaBuilder.lower(root.join("features").get("feature").get("name")), criteriaBuilder.lower(criteriaBuilder.literal(key))),
                                criteriaBuilder.equal(criteriaBuilder.lower(root.join("features").get("value")), criteriaBuilder.lower(criteriaBuilder.literal(value)))
                                )
                        );
                    });
                }




                //sorting
                if(cardTypeFilter.getProductSorting()!=null) {

                    switch (cardTypeFilter.getProductSorting()) {
                        case PRICE_ASC:
                            query.orderBy(criteriaBuilder.asc(root.get("minPrice").get("amount")));
                            break;
                        case PRICE_DESC:
                            query.orderBy(criteriaBuilder.desc(root.get("minPrice").get("amount")));
                            break;
                        default:
                            query.orderBy(criteriaBuilder.desc(root.get("numSell")));
                            break;
                    }
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
    }

    @Data
    public static class Filter {
        private String language;
        private String name;
        private Long minPrice;
        private Long maxPrice;
        private String type;
        private ProductSorting productSorting;
        private Map<String, String> features;
        private String game;
    }

}

package unical.enterpriceapplication.onlycards.application.data.repository;

import jakarta.persistence.criteria.*;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Product;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductType;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Orders;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Status;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.User;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Transactions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderSpecification {
    private OrderSpecification() {}

    @Data
    public static class Filter {
        private String userId;           // id dell'utente/email venditore
        private Status status;           // stato dell'ordine: in elaborazione, spedito, completato, annullato
        private LocalDate addDate;       // data in cui è stato effettuato l'ordine
        private boolean type;            // tipo di ordine: acquisto (false), vendita (true)
        private Double minPrice;         // prezzo minimo
        private Double maxPrice;         // prezzo massimo
        private String productName;      // nome del prodotto
    }

    public static Specification<Orders> buildSpecification(Filter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro per userId o vendorEmail
            if (filter.getUserId() != null) {
                if (filter.isType()) {
                    // Ordine di vendita: filtro su vendorEmail
                    predicates.add(criteriaBuilder.equal(root.get("vendorEmail"), filter.getUserId()));
                } else {
                    // Ordine di acquisto: filtro su userId
                    Join<Orders, User> userJoin = root.join("user");
                    predicates.add(criteriaBuilder.equal(userJoin.get("id"), UUID.fromString(filter.getUserId())));
                }
            }

            // Filtro per status dell'ordine
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Filtro per data di aggiunta dell'ordine
            if (filter.getAddDate() != null) {
                predicates.add(criteriaBuilder.equal(root.get("addDate"), filter.getAddDate()));
            }

            // Filtro per prezzo totale delle transazioni
            if (filter.getMinPrice() != null || filter.getMaxPrice() != null) {
                // Subquery per sommare i valori delle transazioni
                Subquery<Double> subquery = query.subquery(Double.class);
                Root<Transactions> subRoot = subquery.from(Transactions.class);
                subquery.select(criteriaBuilder.sum(subRoot.get("value").get("amount")))
                        .where(
                                criteriaBuilder.equal(subRoot.get("orders"), root),
                                criteriaBuilder.equal(subRoot.get("type"), filter.isType())
                        );

                // Aggiunta dei predicati per il prezzo
                if (filter.getMinPrice() != null) {
                    predicates.add(criteriaBuilder.ge(subquery, filter.getMinPrice()));
                }
                if (filter.getMaxPrice() != null) {
                    predicates.add(criteriaBuilder.le(subquery, filter.getMaxPrice()));
                }
            }

            if (filter.getProductName() != null && !filter.getProductName().isEmpty()) {
                Join<Orders, Transactions> transactionsJoin = root.join("transactions");
                Join<Transactions, Product> productJoin = transactionsJoin.join("product");
                Join<Product, ProductType> productTypeJoin = productJoin.join("productType");

                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(productTypeJoin.get("name")),
                        "%" + filter.getProductName().toLowerCase() + "%"));
            }

            // Costruzione finale della query con tutti i predicati
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
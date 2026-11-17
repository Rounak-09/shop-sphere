package com.shopsphere.product.domain.specification;

import com.shopsphere.product.domain.entity.Product;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> withFilters(
            String query,
            String category) {

        return (root, criteriaQuery, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    criteriaBuilder.isTrue(root.get("active"))
            );

            if (query != null && !query.isBlank()) {

                String search =
                        "%" + query.trim().toLowerCase(Locale.ROOT) + "%";

                Predicate nameMatch =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                search
                        );

                Predicate descriptionMatch =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("description")),
                                search
                        );

                Predicate skuMatch =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("sku")),
                                search
                        );

                predicates.add(
                        criteriaBuilder.or(
                                nameMatch,
                                descriptionMatch,
                                skuMatch
                        )
                );
            }

            if (category != null && !category.isBlank()) {

                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("category")),
                                category.trim().toLowerCase(Locale.ROOT)
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
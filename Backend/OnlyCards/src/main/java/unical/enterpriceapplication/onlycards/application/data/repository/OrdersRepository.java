package unical.enterpriceapplication.onlycards.application.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Orders;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, UUID> {
    // Metodo per ottenere tutti gli ordini
    Page<Orders> findAll(Specification<Orders> ordersSpecification, Pageable pageable);
    
    Page<Orders> findAllByUser_UsernameContaining(String buyer, Pageable pageable);
    
    Page<Orders> findAllByUser_UsernameContainingAndVendorEmailContaining(String buyer, String seller,  Pageable pageable);

    Page<Orders> findAllByVendorEmailContaining(String seller, Pageable pageable);

    List<Orders> findByUserId(UUID userId);
}

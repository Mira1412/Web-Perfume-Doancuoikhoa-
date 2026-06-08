package com.haan.perfumeshop.repository;

import com.haan.perfumeshop.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    // Sử dụng câu lệnh JPQL tường minh để chỉ định rõ tìm theo id_user
    @Query("SELECT c FROM Cart c WHERE c.user.id_user = :idUser")
    List<Cart> findByUser_Id_user(@Param("idUser") Long idUser);
}
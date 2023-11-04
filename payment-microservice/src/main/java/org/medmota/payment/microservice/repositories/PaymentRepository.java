package org.medmota.payment.microservice.repositories;

import java.util.List;

import org.medmota.payment.microservice.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository  extends JpaRepository<Payment, Long>{

	public List<Payment> findByOrderId(long orderId);
}

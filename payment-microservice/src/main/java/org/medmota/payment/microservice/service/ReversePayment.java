package org.medmota.payment.microservice.service;

import org.medmota.payment.microservice.entities.Payment;
import org.medmota.payment.microservice.models.CustomerOrder;
import org.medmota.payment.microservice.models.OrderEvent;
import org.medmota.payment.microservice.models.PaymentEvent;
import org.medmota.payment.microservice.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ReversePayment {

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private KafkaTemplate<String, OrderEvent> kafkaTemplate;

	@KafkaListener(topics = "reversed-payments", groupId = "payments-group")
	public void reversePayment(String event) {

		try {

			PaymentEvent paymentEvent = new ObjectMapper().readValue(event, PaymentEvent.class);

			CustomerOrder order = paymentEvent.getOrder();

			Iterable<Payment> payments = this.paymentRepository.findByOrderId(order.getOrderId());

			payments.forEach(p -> {

				p.setStatus("FAILED");
				this.paymentRepository.save(p);
			});

			// reverse previous task
			OrderEvent orderEvent = new OrderEvent();
			orderEvent.setOrder(paymentEvent.getOrder());
			orderEvent.setType("ORDER_REVERSED");
			this.kafkaTemplate.send("reversed-orders", orderEvent);

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

}

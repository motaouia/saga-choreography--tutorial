package org.medmota.payment.microservice.controllers;

import org.medmota.payment.microservice.entities.Payment;
import org.medmota.payment.microservice.models.CustomerOrder;
import org.medmota.payment.microservice.models.OrderEvent;
import org.medmota.payment.microservice.models.PaymentEvent;
import org.medmota.payment.microservice.repositories.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class PaymentController {
	
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

	@Autowired
	private KafkaTemplate<String, OrderEvent> kafkaOrderTemplate;

	@KafkaListener(topics = "new-orders", groupId = "orders-group")
	public void processPayment(String event) throws JsonMappingException, JsonProcessingException {

		logger.info("Recieved event" + event);
		OrderEvent orderEvent = new ObjectMapper().readValue(event, OrderEvent.class);

		CustomerOrder order = orderEvent.getOrder();
		Payment payment = new Payment();
		try {

			// save payment details in db
			payment.setAmount(order.getAmount());
			payment.setMode(order.getPaymentMode());
			payment.setOrderId(order.getOrderId());
			payment.setStatus("SUCCESS");
			this.paymentRepository.save(payment);
			// publish payment created event for inventory microservice to consume.
			PaymentEvent paymentEvent = new PaymentEvent();
			paymentEvent.setOrder(orderEvent.getOrder());
			paymentEvent.setType("PAYMENT_CREATED");
			this.kafkaTemplate.send("new-payments", paymentEvent);
		} catch (Exception e) {
			payment.setOrderId(order.getOrderId());
			payment.setStatus("FAILED");
			paymentRepository.save(payment);
			// reverse previous task
			OrderEvent oe = new OrderEvent();
			oe.setOrder(order);
			oe.setType("ORDER_REVERSED");
			this.kafkaOrderTemplate.send("reversed-orders", orderEvent);

		}

	}
}

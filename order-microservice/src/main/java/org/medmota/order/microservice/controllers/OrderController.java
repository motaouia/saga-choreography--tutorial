package org.medmota.order.microservice.controllers;

import org.medmota.order.microservice.entities.Order;
import org.medmota.order.microservice.models.CustomerOrder;
import org.medmota.order.microservice.models.OrderEvent;
import org.medmota.order.microservice.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private KafkaTemplate<String, OrderEvent> kafkaTemplate;
	
	  @PostMapping("/orders")
	    public void createOrder(@RequestBody CustomerOrder customerOrder) {

		  Order order = new Order();
	        try {
	            // save order in database

	            order.setAmount(customerOrder.getAmount());
	            order.setItem(customerOrder.getItem());
	            order.setQuantity(customerOrder.getQuantity());
	            order.setStatus("CREATED");
	            order = this.orderRepository.save(order);

	            customerOrder.setOrderId(order.getId());

	            // publish order created event for payment microservice to consume.

	            OrderEvent event = new OrderEvent();
	            event.setOrder(customerOrder);
	            event.setType("ORDER_CREATED");
	            this.kafkaTemplate.send("new-orders", event);
	        } catch (Exception e) {

	            order.setStatus("FAILED");
	            this.orderRepository.save(order);

	        }

	    }

}

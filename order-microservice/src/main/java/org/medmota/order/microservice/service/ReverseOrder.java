package org.medmota.order.microservice.service;

import java.util.Optional;

import org.medmota.order.microservice.entities.Order;
import org.medmota.order.microservice.models.OrderEvent;
import org.medmota.order.microservice.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ReverseOrder {
	
	@Autowired
	private OrderRepository orderRepository;
	
	@KafkaListener(topics = "reversed-orders", groupId = "orders-group")
    public void reverseOrder(String event) {
 
        try {
            OrderEvent orderEvent = new ObjectMapper().readValue(event, OrderEvent.class);
            Optional<Order> order = this.orderRepository.findById(orderEvent.getOrder().getOrderId());
            order.ifPresent(o -> {
                o.setStatus("FAILED");
                this.orderRepository.save(o);
            });
        } catch (Exception e) {
 
            e.printStackTrace();
        }
 
    }

}

package org.medmota.inventory.microservice.service;

import org.medmota.inventory.microservice.entities.Inventory;
import org.medmota.inventory.microservice.models.InventoryEvent;
import org.medmota.inventory.microservice.models.PaymentEvent;
import org.medmota.inventory.microservice.repositories.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ReverseInventory {
	
	   @Autowired
	    private InventoryRepository inventoryRepository;

	    @Autowired
	    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

	    @KafkaListener(topics = "reversed-inventory", groupId = "inventory-group")
	    public void reverseInventory(String event) {

	        try {
	            InventoryEvent inventoryEvent = new ObjectMapper().readValue(event, InventoryEvent.class);
	            Iterable<Inventory> inv = this.inventoryRepository.findByItem(inventoryEvent.getOrder().getItem());
	            inv.forEach(i -> {
	                i.setQuantity(i.getQuantity() + inventoryEvent.getOrder().getQuantity());
	                this.inventoryRepository.save(i);
	            });
	            // reverse previous task
	            PaymentEvent paymentEvent = new PaymentEvent();
	            paymentEvent.setOrder(inventoryEvent.getOrder());
	            paymentEvent.setType("PAYMENT_REVERSED");
	            this.kafkaTemplate.send("reversed-payments", paymentEvent);
	        } catch (Exception e) {

	            e.printStackTrace();

	        }
	    }

}

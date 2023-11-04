package org.medmota.inventory.microservice.controller;

import org.medmota.inventory.microservice.entities.Inventory;
import org.medmota.inventory.microservice.models.CustomerOrder;
import org.medmota.inventory.microservice.models.InventoryEvent;
import org.medmota.inventory.microservice.models.PaymentEvent;
import org.medmota.inventory.microservice.models.Stock;
import org.medmota.inventory.microservice.repositories.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class InventoryController {

	@Autowired
	private InventoryRepository inventoryRepository;

	@Autowired
	private KafkaTemplate<String, InventoryEvent> kafkaTemplate;

	@Autowired
	private KafkaTemplate<String, PaymentEvent> kafkaPaymentTemplate;

	@KafkaListener(topics = "new-payments", groupId = "payments-group")
	public void updateInventory(String paymentEvent) throws JsonMappingException, JsonProcessingException {

		InventoryEvent event = new InventoryEvent();

		PaymentEvent p = new ObjectMapper().readValue(paymentEvent, PaymentEvent.class);
		CustomerOrder order = p.getOrder();

		try {
			// update stock in database
			Iterable<Inventory> inventories = this.inventoryRepository.findByItem(order.getItem());

			boolean exists = inventories.iterator().hasNext();

			if (!exists)
				throw new Exception("Stock not available");

			inventories.forEach(i -> {
				i.setQuantity(i.getQuantity() - order.getQuantity());

				this.inventoryRepository.save(i);
			});

			event.setType("INVENTORY_UPDATED");
			event.setOrder(p.getOrder());
			this.kafkaTemplate.send("new-inventory", event);

		} catch (Exception e) {

			// reverse previous task
			PaymentEvent pe = new PaymentEvent();
			pe.setOrder(order);
			pe.setType("PAYMENT_REVERSED");
			this.kafkaPaymentTemplate.send("reversed-payments", pe);

		}

	}

	@PostMapping("/inventory")
	public void addInventory(@RequestBody Stock stock) {

		Iterable<Inventory> items = this.inventoryRepository.findByItem(stock.getItem());

		if (items.iterator().hasNext()) {

			items.forEach(i -> {

				i.setQuantity(stock.getQuantity() + i.getQuantity());
				this.inventoryRepository.save(i);
			});
		} else {

			Inventory i = new Inventory();
			i.setItem(stock.getItem());
			i.setQuantity(stock.getQuantity());
			this.inventoryRepository.save(i);
		}
	}

}

package org.medmota.inventory.microservice.models;

public class PaymentEvent {

	private String type;
	private CustomerOrder customerOrder;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public CustomerOrder getOrder() {
		return customerOrder;
	}

	public void setOrder(CustomerOrder customerOrder) {
		this.customerOrder = customerOrder;
	}

}

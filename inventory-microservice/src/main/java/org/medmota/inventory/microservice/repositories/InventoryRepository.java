package org.medmota.inventory.microservice.repositories;

import org.medmota.inventory.microservice.entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long>{

	Iterable<Inventory> findByItem(String item);
}

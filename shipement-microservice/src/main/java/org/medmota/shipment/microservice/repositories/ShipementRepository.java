package org.medmota.shipment.microservice.repositories;

import org.medmota.shipment.microservice.entities.Shipement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipementRepository extends JpaRepository<Shipement, Long>{

}

## Saga Pattern in Microservices
https://www.baeldung.com/cs/saga-pattern-microservices
https://fullstackdeveloper.guru/2023/05/11/how-to-implement-saga-design-pattern-in-spring-boot/

# What Is Saga Architecture Pattern?
The Saga architecture pattern provides transaction management using a sequence of local transactions.

A local transaction is the unit of work performed by a Saga participant. Every operation that is part of the Saga can be rolled back by a compensating transaction. Further, the Saga pattern guarantees that either all operations complete successfully or the corresponding compensation transactions are run to undo the work previously completed.

In the Saga pattern, a compensating transaction must be idempotent and retryable. These two principles ensure that we can manage transactions without any manual intervention.
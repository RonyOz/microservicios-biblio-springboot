# Kafka en Microservicios de Circulación y Notificación — Taller literal

> **For agentic workers:** Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Migrar la devolución de libros a Kaka (topic `devolucion-libro`) dejando la única tarea manual al usuario como crear el topic desde Offset Explorer (Kafka Tool). `prestarLibro` y RabbitMQ se mantienen como están.

**Architecture:** Kafka (confluentinc/cp-kafka + cp-zookeeper) corre en red `host` del repo y queda en `localhost:9092`. `microservicio-circulacion` publica `NotificacionDTO` al topic `devolucion-libro` cuando devuelve un libro; `microservicio-notificacion` lo consume con `@KafkaListener`.

**Tech Stack:** Apache Kafka, Spring Kafka, Docker Compose, Spring Boot 3.3.2.

---

## Files

- Modify: `docker-compose.yml`
- Modify: `microservicio-circulacion/pom.xml`, `.../application.properties`, `.../service/CirculacionService.java`
- Create: `microservicio-circulacion/.../config/KafkaConfig.java`, `microservicio-circulacion/src/test/java/co/analisys/biblioteca/service/CirculacionServiceTest.java`
- Modify: `microservicio-notificacion/pom.xml`, `.../application.properties`, `.../service/NotificacionService.java`

---

### Task 1: Añadir Zookeeper + Kafka al docker-compose

Añadir tras `rabbitmq` (mismo patrón del repo: `network_mode: host`):

```yaml
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    network_mode: host
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:latest
    network_mode: host
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: localhost:2181
      KAFKA_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

Validar: `docker compose config --quiet`.

### Task 2: Productor en microservicio-circulacion

`pom.xml`: dependencia `org.springframework.kafka:spring-kafka` (versión gestionada por Boot).
`application.properties`:

```properties
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

### Task 3: KafkaConfig

Crear `config/KafkaConfig.java` con bean `KafkaTemplate<String, NotificacionDTO>`.

### Task 4: TDD en CirculacionService

1. Test `CirculacionServiceTest` (Mockito) que verifica `kafkaTemplate.send("devolucion-libro", notif)` en `devolverLibro`. Debe fallar (NPE en `rabbitTemplate`).
2. Refactor: en `devolverLibro` reemplazar `rabbitTemplate.convertAndSend(...)` por `kafkaTemplate.send("devolucion-libro", notificacion)`. Mantener `RabbitTemplate` (lo usa `prestarLibro`).
3. Re-ejecutar test → PASS.

### Task 5: Consumidor en microservicio-notificacion

`pom.xml`: `spring-kafka`. `application.properties`:

```properties
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
spring.kafka.consumer.group-id=notificacion-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
```

### Task 6: KafkaListener

`NotificacionService.enviarNotificacion` se anota con `@KafkaListener(topics = "devolucion-libro", groupId = "notificacion-group")`. `NotificacionConsumer` (Rabbit) se mantiene.

### Task 7: Verificación

- `docker compose config --quiet`
- `./mvnw test` en ambos microservicios (sin broker los tests cargan el contexto igual; el listener solo loguea warnings).

### Task 8 (manual — usuario): Offset Explorer

1. `docker compose up -d`
2. Kafka Tool → Add Cluster: Bootstrap `localhost:9092`, Zookeeper `localhost:2181`.
3. Crear topic `devolucion-libro`, 1 partición, RF 1.
4. `POST /circulacion/devolver` → log de notificacion: `Notificación enviada a ...: Libro devuelto: ...`.
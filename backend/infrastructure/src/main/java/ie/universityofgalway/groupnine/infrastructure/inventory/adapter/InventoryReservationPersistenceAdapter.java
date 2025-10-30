package ie.universityofgalway.groupnine.infrastructure.inventory.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ie.universityofgalway.groupnine.domain.inventory.InventoryReservation;
import ie.universityofgalway.groupnine.domain.inventory.InventoryReservationId;
import ie.universityofgalway.groupnine.domain.inventory.InventoryReservationStatus;
import ie.universityofgalway.groupnine.domain.inventory.ReservationItem;
import ie.universityofgalway.groupnine.domain.order.OrderId;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.infrastructure.inventory.jpa.InventoryReservationEntity;
import ie.universityofgalway.groupnine.infrastructure.inventory.jpa.InventoryReservationJpaRepository;
import ie.universityofgalway.groupnine.service.inventory.port.InventoryReservationRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * JPA‑backed repository adapter for {@code InventoryReservation} aggregates. Performs
 * JSON serialization of reservation items and maps to/from entity representations.
 */
@Component
public class InventoryReservationPersistenceAdapter implements InventoryReservationRepository {
    private final InventoryReservationJpaRepository repo;
    private final ObjectMapper mapper;

    public InventoryReservationPersistenceAdapter(InventoryReservationJpaRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    /** Persists or updates a reservation and returns the mapped domain object. */
    @Override
    public InventoryReservation save(InventoryReservation reservation) {
        InventoryReservationEntity e = toEntity(reservation);
        InventoryReservationEntity saved = repo.save(e);
        return toDomain(saved);
    }

    /** Finds a reservation by id. */
    @Override
    public Optional<InventoryReservation> findById(InventoryReservationId id) {
        return repo.findById(id.value()).map(this::toDomain);
    }

    /** Finds a reservation by order id. */
    @Override
    public Optional<InventoryReservation> findByOrderId(OrderId orderId) {
        return repo.findByOrderId(orderId.value()).map(this::toDomain);
    }

    private InventoryReservationEntity toEntity(InventoryReservation r) {
        InventoryReservationEntity e = new InventoryReservationEntity();
        e.setId(r.getId().value());
        e.setOrderId(r.getOrderId().value());
        e.setItemsJson(serializeItems(r.getItems()));
        e.setStatus(r.getStatus().name());
        e.setExpiresAt(r.getExpiresAt());
        e.setCreatedAt(r.getCreatedAt());
        e.setUpdatedAt(r.getUpdatedAt());
        return e;
    }

    private InventoryReservation toDomain(InventoryReservationEntity e) {
        return new InventoryReservation(new InventoryReservationId(e.getId()), new OrderId(e.getOrderId()),
                deserializeItems(e.getItemsJson()), InventoryReservationStatus.valueOf(e.getStatus()),
                e.getCreatedAt(), e.getUpdatedAt(), e.getExpiresAt());
    }

    private String serializeItems(List<ReservationItem> items) {
        try {
            List<Map<String, Object>> list = new ArrayList<>();
            for (ReservationItem it : items) {
                list.add(Map.of("variant_id", it.getVariantId().getId().toString(), "quantity", it.getQuantity()));
            }
            return mapper.writeValueAsString(list);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private List<ReservationItem> deserializeItems(String json) {
        try {
            List<Map<String, Object>> list = mapper.readValue(json, List.class);
            List<ReservationItem> out = new ArrayList<>();
            for (Map<String, Object> m : list) {
                UUID vid = UUID.fromString((String) m.get("variant_id"));
                int qty = (Integer) m.get("quantity");
                out.add(new ReservationItem(new VariantId(vid), qty));
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}

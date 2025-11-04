package ie.universityofgalway.groupnine.infrastructure.inventory.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.universityofgalway.groupnine.domain.inventory.InventoryReservation;
import ie.universityofgalway.groupnine.domain.inventory.InventoryReservationId;
import ie.universityofgalway.groupnine.domain.inventory.InventoryReservationStatus;
import ie.universityofgalway.groupnine.domain.inventory.ReservationItem;
import ie.universityofgalway.groupnine.domain.order.OrderId;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.infrastructure.inventory.jpa.InventoryReservationEntity;
import ie.universityofgalway.groupnine.infrastructure.inventory.jpa.InventoryReservationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class InventoryReservationPersistenceAdapterTest {

    private InventoryReservationJpaRepository repo;
    private ObjectMapper mapper;
    private InventoryReservationPersistenceAdapter adapter;

    @BeforeEach
    void setup() {
        repo = Mockito.mock(InventoryReservationJpaRepository.class);
        mapper = new ObjectMapper();
        adapter = new InventoryReservationPersistenceAdapter(repo, mapper);
    }

    private InventoryReservation sampleReservation() {
        OrderId oid = OrderId.of(UUID.randomUUID());
        List<ReservationItem> items = List.of(new ReservationItem(new VariantId(UUID.randomUUID()), 2));
        InventoryReservation r = InventoryReservation.pending(oid, items, Instant.parse("2024-01-01T00:10:00Z"));
        r.markReserved();
        return r;
    }

    @Test
    void save_maps_domain_to_entity_and_back() throws Exception {
        InventoryReservation r = sampleReservation();

        ArgumentCaptor<InventoryReservationEntity> cap = ArgumentCaptor.forClass(InventoryReservationEntity.class);
        when(repo.save(any(InventoryReservationEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        InventoryReservation saved = adapter.save(r);

        assertEquals(r.getOrderId(), saved.getOrderId());
        assertEquals(InventoryReservationStatus.RESERVED, saved.getStatus());
        assertEquals(1, saved.getItems().size());
        assertEquals(r.getExpiresAt(), saved.getExpiresAt());
    }

    @Test
    void findById_maps_entity_to_domain() {
        UUID id = UUID.randomUUID();
        UUID oid = UUID.randomUUID();
        UUID vid = UUID.randomUUID();
        InventoryReservationEntity e = new InventoryReservationEntity();
        e.setId(id);
        e.setOrderId(oid);
        e.setStatus("PENDING");
        e.setItemsJson("[{\"variant_id\":\"" + vid + "\",\"quantity\":3}]");
        e.setExpiresAt(Instant.parse("2024-01-01T00:20:00Z"));
        e.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        e.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));

        when(repo.findById(id)).thenReturn(Optional.of(e));

        InventoryReservation found = adapter.findById(new InventoryReservationId(id)).orElseThrow();
        assertEquals(new InventoryReservationId(id), found.getId());
        assertEquals(OrderId.of(oid), found.getOrderId());
        assertEquals(InventoryReservationStatus.PENDING, found.getStatus());
        assertEquals(1, found.getItems().size());
        assertEquals(3, found.getItems().get(0).getQuantity());
        assertEquals(vid, found.getItems().get(0).getVariantId().getId());
    }

    @Test
    void findByOrderId_maps_entity_to_domain() {
        UUID oid = UUID.randomUUID();
        InventoryReservationEntity e = new InventoryReservationEntity();
        e.setId(UUID.randomUUID());
        e.setOrderId(oid);
        e.setStatus("RESERVED");
        e.setItemsJson("[]");
        e.setExpiresAt(Instant.parse("2024-01-01T00:20:00Z"));
        e.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        e.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));

        when(repo.findByOrderId(oid)).thenReturn(Optional.of(e));

        var found = adapter.findByOrderId(OrderId.of(oid)).orElseThrow();
        assertEquals(OrderId.of(oid), found.getOrderId());
        assertEquals(InventoryReservationStatus.RESERVED, found.getStatus());
    }
}


package ie.universityofgalway.groupnine.service.payments.dto;

import ie.universityofgalway.groupnine.domain.payment.OrderSnapshotItem;

import java.util.ArrayList;
import java.util.List;

public final class InventoryReserveRequestDto {
    public final String order_id;
    public final List<Item> items;
    public final String expires_at;

    public InventoryReserveRequestDto(String order_id, List<OrderSnapshotItem> items, String expires_at) {
        this.order_id = order_id;
        this.items = toReserveItems(items);
        this.expires_at = expires_at;
    }

    public static final class Item {
        public final String variant_id;
        public final int quantity;

        public Item(String variant_id, int quantity) {
            this.variant_id = variant_id;
            this.quantity = quantity;
        }
    }


    private static List<InventoryReserveRequestDto.Item> toReserveItems(List<OrderSnapshotItem> items) {
        List<InventoryReserveRequestDto.Item> out = new ArrayList<>();
        for (OrderSnapshotItem it : items) {
            out.add(new InventoryReserveRequestDto.Item(it.getVariantId().getId().toString(), it.getQuantity()));
        }
        return out;
    }
}
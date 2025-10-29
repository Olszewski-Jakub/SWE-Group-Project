package ie.universityofgalway.groupnine.delivery.rest.product;

import ie.universityofgalway.groupnine.delivery.rest.product.dto.AttributeDto;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.ProductManagementResponse;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.ProductRequest;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.StockDto;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.VariantManagementResponse;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.VariantRequest;
import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.admin.UpdateProductCommand;
import ie.universityofgalway.groupnine.service.product.admin.UpdateVariantCommand;

import java.util.List;
import java.util.UUID;

public final class ProductManagementDtoMapper {
    private ProductManagementDtoMapper() {}

    public static Product toDomain(ProductRequest req) {
        List<Variant> variants = req.getVariants() == null ? List.of() :
                req.getVariants().stream().map(ProductManagementDtoMapper::toDomain).toList();
        return new Product(
                null,
                req.getName(),
                req.getDescription(),
                req.getCategory(),
                req.getStatus(),
                variants,
                null,
                null
        );
    }

    public static UpdateProductCommand toUpdateCommand(UUID id, ProductRequest req) {
        return new UpdateProductCommand(new ProductId(id), req.getName(), req.getDescription(), req.getCategory(), req.getStatus());
    }

    public static Variant toDomain(VariantRequest req) {
        UUID vid = req.getId();
        java.util.Currency currency = java.util.Currency.getInstance(req.getPrice().getCurrency().toUpperCase());
        Stock stock = new Stock(req.getStock().getQuantity(), req.getStock().getReserved());
        java.util.List<Attribute> attrs = req.getAttributes() == null ? java.util.List.of()
                : req.getAttributes().stream().map(a -> new Attribute(a.getName(), a.getValue())).toList();
        return new Variant(
                vid == null ? null : new VariantId(vid),
                new Sku(req.getSku()),
                new Money(req.getPrice().getAmount(), currency),
                stock,
                attrs
        );
    }

    public static UpdateVariantCommand toUpdateCommand(UUID id, VariantRequest req) {
        return new UpdateVariantCommand(
                new VariantId(id),
                req.getSku(),
                req.getPrice() == null ? null : req.getPrice().getAmount(),
                req.getPrice() == null ? null : req.getPrice().getCurrency(),
                req.getStock() == null ? null : req.getStock().getQuantity(),
                req.getStock() == null ? null : req.getStock().getReserved(),
                req.getImageUrl(),
                req.getAttributes() == null ? null : req.getAttributes().stream().map(a -> new ie.universityofgalway.groupnine.service.product.admin.UpdateVariantCommand.AttributePair(a.getName(), a.getValue())).toList()
        );
    }

    public static ProductManagementResponse toDto(Product p) {
        return new ProductManagementResponse(
                p.getId().getId(),
                p.getName(),
                p.getDescription(),
                p.getCategory(),
                p.getStatus(),
                p.getVariants().stream().map(ProductManagementDtoMapper::toDto).toList(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    public static VariantManagementResponse toDto(Variant v) {
        return new VariantManagementResponse(
                v.getId().getId(),
                v.getSku().getValue(),
                new MoneyDto(v.getPrice().getAmount(), v.getPrice().getCurrency().getCurrencyCode()),
                new StockDto(v.getStock().getQuantity(), v.getStock().getReserved()),
                null,
                v.getAttributes().stream().map(a -> new AttributeDto(a.name(), a.value())).toList(),
                null,
                null
        );
    }
}

package dev.olaxomi.backend.request;

import lombok.Data;

@Data
public class CreateUpdateProductVariantRequest {
    private double weight;
    private int inventory;
}

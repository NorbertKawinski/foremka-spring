package com.codechievement.foremka.v1.fixture;

import com.codechievement.foremka.v1.api.ScenarioFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductScenarioFactory implements ScenarioFactory<ProductInput, ProductScenario> {
    @Override
    public Class<ProductScenario> getScenarioClass() {
        return ProductScenario.class;
    }

    /**
     * Simulates calling the catalogue service and pricing engine. In a real setup both calls can
     * take several seconds. Discount rules: SEASONAL = 30 % off; all other categories = 10 % off.
     */
    @Override
    public ProductScenario create(ProductInput input) {
        String id = String.format("prod-%08x", input.hashCode());
        int discountedPrice = input.category().equals("SEASONAL")
                ? (int) (input.basePrice() * 0.70)
                : (int) (input.basePrice() * 0.90);
        return new ProductScenario(id, input.name(), input.category(), input.basePrice(), discountedPrice);
    }
}

package com.springbatch.processor;

import org.springframework.batch.item.ItemProcessor;
import com.springbatch.Product;

public class ProductProcessor implements ItemProcessor<Product, Product> {
    @Override
    public Product process(Product product) throws Exception {
        // Example: Apply a discount to the product price
        product.setProductPrice(product.getProductPrice() * 0.9); // 10% discount
        return product;
    }
}

package com.j20_pos.service;

import com.j20_pos.dto.ProductDTO;
import java.util.List;

public interface ProductService {
    List<ProductDTO> getAllProducts();
    ProductDTO getProductById(Long id);
    ProductDTO getProductBySku(String sku);
    ProductDTO getProductByBarcode(String barcode);
    List<ProductDTO> searchProducts(String query, String category);
    ProductDTO createProduct(ProductDTO dto);
    ProductDTO updateProduct(Long id, ProductDTO dto);
    void deleteProduct(Long id);
}

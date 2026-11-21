package com.j20_pos.service;

import com.j20_pos.dto.ProductDTO;
import com.j20_pos.entity.InventoryMovement;
import com.j20_pos.entity.Product;
import com.j20_pos.exception.BadRequestException;
import com.j20_pos.exception.ResourceNotFoundException;
import com.j20_pos.repository.InventoryMovementRepository;
import com.j20_pos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final InventoryMovementRepository movementRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = findEntityById(id);
        return mapToDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return mapToDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with barcode: " + barcode));
        return mapToDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String query, String category) {
        List<Product> products;

        if (query != null && !query.trim().isEmpty()) {
            products = productRepository.searchProducts(query.trim());
        } else if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            products = productRepository.findByCategoryIgnoreCase(category.trim());
        } else {
            products = productRepository.findAll();
        }

        // Secondary category filter if both query and category are provided
        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            products = products.stream()
                    .filter(p -> p.getCategory().equalsIgnoreCase(category.trim()))
                    .collect(Collectors.toList());
        }

        return products.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        // Validation: Unique SKU
        if (productRepository.existsBySku(dto.getSku())) {
            throw new BadRequestException("Product with SKU '" + dto.getSku() + "' already exists");
        }

        // Validation: Unique Barcode if supplied
        if (dto.getBarcode() != null && !dto.getBarcode().trim().isEmpty() &&
                productRepository.existsByBarcode(dto.getBarcode().trim())) {
            throw new BadRequestException("Product with barcode '" + dto.getBarcode() + "' already exists");
        }

        Product product = Product.builder()
                .name(dto.getName().trim())
                .sku(dto.getSku().trim().toUpperCase())
                .barcode(dto.getBarcode() != null ? dto.getBarcode().trim() : null)
                .category(dto.getCategory().trim())
                .costPrice(dto.getCostPrice())
                .sellingPrice(dto.getSellingPrice())
                .taxRate(dto.getTaxRate())
                .stockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0)
                .minStockLevel(dto.getMinStockLevel() != null ? dto.getMinStockLevel() : 10)
                .unit(dto.getUnit().trim())
                .status(dto.getStatus() != null ? dto.getStatus().trim().toUpperCase() : "ACTIVE")
                .description(dto.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Product saved = productRepository.save(product);

        // Record initial inventory movement if initial stock > 0
        if (saved.getStockQuantity() > 0) {
            InventoryMovement movement = InventoryMovement.builder()
                    .product(saved)
                    .movementType("STOCK_IN")
                    .quantity(saved.getStockQuantity())
                    .previousStock(0)
                    .newStock(saved.getStockQuantity())
                    .reason("Initial stock upon product creation")
                    .referenceNumber("INIT-" + saved.getSku())
                    .performedBy("System")
                    .timestamp(LocalDateTime.now())
                    .build();
            movementRepository.save(movement);
        }

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product product = findEntityById(id);

        // Validation: SKU uniqueness if changed
        if (!product.getSku().equalsIgnoreCase(dto.getSku().trim()) &&
                productRepository.existsBySku(dto.getSku().trim())) {
            throw new BadRequestException("Product with SKU '" + dto.getSku() + "' already exists");
        }

        // Validation: Barcode uniqueness if changed
        if (dto.getBarcode() != null && !dto.getBarcode().trim().isEmpty() &&
                (product.getBarcode() == null || !product.getBarcode().equalsIgnoreCase(dto.getBarcode().trim())) &&
                productRepository.existsByBarcode(dto.getBarcode().trim())) {
            throw new BadRequestException("Product with barcode '" + dto.getBarcode() + "' already exists");
        }

        // Update fields
        product.setName(dto.getName().trim());
        product.setSku(dto.getSku().trim().toUpperCase());
        product.setBarcode(dto.getBarcode() != null ? dto.getBarcode().trim() : null);
        product.setCategory(dto.getCategory().trim());
        product.setCostPrice(dto.getCostPrice());
        product.setSellingPrice(dto.getSellingPrice());
        product.setTaxRate(dto.getTaxRate());
        product.setMinStockLevel(dto.getMinStockLevel());
        product.setUnit(dto.getUnit().trim());
        if (dto.getStatus() != null) {
            product.setStatus(dto.getStatus().trim().toUpperCase());
        }
        product.setDescription(dto.getDescription());
        product.setUpdatedAt(LocalDateTime.now());

        Product updated = productRepository.save(product);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findEntityById(id);
        // Soft delete by setting status to INACTIVE or delete if no sales orders
        product.setStatus("INACTIVE");
        productRepository.save(product);
    }

    private Product findEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    private ProductDTO mapToDTO(Product p) {
        return ProductDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .sku(p.getSku())
                .barcode(p.getBarcode())
                .category(p.getCategory())
                .costPrice(p.getCostPrice())
                .sellingPrice(p.getSellingPrice())
                .taxRate(p.getTaxRate())
                .stockQuantity(p.getStockQuantity())
                .minStockLevel(p.getMinStockLevel())
                .unit(p.getUnit())
                .status(p.getStatus())
                .description(p.getDescription())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}

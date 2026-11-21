package com.j20_pos.service;

import com.j20_pos.dto.*;
import com.j20_pos.entity.Order;
import com.j20_pos.entity.OrderItem;
import com.j20_pos.entity.Product;
import com.j20_pos.exception.BadRequestException;
import com.j20_pos.repository.OrderRepository;
import com.j20_pos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    private static final DateTimeFormatter DATE_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DAILY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter MONTH_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    @Override
    @Transactional(readOnly = true)
    public DashboardReportDTO getDashboardReport() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);

        SalesSummaryDTO salesSummary = getSalesSummary(null, null);
        List<SalesTrendDTO> weeklyTrend = getSalesTrend("daily", sevenDaysAgo, today);
        List<TopSellingProductDTO> topProducts = getTopSellingProducts(5, null, null);
        List<CategorySalesDTO> categoryBreakdown = getCategorySales(null, null);
        List<PaymentMethodSummaryDTO> paymentBreakdown = getPaymentSummary(null, null);
        InventoryValuationDTO inventoryOverview = getInventoryValuation();
        LowStockReportDTO lowStockAlerts = getLowStockReport();

        return DashboardReportDTO.builder()
                .salesSummary(salesSummary)
                .weeklyTrend(weeklyTrend)
                .topProducts(topProducts)
                .categoryBreakdown(categoryBreakdown)
                .paymentBreakdown(paymentBreakdown)
                .inventoryOverview(inventoryOverview)
                .lowStockAlerts(lowStockAlerts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesSummaryDTO getSalesSummary(LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        List<Order> orders = getOrdersForRange(startDate, endDate);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalOrders = 0;
        int totalUnitsSold = 0;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal netSales = BigDecimal.ZERO;

        Map<LocalDate, BigDecimal> dailyRevenueMap = new HashMap<>();

        for (Order o : orders) {
            if (!"COMPLETED".equalsIgnoreCase(o.getStatus())) continue;

            totalOrders++;
            BigDecimal grandTotal = o.getGrandTotal() != null ? o.getGrandTotal() : BigDecimal.ZERO;
            BigDecimal subtotal = o.getSubtotal() != null ? o.getSubtotal() : BigDecimal.ZERO;
            BigDecimal taxAmount = o.getTaxAmount() != null ? o.getTaxAmount() : BigDecimal.ZERO;
            BigDecimal discount = o.getDiscountAmount() != null ? o.getDiscountAmount() : BigDecimal.ZERO;

            totalRevenue = totalRevenue.add(grandTotal);
            netSales = netSales.add(subtotal);
            totalTax = totalTax.add(taxAmount);
            totalDiscount = totalDiscount.add(discount);

            if (o.getItems() != null) {
                for (OrderItem oi : o.getItems()) {
                    if (oi.getQuantity() != null) {
                        totalUnitsSold += oi.getQuantity();
                    }
                }
            }

            LocalDate date = o.getOrderDate() != null ? o.getOrderDate() : LocalDate.now();
            dailyRevenueMap.put(date, dailyRevenueMap.getOrDefault(date, BigDecimal.ZERO).add(grandTotal));
        }

        BigDecimal highestDayRevenue = BigDecimal.ZERO;
        String highestDayLabel = "-";
        for (Map.Entry<LocalDate, BigDecimal> entry : dailyRevenueMap.entrySet()) {
            if (entry.getValue().compareTo(highestDayRevenue) > 0) {
                highestDayRevenue = entry.getValue();
                highestDayLabel = entry.getKey().format(DAILY_LABEL_FORMATTER);
            }
        }

        BigDecimal aov = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        return SalesSummaryDTO.builder()
                .totalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP))
                .totalOrders(totalOrders)
                .totalUnitsSold(totalUnitsSold)
                .totalDiscount(totalDiscount.setScale(2, RoundingMode.HALF_UP))
                .totalTax(totalTax.setScale(2, RoundingMode.HALF_UP))
                .netSales(netSales.setScale(2, RoundingMode.HALF_UP))
                .averageOrderValue(aov)
                .highestDayRevenue(highestDayRevenue.setScale(2, RoundingMode.HALF_UP))
                .highestDayLabel(highestDayLabel)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesTrendDTO> getSalesTrend(String period, LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        String aggregation = (period != null && !period.isBlank()) ? period.trim().toLowerCase() : "daily";

        List<Order> orders = getOrdersForRange(startDate, endDate);
        Map<String, TrendBucket> bucketMap = new TreeMap<>();

        for (Order o : orders) {
            if (!"COMPLETED".equalsIgnoreCase(o.getStatus())) continue;

            LocalDate date = o.getOrderDate() != null ? o.getOrderDate() : LocalDate.now();
            String key;
            String label;

            if ("weekly".equals(aggregation)) {
                int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                int weekYear = date.get(IsoFields.WEEK_BASED_YEAR);
                key = String.format("%d-W%02d", weekYear, week);
                label = String.format("Week %d, %d", week, weekYear);
            } else if ("monthly".equals(aggregation)) {
                key = date.format(MONTH_KEY_FORMATTER);
                label = date.format(MONTH_LABEL_FORMATTER);
            } else {
                // daily default
                key = date.format(DATE_KEY_FORMATTER);
                label = date.format(DAILY_LABEL_FORMATTER);
            }

            TrendBucket bucket = bucketMap.computeIfAbsent(key, k -> new TrendBucket(key, label));
            bucket.orders++;
            BigDecimal grandTotal = o.getGrandTotal() != null ? o.getGrandTotal() : BigDecimal.ZERO;
            bucket.sales = bucket.sales.add(grandTotal);

            if (o.getItems() != null) {
                for (OrderItem oi : o.getItems()) {
                    if (oi.getQuantity() != null) {
                        bucket.unitsSold += oi.getQuantity();
                    }
                }
            }
        }

        List<SalesTrendDTO> result = new ArrayList<>();
        for (TrendBucket b : bucketMap.values()) {
            BigDecimal aov = b.orders > 0
                    ? b.sales.divide(BigDecimal.valueOf(b.orders), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            result.add(SalesTrendDTO.builder()
                    .dateKey(b.dateKey)
                    .periodLabel(b.periodLabel)
                    .sales(b.sales.setScale(2, RoundingMode.HALF_UP))
                    .orders(b.orders)
                    .unitsSold(b.unitsSold)
                    .averageOrderValue(aov)
                    .build());
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopSellingProductDTO> getTopSellingProducts(Integer limit, LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        int maxResults = (limit != null && limit > 0) ? limit : 10;

        List<Order> orders = getOrdersForRange(startDate, endDate);
        Map<Long, ProductSalesAggregator> aggMap = new HashMap<>();
        BigDecimal totalSalesRevenue = BigDecimal.ZERO;

        for (Order o : orders) {
            if (!"COMPLETED".equalsIgnoreCase(o.getStatus()) || o.getItems() == null) continue;

            for (OrderItem oi : o.getItems()) {
                Product product = oi.getProduct();
                if (product == null) continue;

                Long pId = product.getId();
                int qty = oi.getQuantity() != null ? oi.getQuantity() : 0;
                BigDecimal rev = oi.getTotal() != null ? oi.getTotal() : BigDecimal.ZERO;
                totalSalesRevenue = totalSalesRevenue.add(rev);

                ProductSalesAggregator agg = aggMap.computeIfAbsent(pId, id -> new ProductSalesAggregator(product));
                agg.unitsSold += qty;
                agg.totalRevenue = agg.totalRevenue.add(rev);
            }
        }

        List<ProductSalesAggregator> sorted = new ArrayList<>(aggMap.values());
        sorted.sort((a, b) -> {
            int cmp = Integer.compare(b.unitsSold, a.unitsSold);
            return cmp != 0 ? cmp : b.totalRevenue.compareTo(a.totalRevenue);
        });

        List<TopSellingProductDTO> result = new ArrayList<>();
        int rank = 1;
        for (ProductSalesAggregator agg : sorted) {
            if (result.size() >= maxResults) break;

            Product p = agg.product;
            BigDecimal costPrice = p.getCostPrice() != null ? p.getCostPrice() : BigDecimal.ZERO;
            BigDecimal totalCost = costPrice.multiply(BigDecimal.valueOf(agg.unitsSold)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal grossProfit = agg.totalRevenue.subtract(totalCost).setScale(2, RoundingMode.HALF_UP);

            double marginPercent = agg.totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? grossProfit.divide(agg.totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;

            double marketShare = totalSalesRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? agg.totalRevenue.divide(totalSalesRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;

            int stock = p.getStockQuantity() != null ? p.getStockQuantity() : 0;
            int minStock = p.getMinStockLevel() != null ? p.getMinStockLevel() : 0;
            String stockStatus;
            if (stock <= 0) {
                stockStatus = "OUT_OF_STOCK";
            } else if (stock <= minStock) {
                stockStatus = "LOW_STOCK";
            } else {
                stockStatus = "IN_STOCK";
            }

            result.add(TopSellingProductDTO.builder()
                    .rank(rank++)
                    .productId(p.getId())
                    .name(p.getName())
                    .sku(p.getSku())
                    .category(p.getCategory())
                    .unitsSold(agg.unitsSold)
                    .revenue(agg.totalRevenue.setScale(2, RoundingMode.HALF_UP))
                    .cost(totalCost)
                    .grossProfit(grossProfit)
                    .profitMarginPercent(Math.round(marginPercent * 100.0) / 100.0)
                    .currentStock(stock)
                    .stockStatus(stockStatus)
                    .marketSharePercent(Math.round(marketShare * 100.0) / 100.0)
                    .build());
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorySalesDTO> getCategorySales(LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        List<Order> orders = getOrdersForRange(startDate, endDate);

        Map<String, CategoryAggregator> categoryMap = new HashMap<>();
        BigDecimal grandRevenue = BigDecimal.ZERO;

        for (Order o : orders) {
            if (!"COMPLETED".equalsIgnoreCase(o.getStatus()) || o.getItems() == null) continue;

            for (OrderItem oi : o.getItems()) {
                String category = (oi.getProduct() != null && oi.getProduct().getCategory() != null)
                        ? oi.getProduct().getCategory().trim() : "General";

                int qty = oi.getQuantity() != null ? oi.getQuantity() : 0;
                BigDecimal rev = oi.getTotal() != null ? oi.getTotal() : BigDecimal.ZERO;
                grandRevenue = grandRevenue.add(rev);

                CategoryAggregator agg = categoryMap.computeIfAbsent(category, CategoryAggregator::new);
                agg.unitsSold += qty;
                agg.totalRevenue = agg.totalRevenue.add(rev);
                agg.orderIds.add(o.getId());
            }
        }

        List<CategoryAggregator> list = new ArrayList<>(categoryMap.values());
        list.sort((a, b) -> b.totalRevenue.compareTo(a.totalRevenue));

        List<CategorySalesDTO> result = new ArrayList<>();
        for (CategoryAggregator agg : list) {
            double sharePercent = grandRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? agg.totalRevenue.divide(grandRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;

            BigDecimal avgUnitPrice = agg.unitsSold > 0
                    ? agg.totalRevenue.divide(BigDecimal.valueOf(agg.unitsSold), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            result.add(CategorySalesDTO.builder()
                    .category(agg.category)
                    .unitsSold(agg.unitsSold)
                    .totalRevenue(agg.totalRevenue.setScale(2, RoundingMode.HALF_UP))
                    .ordersCount(agg.orderIds.size())
                    .revenueSharePercent(Math.round(sharePercent * 100.0) / 100.0)
                    .averageUnitSellingPrice(avgUnitPrice)
                    .build());
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodSummaryDTO> getPaymentSummary(LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        List<Order> orders = getOrdersForRange(startDate, endDate);

        Map<String, PaymentAggregator> paymentMap = new LinkedHashMap<>();
        paymentMap.put("CASH", new PaymentAggregator("CASH", "Cash Payment"));
        paymentMap.put("UPI", new PaymentAggregator("UPI", "UPI Instant Transfer"));
        paymentMap.put("CARD", new PaymentAggregator("CARD", "Debit / Credit Card"));

        BigDecimal totalSales = BigDecimal.ZERO;

        for (Order o : orders) {
            if (!"COMPLETED".equalsIgnoreCase(o.getStatus())) continue;

            String method = o.getPaymentMethod() != null ? o.getPaymentMethod().toUpperCase() : "CASH";
            BigDecimal grandTotal = o.getGrandTotal() != null ? o.getGrandTotal() : BigDecimal.ZERO;
            totalSales = totalSales.add(grandTotal);

            PaymentAggregator agg = paymentMap.computeIfAbsent(method, m -> new PaymentAggregator(m, m + " Payment"));
            agg.totalAmount = agg.totalAmount.add(grandTotal);
            agg.transactionCount++;
        }

        List<PaymentMethodSummaryDTO> result = new ArrayList<>();
        for (PaymentAggregator agg : paymentMap.values()) {
            double share = totalSales.compareTo(BigDecimal.ZERO) > 0
                    ? agg.totalAmount.divide(totalSales, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;

            BigDecimal avgTicket = agg.transactionCount > 0
                    ? agg.totalAmount.divide(BigDecimal.valueOf(agg.transactionCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            result.add(PaymentMethodSummaryDTO.builder()
                    .method(agg.method)
                    .label(agg.label)
                    .totalAmount(agg.totalAmount.setScale(2, RoundingMode.HALF_UP))
                    .transactionCount(agg.transactionCount)
                    .sharePercent(Math.round(share * 100.0) / 100.0)
                    .averageTicketSize(avgTicket)
                    .build());
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryValuationDTO getInventoryValuation() {
        List<Product> products = productRepository.findAll();

        int totalSkus = products.size();
        int totalStockUnits = 0;
        BigDecimal totalCostValuation = BigDecimal.ZERO;
        BigDecimal totalRetailValuation = BigDecimal.ZERO;

        List<InventoryValuationDTO.ValuationItemDTO> items = new ArrayList<>();

        for (Product p : products) {
            int stock = p.getStockQuantity() != null ? p.getStockQuantity() : 0;
            BigDecimal costPrice = p.getCostPrice() != null ? p.getCostPrice() : BigDecimal.ZERO;
            BigDecimal sellingPrice = p.getSellingPrice() != null ? p.getSellingPrice() : BigDecimal.ZERO;

            BigDecimal itemCostVal = costPrice.multiply(BigDecimal.valueOf(stock)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal itemRetailVal = sellingPrice.multiply(BigDecimal.valueOf(stock)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal projectedProfit = itemRetailVal.subtract(itemCostVal).setScale(2, RoundingMode.HALF_UP);

            double marginPercent = itemRetailVal.compareTo(BigDecimal.ZERO) > 0
                    ? projectedProfit.divide(itemRetailVal, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;

            totalStockUnits += stock;
            totalCostValuation = totalCostValuation.add(itemCostVal);
            totalRetailValuation = totalRetailValuation.add(itemRetailVal);

            items.add(InventoryValuationDTO.ValuationItemDTO.builder()
                    .productId(p.getId())
                    .sku(p.getSku())
                    .name(p.getName())
                    .category(p.getCategory())
                    .stockQuantity(stock)
                    .unit(p.getUnit())
                    .costPrice(costPrice)
                    .sellingPrice(sellingPrice)
                    .totalCostValue(itemCostVal)
                    .totalRetailValue(itemRetailVal)
                    .projectedProfit(projectedProfit)
                    .marginPercent(Math.round(marginPercent * 100.0) / 100.0)
                    .status(p.getStatus())
                    .build());
        }

        BigDecimal totalProjectedProfit = totalRetailValuation.subtract(totalCostValuation).setScale(2, RoundingMode.HALF_UP);
        double overallMargin = totalRetailValuation.compareTo(BigDecimal.ZERO) > 0
                ? totalProjectedProfit.divide(totalRetailValuation, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        return InventoryValuationDTO.builder()
                .totalSkus(totalSkus)
                .totalStockUnits(totalStockUnits)
                .totalCostValuation(totalCostValuation.setScale(2, RoundingMode.HALF_UP))
                .totalRetailValuation(totalRetailValuation.setScale(2, RoundingMode.HALF_UP))
                .totalProjectedProfit(totalProjectedProfit)
                .overallMarginPercent(Math.round(overallMargin * 100.0) / 100.0)
                .items(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LowStockReportDTO getLowStockReport() {
        List<Product> products = productRepository.findAll();

        List<LowStockReportDTO.LowStockItemDTO> lowStockItems = new ArrayList<>();
        int outOfStockCount = 0;
        int criticalStockCount = 0;
        BigDecimal totalEstimatedReorderCost = BigDecimal.ZERO;

        for (Product p : products) {
            if (!"ACTIVE".equalsIgnoreCase(p.getStatus())) continue;

            int currentStock = p.getStockQuantity() != null ? p.getStockQuantity() : 0;
            int minStockLevel = p.getMinStockLevel() != null ? p.getMinStockLevel() : 0;

            if (currentStock <= minStockLevel) {
                int shortfall = Math.max(0, minStockLevel - currentStock);
                int recommendedReorder = Math.max(minStockLevel * 2 - currentStock, 10);
                BigDecimal unitCost = p.getCostPrice() != null ? p.getCostPrice() : BigDecimal.ZERO;
                BigDecimal estimatedCost = unitCost.multiply(BigDecimal.valueOf(recommendedReorder)).setScale(2, RoundingMode.HALF_UP);

                String urgency;
                if (currentStock <= 0) {
                    urgency = "OUT_OF_STOCK";
                    outOfStockCount++;
                } else if (currentStock <= minStockLevel / 2) {
                    urgency = "CRITICAL";
                    criticalStockCount++;
                } else {
                    urgency = "LOW";
                }

                totalEstimatedReorderCost = totalEstimatedReorderCost.add(estimatedCost);

                lowStockItems.add(LowStockReportDTO.LowStockItemDTO.builder()
                        .productId(p.getId())
                        .sku(p.getSku())
                        .name(p.getName())
                        .category(p.getCategory())
                        .currentStock(currentStock)
                        .minStockLevel(minStockLevel)
                        .unit(p.getUnit())
                        .shortfall(shortfall)
                        .recommendedReorderQty(recommendedReorder)
                        .unitCostPrice(unitCost)
                        .estimatedReorderCost(estimatedCost)
                        .urgency(urgency)
                        .status(p.getStatus())
                        .build());
            }
        }

        return LowStockReportDTO.builder()
                .totalLowStockItems(lowStockItems.size())
                .outOfStockCount(outOfStockCount)
                .criticalStockCount(criticalStockCount)
                .estimatedReorderCost(totalEstimatedReorderCost.setScale(2, RoundingMode.HALF_UP))
                .items(lowStockItems)
                .build();
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date (" + startDate + ") cannot be after end date (" + endDate + ")");
        }
    }

    private List<Order> getOrdersForRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return orderRepository.findByOrderDateBetweenOrderByCreatedAtDesc(startDate, endDate);
        } else if (startDate != null) {
            return orderRepository.findByOrderDateBetweenOrderByCreatedAtDesc(startDate, LocalDate.now());
        } else if (endDate != null) {
            return orderRepository.findByOrderDateBetweenOrderByCreatedAtDesc(LocalDate.of(2000, 1, 1), endDate);
        } else {
            return orderRepository.findAllByOrderByCreatedAtDesc();
        }
    }

    private static class TrendBucket {
        final String dateKey;
        final String periodLabel;
        BigDecimal sales = BigDecimal.ZERO;
        int orders = 0;
        int unitsSold = 0;

        TrendBucket(String dateKey, String periodLabel) {
            this.dateKey = dateKey;
            this.periodLabel = periodLabel;
        }
    }

    private static class ProductSalesAggregator {
        final Product product;
        int unitsSold = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        ProductSalesAggregator(Product product) {
            this.product = product;
        }
    }

    private static class CategoryAggregator {
        final String category;
        int unitsSold = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        final Set<Long> orderIds = new HashSet<>();

        CategoryAggregator(String category) {
            this.category = category;
        }
    }

    private static class PaymentAggregator {
        final String method;
        final String label;
        BigDecimal totalAmount = BigDecimal.ZERO;
        int transactionCount = 0;

        PaymentAggregator(String method, String label) {
            this.method = method;
            this.label = label;
        }
    }
}

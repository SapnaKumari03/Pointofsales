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
public class AnalyticsServiceImpl implements AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    private static final DateTimeFormatter DATE_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DAILY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter MONTH_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getDashboardSummary() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);

        List<Order> orders = orderRepository.findAll();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalOrders = 0;
        BigDecimal todaySales = BigDecimal.ZERO;
        int todayOrders = 0;
        BigDecimal weeklyRevenue = BigDecimal.ZERO;
        BigDecimal monthlyRevenue = BigDecimal.ZERO;
        BigDecimal cashSales = BigDecimal.ZERO;
        BigDecimal upiSales = BigDecimal.ZERO;
        BigDecimal cardSales = BigDecimal.ZERO;

        for (Order o : orders) {
            if ("COMPLETED".equalsIgnoreCase(o.getStatus())) {
                totalOrders++;
                BigDecimal amount = o.getGrandTotal() != null ? o.getGrandTotal() : BigDecimal.ZERO;
                totalRevenue = totalRevenue.add(amount);

                LocalDate orderDate = o.getOrderDate();
                if (orderDate != null) {
                    if (today.equals(orderDate)) {
                        todayOrders++;
                        todaySales = todaySales.add(amount);
                    }
                    if (!orderDate.isBefore(sevenDaysAgo) && !orderDate.isAfter(today)) {
                        weeklyRevenue = weeklyRevenue.add(amount);
                    }
                    if (!orderDate.isBefore(monthStart) && !orderDate.isAfter(today)) {
                        monthlyRevenue = monthlyRevenue.add(amount);
                    }
                }

                String method = o.getPaymentMethod() != null ? o.getPaymentMethod().toUpperCase() : "CASH";
                if ("CASH".equals(method)) {
                    cashSales = cashSales.add(amount);
                } else if ("UPI".equals(method)) {
                    upiSales = upiSales.add(amount);
                } else if ("CARD".equals(method)) {
                    cardSales = cardSales.add(amount);
                }
            }
        }

        BigDecimal avgOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        List<Product> products = productRepository.findAll();
        int lowStockCount = (int) products.stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus())
                        && p.getStockQuantity() != null
                        && p.getMinStockLevel() != null
                        && p.getStockQuantity() <= p.getMinStockLevel())
                .count();

        return DashboardSummaryDTO.builder()
                .totalSales(totalRevenue.setScale(2, RoundingMode.HALF_UP))
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP))
                .lowStockCount(lowStockCount)
                .averageOrderValue(avgOrderValue)
                .todaySales(todaySales.setScale(2, RoundingMode.HALF_UP))
                .todayOrders(todayOrders)
                .weeklyRevenue(weeklyRevenue.setScale(2, RoundingMode.HALF_UP))
                .monthlyRevenue(monthlyRevenue.setScale(2, RoundingMode.HALF_UP))
                .cashSales(cashSales.setScale(2, RoundingMode.HALF_UP))
                .upiSales(upiSales.setScale(2, RoundingMode.HALF_UP))
                .cardSales(cardSales.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesReportDTO getSalesReport(String period, LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        String aggregation = (period != null && !period.isBlank()) ? period.trim().toLowerCase() : "daily";

        List<Order> orders = getOrdersForRange(startDate, endDate);

        // Group orders into chronological buckets
        Map<String, SalesReportDTO.SalesRevenueItemDTO> bucketMap = new TreeMap<>();

        BigDecimal overallRevenue = BigDecimal.ZERO;
        int overallOrders = 0;
        int overallUnits = 0;
        BigDecimal overallTax = BigDecimal.ZERO;
        BigDecimal overallDiscount = BigDecimal.ZERO;

        for (Order o : orders) {
            if (!"COMPLETED".equalsIgnoreCase(o.getStatus())) continue;

            LocalDate orderDate = o.getOrderDate() != null ? o.getOrderDate() : LocalDate.now();
            String dateKey;
            String periodLabel;

            if ("weekly".equals(aggregation)) {
                int week = orderDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                int weekYear = orderDate.get(IsoFields.WEEK_BASED_YEAR);
                dateKey = String.format("%d-W%02d", weekYear, week);
                periodLabel = String.format("Week %d, %d", week, weekYear);
            } else if ("monthly".equals(aggregation)) {
                dateKey = orderDate.format(MONTH_KEY_FORMATTER);
                periodLabel = orderDate.format(MONTH_LABEL_FORMATTER);
            } else {
                // daily default
                dateKey = orderDate.format(DATE_KEY_FORMATTER);
                periodLabel = orderDate.format(DAILY_LABEL_FORMATTER);
            }

            SalesReportDTO.SalesRevenueItemDTO item = bucketMap.computeIfAbsent(dateKey, k ->
                    SalesReportDTO.SalesRevenueItemDTO.builder()
                            .dateKey(dateKey)
                            .periodLabel(periodLabel)
                            .orderCount(0)
                            .unitsSold(0)
                            .grossSales(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                            .discountAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                            .taxCollected(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                            .netRevenue(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                            .averageOrderValue(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                            .build()
            );

            BigDecimal net = o.getGrandTotal() != null ? o.getGrandTotal() : BigDecimal.ZERO;
            BigDecimal tax = o.getTaxAmount() != null ? o.getTaxAmount() : BigDecimal.ZERO;
            BigDecimal disc = o.getDiscountAmount() != null ? o.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal sub = o.getSubtotal() != null ? o.getSubtotal() : BigDecimal.ZERO;
            BigDecimal gross = sub.add(tax);

            int orderUnits = 0;
            if (o.getItems() != null) {
                for (OrderItem oi : o.getItems()) {
                    if (oi.getQuantity() != null) {
                        orderUnits += oi.getQuantity();
                    }
                }
            }

            item.setOrderCount(item.getOrderCount() + 1);
            item.setUnitsSold(item.getUnitsSold() + orderUnits);
            item.setGrossSales(item.getGrossSales().add(gross).setScale(2, RoundingMode.HALF_UP));
            item.setDiscountAmount(item.getDiscountAmount().add(disc).setScale(2, RoundingMode.HALF_UP));
            item.setTaxCollected(item.getTaxCollected().add(tax).setScale(2, RoundingMode.HALF_UP));
            item.setNetRevenue(item.getNetRevenue().add(net).setScale(2, RoundingMode.HALF_UP));

            overallRevenue = overallRevenue.add(net);
            overallOrders++;
            overallUnits += orderUnits;
            overallTax = overallTax.add(tax);
            overallDiscount = overallDiscount.add(disc);
        }

        BigDecimal highestRevenue = BigDecimal.ZERO;
        String highestLabel = "-";

        List<SalesReportDTO.SalesRevenueItemDTO> itemsList = new ArrayList<>(bucketMap.values());
        for (SalesReportDTO.SalesRevenueItemDTO item : itemsList) {
            if (item.getOrderCount() > 0) {
                BigDecimal aov = item.getNetRevenue().divide(BigDecimal.valueOf(item.getOrderCount()), 2, RoundingMode.HALF_UP);
                item.setAverageOrderValue(aov);
            }
            if (item.getNetRevenue().compareTo(highestRevenue) > 0) {
                highestRevenue = item.getNetRevenue();
                highestLabel = item.getPeriodLabel();
            }
        }

        BigDecimal overallAov = overallOrders > 0
                ? overallRevenue.divide(BigDecimal.valueOf(overallOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        return SalesReportDTO.builder()
                .period(aggregation)
                .totalRevenue(overallRevenue.setScale(2, RoundingMode.HALF_UP))
                .totalOrders(overallOrders)
                .totalUnitsSold(overallUnits)
                .totalTax(overallTax.setScale(2, RoundingMode.HALF_UP))
                .totalDiscount(overallDiscount.setScale(2, RoundingMode.HALF_UP))
                .averageOrderValue(overallAov)
                .highestDayRevenue(highestRevenue.setScale(2, RoundingMode.HALF_UP))
                .highestDayLabel(highestLabel)
                .items(itemsList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BestSellerDTO> getBestSellers(Integer limit, LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        int maxResults = (limit != null && limit > 0) ? limit : 10;

        List<Order> orders = getOrdersForRange(startDate, endDate);

        // Map product ID to aggregated statistics
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

        List<BestSellerDTO> result = new ArrayList<>();
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

            result.add(BestSellerDTO.builder()
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
                        ? oi.getProduct().getCategory().trim() : "Uncategorized";

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
    public GstSummaryDTO getGstSummary(LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        List<Order> orders = getOrdersForRange(startDate, endDate);

        // Prepopulate standard Indian GST tax rates: 0%, 5%, 12%, 18%
        Map<Double, GstSlabAccumulator> slabMap = new TreeMap<>();
        for (double r : List.of(0.0, 5.0, 12.0, 18.0)) {
            slabMap.put(r, new GstSlabAccumulator(r));
        }

        BigDecimal totalTaxableAmount = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal grandTotalGst = BigDecimal.ZERO;

        for (Order o : orders) {
            if (!"COMPLETED".equalsIgnoreCase(o.getStatus()) || o.getItems() == null) continue;

            for (OrderItem oi : o.getItems()) {
                Double rate = oi.getTaxRate() != null ? oi.getTaxRate() : 0.0;
                BigDecimal taxable = oi.getSubtotal() != null ? oi.getSubtotal() : BigDecimal.ZERO;
                BigDecimal taxAmount = oi.getTaxAmount() != null ? oi.getTaxAmount() : BigDecimal.ZERO;
                int qty = oi.getQuantity() != null ? oi.getQuantity() : 0;

                GstSlabAccumulator slab = slabMap.computeIfAbsent(rate, GstSlabAccumulator::new);
                slab.taxableAmount = slab.taxableAmount.add(taxable);
                slab.totalGst = slab.totalGst.add(taxAmount);
                slab.itemCount += qty;
                if (o.getInvoiceNumber() != null) {
                    slab.invoiceNumbers.add(o.getInvoiceNumber());
                }
            }
        }

        List<GstSummaryDTO.GstSlabDTO> slabDTOs = new ArrayList<>();
        for (GstSlabAccumulator slab : slabMap.values()) {
            double halfRate = slab.taxRate / 2.0;
            BigDecimal cgstAmt = slab.totalGst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            BigDecimal sgstAmt = slab.totalGst.subtract(cgstAmt).setScale(2, RoundingMode.HALF_UP);

            String rateLabel = "GST " + (slab.taxRate % 1 == 0 ? String.valueOf((int) slab.taxRate) : String.valueOf(slab.taxRate)) + "%";

            slabDTOs.add(GstSummaryDTO.GstSlabDTO.builder()
                    .rateLabel(rateLabel)
                    .taxRate(slab.taxRate)
                    .taxableAmount(slab.taxableAmount.setScale(2, RoundingMode.HALF_UP))
                    .cgstRate(halfRate)
                    .cgstAmount(cgstAmt)
                    .sgstRate(halfRate)
                    .sgstAmount(sgstAmt)
                    .totalGst(slab.totalGst.setScale(2, RoundingMode.HALF_UP))
                    .itemCount(slab.itemCount)
                    .invoiceCount(slab.invoiceNumbers.size())
                    .build());

            totalTaxableAmount = totalTaxableAmount.add(slab.taxableAmount);
            totalCgst = totalCgst.add(cgstAmt);
            totalSgst = totalSgst.add(sgstAmt);
            grandTotalGst = grandTotalGst.add(slab.totalGst);
        }

        return GstSummaryDTO.builder()
                .totalTaxableAmount(totalTaxableAmount.setScale(2, RoundingMode.HALF_UP))
                .totalCgst(totalCgst.setScale(2, RoundingMode.HALF_UP))
                .totalSgst(totalSgst.setScale(2, RoundingMode.HALF_UP))
                .grandTotalGst(grandTotalGst.setScale(2, RoundingMode.HALF_UP))
                .slabs(slabDTOs)
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

    private static class GstSlabAccumulator {
        final double taxRate;
        BigDecimal taxableAmount = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;
        int itemCount = 0;
        final Set<String> invoiceNumbers = new HashSet<>();

        GstSlabAccumulator(double taxRate) {
            this.taxRate = taxRate;
        }
    }
}

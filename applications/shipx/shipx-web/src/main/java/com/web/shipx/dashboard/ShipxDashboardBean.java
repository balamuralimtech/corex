package com.web.shipx.dashboard;

import com.module.shipx.request.ICustomerRequestService;
import com.persist.coretix.modal.systemmanagement.Countries;
import com.persist.shipx.request.CustomerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Named("shipxDashboardBean")
@Scope("session")
public class ShipxDashboardBean implements Serializable {

    private static final long serialVersionUID = 6173348923411221955L;
    private static final Logger logger = LoggerFactory.getLogger(ShipxDashboardBean.class);

    @Inject
    private ICustomerRequestService customerRequestService;

    private boolean timerEnabled = true;
    private int refreshSequence;
    private long liveShipmentCount;
    private long onTimeDeliveryRate;
    private long customsClearanceRate;
    private long capacityUtilizationRate;
    private long revenueAtRiskUsd;
    private long delayedShipmentCount;
    private long activeExceptionCount;
    private long pendingCustomsCount;
    private long awaitingAllocationCount;
    private long oceanShipmentCount;
    private long airShipmentCount;
    private long roadShipmentCount;
    private long railShipmentCount;
    private long bookingBacklogCount;
    private long documentsReadyRate;
    private long emptyContainerBalance;
    private long customerRequestCount;
    private long newRequestCount;
    private long organizationRequestCount;
    private long individualRequestCount;
    private long weightRequestCount;
    private long containerRequestCount;
    private long spaceRequestCount;
    private String dashboardInsights;
    private String customerRequestInsight;
    private String lastRefreshLabel;
    private List<TradeLaneSnapshot> corridorPerformance;
    private List<OperationalAlert> operationalAlerts;
    private List<MilestoneCard> milestoneCards;
    private List<CustomerRequestLaneInsight> customerRequestLanes;
    private List<CustomerRequest> customerRequests = new ArrayList<>();

    public void initializePageAttributes() {
        if (corridorPerformance == null) {
            loadDashboardData();
            logger.info("ShipX dashboard data loaded");
        }
    }

    public void refreshForm() {
        loadDashboardData();
        logger.info("ShipX dashboard refreshed at {}", System.currentTimeMillis());
    }

    private void loadDashboardData() {
        refreshSequence++;
        int sequence = refreshSequence - 1;
        customerRequests = new ArrayList<>(customerRequestService.getCustomerRequestList());

        customerRequestCount = customerRequests.size();
        newRequestCount = customerRequests.stream()
                .filter(request -> "NEW".equalsIgnoreCase(request.getStatus()))
                .count();
        organizationRequestCount = customerRequests.stream()
                .filter(request -> "ORGANIZATION".equalsIgnoreCase(request.getCustomerType()))
                .count();
        individualRequestCount = customerRequestCount - organizationRequestCount;
        weightRequestCount = customerRequests.stream()
                .filter(request -> "WEIGHT".equalsIgnoreCase(request.getCapacityType()))
                .count();
        containerRequestCount = customerRequests.stream()
                .filter(request -> "CONTAINER".equalsIgnoreCase(request.getCapacityType()))
                .count();
        spaceRequestCount = customerRequests.stream()
                .filter(request -> "SPACE".equalsIgnoreCase(request.getCapacityType()))
                .count();

        liveShipmentCount = 900 + (customerRequestCount * 7) + (sequence * 11L % 60L);
        onTimeDeliveryRate = 90 + (customerRequestCount % 5);
        customsClearanceRate = 87 + ((customerRequestCount + sequence) % 6);
        capacityUtilizationRate = 68 + (int) Math.min(22, customerRequestCount * 2L);
        revenueAtRiskUsd = 120000 + (customerRequestCount * 8500L);
        delayedShipmentCount = Math.max(6, (int) Math.round(customerRequestCount * 0.35) + (sequence % 5));
        activeExceptionCount = Math.max(4, (int) Math.round(customerRequestCount * 0.22) + (sequence % 4));
        pendingCustomsCount = Math.max(3, (int) Math.round(customerRequestCount * 0.28) + (sequence % 3));
        awaitingAllocationCount = (int) Math.max(2, newRequestCount + (sequence % 3));
        oceanShipmentCount = 280 + containerRequestCount * 10 + (sequence % 12);
        airShipmentCount = 140 + weightRequestCount * 8 + (sequence % 9);
        roadShipmentCount = 180 + spaceRequestCount * 7 + (sequence % 10);
        railShipmentCount = 70 + Math.max(1, customerRequestCount / 3) + (sequence % 6);
        bookingBacklogCount = Math.max(5, newRequestCount + 8);
        documentsReadyRate = 91 + (sequence % 4);
        emptyContainerBalance = 18 - (int) Math.min(12, containerRequestCount);
        lastRefreshLabel = new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(new Date());

        dashboardInsights = String.format(Locale.US,
                "ShipX is coordinating %d active freight movements while digesting %d captured customer requests. " +
                        "The control tower is prioritizing %d new opportunities, with capacity utilization at %d%% and %d active exceptions requiring intervention.",
                liveShipmentCount,
                customerRequestCount,
                newRequestCount,
                capacityUtilizationRate,
                activeExceptionCount);

        customerRequestInsight = String.format(Locale.US,
                "%d customer requests are now feeding the planning pipeline, split %d organization-led and %d individual-led enquiries. " +
                        "%d are still new and ready for conversion into bookings.",
                customerRequestCount,
                organizationRequestCount,
                individualRequestCount,
                newRequestCount);

        customerRequestLanes = buildCustomerRequestLanes();
        corridorPerformance = buildCorridorPerformance(sequence);
        operationalAlerts = buildOperationalAlerts();
        milestoneCards = buildMilestoneCards(sequence);
    }

    private List<TradeLaneSnapshot> buildCorridorPerformance(int sequence) {
        List<TradeLaneSnapshot> requestDrivenLanes = customerRequestLanes.stream()
                .limit(4)
                .map(lane -> new TradeLaneSnapshot(
                        lane.getLane(),
                        lane.getReliability(),
                        lane.getTransitSignal(),
                        lane.getRevenue(),
                        lane.getRemark()))
                .collect(Collectors.toList());

        if (!requestDrivenLanes.isEmpty()) {
            return requestDrivenLanes;
        }

        return Arrays.asList(
                new TradeLaneSnapshot("Shanghai -> Los Angeles", 96, "18h avg dwell", "$4.8M weekly", "Ocean consolidation stable"),
                new TradeLaneSnapshot("Singapore -> Rotterdam", 91, "22h avg dwell", "$3.4M weekly", "Customs docs ahead of plan"),
                new TradeLaneSnapshot("Frankfurt -> Chicago", 94, "6h handoff", "$2.1M weekly", "Air uplift capacity protected"),
                new TradeLaneSnapshot("Dubai -> Nairobi", 87, "11h border cycle", "$1.2M weekly", "Road feeder exposed to delays")
        );
    }

    private List<OperationalAlert> buildOperationalAlerts() {
        List<OperationalAlert> alerts = new ArrayList<>();
        alerts.add(new OperationalAlert("New Requests",
                newRequestCount + " customer requests are waiting for sales and planning qualification", "High"));
        alerts.add(new OperationalAlert("Route Demand",
                customerRequestLanes.isEmpty() ? "No customer routes captured yet"
                        : customerRequestLanes.get(0).getRequestCount() + " requests concentrated on " + customerRequestLanes.get(0).getLane(),
                "Amber"));
        alerts.add(new OperationalAlert("Capacity Preference",
                weightRequestCount + " weight-driven, " + containerRequestCount + " container-driven, " + spaceRequestCount + " space-driven requests",
                "Medium"));
        alerts.add(new OperationalAlert("Revenue Risk",
                formatCurrency(revenueAtRiskUsd) + " margin exposed while unconverted demand stays in queue",
                "Critical"));
        return alerts;
    }

    private List<MilestoneCard> buildMilestoneCards(int sequence) {
        return Arrays.asList(
                new MilestoneCard("Requests Captured", (int) customerRequestCount, "live pipeline"),
                new MilestoneCard("New Enquiries", (int) newRequestCount, "ready for qualification"),
                new MilestoneCard("Organization Leads", (int) organizationRequestCount, "commercial accounts"),
                new MilestoneCard("Individual Leads", (int) individualRequestCount + sequence % 2, "retail enquiries")
        );
    }

    private List<CustomerRequestLaneInsight> buildCustomerRequestLanes() {
        Map<String, List<CustomerRequest>> grouped = customerRequests.stream()
                .filter(request -> request.getOriginCountry() != null && request.getDestinationCountry() != null)
                .collect(Collectors.groupingBy(request -> request.getOriginCountry().getName() + " -> " + request.getDestinationCountry().getName()));

        return grouped.entrySet().stream()
                .map(entry -> buildLaneInsight(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(CustomerRequestLaneInsight::getRequestCount).reversed())
                .limit(6)
                .collect(Collectors.toList());
    }

    private CustomerRequestLaneInsight buildLaneInsight(String laneName, List<CustomerRequest> requests) {
        CustomerRequest sample = requests.get(0);
        int requestCount = requests.size();
        int organizationCount = (int) requests.stream()
                .filter(request -> "ORGANIZATION".equalsIgnoreCase(request.getCustomerType()))
                .count();
        int reliability = Math.max(78, 96 - requestCount * 2);
        String transitSignal = sample.getCapacityType() + " focus";
        String revenue = formatCurrency(requestCount * 95000L);
        String remark = requestCount + " captured requests, " + organizationCount + " organization accounts";
        return new CustomerRequestLaneInsight(
                laneName,
                requestCount,
                reliability,
                transitSignal,
                revenue,
                remark,
                sample.getOriginCountry(),
                sample.getDestinationCountry());
    }

    public boolean isTimerEnabled() {
        return timerEnabled;
    }

    public void setTimerEnabled(boolean timerEnabled) {
        this.timerEnabled = timerEnabled;
    }

    public long getLiveShipmentCount() {
        return liveShipmentCount;
    }

    public long getOnTimeDeliveryRate() {
        return onTimeDeliveryRate;
    }

    public long getCustomsClearanceRate() {
        return customsClearanceRate;
    }

    public long getCapacityUtilizationRate() {
        return capacityUtilizationRate;
    }

    public long getRevenueAtRiskUsd() {
        return revenueAtRiskUsd;
    }

    public long getDelayedShipmentCount() {
        return delayedShipmentCount;
    }

    public long getActiveExceptionCount() {
        return activeExceptionCount;
    }

    public long getPendingCustomsCount() {
        return pendingCustomsCount;
    }

    public long getAwaitingAllocationCount() {
        return awaitingAllocationCount;
    }

    public long getOceanShipmentCount() {
        return oceanShipmentCount;
    }

    public long getAirShipmentCount() {
        return airShipmentCount;
    }

    public long getRoadShipmentCount() {
        return roadShipmentCount;
    }

    public long getRailShipmentCount() {
        return railShipmentCount;
    }

    public long getBookingBacklogCount() {
        return bookingBacklogCount;
    }

    public long getDocumentsReadyRate() {
        return documentsReadyRate;
    }

    public long getEmptyContainerBalance() {
        return emptyContainerBalance;
    }

    public long getCustomerRequestCount() {
        return customerRequestCount;
    }

    public long getNewRequestCount() {
        return newRequestCount;
    }

    public long getOrganizationRequestCount() {
        return organizationRequestCount;
    }

    public long getIndividualRequestCount() {
        return individualRequestCount;
    }

    public long getWeightRequestCount() {
        return weightRequestCount;
    }

    public long getContainerRequestCount() {
        return containerRequestCount;
    }

    public long getSpaceRequestCount() {
        return spaceRequestCount;
    }

    public String getDashboardInsights() {
        return dashboardInsights;
    }

    public String getCustomerRequestInsight() {
        return customerRequestInsight;
    }

    public String getLastRefreshLabel() {
        return lastRefreshLabel;
    }

    public List<TradeLaneSnapshot> getCorridorPerformance() {
        return corridorPerformance;
    }

    public List<OperationalAlert> getOperationalAlerts() {
        return operationalAlerts;
    }

    public List<MilestoneCard> getMilestoneCards() {
        return milestoneCards;
    }

    public List<CustomerRequestLaneInsight> getCustomerRequestLanes() {
        return customerRequestLanes;
    }

    public long getTotalInTransitShipments() {
        return oceanShipmentCount + airShipmentCount + roadShipmentCount + railShipmentCount;
    }

    public double getExceptionRate() {
        return percentage(activeExceptionCount, liveShipmentCount);
    }

    public double getDelayedShare() {
        return percentage(delayedShipmentCount, liveShipmentCount);
    }

    public double getAllocationReadiness() {
        return percentage(liveShipmentCount - awaitingAllocationCount, liveShipmentCount);
    }

    public double getCustomerRequestConversionPressure() {
        return percentage(newRequestCount, Math.max(1, customerRequestCount));
    }

    public String getRefreshStatusLabel() {
        return "Snapshot refreshed " + lastRefreshLabel;
    }

    public String getShipmentVolumeJson() {
        return String.format(Locale.US,
                "[['Ocean', %d], ['Air', %d], ['Road', %d], ['Rail', %d]]",
                oceanShipmentCount,
                airShipmentCount,
                roadShipmentCount,
                railShipmentCount);
    }

    public String getExecutionPulseJson() {
        return String.format(Locale.US,
                "[%d, %d, %.2f, %.2f, %.2f]",
                onTimeDeliveryRate,
                customsClearanceRate,
                getExceptionRate(),
                getDelayedShare(),
                getAllocationReadiness());
    }

    public String getExceptionTrendJson() {
        int base = (int) activeExceptionCount;
        return String.format(Locale.US,
                "[['-5h', %d], ['-4h', %d], ['-3h', %d], ['-2h', %d], ['-1h', %d], ['Now', %d]]",
                Math.max(2, base - 4),
                Math.max(3, base - 3),
                Math.max(4, base - 2),
                Math.max(4, base - 1),
                Math.max(5, base + 1),
                base);
    }

    public String getRegionServiceJson() {
        Map<String, Long> regionCounts = customerRequests.stream()
                .filter(request -> request.getOriginCountry() != null && request.getOriginCountry().getRegionEntity() != null)
                .collect(Collectors.groupingBy(request -> request.getOriginCountry().getRegionEntity().getName(), LinkedHashMap::new, Collectors.counting()));

        if (regionCounts.isEmpty()) {
            return "[['Asia', 92], ['Europe', 90], ['North America', 89], ['Middle East', 87], ['Africa', 85]]";
        }

        return regionCounts.entrySet().stream()
                .limit(5)
                .map(entry -> "['" + escape(entry.getKey()) + "', " + Math.max(76, 98 - entry.getValue().intValue() * 3) + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getCapacityMixJson() {
        long utilized = Math.min(100, capacityUtilizationRate);
        return String.format(Locale.US,
                "[['Utilized', %d], ['Protected Buffer', %d], ['Open Capacity', %d]]",
                utilized,
                12,
                Math.max(0, 100 - utilized - 12));
    }

    public String getMilestoneVelocityJson() {
        return String.format(Locale.US,
                "[['Requests', %d], ['Qualified', %d], ['Quoted', %d], ['Booked', %d]]",
                customerRequestCount,
                Math.max(0, customerRequestCount - newRequestCount),
                Math.max(0, customerRequestCount / 2),
                Math.max(0, customerRequestCount / 3));
    }

    public String getNetworkBalanceJson() {
        return String.format(Locale.US,
                "[%.2f, %.2f, %.2f, %.2f, %.2f, %.2f]",
                ratio(oceanShipmentCount, liveShipmentCount),
                ratio(airShipmentCount, liveShipmentCount),
                ratio(roadShipmentCount, liveShipmentCount),
                ratio(railShipmentCount, liveShipmentCount),
                getDelayedShare(),
                getExceptionRate());
    }

    public String getCustomerRequestTypeJson() {
        return String.format(Locale.US,
                "[['Weight', %d], ['Container', %d], ['Space', %d]]",
                weightRequestCount,
                containerRequestCount,
                spaceRequestCount);
    }

    public String getCustomerTypeJson() {
        return String.format(Locale.US,
                "[['Organization', %d], ['Individual', %d]]",
                organizationRequestCount,
                individualRequestCount);
    }

    public String getRequestStatusJson() {
        long newCount = customerRequests.stream().filter(request -> "NEW".equalsIgnoreCase(request.getStatus())).count();
        long otherCount = Math.max(0, customerRequestCount - newCount);
        return String.format(Locale.US,
                "[['New', %d], ['Progressing', %d]]",
                newCount,
                otherCount);
    }

    public String getTopOriginJson() {
        return buildCountryFrequencyJson(true);
    }

    public String getTopDestinationJson() {
        return buildCountryFrequencyJson(false);
    }

    private String buildCountryFrequencyJson(boolean origin) {
        Map<String, Long> grouped = customerRequests.stream()
                .map(request -> origin ? request.getOriginCountry() : request.getDestinationCountry())
                .filter(country -> country != null)
                .collect(Collectors.groupingBy(Countries::getName, LinkedHashMap::new, Collectors.counting()));

        if (grouped.isEmpty()) {
            return "[]";
        }

        return grouped.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .map(entry -> "['" + escape(entry.getKey()) + "', " + entry.getValue() + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String getCustomerRequestRouteMapJson() {
        List<String> points = new ArrayList<>();
        int colorIndex = 0;
        for (CustomerRequestLaneInsight lane : customerRequestLanes) {
            Countries origin = lane.getOriginCountry();
            Countries destination = lane.getDestinationCountry();
            if (!hasCoordinates(origin) || !hasCoordinates(destination)) {
                continue;
            }

            points.add(String.format(Locale.US,
                    "{type:'line',name:'%s',colorIndex:%d,data:[[%.4f, %.4f],[%.4f, %.4f]]}",
                    escape(lane.getLane()),
                    colorIndex % 6,
                    origin.getLongitude().doubleValue(),
                    origin.getLatitude().doubleValue(),
                    destination.getLongitude().doubleValue(),
                    destination.getLatitude().doubleValue()));
            points.add(String.format(Locale.US,
                    "{type:'scatter',name:'%s',colorIndex:%d,data:[{x:%.4f,y:%.4f,name:'%s',marker:{radius:%d}},{x:%.4f,y:%.4f,name:'%s',marker:{radius:%d}}]}",
                    escape(lane.getLane()),
                    colorIndex % 6,
                    origin.getLongitude().doubleValue(),
                    origin.getLatitude().doubleValue(),
                    escape(origin.getName()),
                    Math.min(12, 5 + lane.getRequestCount()),
                    destination.getLongitude().doubleValue(),
                    destination.getLatitude().doubleValue(),
                    escape(destination.getName()),
                    Math.min(12, 5 + lane.getRequestCount())));
            colorIndex++;
        }
        return points.isEmpty() ? "[]" : points.stream().collect(Collectors.joining(", ", "[", "]"));
    }

    private boolean hasCoordinates(Countries country) {
        return country != null && country.getLatitude() != null && country.getLongitude() != null;
    }

    public String getFormatRevenueAtRisk() {
        return formatCurrency(revenueAtRiskUsd);
    }

    private String formatCurrency(long amount) {
        return String.format(Locale.US, "$%,d", amount);
    }

    private double ratio(double numerator, double denominator) {
        if (denominator <= 0) {
            return 0;
        }
        return round((numerator / denominator) * 100.0);
    }

    private double percentage(double part, double total) {
        if (total <= 0) {
            return 0;
        }
        return round((part / total) * 100.0);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("'", "\\'");
    }

    public static class TradeLaneSnapshot implements Serializable {
        private static final long serialVersionUID = 209113993311L;
        private final String lane;
        private final int reliability;
        private final String transitSignal;
        private final String revenue;
        private final String remark;

        public TradeLaneSnapshot(String lane, int reliability, String transitSignal, String revenue, String remark) {
            this.lane = lane;
            this.reliability = reliability;
            this.transitSignal = transitSignal;
            this.revenue = revenue;
            this.remark = remark;
        }

        public String getLane() {
            return lane;
        }

        public int getReliability() {
            return reliability;
        }

        public String getTransitSignal() {
            return transitSignal;
        }

        public String getRevenue() {
            return revenue;
        }

        public String getRemark() {
            return remark;
        }
    }

    public static class OperationalAlert implements Serializable {
        private static final long serialVersionUID = 921733112011L;
        private final String title;
        private final String detail;
        private final String severity;

        public OperationalAlert(String title, String detail, String severity) {
            this.title = title;
            this.detail = detail;
            this.severity = severity;
        }

        public String getTitle() {
            return title;
        }

        public String getDetail() {
            return detail;
        }

        public String getSeverity() {
            return severity;
        }
    }

    public static class MilestoneCard implements Serializable {
        private static final long serialVersionUID = 814553220191L;
        private final String label;
        private final int value;
        private final String timeframe;

        public MilestoneCard(String label, int value, String timeframe) {
            this.label = label;
            this.value = value;
            this.timeframe = timeframe;
        }

        public String getLabel() {
            return label;
        }

        public int getValue() {
            return value;
        }

        public String getTimeframe() {
            return timeframe;
        }
    }

    public static class CustomerRequestLaneInsight implements Serializable {
        private static final long serialVersionUID = 321778992210L;
        private final String lane;
        private final int requestCount;
        private final int reliability;
        private final String transitSignal;
        private final String revenue;
        private final String remark;
        private final Countries originCountry;
        private final Countries destinationCountry;

        public CustomerRequestLaneInsight(String lane, int requestCount, int reliability, String transitSignal,
                                          String revenue, String remark, Countries originCountry, Countries destinationCountry) {
            this.lane = lane;
            this.requestCount = requestCount;
            this.reliability = reliability;
            this.transitSignal = transitSignal;
            this.revenue = revenue;
            this.remark = remark;
            this.originCountry = originCountry;
            this.destinationCountry = destinationCountry;
        }

        public String getLane() {
            return lane;
        }

        public int getRequestCount() {
            return requestCount;
        }

        public int getReliability() {
            return reliability;
        }

        public String getTransitSignal() {
            return transitSignal;
        }

        public String getRevenue() {
            return revenue;
        }

        public String getRemark() {
            return remark;
        }

        public Countries getOriginCountry() {
            return originCountry;
        }

        public Countries getDestinationCountry() {
            return destinationCountry;
        }
    }
}

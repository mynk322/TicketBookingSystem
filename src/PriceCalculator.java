import enums.SeatCategory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriceCalculator {
    
    // Pricing map for different seat categories
    private static final Map<SeatCategory, Double> SEAT_PRICING = new HashMap<>();
    
    static {
        SEAT_PRICING.put(SeatCategory.PREMIUM, 500.0);
        SEAT_PRICING.put(SeatCategory.GOLD, 300.0);
        SEAT_PRICING.put(SeatCategory.SILVER, 200.0);
        SEAT_PRICING.put(SeatCategory.STANDARD, 100.0);
    }
    
    /**
     * Get price for a seat category
     */
    public static double getPrice(SeatCategory category) {
        return SEAT_PRICING.getOrDefault(category, 100.0); // Default to 100 if category not found
    }
    
    /**
     * Get all pricing map
     */
    public static Map<SeatCategory, Double> getAllPricing() {
        return new HashMap<>(SEAT_PRICING);
    }
    
    /**
     * Calculate total amount for a list of seats
     */
    public static double calculateTotal(List<Seat> seats) {
        double total = 0.0;
        for (Seat seat : seats) {
            total += getPrice(seat.getSeatCategory());
        }
        return total;
    }
}

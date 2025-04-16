package yuparking.services;

import yuparking.database.Database;
import yuparking.models.User;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class UserBookingService {
    private final Database db;
    private final BookingService bookingService;

    public UserBookingService() {
        this.db = new Database();
        this.bookingService = new BookingService();
    }

    public void showUserBookings(User user) {
        List<String[]> bookings = db.retrieveData("bookings");
        System.out.println("\nYour Bookings:");
        boolean found = false;
        for (int i = 1; i < bookings.size(); i++) {
            String[] row = bookings.get(i);
            if (Integer.parseInt(row[1]) == user.getUserID()) {
                found = true;
                System.out.println("BookingID: " + row[0] +
                        " | SpaceID: " + row[2] +
                        " | Start: " + row[3] +
                        " | End: " + row[4] +
                        " | Status: " + row[5]);
            }
        }
        if (!found) {
            System.out.println("No bookings found for your account.");
        }
    }

    public void cancelUserBooking(User user, int bookingID) {
        List<String[]> bookings = db.retrieveData("bookings");
        PaymentService paymentService = new PaymentService();

        for (int i = 1; i < bookings.size(); i++) {  // start from index 1
            String[] row = bookings.get(i);
            int bookingUserId = Integer.parseInt(row[1]);
            if (row[0].equals(String.valueOf(bookingID)) && bookingUserId == user.getUserID()) {
                if (!row[5].equalsIgnoreCase("Cancelled")) {
                    row[5] = "Cancelled";
                    db.confirmUpdate("bookings", bookings);
                    System.out.println("Booking " + bookingID + " cancelled.");
                } else {
                    System.out.println("Booking is already cancelled.");
                }
                paymentService.refundPayment(bookingID);
                return;
            }
        }
        System.out.println("Booking not found or does not belong to your account.");
    }


    public void modifyUserBooking(User user, int bookingID, String newStartTime, String newEndTime) {
        List<String[]> bookings = db.retrieveData("bookings");

        LocalDateTime newStart = LocalDateTime.parse(newStartTime);
        LocalDateTime newEnd = LocalDateTime.parse(newEndTime);

        if (newEnd.isBefore(newStart) || newEnd.equals(newStart)) {
            System.out.println("Invalid time range: End time must be after start time.");
            return;
        }
        for (int i = 1; i < bookings.size(); i++) {  // Start from i = 1 to skip header
            String[] row = bookings.get(i);
            int bookingUserId = Integer.parseInt(row[1]);
            if (row[0].equals(String.valueOf(bookingID)) && bookingUserId == user.getUserID()) {
                if (row[5].equalsIgnoreCase("Booked")) {
                    row[3] = newStartTime;
                    row[4] = newEndTime;
                    row[5] = "Modified";
                    db.confirmUpdate("bookings", bookings);
                    System.out.println("Booking " + bookingID + " modified.");
                } else {
                    System.out.println("Cannot modify a booking that is not active.");
                }
                return;
            }
        }
        System.out.println("Booking not found or does not belong to your account.");
    }

    public void createUserBooking(User user, int spaceID, String startTime, String endTime) {
        if (!user.isVerified()) {
            System.out.println("User " + user.getEmail() + " is not verified. Cannot create booking.");
            return;
        }
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);

        if (end.isBefore(start) || end.equals(start)) {
            System.out.println("End time cannot be before start time.");
            return;
        }


        List<String[]> bookings = db.retrieveData("bookings");
        int nextBookingID = bookings.size();

        String[] bookingRow = new String[]{
                String.valueOf(nextBookingID),
                String.valueOf(user.getUserID()),
                String.valueOf(spaceID),
                startTime,
                endTime,
                "Booked"
        };

        bookings.add(bookingRow);
        db.confirmUpdate("bookings", bookings);

        long hours = Duration.between(LocalDateTime.parse(startTime), LocalDateTime.parse(endTime)).toHours();
        double fee = bookingService.calculateFeeForBooking(user, (int) hours);


        System.out.println("Booking created for user: " + user.getEmail() +
                " | Booking ID: " + nextBookingID +
                " | Space ID: " + spaceID +
                " | Duration: " + hours + " hours" +
                " | Parking Fee: $" + fee);
    }

}

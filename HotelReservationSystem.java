import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.io.*;

public class HotelReservationSystem {

    static List<Room> rooms = new ArrayList<>();
    static List<Reservation> reservations = new ArrayList<>();
    static List<User> users = new ArrayList<>();
    static Scanner scanner = new Scanner(System.in);
    static User currentUser = null;
    static int nextReservationId = 1;

    public static void main(String[] args) {
        initializeRooms();

        while (true) {
            if (currentUser == null) showLoginMenu();
            else showMainMenu();
        }
    }

    // ---------------- MENU ----------------

    static void showLoginMenu() {
        System.out.println("\n1.Login 2.Register 3.Guest 4.Exit");
        int ch = getInt();

        switch (ch) {
            case 1 -> login();
            case 2 -> register();
            case 3 -> guestBooking();
            case 4 -> System.exit(0);
        }
    }

    static void showMainMenu() {
        System.out.println("\nWelcome " + currentUser.getName());
        System.out.println("1.Search 2.Book 3.My Booking 4.Cancel 5.Payments 6.Logout");

        int ch = getInt();

        switch (ch) {
            case 1 -> searchRooms();
            case 2 -> makeReservation();
            case 3 -> viewReservations();
            case 4 -> cancelReservation();
            case 5 -> currentUser.viewPayments();
            case 6 -> currentUser = null;
        }
    }

    // ---------------- USER ----------------

    static void register() {
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();

        users.add(new User(name, email, "NA"));
        currentUser = users.get(users.size() - 1);
    }

    static void login() {
        System.out.print("Email: ");
        String email = scanner.nextLine();

        for (User u : users) {
            if (u.getEmail().equals(email)) {
                currentUser = u;
                return;
            }
        }
        System.out.println("Not found!");
    }

    static void guestBooking() {
        currentUser = new User("Guest", "guest@mail.com", "NA");
    }

    // ---------------- ROOMS ----------------

    static void searchRooms() {
        LocalDate in = readDate("Check-in: ");
        LocalDate out = readDate("Check-out: ");

        for (Room r : rooms) {
            if (r.isAvailable(in, out)) {
                System.out.println("Room " + r.getId() + " " + r.getType() + " $" + r.getPrice());
            }
        }
    }

    static void makeReservation() {
        searchRooms();

        System.out.print("Room ID: ");
        int id = getInt();

        Room room = findRoom(id);
        if (room == null) return;

        LocalDate in = readDate("Check-in: ");
        LocalDate out = readDate("Check-out: ");

        if (!room.isAvailable(in, out)) {
            System.out.println("Not available!");
            return;
        }

        Reservation r = new Reservation(nextReservationId++, currentUser.getName(),
                currentUser.getEmail(), room, in, out);

        reservations.add(r);
        room.addReservation(r);
        currentUser.addReservation(r);

        System.out.println("Booked!\n" + r);
    }

    static void viewReservations() {
        for (Reservation r : reservations) {
            if (r.getGuestEmail().equals(currentUser.getEmail()))
                System.out.println(r);
        }
    }

    static void cancelReservation() {
        System.out.print("Enter ID: ");
        int id = getInt();

        reservations.removeIf(r -> {
            if (r.getId() == id) {
                r.getRoom().removeReservation(r);
                currentUser.removeReservation(r);
                System.out.println("Cancelled");
                return true;
            }
            return false;
        });
    }

    // ---------------- HELPERS ----------------

    static Room findRoom(int id) {
        for (Room r : rooms)
            if (r.getId() == id) return r;
        return null;
    }

    static int getInt() {
        while (!scanner.hasNextInt()) {
            System.out.println("Enter number!");
            scanner.next();
        }
        int val = scanner.nextInt();
        scanner.nextLine(); // FIX
        return val;
    }

    static LocalDate readDate(String msg) {
        System.out.print(msg);
        while (true) {
            try {
                return LocalDate.parse(scanner.nextLine());
            } catch (Exception e) {
                System.out.print("Wrong format (yyyy-MM-dd): ");
            }
        }
    }

    static void initializeRooms() {
        rooms.add(new Room(101, "Standard", 100));
        rooms.add(new Room(102, "Deluxe", 200));
        rooms.add(new Room(103, "Suite", 300));
    }
}

// ---------------- ROOM ----------------

class Room {
    int id;
    String type;
    double price;
    List<Reservation> list = new ArrayList<>();

    Room(int i, String t, double p) {
        id = i;
        type = t;
        price = p;
    }

    boolean isAvailable(LocalDate in, LocalDate out) {
        for (Reservation r : list) {
            if (in.isBefore(r.out) && out.isAfter(r.in)) return false;
        }
        return true;
    }

    void addReservation(Reservation r) { list.add(r); }
    void removeReservation(Reservation r) { list.remove(r); }

    int getId() { return id; }
    String getType() { return type; }
    double getPrice() { return price; }
}

// ---------------- RESERVATION ----------------

class Reservation {
    int id;
    String name, email;
    Room room;
    LocalDate in, out;

    Reservation(int id, String n, String e, Room r, LocalDate i, LocalDate o) {
        this.id = id;
        name = n;
        email = e;
        room = r;
        in = i;
        out = o;
    }

    public String toString() {
        long days = ChronoUnit.DAYS.between(in, out);
        return "ID:" + id + " Room:" + room.getId() +
                " From:" + in + " To:" + out +
                " Total:$" + (days * room.getPrice());
    }

    int getId() { return id; }
    String getGuestEmail() { return email; }
    Room getRoom() { return room; }
}

// ---------------- USER ----------------

class User {
    String name, email, phone;
    List<Reservation> list = new ArrayList<>();

    User(String n, String e, String p) {
        name = n;
        email = e;
        phone = p;
    }

    void addReservation(Reservation r) { list.add(r); }
    void removeReservation(Reservation r) { list.remove(r); }

    void viewPayments() {
        System.out.println("All bookings are prepaid (simulation)");
    }

    String getName() { return name; }
    String getEmail() { return email; }
}
import java.io.*;
import java.util.*;

public class StockTradingPlatform {
    static List<Stock> stocks = new ArrayList<>();
    static List<User> users = new ArrayList<>();
    static Scanner scanner = new Scanner(System.in);
    static final String DATA_FILE = "trading_data.txt";
    static User currentUser = null;

    public static void main(String[] args) {
        initializeStocks();
        loadData();

        while (true) {
            if (currentUser == null) showLoginMenu();
            else showMainMenu();
        }
    }

    static void showLoginMenu() {
        System.out.println("\n1.Login  2.Register  3.Exit");
        int ch = getIntInput("Choice: ");

        switch (ch) {
            case 1 -> login();
            case 2 -> register();
            case 3 -> { saveData(); System.exit(0); }
            default -> System.out.println("Invalid!");
        }
    }

    static void showMainMenu() {
        System.out.println("\nWelcome " + currentUser.getName());
        System.out.println("1.Market 2.Portfolio 3.Buy 4.Sell 5.Transactions 6.Exit");

        int ch = getIntInput("Choice: ");

        switch (ch) {
            case 1 -> viewMarket();
            case 2 -> currentUser.viewPortfolio();
            case 3 -> buyStock();
            case 4 -> sellStock();
            case 5 -> currentUser.viewTransactions();
            case 6 -> currentUser = null;
        }
    }

    static void register() {
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();

        if (findUser(email) != null) {
            System.out.println("Already exists!");
            return;
        }

        User u = new User(name, email, 10000);
        users.add(u);
        currentUser = u;
    }

    static void login() {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        User u = findUser(email);

        if (u == null) System.out.println("Not found!");
        else currentUser = u;
    }

    static void viewMarket() {
        for (Stock s : stocks) {
            s.updatePrice();
            System.out.println(s.symbol + " $" + s.price);
        }
    }

    static void buyStock() {
        viewMarket();
        System.out.print("Symbol: ");
        String sym = scanner.nextLine().toUpperCase();

        Stock s = findStock(sym);
        if (s == null) return;

        int qty = getIntInput("Qty: ");
        double cost = s.price * qty;

        if (currentUser.balance < cost) {
            System.out.println("No money!");
            return;
        }

        currentUser.buy(s, qty);
    }

    static void sellStock() {
        System.out.print("Symbol: ");
        String sym = scanner.nextLine().toUpperCase();
        int qty = getIntInput("Qty: ");

        currentUser.sell(sym, qty);
    }

    static User findUser(String email) {
        for (User u : users)
            if (u.email.equalsIgnoreCase(email)) return u;
        return null;
    }

    static Stock findStock(String sym) {
        for (Stock s : stocks)
            if (s.symbol.equalsIgnoreCase(sym)) return s;
        return null;
    }

    static int getIntInput(String msg) {
        System.out.print(msg);
        while (!scanner.hasNextInt()) {
            System.out.print("Enter number: ");
            scanner.next();
        }
        int val = scanner.nextInt();
        scanner.nextLine(); // FIX
        return val;
    }

    static void initializeStocks() {
        stocks.add(new Stock("AAPL", 150));
        stocks.add(new Stock("GOOGL", 2800));
        stocks.add(new Stock("MSFT", 400));
    }

    static void saveData() {
        try (PrintWriter pw = new PrintWriter(DATA_FILE)) {
            for (User u : users)
                pw.println(u.name + "," + u.email + "," + u.balance);
        } catch (Exception e) {}
    }

    static void loadData() {
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                users.add(new User(p[0], p[1], Double.parseDouble(p[2])));
            }
        } catch (Exception e) {}
    }
}

// ---------- STOCK ----------
class Stock {
    String symbol;
    double price;
    Random r = new Random();

    Stock(String s, double p) {
        symbol = s;
        price = p;
    }

    void updatePrice() {
        price += (r.nextDouble() - 0.5) * 10;
    }
}

// ---------- USER ----------
class User {
    String name, email;
    double balance;
    Map<String, Integer> portfolio = new HashMap<>();
    List<String> transactions = new ArrayList<>();

    User(String n, String e, double b) {
        name = n;
        email = e;
        balance = b;
    }

    void buy(Stock s, int qty) {
        double cost = s.price * qty;
        balance -= cost;

        portfolio.put(s.symbol, portfolio.getOrDefault(s.symbol, 0) + qty);

        transactions.add("BUY " + s.symbol + " " + qty);
        System.out.println("Bought!");
    }

    void sell(String sym, int qty) {
        if (!portfolio.containsKey(sym) || portfolio.get(sym) < qty) {
            System.out.println("No shares!");
            return;
        }

        portfolio.put(sym, portfolio.get(sym) - qty);
        balance += qty * 100; // simple

        transactions.add("SELL " + sym + " " + qty);
        System.out.println("Sold!");
    }

    void viewPortfolio() {
        System.out.println(portfolio);
        System.out.println("Balance: " + balance);
    }

    void viewTransactions() {
        for (String t : transactions)
            System.out.println(t);
    }

    String getName() { return name; }
}
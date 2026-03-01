import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.*;

import javax.swing.*;

// Database connection class
class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/bookstore";
    private static final String USER = "root";
    private static final String PASSWORD = "Your_Password_Here";

    public static Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage());
            return null;
        }
    }
}

// Book operations class
class BookOperations {

    public static void addBook(String title, String author, double price, int stock) {
        String sql = "INSERT INTO books (title, author, price, stock) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.connect();
        if (conn == null) return;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setDouble(3, price);
            stmt.setInt(4, stock);
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error adding book: " + e.getMessage());
        }
    }

    public static String searchBooks(String keyword) {
        StringBuilder result = new StringBuilder();
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE LOWER(?) OR LOWER(author) LIKE LOWER(?)";
        Connection conn = DatabaseConnection.connect();
        if (conn == null) return "Database connection error.";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                result.append("ID: ").append(rs.getInt("id"))
                        .append(" | Title: ").append(rs.getString("title"))
                        .append(" | Author: ").append(rs.getString("author"))
                        .append(" | Price: ").append(rs.getDouble("price"))
                        .append(" | Stock: ").append(rs.getInt("stock"))
                        .append("\n");
            }

            return result.length() > 0 ? result.toString() : "No books found.";
        } catch (SQLException e) {
            return "Error searching books: " + e.getMessage();
        }
    }

    public static void sellBookByTitle(String title, int quantity) {
        String checkSql = "SELECT stock FROM books WHERE title = ?";
        String updateSql = "UPDATE books SET stock = stock - ? WHERE title = ?";

        Connection conn = DatabaseConnection.connect();
        if (conn == null) return;

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            checkStmt.setString(1, title);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int stock = rs.getInt("stock");
                if (stock >= quantity) {
                    updateStmt.setInt(1, quantity);
                    updateStmt.setString(2, title);
                    updateStmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Sold successfully.");
                } else {
                    JOptionPane.showMessageDialog(null, "Not enough stock.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Book title not found.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error selling book: " + e.getMessage());
        }
    }

    public static void updateStockByTitle(String title, int newStock) {
        String sql = "UPDATE books SET stock = ? WHERE title = ?";
        Connection conn = DatabaseConnection.connect();
        if (conn == null) return;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newStock);
            stmt.setString(2, title);
            int rows = stmt.executeUpdate();

            if (rows > 0)
                JOptionPane.showMessageDialog(null, "Stock updated successfully.");
            else
                JOptionPane.showMessageDialog(null, "Book not found.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating stock: " + e.getMessage());
        }
    }

    public static String getAllBooks() {
        StringBuilder result = new StringBuilder();
        String sql = "SELECT * FROM books";
        Connection conn = DatabaseConnection.connect();
        if (conn == null) return "Database connection error.";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.append("ID: ").append(rs.getInt("id"))
                        .append(" | Title: ").append(rs.getString("title"))
                        .append(" | Author: ").append(rs.getString("author"))
                        .append(" | Price: ").append(rs.getDouble("price"))
                        .append(" | Stock: ").append(rs.getInt("stock"))
                        .append("\n");
            }

            return result.length() > 0 ? result.toString() : "No books in stock.";
        } catch (SQLException e) {
            return "Error retrieving books: " + e.getMessage();
        }
    }
}

// GUI Application
public class Bookmanagement {
    public static void main(String[] args) {
        JFrame frame = new JFrame("📚 Bookstore Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 14));

        tabs.addTab("➕ Add Book", createAddBookPanel());
        tabs.addTab("🔍 Search Book", createSearchBookPanel());
        tabs.addTab("🛒 Sell Book", createSellBookPanel());
        tabs.addTab("✏️ Update Stock", createUpdateStockPanel());
        tabs.addTab("📦 View Stock", createViewStockPanel());

        frame.add(tabs);
        frame.setVisible(true);
    }

    private static JPanel createAddBookPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        JButton addButton = new JButton("Add Book");

        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Author:"));
        panel.add(authorField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Stock:"));
        panel.add(stockField);
        panel.add(new JLabel());
        panel.add(addButton);

        addButton.addActionListener(e -> {
            try {
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                String priceText = priceField.getText().trim();
                String stockText = stockField.getText().trim();

                if (title.isEmpty() || author.isEmpty() || priceText.isEmpty() || stockText.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "All fields are required.");
                    return;
                }

                double price = Double.parseDouble(priceText);
                int stock = Integer.parseInt(stockText);
                BookOperations.addBook(title, author, price, stock);
                JOptionPane.showMessageDialog(null, "Book added successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Invalid input: " + ex.getMessage());
            }
        });

        return panel;
    }

    private static JPanel createSearchBookPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JTextField searchField = new JTextField();
        JTextArea searchResults = new JTextArea();
        JButton searchButton = new JButton("Search");
        searchResults.setEditable(false);
        searchResults.setFont(new Font("Monospaced", Font.PLAIN, 12));

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(searchField, BorderLayout.NORTH);
        panel.add(new JScrollPane(searchResults), BorderLayout.CENTER);
        panel.add(searchButton, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter a search keyword.");
                return;
            }
            String results = BookOperations.searchBooks(keyword);
            searchResults.setText(results);
        });

        return panel;
    }

    private static JPanel createSellBookPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField titleField = new JTextField();
        JTextField qtyField = new JTextField();
        JButton sellBtn = new JButton("Sell");

        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        panel.add(new JLabel("Book Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Quantity:"));
        panel.add(qtyField);
        panel.add(new JLabel());
        panel.add(sellBtn);

        sellBtn.addActionListener(e -> {
            try {
                String title = titleField.getText().trim();
                String qtyText = qtyField.getText().trim();

                if (title.isEmpty() || qtyText.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Both fields are required.");
                    return;
                }

                int qty = Integer.parseInt(qtyText);
                BookOperations.sellBookByTitle(title, qty);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Invalid input: " + ex.getMessage());
            }
        });

        return panel;
    }

    private static JPanel createUpdateStockPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField titleField = new JTextField();
        JTextField stockField = new JTextField();
        JButton updateBtn = new JButton("Update");

        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        panel.add(new JLabel("Book Title:"));
        panel.add(titleField);
        panel.add(new JLabel("New Stock:"));
        panel.add(stockField);
        panel.add(new JLabel());
        panel.add(updateBtn);

        updateBtn.addActionListener(e -> {
            try {
                String title = titleField.getText().trim();
                String stockText = stockField.getText().trim();

                if (title.isEmpty() || stockText.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Both fields are required.");
                    return;
                }

                int stock = Integer.parseInt(stockText);
                BookOperations.updateStockByTitle(title, stock);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Invalid input: " + ex.getMessage());
            }
        });

        return panel;
    }

    private static JPanel createViewStockPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea viewResults = new JTextArea();
        JButton viewButton = new JButton("Refresh Stock");
        viewResults.setEditable(false);
        viewResults.setFont(new Font("Monospaced", Font.PLAIN, 12));

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(viewResults), BorderLayout.CENTER);
        panel.add(viewButton, BorderLayout.SOUTH);

        viewButton.addActionListener(e -> {
            String results = BookOperations.getAllBooks();
            viewResults.setText(results);
        });

        return panel;
    }
}

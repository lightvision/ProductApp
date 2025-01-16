package routmmariusionel.productapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class ProductApp extends Application {

    private TableView<Product> tableView;
    private TextField nameField, priceField;

    @Override
    public void start(Stage primaryStage) {
        // Create database and table
        createDatabase();

        // Initialize TableView
        tableView = new TableView<>();
        TableColumn<Product, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        tableView.getColumns().addAll(nameColumn, priceColumn);
        loadProducts();

        // Form for adding products
        nameField = new TextField();
        nameField.setPromptText("Product Name");

        priceField = new TextField();
        priceField.setPromptText("Price");

        Button addButton = new Button("Add Product");
        addButton.setOnAction(e -> addProduct());

        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Name:"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Price:"), 0, 1);
        form.add(priceField, 1, 1);
        form.add(addButton, 1, 2);

        // Layout
        VBox layout = new VBox(10, tableView, form);
        layout.setPadding(new Insets(10));

        // Scene
        Scene scene = new Scene(layout, 400, 400);
        primaryStage.setTitle("Product Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createDatabase() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:products.db")) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "price REAL NOT NULL);";
            conn.createStatement().execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadProducts() {
        tableView.getItems().clear();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:products.db")) {
            String query = "SELECT * FROM products";
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                tableView.getItems().add(new Product(rs.getString("name"), rs.getDouble("price")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addProduct() {
        String name = nameField.getText();
        String priceText = priceField.getText();

        if (name.isEmpty() || priceText.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill all fields.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:products.db")) {
                String insertSQL = "INSERT INTO products (name, price) VALUES (?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setString(1, name);
                pstmt.setDouble(2, price);
                pstmt.executeUpdate();

                nameField.clear();
                priceField.clear();
                loadProducts();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid price format.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Product {
        private final String name;
        private final double price;

        public Product(String name, double price) {
            this.name = name;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }
    }
}


/**
 * ProductApp
 * Această aplicație JavaFX permite gestionarea produselor utilizând o interfață grafică conectată la o bază de date SQLite.
 * Funcționalități principale:
 * - Adăugarea unui produs (nume și preț).
 * - Vizualizarea produselor într-un tabel.
 * - Actualizarea informațiilor despre un produs selectat.
 * - Ștergerea unui produs selectat, cu confirmare prealabilă.
 *
 * Aplicația include validări pentru datele introduse și notificări pentru utilizator în cazul erorilor.
 *
 * Pentru detalii de rulare se pot vedea si screenshot-urile atasate
 *
 * @copyright Marius Ionel, Informatica ID, anul 3, promotia 2022 - 2025
 */
package routmmariusionel.productapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class ProductApp extends Application {

    private TableView<Product> tableView;
    private TextField nameField, priceField;

    @Override
    public void start(Stage primaryStage) {
        // Creare baza de date si tabel
        createDatabase();

        // Initializare TableView
        tableView = new TableView<>();
        TableColumn<Product, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        tableView.getColumns().addAll(nameColumn, priceColumn);
        loadProducts();

        // Form pentru adaugarea produselor
        nameField = new TextField();
        nameField.setPromptText("Product Name");

        priceField = new TextField();
        priceField.setPromptText("Price");

        Button addButton = new Button("Add Product");
        addButton.setOnAction(e -> addProduct());

        Button updateButton = new Button("Update Product");
        updateButton.setOnAction(e -> updateProduct());

        Button deleteButton = new Button("Delete Product");
        deleteButton.setOnAction(e -> confirmAndDeleteProduct());

        // Gestionare tasta Enter
        priceField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addProduct();
            }
        });

        tableView.setOnMouseClicked(event -> {
            Product selectedProduct = tableView.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                nameField.setText(selectedProduct.getName());
                priceField.setText(String.valueOf(selectedProduct.getPrice()));
            }
        });

        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Name:"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Price:"), 0, 1);
        form.add(priceField, 1, 1);
        form.add(addButton, 0, 2);
        form.add(updateButton, 1, 2);
        form.add(deleteButton, 2, 2);

        // Layout principal
        VBox layout = new VBox(10, tableView, form);
        layout.setPadding(new Insets(10));

        // Configurare scena
        Scene scene = new Scene(layout, 600, 400);
        primaryStage.setTitle("Product Manager");

        // Adaugare iconita personalizata
        primaryStage.getIcons().add(new Image(ProductApp.class.getResourceAsStream("/routmmariusionel/productapp/ProductAppIcon.png")));



        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createDatabase() {
        // Creare baza de date si tabel daca nu exista
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
        // Incarcare produse din baza de date in tabel
        tableView.getItems().clear();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:products.db")) {
            String query = "SELECT * FROM products";
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                tableView.getItems().add(new Product(rs.getInt("id"), rs.getString("name"), rs.getDouble("price")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addProduct() {
        // Adaugare produs in baza de date
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

    private void updateProduct() {
        // Actualizare produs selectat in baza de date
        Product selectedProduct = tableView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No product selected.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

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
                String updateSQL = "UPDATE products SET name = ?, price = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(updateSQL);
                pstmt.setString(1, name);
                pstmt.setDouble(2, price);
                pstmt.setInt(3, selectedProduct.getId());
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

    private void confirmAndDeleteProduct() {
        // Confirmare si stergere produs selectat
        Product selectedProduct = tableView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No product selected.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Are you sure you want to delete this product?");
        confirmAlert.setContentText("Product: " + selectedProduct.getName());

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:products.db")) {
                    String deleteSQL = "DELETE FROM products WHERE id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
                    pstmt.setInt(1, selectedProduct.getId());
                    pstmt.executeUpdate();

                    nameField.clear();
                    priceField.clear();
                    loadProducts();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Product {
        private final int id;
        private final String name;
        private final double price;

        public Product(int id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }
    }
}

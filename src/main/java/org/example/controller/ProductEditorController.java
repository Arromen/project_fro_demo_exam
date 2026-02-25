package org.example.controller;

import org.example.database.DatabaseHandler;
import org.example.model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.nio.file.*;

public class ProductEditorController {
    @FXML private ImageView productImage;
    @FXML private TextField articleField;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private ComboBox<String> manufacturerBox;
    @FXML private ComboBox<String> supplierBox;
    @FXML private TextField priceField;
    @FXML private TextField discountField;
    @FXML private TextField stockField;
    @FXML private TextField unitField;
    @FXML private TextArea descriptionField;

    private MainController mainController;
    private Product currentProduct;
    private String currentPhotoPath;
    private FileChooser fileChooser;

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    public void setProduct(Product product) {
        this.currentProduct = product;
        initializeFields();
        loadComboBoxes();

        if (product != null) {
            articleField.setText(product.getArticle());
            nameField.setText(product.getName());
            priceField.setText(String.valueOf(product.getPrice()));
            discountField.setText(String.valueOf(product.getDiscount()));
            stockField.setText(String.valueOf(product.getStock()));
            unitField.setText(product.getUnitName());
            descriptionField.setText(product.getDescription());
            currentPhotoPath = product.getPhotoPath();
            loadImage(currentPhotoPath);
        } else {
            unitField.setText("шт.");
            currentPhotoPath = "picture.png";
            loadImage("picture.png");
        }
    }

    private void initializeFields() {
        // Валидация ввода (только числа)
        priceField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*\\.?\\d*")) priceField.setText(old);
        });
        discountField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) discountField.setText(old);
        });
        stockField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) stockField.setText(old);
        });

        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
    }

    private void loadComboBoxes() {
        try {
            Connection conn = DatabaseHandler.getConnection();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT name FROM categories");
            while (rs.next()) categoryBox.getItems().add(rs.getString("name"));

            rs = stmt.executeQuery("SELECT name FROM manufacturers");
            while (rs.next()) manufacturerBox.getItems().add(rs.getString("name"));

            rs = stmt.executeQuery("SELECT name FROM suppliers");
            while (rs.next()) supplierBox.getItems().add(rs.getString("name"));

            if (currentProduct != null) {
                categoryBox.setValue(currentProduct.getCategoryName());
                manufacturerBox.setValue(currentProduct.getManufacturerName());
                supplierBox.setValue(currentProduct.getSupplierName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadImage(String path) {
        if (path == null || path.isEmpty()) path = "picture.png";
        File file = new File("images/" + path);
        if (file.exists()) {
            productImage.setImage(new Image(file.toURI().toString()));
        } else {
            productImage.setImage(new Image(getClass().getResourceAsStream("/images/picture.png")));
        }
    }

    @FXML
    private void handleUploadImage() {
        File file = fileChooser.showOpenDialog(productImage.getScene().getWindow());
        if (file != null) {
            try {
                BufferedImage img = ImageIO.read(file);
                if (img.getWidth() > 300 || img.getHeight() > 200) {
                    showAlert("Ошибка", "Размер фото превышает 300x200 пикселей");
                    return;
                }

                // Копируем в папку images
                String fileName = System.currentTimeMillis() + "_" + file.getName();
                Path dest = Paths.get("images/" + fileName);
                Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

                // Удаляем старое фото если оно не заглушка
                if (currentPhotoPath != null && !currentPhotoPath.equals("picture.png")) {
                    new File("images/" + currentPhotoPath).delete();
                }

                currentPhotoPath = fileName;
                loadImage(fileName);
            } catch (IOException e) {
                showAlert("Ошибка", "Не удалось загрузить изображение: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSave() {
        // Валидация
        if (articleField.getText().isEmpty() || nameField.getText().isEmpty()) {
            showAlert("Ошибка", "Заполните обязательные поля (Артикул, Наименование)");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceField.getText());
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Цена должна быть неотрицательным числом");
            return;
        }

        int discount, stock;
        try {
            discount = Integer.parseInt(discountField.getText());
            stock = Integer.parseInt(stockField.getText());
            if (discount < 0 || stock < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Скидка и количество должны быть неотрицательными целыми числами");
            return;
        }

        try {
            Connection conn = DatabaseHandler.getConnection();

            // Получаем ID справочников
            int catId = getIdByName(conn, "categories", categoryBox.getValue());
            int manId = getIdByName(conn, "manufacturers", manufacturerBox.getValue());
            int supId = getIdByName(conn, "suppliers", supplierBox.getValue());
            int unitId = getIdByName(conn, "units", unitField.getText());

            if (currentProduct == null) {
                // Добавление
                String query = "INSERT INTO products (article, name, unit_id, price, supplier_id, " +
                        "manufacturer_id, category_id, discount, stock, description, photo_path) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, articleField.getText());
                ps.setString(2, nameField.getText());
                ps.setInt(3, unitId);
                ps.setDouble(4, price);
                ps.setInt(5, supId);
                ps.setInt(6, manId);
                ps.setInt(7, catId);
                ps.setInt(8, discount);
                ps.setInt(9, stock);
                ps.setString(10, descriptionField.getText());
                ps.setString(11, currentPhotoPath);
                ps.executeUpdate();
            } else {
                // Редактирование
                String query = "UPDATE products SET article=?, name=?, unit_id=?, price=?, " +
                        "supplier_id=?, manufacturer_id=?, category_id=?, discount=?, " +
                        "stock=?, description=?, photo_path=? WHERE id=?";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, articleField.getText());
                ps.setString(2, nameField.getText());
                ps.setInt(3, unitId);
                ps.setDouble(4, price);
                ps.setInt(5, supId);
                ps.setInt(6, manId);
                ps.setInt(7, catId);
                ps.setInt(8, discount);
                ps.setInt(9, stock);
                ps.setString(10, descriptionField.getText());
                ps.setString(11, currentPhotoPath);
                ps.setInt(12, currentProduct.getId());
                ps.executeUpdate();
            }

            mainController.refreshProducts();
            ((Stage) productImage.getScene().getWindow()).close();
        } catch (SQLException e) {
            if (e.getMessage().contains("Unique constraint")) {
                showAlert("Ошибка", "Товар с таким артикулом уже существует");
            } else {
                showAlert("Ошибка БД", e.getMessage());
            }
            e.printStackTrace();
        }
    }

    private int getIdByName(Connection conn, String table, String name) throws SQLException {
        String query = "SELECT id FROM " + table + " WHERE name = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("id");
        return 1; // Fallback
    }

    @FXML
    private void handleCancel() {
        ((Stage) productImage.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

package org.example.controller;

import org.example.database.DatabaseHandler;
import org.example.model.Product;
import org.example.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

public class MainController {
    @FXML private TableView<Product> productTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> supplierFilter;
    @FXML private Label userFioLabel;
    @FXML private Button addProductBtn;
    @FXML private Button logoutBtn;

    private ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private User currentUser;
    private static boolean isEditorOpen = false; // Блокировка множественного открытия

    public void initialize() {
        loadProducts();
        setupTableColumns();
        setupFilters();
        setupRoleAccess();
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            userFioLabel.setText(user.getFio());
        } else {
            userFioLabel.setText("Гость");
        }
        setupRoleAccess();
    }

    private void setupRoleAccess() {
        if (currentUser == null || !currentUser.isAdmin()) {
            addProductBtn.setVisible(false);
            addProductBtn.setManaged(false);
        } else {
            addProductBtn.setVisible(true);
            addProductBtn.setManaged(true);
        }
    }

    private void loadProducts() {
        try {
            Connection conn = DatabaseHandler.getConnection();
            String query = "SELECT p.*, s.name as supplier, c.name as category, " +
                    "m.name as manufacturer, u.name as unit " +
                    "FROM products p " +
                    "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                    "LEFT JOIN categories c ON p.category_id = c.id " +
                    "LEFT JOIN manufacturers m ON p.manufacturer_id = m.id " +
                    "LEFT JOIN units u ON p.unit_id = u.id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            allProducts.clear();
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setArticle(rs.getString("article"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getDouble("price"));
                p.setDiscount(rs.getInt("discount"));
                p.setStock(rs.getInt("stock"));
                p.setDescription(rs.getString("description"));
                p.setPhotoPath(rs.getString("photo_path"));
                p.setSupplierName(rs.getString("supplier"));
                p.setCategoryName(rs.getString("category"));
                p.setManufacturerName(rs.getString("manufacturer"));
                p.setUnitName(rs.getString("unit"));
                allProducts.add(p);
            }
            productTable.setItems(allProducts);

            // Заполняем фильтр поставщиков
            List<String> suppliers = allProducts.stream()
                    .map(Product::getSupplierName)
                    .distinct()
                    .collect(Collectors.toList());
            supplierFilter.getItems().clear();
            supplierFilter.getItems().addAll("Все поставщики");
            supplierFilter.getItems().addAll(suppliers);
            supplierFilter.setValue("Все поставщики");

        } catch (SQLException e) {
            showAlert("Ошибка БД", "Не удалось загрузить товары", e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        // Фото
        TableColumn<Product, Void> photoCol = new TableColumn<>("Фото");
        photoCol.setPrefWidth(80);
        photoCol.setCellFactory(param -> new TableCell<Product, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Product p = getTableView().getItems().get(getIndex());
                    ImageView img = new ImageView();
                    img.setFitWidth(50);
                    img.setFitHeight(50);
                    img.setPreserveRatio(true);

                    String path = p.getPhotoPath();
                    if (path != null && !path.isEmpty()) {
                        File file = new File("images/" + path);
                        if (file.exists()) {
                            img.setImage(new Image(file.toURI().toString()));
                        } else {
                            img.setImage(new Image(getClass().getResourceAsStream("/images/picture.png")));
                        }
                    } else {
                        img.setImage(new Image(getClass().getResourceAsStream("/images/picture.png")));
                    }
                    setGraphic(img);
                }
            }
        });

        // Остальные колонки
        TableColumn<Product, String> articleCol = new TableColumn<>("Артикул");
        articleCol.setCellValueFactory(new PropertyValueFactory<>("article"));
        articleCol.setPrefWidth(100);

        TableColumn<Product, String> nameCol = new TableColumn<>("Наименование");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<Product, String> categoryCol = new TableColumn<>("Категория");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryCol.setPrefWidth(120);

        TableColumn<Product, String> manufacturerCol = new TableColumn<>("Производитель");
        manufacturerCol.setCellValueFactory(new PropertyValueFactory<>("manufacturerName"));
        manufacturerCol.setPrefWidth(120);

        TableColumn<Product, String> supplierCol = new TableColumn<>("Поставщик");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        supplierCol.setPrefWidth(120);

        // Цена с зачеркиванием
        TableColumn<Product, Void> priceCol = new TableColumn<>("Цена");
        priceCol.setPrefWidth(120);
        priceCol.setCellFactory(param -> new TableCell<Product, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Product p = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(5);
                    Text originalPrice = new Text(String.format("%.2f", p.getPrice()));

                    if (p.getDiscount() > 0) {
                        originalPrice.setStrikethrough(true);
                        originalPrice.setStyle("-fx-fill: red;");
                        Text finalPrice = new Text(String.format("%.2f", p.getFinalPrice()));
                        finalPrice.setStyle("-fx-fill: black; -fx-font-weight: bold;");
                        box.getChildren().addAll(originalPrice, finalPrice);
                    } else {
                        box.getChildren().add(originalPrice);
                    }
                    setGraphic(box);
                }
            }
        });

        TableColumn<Product, Integer> discountCol = new TableColumn<>("Скидка %");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discount"));
        discountCol.setPrefWidth(80);

        TableColumn<Product, Integer> stockCol = new TableColumn<>("На складе");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockCol.setPrefWidth(80);

        // Сортировка по количеству (Требование Модуля 3)
        stockCol.setSortType(TableColumn.SortType.ASCENDING);

        // Действия (для Админа)
        TableColumn<Product, Void> actionCol = new TableColumn<>("Действия");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(param -> new TableCell<Product, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || currentUser == null || !currentUser.isAdmin()) {
                    setGraphic(null);
                } else {
                    Button editBtn = new Button("✏️");
                    editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    editBtn.setOnAction(e -> handleEditProduct(getTableView().getItems().get(getIndex())));

                    Button deleteBtn = new Button("🗑️");
                    deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                    deleteBtn.setOnAction(e -> handleDeleteProduct(getTableView().getItems().get(getIndex())));

                    setGraphic(new HBox(5, editBtn, deleteBtn));
                }
            }
        });

        productTable.getColumns().clear();
        productTable.getColumns().addAll(photoCol, articleCol, nameCol, categoryCol,
                manufacturerCol, supplierCol, priceCol,
                discountCol, stockCol, actionCol);
    }

    private void setupFilters() {
        // Подсветка строк (Требование Модуля 2)
        productTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.itemProperty().addListener((obs, old, product) -> {
                if (product != null) {
                    if (product.getDiscount() > 15) {
                        row.setStyle("-fx-background-color: #2E8B57; -fx-text-fill: white;");
                    } else if (product.getStock() == 0) {
                        row.setStyle("-fx-background-color: #ADD8E6;");
                    } else {
                        row.setStyle("");
                    }
                }
            });
            return row;
        });

        // Поиск и фильтрация в реальном времени (Требование Модуля 3)
        searchField.textProperty().addListener((obs, old, val) -> filterData());
        supplierFilter.valueProperty().addListener((obs, old, val) -> filterData());
    }

    private void filterData() {
        String query = searchField.getText().toLowerCase();
        String supplier = supplierFilter.getValue();

        ObservableList<Product> filtered = allProducts.stream()
                .filter(p -> {
                    boolean matchesSearch = p.getName().toLowerCase().contains(query) ||
                            p.getArticle().toLowerCase().contains(query) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(query));
                    boolean matchesSupplier = "Все поставщики".equals(supplier) ||
                            p.getSupplierName().equals(supplier);
                    return matchesSearch && matchesSupplier;
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        productTable.setItems(filtered);
    }

    @FXML
    private void handleAddProduct() {
        if (isEditorOpen) {
            showAlert("Внимание", "Редактор уже открыт", "Нельзя открыть более одного окна редактирования.");
            return;
        }
        openEditor(null);
    }

    private void handleEditProduct(Product product) {
        if (isEditorOpen) {
            showAlert("Внимание", "Редактор уже открыт", "Нельзя открыть более одного окна редактирования.");
            return;
        }
        openEditor(product);
    }

    private void handleDeleteProduct(Product product) {
        if (!currentUser.isAdmin()) return;

        // Проверка: есть ли товар в заказах (Требование Модуля 3)
        try {
            Connection conn = DatabaseHandler.getConnection();
            String checkQuery = "SELECT COUNT(*) FROM order_items WHERE product_id = ?";
            PreparedStatement ps = conn.prepareStatement(checkQuery);
            ps.setInt(1, product.getId());
            ResultSet rs = ps.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                showAlert("Ошибка удаления", "Товар нельзя удалить",
                        "Товар присутствует в заказах. Удаление запрещено.");
                return;
            }

            // Подтверждение
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Подтверждение удаления");
            confirm.setHeaderText("Удаление товара: " + product.getName());
            confirm.setContentText("Вы уверены? Это действие нельзя отменить.");

            if (confirm.showAndWait().get() == ButtonType.OK) {
                String deleteQuery = "DELETE FROM products WHERE id = ?";
                PreparedStatement deletePs = conn.prepareStatement(deleteQuery);
                deletePs.setInt(1, product.getId());
                deletePs.executeUpdate();

                // Удаляем фото если есть
                if (product.getPhotoPath() != null && !product.getPhotoPath().equals("picture.png")) {
                    new File("images/" + product.getPhotoPath()).delete();
                }

                loadProducts();
                showAlert("Успешно", "Товар удален", "Товар успешно удален из базы данных.");
            }
        } catch (SQLException e) {
            showAlert("Ошибка БД", "Не удалось удалить товар", e.getMessage());
            e.printStackTrace();
        }
    }

    private void openEditor(Product product) {
        try {
            isEditorOpen = true;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/product_editor.fxml"));
            Parent root = loader.load();
            ProductEditorController controller = loader.getController();
            controller.setMainController(this);
            controller.setProduct(product);

            Stage stage = new Stage();
            stage.setTitle(product == null ? "Добавление товара" : "Редактирование товара");
            stage.setScene(new Scene(root, 700, 800));
            stage.setResizable(false);

            // Блокировка закрытия для сброса флага
            stage.setOnCloseRequest(e -> isEditorOpen = false);

            stage.showAndWait();
        } catch (Exception e) {
            isEditorOpen = false;
            showAlert("Ошибка", "Не удалось открыть редактор", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            isEditorOpen = false;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Магазин обуви - Вход");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshProducts() {
        loadProducts();
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
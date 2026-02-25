package org.example.controller;

import org.example.database.DatabaseHandler;
import org.example.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;

public class LoginController {
    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (login.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Введите логин и пароль");
            return;
        }

        try {
            Connection conn = DatabaseHandler.getConnection();
            String query = "SELECT u.id, u.fio, r.name as role " +
                    "FROM users u JOIN roles r ON u.role_id = r.id " +
                    "WHERE u.login = ? AND u.password = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User currentUser = new User(
                        rs.getInt("id"),
                        rs.getString("fio"),
                        login,
                        rs.getString("role")
                );
                openMainApp(currentUser);
            } else {
                errorLabel.setText("Неверный логин или пароль");
            }
        } catch (SQLException e) {
            errorLabel.setText("Ошибка БД: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGuest() {
        openMainApp(null); // null означает гостя
    }

    private void openMainApp(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_view.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) loginField.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Магазин обуви - Список товаров");
            stage.show();
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось загрузить главное окно", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        // Обработка Enter в полях
        loginField.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

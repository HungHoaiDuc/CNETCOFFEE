package com.example.cnetcoffee.Controller.User;

import com.example.cnetcoffee.Model.User;
import com.example.cnetcoffee.dao.UserManagerDAO;
import com.example.cnetcoffee.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ProfileUserController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField txtFullname;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;

    @FXML private Label lblConfirm;

    @FXML private Button btnEdit, btnSave, btnCancel;

    private User currentUser;
    private final UserManagerDAO userDAO = new UserManagerDAO();

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            loadUserInfo();
        }
        setEditable(false);
        toggleConfirmPassword(false);
    }

    private void loadUserInfo() {
        User updatedUser = userDAO.getUserById(currentUser.getUserId());
        if (updatedUser != null) {
            currentUser = updatedUser;
            txtUsername.setText(currentUser.getUsername());
            txtPassword.setText(currentUser.getPassword());
            txtConfirmPassword.setText(currentUser.getPassword());
            txtFullname.setText(currentUser.getFullName());
            txtEmail.setText(currentUser.getEmail());
            txtPhone.setText(currentUser.getPhone());
        }
    }

    private void setEditable(boolean editable) {
        txtUsername.setEditable(editable);
        txtPassword.setEditable(editable);
        txtConfirmPassword.setEditable(editable);
        txtFullname.setEditable(editable);
        txtEmail.setEditable(editable);
        txtPhone.setEditable(editable);

        btnSave.setVisible(editable);
        btnCancel.setVisible(editable);
        btnEdit.setVisible(!editable);
    }

    private void toggleConfirmPassword(boolean visible) {
        lblConfirm.setVisible(visible);
        lblConfirm.setManaged(visible);
        txtConfirmPassword.setVisible(visible);
        txtConfirmPassword.setManaged(visible);
    }

    @FXML
    private void handleEdit() {
        setEditable(true);
        toggleConfirmPassword(true);
    }

    @FXML
    private void handleCancel() {
        loadUserInfo(); // Reload user info to discard changes
        setEditable(false);
        toggleConfirmPassword(false);
    }

    @FXML
    private void handleSave() {
        String newPassword = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (!newPassword.equals(confirmPassword)) {
            showAlert("Lỗi", "Mật khẩu xác nhận không khớp!");
            return;
        }

        // Update user information from text fields
        currentUser.setUsername(txtUsername.getText()); // Be careful with this!
        currentUser.setPassword(newPassword);
        currentUser.setFullName(txtFullname.getText());
        currentUser.setEmail(txtEmail.getText());
        currentUser.setPhone(txtPhone.getText());

        userDAO.updateUserProfile(currentUser);

        setEditable(false);
        toggleConfirmPassword(false);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

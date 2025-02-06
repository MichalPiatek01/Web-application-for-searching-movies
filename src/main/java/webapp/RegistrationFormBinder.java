package webapp;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import webapp.entities.User;
import webapp.services.UserService;
import webapp.services.UserServiceImpl;

public class RegistrationFormBinder {

    private final UserService userService;

    private final RegistrationForm registrationForm;

    private boolean enablePasswordValidation;

    public RegistrationFormBinder(RegistrationForm registrationForm, UserService userService) {
        this.userService = userService;
        this.registrationForm = registrationForm;
    }

    public void addBindingAndValidation(Runnable onSuccess) {
        BeanValidationBinder<User> binder = new BeanValidationBinder<>(User.class);

        binder.forField(registrationForm.getUsername())
                .asRequired("Username is required")
                .bind("username");

        binder.forField(registrationForm.getEmail())
                .asRequired("Email is required")
                .bind("email");

        binder.forField(registrationForm.getPasswordField())
                .withValidator(this::passwordValidator)
                .bind("password");

        registrationForm.getPasswordConfirmField().addValueChangeListener(e -> {
            enablePasswordValidation = true;
            binder.validate();
        });

        binder.setStatusLabel(registrationForm.getErrorMessageField());

        registrationForm.getSubmitButton().addClickListener(event -> {
            try {
                User userBean = new User();
                binder.writeBean(userBean);
                userService.save(userBean);
                onSuccess.run();
            } catch (UserServiceImpl.DuplicateUsernameException e) {
                showErrorDialog("This username is already taken. Choose a different one.");
            } catch (UserServiceImpl.DuplicateEmailException e) {
                showErrorDialog("This email is already in use. Use another email address.");
            } catch (ValidationException e) {
                showErrorNotification();
            }
        });
    }

    private void showErrorNotification() {
        Notification notification = new Notification("Please check the form for errors.", 3000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.open();
    }

    private void showErrorDialog(String message) {
        Dialog dialog = new Dialog();
        Span span = new Span(message);
        dialog.add(span);

        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        dialog.open();
    }


    private ValidationResult passwordValidator(String pass1, ValueContext ctx) {

        if (pass1 == null || pass1.length() < 8) {
            return ValidationResult.error("Password should be at least 8 characters long");
        }
        if (!enablePasswordValidation) {
            enablePasswordValidation = true;
            return ValidationResult.ok();
        }
        String pass2 = registrationForm.getPasswordField().getValue();
        if (pass1.equals(pass2)) {
            return ValidationResult.ok();
        }
        return ValidationResult.error("Passwords do not match");
    }

    private void showSuccess(User userBean) {
        Notification notification =
                Notification.show("Data saved, welcome " + userBean.getUsername());
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        Button loginButton = new Button("Go to Login", event -> {
            UI.getCurrent().navigate("login");
        });
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        notification.add(loginButton);
        notification.setDuration(5000);
    }

}
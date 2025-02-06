package webapp.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import webapp.RegistrationForm;
import webapp.RegistrationFormBinder;
import webapp.services.UserService;

@AnonymousAllowed
@Route("registration")
public class RegistrationView extends VerticalLayout {

    private final RegistrationForm registrationForm;

    public RegistrationView(UserService userService) {

        this.registrationForm = new RegistrationForm();
        setHorizontalComponentAlignment(Alignment.CENTER, registrationForm);
        add(registrationForm);

        RegistrationFormBinder registrationFormBinder = new RegistrationFormBinder(registrationForm, userService);
        registrationFormBinder.addBindingAndValidation(this::showSuccessView);
    }

    private void showSuccessView() {
        remove(registrationForm);

        Span successText = new Span("Successful registration!");
        Button loginButton = new Button("Go back to Login Screen", event -> {
            UI.getCurrent().navigate("login");
        });
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        VerticalLayout successLayout = new VerticalLayout(successText, loginButton);
        successLayout.setSpacing(true);
        successLayout.setAlignItems(Alignment.CENTER);

        add(successLayout);
    }
}

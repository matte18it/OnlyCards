package unical.enterpriceapplication.onlycards.application.core.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.User;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Orders;
import unical.enterpriceapplication.onlycards.application.data.service.UserService;
import unical.enterpriceapplication.onlycards.application.dto.ProductCartDTO;
import unical.enterpriceapplication.onlycards.application.dto.RequestProductDto;
import unical.enterpriceapplication.onlycards.application.dto.UserDTO;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;
import unical.enterpriceapplication.onlycards.application.utility.email.EmailProperties;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderService {
    private final UserService userService;
    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;
    private final VelocityEngine velocityEngine;

    public void sendRequestEmail(RequestProductDto requestProductDto) {
        try {
            String username = userService.findById(requestProductDto.getId()).get().getUsername();
            User admin = userService.getAllUsersAdmin().get((int) (Math.random() * userService.getAllUsersAdmin().size()));
            log.debug("Sending email to Admin: {}", admin.getEmail());
            String email = admin.getEmail();
            String usernameAdmin = admin.getUsername();

            // Prepara i dati per il template
            Map<String, Object> model = new HashMap<>();
            model.put("usernameAdmin", usernameAdmin);
            model.put("username", username);
            model.put("name", requestProductDto.getName());
            model.put("game", requestProductDto.getGame());
            model.put("message", requestProductDto.getMessage());
            model.put("timestamp", LocalDate.now());

            // Usa VelocityContext per i dati
            VelocityContext context = new VelocityContext(model);

            // Genera il contenuto dell'email usando il template Velocity
            StringWriter stringWriter = new StringWriter();
            velocityEngine.mergeTemplate("emailTemplate/requestProductTemplate.vm", "UTF-8", context, stringWriter);

            // Ottieni il contenuto generato
            String emailContent = stringWriter.toString();

            MimeMessage emailSender = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(emailSender, true);
            helper.setFrom(emailProperties.getEmail());
            helper.setTo(email);
            helper.setSubject("Richiesta inserimento carta");
            helper.setText(emailContent, true);
            helper.addInline("logo", new ClassPathResource("static/img/logo.png"));

            // Aggiunge l'immagine se presente
            if (requestProductDto.getImage() != null && !requestProductDto.getImage().isEmpty()) {
                ByteArrayResource imageResource = new ByteArrayResource(requestProductDto.getImage().getBytes());
                helper.addInline("cardImage", imageResource, Objects.requireNonNull(requestProductDto.getImage().getContentType()));
            }

            mailSender.send(emailSender);
        } catch (Exception e) {
            throw new RuntimeException("Errore nell'invio dell'email", e);
        }
    }
    public void sendHelpRequest(String object, String userId, String description) {
        try {
            String username;
            // Controlla se userId è "Anonimo"
            if ("Anonimo".equalsIgnoreCase(userId)) {
                username = "Anonimo";
            } else {
                // Altrimenti, cerca l'utente tramite l'ID
                UserDTO user = userService.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
                username = user.getUsername();
            }

            // Seleziona un admin casualmente
            User admin = userService.getAllUsersAdmin().get((int) (Math.random() * userService.getAllUsersAdmin().size()));
            log.debug("Sending email to Admin: {}", admin.getEmail());
            String email = admin.getEmail();
            String usernameAdmin = admin.getUsername();

            // Prepara i dati per il template
            Map<String, Object> model = new HashMap<>();
            model.put("usernameAdmin", usernameAdmin);
            model.put("username", username);
            model.put("object", object);
            model.put("description", description);
            model.put("timestamp", LocalDate.now());

            // Usa VelocityContext per i dati
            VelocityContext context = new VelocityContext(model);

            // Genera il contenuto dell'email usando il template Velocity
            StringWriter stringWriter = new StringWriter();
            velocityEngine.mergeTemplate("emailTemplate/helpRequestTemplate.vm", "UTF-8", context, stringWriter);

            // Ottieni il contenuto generato
            String emailContent = stringWriter.toString();

            // Crea e configura l'email
            MimeMessage emailSender = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(emailSender, true);
            helper.setFrom(emailProperties.getEmail());
            helper.setTo(email);
            helper.setSubject("Richiesta di Supporto");
            helper.setText(emailContent, true);
            helper.addInline("logo", new ClassPathResource("static/img/logo.png"));

            // Invia l'email
            mailSender.send(emailSender);
        } catch (Exception e) {
            throw new RuntimeException("Errore nell'invio dell'email di supporto", e);
        }
    }
    public void sendOrderConfirmation(String userId, List<ProductCartDTO> products) {
        try {
            UserDTO user = userService.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(userId, "User not found"));
            String email = user.getEmail();
            String username = user.getUsername();

            Map<String, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("products", products);
            model.put("timestamp", LocalDate.now());

            VelocityContext context = new VelocityContext(model);

            StringWriter stringWriter = new StringWriter();
            velocityEngine.mergeTemplate("emailTemplate/orderConfirmationTemplate.vm", "UTF-8", context, stringWriter);

            String emailContent = stringWriter.toString();

            MimeMessage emailSender = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(emailSender, true);
            helper.setFrom(emailProperties.getEmail());
            helper.setTo(email);
            helper.setSubject("Conferma del tuo Ordine");
            helper.setText(emailContent, true);
            helper.addInline("logo", new ClassPathResource("static/img/logo.png"));

            mailSender.send(emailSender);

        } catch (Exception e) {
            throw new RuntimeException("Errore nell'invio dell'email di conferma ordine", e);
        }
    }
    public void sendOrderStatusChange(Orders order, String status) {
        try {
            String email = order.getUser().getEmail();
            String username = order.getUser().getUsername();

            Map<String, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("order", order);
            model.put("status", status);
            model.put("data", order.getAddDate());
            model.put("timestamp", LocalDate.now());

            VelocityContext context = new VelocityContext(model);

            StringWriter stringWriter = new StringWriter();
            velocityEngine.mergeTemplate("emailTemplate/orderStatus.vm", "UTF-8", context, stringWriter);

            String emailContent = stringWriter.toString();

            MimeMessage emailSender = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(emailSender, true);
            helper.setFrom(emailProperties.getEmail());
            helper.setTo(email);
            helper.setSubject("Aggiornamento ordine");
            helper.setText(emailContent, true);
            helper.addInline("logo", new ClassPathResource("static/img/logo.png"));

            mailSender.send(emailSender);
        }
        catch (Exception e) {
            throw new RuntimeException("Errore nell'invio dell'email di conferma ordine", e);
        }
    }
}

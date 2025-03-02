package me.artemiyulyanov.uptodate.controllers.api.account;

import me.artemiyulyanov.uptodate.controllers.AuthenticatedController;
import me.artemiyulyanov.uptodate.controllers.api.auth.requests.RegisterRequest;
import me.artemiyulyanov.uptodate.mail.EmailVerificationCode;
import me.artemiyulyanov.uptodate.mail.MailService;
import me.artemiyulyanov.uptodate.models.User;
import me.artemiyulyanov.uptodate.services.UserService;
import me.artemiyulyanov.uptodate.web.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account/email")
public class AccountEmailController extends AuthenticatedController {
    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    @Autowired
    private RequestService requestService;

    @PatchMapping
    public ResponseEntity<?> editEmail(@RequestParam String email) {
        User user = getAuthorizedUser().get();

        if (!userService.getConflictedColumnsWhileEditing(user, email, user.getUsername()).isEmpty() || mailService.isCodeSent(email)) {
            return requestService.executeApiResponse(HttpStatus.CONFLICT, "The email is already taken!");
        }

        EmailVerificationCode emailVerificationCode = mailService.sendEmailChangeConfirmationCode(email, List.of(
                EmailVerificationCode.Credential
                        .builder()
                        .key("user")
                        .value(user)
                        .build()
        ));
        return requestService.executeApiResponse(HttpStatus.OK, "The code has been sent to your email address!");
    }

    @PostMapping
    public ResponseEntity<?> confirmEmail(
            @RequestParam String email,
            @RequestParam String code) {
        User user = getAuthorizedUser().get();

        if (!mailService.validateCode(email, code, EmailVerificationCode.EmailVerificationScope.CHANGING)) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "The code is invalid!");
        }

        EmailVerificationCode emailVerificationCode = mailService.getVerificationCode(email);
        User userToChangeEmail = emailVerificationCode.getCredential("user").getValue(User.class);

        if (!userToChangeEmail.getId().equals(user.getId())) {
            return requestService.executeApiResponse(HttpStatus.FORBIDDEN, "You are not allowed to confirm this email!");
        }

        mailService.enterCode(email, code, EmailVerificationCode.EmailVerificationScope.CHANGING);

        user.setEmail(email);
        userService.save(user);

        return requestService.executeEntityResponse(HttpStatus.OK, "The email has been updated successfully!", user);
    }
}
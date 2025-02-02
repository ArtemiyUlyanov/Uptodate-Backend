package me.artemiyulyanov.uptodate.controllers.api.account;

import me.artemiyulyanov.uptodate.controllers.AuthenticatedController;
import me.artemiyulyanov.uptodate.models.Article;
import me.artemiyulyanov.uptodate.models.User;
import me.artemiyulyanov.uptodate.services.UserService;
import me.artemiyulyanov.uptodate.web.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/account")
public class AccountController extends AuthenticatedController {
    @Autowired
    private UserService userService;

    @Autowired
    private RequestService requestService;

    @GetMapping("/info")
    public ResponseEntity<?> accountInfo(Model model) {
        Optional<User> wrappedUser = getAuthorizedUser();
        return requestService.executeEntityResponse(HttpStatus.OK, "The user information has been retrieved successfully!", wrappedUser.get());
    }

    @PatchMapping("/edit")
    public ResponseEntity<?> editAccount(@RequestBody Map<String, Object> updates, @RequestParam(value = "icon", required = false) MultipartFile icon, Model model) {
        Optional<User> wrappedUser = getAuthorizedUser();

//        if (!isUserAuthorized()) {
//            return requestService.executeError(HttpStatus.BAD_REQUEST, 10, "The authorized user is undefined!");
//        }

        User newUser = wrappedUser.get();
        if (icon != null) userService.getResourceManager().updateResources(newUser, List.of(icon));

        updates.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(Article.class, key);
            if (field != null) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, newUser, value);
            }
        });

        userService.save(newUser);
        return requestService.executeApiResponse(HttpStatus.OK, "The changes have been applied successfully!");
    }
}
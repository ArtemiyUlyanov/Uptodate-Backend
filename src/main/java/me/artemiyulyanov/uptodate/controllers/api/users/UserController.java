package me.artemiyulyanov.uptodate.controllers.api.users;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import me.artemiyulyanov.uptodate.controllers.AuthenticatedController;
import me.artemiyulyanov.uptodate.models.Article;
import me.artemiyulyanov.uptodate.models.User;
import me.artemiyulyanov.uptodate.services.UserService;
import me.artemiyulyanov.uptodate.web.RequestService;
import me.artemiyulyanov.uptodate.web.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/api/users")
public class UserController extends AuthenticatedController {
    @Autowired
    private UserService userService;

    @Autowired
    private RequestService requestService;

    @GetMapping("/get")
    public ResponseEntity<ServerResponse> getUser(@RequestParam Long id, Model model) {
        Optional<User> wrappedUser = userService.findById(id);

        if (!wrappedUser.isPresent()) {
            return requestService.executeError(HttpStatus.BAD_REQUEST, 10, "User is undefined!");
        }

        return requestService.executeEntity(HttpStatus.OK, 200, "The request has been proceeded successfully!", wrappedUser.get());
    }

    @PatchMapping("/edit")
    public ResponseEntity<ServerResponse> editUser(@RequestBody Map<String, Object> updates, @RequestParam(value = "icon", required = false) MultipartFile icon, Model model) {
        Optional<User> wrappedUser = getAuthorizedUser();

        if (!isUserAuthorized()) {
            return requestService.executeError(HttpStatus.BAD_REQUEST, 10, "The authorized user is undefined!");
        }

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
        return requestService.executeMessage(HttpStatus.OK, 200, "The changes have been applied successfully!");
    }
}
package me.artemiyulyanov.uptodate.controllers.api.account;

import me.artemiyulyanov.uptodate.controllers.AuthenticatedController;
import me.artemiyulyanov.uptodate.models.Article;
import me.artemiyulyanov.uptodate.models.User;
import me.artemiyulyanov.uptodate.models.UserSettings;
import me.artemiyulyanov.uptodate.repositories.ArticleLikeRepository;
import me.artemiyulyanov.uptodate.repositories.UserSettingsRepository;
import me.artemiyulyanov.uptodate.services.ArticleLikeService;
import me.artemiyulyanov.uptodate.services.ArticleViewService;
import me.artemiyulyanov.uptodate.services.UserService;
import me.artemiyulyanov.uptodate.web.RequestService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/account")
public class AccountController extends AuthenticatedController {
    @Autowired
    private UserService userService;

    @Autowired
    private ArticleViewService articleViewService;

    @Autowired
    private ArticleLikeService articleLikeService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @GetMapping
    public ResponseEntity<?> getInfo() {
        Optional<User> wrappedUser = getAuthorizedUser();
        return requestService.executeEntityResponse(HttpStatus.OK, "The user information has been retrieved successfully!", wrappedUser.get());
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        Optional<User> wrappedUser = getAuthorizedUser();
        return requestService.executeEntityResponse(HttpStatus.OK, "The statistics has been retrieved successfully!", wrappedUser.get().getStatistics());
    }

    @PutMapping
    public ResponseEntity<?> editAccount(
            @RequestParam String username,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam(value = "icon", required = false) MultipartFile icon) {
        User user = getAuthorizedUser().get();

        if (!username.equals(user.getUsername()) && userService.existsByUsername(username)) {
            return requestService.executeApiResponse(HttpStatus.CONFLICT, "The username is already taken!");
        }

        User updatedUser = userService.edit(user.getId(), username, firstName, lastName, icon);
        return requestService.executeEntityResponse(HttpStatus.OK, "The changes have been applied successfully!", updatedUser);
    }

    @PutMapping("/icon")
    public ResponseEntity<?> uploadIcon(@RequestParam(value = "icon") MultipartFile icon) {
        User user = getAuthorizedUser().get();

        String iconObjectKey = userService.getResourceManager().getResourceFolder(user) + File.separator + icon.getOriginalFilename();
        userService.getResourceManager().updateResources(user, List.of(icon));

        user.setIcon(iconObjectKey);
        userService.save(user);

        return requestService.executeApiResponse(HttpStatus.OK, "The icon has been updated successfully!");
    }

    @GetMapping("/settings")
    public ResponseEntity<?> getSettings() {
        User user = getAuthorizedUser().get();
        return requestService.executeEntityResponse(HttpStatus.OK, "The settings have been retrieved successfully!", user.getSettings());
    }

    @PutMapping("/settings")
    public ResponseEntity<?> editSettings(@RequestBody UserSettings settings) {
        User user = getAuthorizedUser().get();

        if (settings.getId().equals(user.getId())) {
            return requestService.executeApiResponse(HttpStatus.FORBIDDEN, "You are unable of editing these settings!");
        }

        userSettingsRepository.save(settings);
        return requestService.executeApiResponse(HttpStatus.OK, "The changes have been applied successfully!");
    }
}
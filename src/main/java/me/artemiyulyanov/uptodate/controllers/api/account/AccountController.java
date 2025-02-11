package me.artemiyulyanov.uptodate.controllers.api.account;

import me.artemiyulyanov.uptodate.controllers.AuthenticatedController;
import me.artemiyulyanov.uptodate.controllers.api.account.responses.StatisticsResponse;
import me.artemiyulyanov.uptodate.models.Article;
import me.artemiyulyanov.uptodate.models.User;
import me.artemiyulyanov.uptodate.repositories.ArticleLikeRepository;
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

    @GetMapping("/info")
    public ResponseEntity<?> accountInfo() {
        Optional<User> wrappedUser = getAuthorizedUser();
        return requestService.executeEntityResponse(HttpStatus.OK, "The user information has been retrieved successfully!", wrappedUser.get());
    }

    @GetMapping("/info/statistics")
    public ResponseEntity<?> statistics() {
        Optional<User> wrappedUser = getAuthorizedUser();

        return requestService.executeCustomResponse(
                StatisticsResponse.builder()
                        .status(HttpStatus.OK.value())
                        .lastViews(articleViewService.findLastViewsOfAuthor(wrappedUser.get(), LocalDateTime.now().minusDays(1)))
                        .lastLikes(articleLikeService.findLastLikesOfAuthor(wrappedUser.get(), LocalDateTime.now().minusDays(1)))
                        .message("The statistics has been retrieved successfully!")
                        .build()
        );
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editAccount(
            @RequestParam String username,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam(value = "icon", required = false) MultipartFile icon) {
        User user = getAuthorizedUser().get();

        if (!username.equals(user.getUsername()) && userService.existsByUsername(username)) {
            return requestService.executeApiResponse(HttpStatus.CONFLICT, "The username is already taken!");
        }

        userService.editUser(user.getId(), username, firstName, lastName, icon);
        return requestService.executeApiResponse(HttpStatus.OK, "The changes have been applied successfully!");
    }

    @PostMapping("/icon/upload")
    public ResponseEntity<?> uploadIcon(@RequestParam(value = "icon") MultipartFile icon) {
        User user = getAuthorizedUser().get();
        System.out.println("test");

        String iconObjectKey = userService.getResourceManager().getResourceFolder(user) + File.separator + icon.getOriginalFilename();
        userService.getResourceManager().updateResources(user, List.of(icon));

        user.setIcon(iconObjectKey);
        userService.save(user);

        return requestService.executeApiResponse(HttpStatus.OK, "The icon has been updated successfully!");
    }
}
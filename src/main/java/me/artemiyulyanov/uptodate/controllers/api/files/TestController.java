package me.artemiyulyanov.uptodate.controllers.api.files;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    @GetMapping("/api/files/test")
    public String test(Model model) {
        return "upload_form";
    }
}

package gr.hua.dit.StudyRooms.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')") // κανένας δεν έχει ADMIN
    public String adminOnly() {
        return "This should never be visible";
    }
}


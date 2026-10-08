package com.platform.recs.controller;

import com.platform.recs.enumtype.RoleName;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class HealthRolesController {

    @GetMapping("/roles")
    public List<String> roles() {
        return Arrays.stream(RoleName.values()).map(Enum::name).toList();
    }
}

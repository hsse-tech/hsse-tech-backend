package com.mipt.hsse.hssetechbackend.controllers.users;

import com.mipt.hsse.hssetechbackend.controllers.users.responses.GetHumanUserResponse;
import com.mipt.hsse.hssetechbackend.oauth.services.OAuth2UserHelper;
import com.mipt.hsse.hssetechbackend.users.administation.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('MIPT_USER')")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("me")
    public ResponseEntity<GetHumanUserResponse> getMe(@AuthenticationPrincipal OAuth2User principal) {
        return ResponseEntity.ok(
                new GetHumanUserResponse(
                        userService.getUserById(OAuth2UserHelper.getUserId(principal))));
    }
}

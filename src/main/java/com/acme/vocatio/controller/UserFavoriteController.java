package com.acme.vocatio.controller;

import com.acme.vocatio.dto.career.CareerListDto;
import com.acme.vocatio.dto.favorite.FavoriteCareerRequest;
import com.acme.vocatio.security.UserPrincipal;
import com.acme.vocatio.service.FavoriteCareerService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/favorites")
@RequiredArgsConstructor
public class UserFavoriteController {

    private final FavoriteCareerService favoriteCareerService;

    @GetMapping
    public ResponseEntity<List<CareerListDto>> listFavorites(@AuthenticationPrincipal UserPrincipal principal) {
        List<CareerListDto> favorites = favoriteCareerService.listFavorites(principal.getUserId());
        return ResponseEntity.ok(favorites);
    }

    @PostMapping
    public ResponseEntity<CareerListDto> addFavorite(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FavoriteCareerRequest request) {
        CareerListDto saved = favoriteCareerService.addFavorite(principal.getUserId(), request.careerId());
        return ResponseEntity
                .created(URI.create("/api/users/me/favorites/" + saved.id()))
                .body(saved);
    }

    @DeleteMapping("/{careerId}")
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long careerId) {
        favoriteCareerService.removeFavorite(principal.getUserId(), careerId);
        return ResponseEntity.noContent().build();
    }
}

package com.acme.vocatio.dto.favorite;

import jakarta.validation.constraints.NotNull;

public record FavoriteCareerRequest(@NotNull Long careerId) {}

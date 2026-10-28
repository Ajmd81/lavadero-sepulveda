package com.lavaderosepulveda.app.security;

import java.security.Principal;

/**
 * Principal que se guarda en el SecurityContext tras validar el JWT.
 * Los controladores lo reciben con @AuthenticationPrincipal y así NUNCA tienen que
 * fiarse de un "username" que venga en el body o en la URL.
 */
public record AuthenticatedUser(Long id, String username, String tokenId) implements Principal {

    @Override
    public String getName() {
        return username;
    }
}

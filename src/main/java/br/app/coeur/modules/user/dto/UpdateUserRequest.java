package br.app.coeur.modules.user.dto;

public record UpdateUserRequest(String email, String name, String roles) {
}

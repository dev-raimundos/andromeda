package br.app.coeur.user.dto;

public record UpdateUserRequest(String email, String name, String roles) {
}

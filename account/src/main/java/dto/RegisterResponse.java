package dto;

import account.model.User;

public record RegisterResponse(String msg, User user) {

}

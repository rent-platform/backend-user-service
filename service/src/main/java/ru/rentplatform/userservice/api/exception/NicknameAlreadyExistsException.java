package ru.rentplatform.userservice.api.exception;

public class NicknameAlreadyExistsException extends RuntimeException {

    public NicknameAlreadyExistsException(String message) {
        super(message);
    }
}

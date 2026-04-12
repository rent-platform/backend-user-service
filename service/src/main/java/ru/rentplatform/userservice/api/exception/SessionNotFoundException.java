package ru.rentplatform.userservice.api.exception;

public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(String message){
        super(message);
    }
}

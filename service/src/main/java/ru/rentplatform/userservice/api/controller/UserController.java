package ru.rentplatform.userservice.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static ru.rentplatform.userservice.api.ApiPaths.USERS;

@RestController
@RequestMapping(USERS)
@RequiredArgsConstructor
@Validated
public class UserController {

}

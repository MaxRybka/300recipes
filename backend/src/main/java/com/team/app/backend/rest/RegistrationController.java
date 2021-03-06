package com.team.app.backend.rest;


import com.team.app.backend.dto.UserRegistrationDto;
import com.team.app.backend.exception.UserAlreadyExistsException;
import com.team.app.backend.persistance.dao.UserDao;
import com.team.app.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.*;

@RestController
@RequestMapping(path = "api")
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    UserDao userDao;

    @Autowired
    MessageSource messageSource;

    @PostMapping("/sign-up")
    public ResponseEntity registerUserAccount(
            @RequestBody @Valid UserRegistrationDto userDto) {

        try {
            userService.registerNewUserAccount(userDto);

        }
        catch (UserAlreadyExistsException e) {
            String[] params = new String[]{userDto.getUsername()};
            return  ResponseEntity.badRequest().body(messageSource.getMessage("user.exist", params, LocaleContextHolder.getLocale()));
       }
        return ResponseEntity.ok().body(new HashMap<String,String>());
    }

}

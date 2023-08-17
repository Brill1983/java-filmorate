package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;

import static org.junit.jupiter.api.Assertions.*;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class UserTest { //TODO - доделать

//    private UserController userController;
//    @BeforeEach
//    void setUp() {
//        userController = new UserController();
//    }

    private static Validator validator;
    static {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.usingContext().getValidator();
    }

    @Test
    void validateName() { // TODO - проверить и исправить
        User user = new User();
        user.setLogin("Log in");

        Set<ConstraintViolation<User>> voilations = validator.validate(user);

        for (ConstraintViolation<User> voilation : voilations) {
            System.out.println("getMessage");
            System.out.println(voilation.getMessage());
            System.out.println("getConstraintDescriptor");
            System.out.println(voilation.getConstraintDescriptor());
            System.out.println("getInvalidValue");
            System.out.println(voilation.getInvalidValue());
            System.out.println("getRootBeanClass");
            System.out.println(voilation.getRootBeanClass());
            System.out.println("getRootBean");
            System.out.println(voilation.getRootBean());
            System.out.println("getExecutableParameters");
            System.out.println(voilation.getExecutableParameters());
        }
        List<ConstraintViolation<User>> viol = new ArrayList<>(voilations);

        assertEquals("В логине не должно быть пробелов", viol.get(0).getMessage(), "Login is empty");
    }

}

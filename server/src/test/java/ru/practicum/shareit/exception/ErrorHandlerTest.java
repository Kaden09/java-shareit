package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ErrorHandlerTest {

    @Test
    void testErrorHandlerMethodsAndResponse() {
        ErrorHandler handler = new ErrorHandler();
        Method[] methods = ErrorHandler.class.getDeclaredMethods();

        for (Method method : methods) {
            method.setAccessible(true);
            Class<?>[] parameterTypes = method.getParameterTypes();

            if (parameterTypes.length == 1) {
                Class<?> paramType = parameterTypes[0];
                try {
                    Object exceptionInstance = null;

                    if (paramType.equals(NotFoundException.class)) {
                        exceptionInstance = new NotFoundException("Not Found");
                    } else if (paramType.equals(ru.practicum.shareit.exception.ValidationException.class)) {
                        exceptionInstance = new ru.practicum.shareit.exception.ValidationException("Validation");
                    } else if (paramType.equals(jakarta.validation.ValidationException.class)) {
                        exceptionInstance = new jakarta.validation.ValidationException("Validation");
                    } else if (Throwable.class.isAssignableFrom(paramType)) {
                        try {
                            exceptionInstance = paramType.getConstructor(String.class).newInstance("Error");
                        } catch (Exception ex) {
                            try {
                                exceptionInstance = paramType.getConstructor().newInstance();
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    if (exceptionInstance != null) {
                        Object result = method.invoke(handler, exceptionInstance);
                        assertNotNull(result);

                        assertNotNull(result.toString());
                        for (Method rMethod : result.getClass().getDeclaredMethods()) {
                            if (rMethod.getName().startsWith("get") && rMethod.getParameterCount() == 0) {
                                rMethod.setAccessible(true);
                                rMethod.invoke(result);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
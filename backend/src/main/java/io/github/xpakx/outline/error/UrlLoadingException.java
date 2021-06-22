package io.github.xpakx.outline.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UrlLoadingException extends RuntimeException {
    public UrlLoadingException(String message) {
        super(message);
    }
}

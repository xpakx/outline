package io.github.xpakx.outline.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class UrlReaderService {
    public String read(URL url) throws IOException {
        try(InputStream in = url.openStream()) {
            byte[] bytes = in.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}

package com.urlShortner.controller;

import com.urlShortner.service.URLShortenerService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

@RestController
public class URLShortenerController {

    @Value("${url.shortener.prefix}")
    private String PREFIX;

    @Autowired
    private URLShortenerService urlShortenerService;

    @PostMapping("/shorten")
    public String shortenURL(@RequestBody String destinationUrl) throws SQLException, NoSuchAlgorithmException, NoSuchAlgorithmException {
        return urlShortenerService.shortenURL(destinationUrl);
    }

    @PostMapping("/update")
    public boolean updateShortURL(@RequestBody Map<String, String> request) throws SQLException {
        return urlShortenerService.updateShortURL(request.get("short_url"), request.get("destination_url"));
    }

    @GetMapping("/get")
    public String getLongURL(@RequestParam String shortUrl) throws SQLException {
        return urlShortenerService.getLongURL(shortUrl);
    }

    @PostMapping("/update-expiry")
    public boolean updateExpiry(@RequestBody Map<String, String> request) throws SQLException {
        return urlShortenerService.updateExpiry(request.get("short_url"), Integer.parseInt(request.get("days_to_add")));
    }

    @GetMapping("/{shortenString}")
    public void redirectToFullUrl(HttpServletResponse response, @PathVariable String shortenString) {
        try {
            String fullUrl = urlShortenerService.getLongURL(PREFIX + shortenString);
            if (fullUrl != null) {
                response.sendRedirect(fullUrl);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Url not found");
            }
        } catch (SQLException | IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not redirect to the full url", e);
        }
    }
}

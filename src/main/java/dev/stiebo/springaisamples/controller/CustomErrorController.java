package dev.stiebo.springaisamples.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        // Check if the request expects an HTML response
        String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
        if (acceptHeader != null && acceptHeader.contains(MediaType.TEXT_HTML_VALUE)) {
            // Forward to the static HTML error page (located in src/main/resources/static)
            return "forward:/error.html";
        }
        // Otherwise, let Spring Boot handle the error response as JSON
        return null;
    }
}

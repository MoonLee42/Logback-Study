package com.example.LogbackStudy.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

@RestController
@Slf4j
public class LogController {

    @GetMapping("/trace")
    public String trace(@NotBlank @RequestParam String message) {
        log.trace(message);
        return "success";
    }

    @GetMapping("/debug")
    public String debug(@NotBlank @RequestParam String message) {
        log.debug(message);
        return "success";
    }

    @GetMapping("/info")
    public String info(@NotBlank @RequestParam String message) {
        log.info(message);
        return "success";
    }

    @GetMapping("/warn")
    public String warn(@NotBlank @RequestParam String message) {
        log.warn(message);
        return "success";
    }

    @GetMapping("/error")
    public String error(@NotBlank @RequestParam String message) {
        log.error(message);
        return "success";
    }

}

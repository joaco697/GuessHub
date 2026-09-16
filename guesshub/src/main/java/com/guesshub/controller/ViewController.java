package com.guesshub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/game")
    public String game() {
        return "game";
    }

    @GetMapping("/ranking")
    public String ranking() {
        return "ranking";
    }

    @GetMapping("/perfil")
    public String perfil() {
        return "perfil";
    }
}
package com.code.mikhaylov.spendstories.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PagesController {

    @GetMapping("/")
    public String indexPage() {
        return "index";
    }

    @GetMapping("/link")
    public String linkPage() {
        return "link";
    }

    @GetMapping("/personal")
    public String personalPage() {
        return "personal";
    }

}

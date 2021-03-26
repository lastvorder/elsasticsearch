package com.last.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author chenzihao
 * @date 2021/03/26
 */
@Controller
public class IndexController {
    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }
}

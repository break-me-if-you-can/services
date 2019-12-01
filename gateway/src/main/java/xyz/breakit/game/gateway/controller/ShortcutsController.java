package xyz.breakit.game.gateway.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;

@Controller
public class ShortcutsController {

    @RequestMapping("/admin/manual")
    Rendering index() {
        return Rendering.view("index").build();
    }
}

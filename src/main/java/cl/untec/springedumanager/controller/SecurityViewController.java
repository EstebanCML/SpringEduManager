package cl.untec.springedumanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SecurityViewController {

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "errores/acceso-denegado";
    }
}


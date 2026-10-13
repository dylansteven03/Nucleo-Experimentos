package co.edu.uceva.numerico.calculo;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/calculo")
public class PingController {

    private final NativoService nativo;

    public PingController(NativoService nativo) {
        this.nativo = nativo;
    }

    @GetMapping("/ping")
    public Map<String, Object> ping(@RequestParam double a, @RequestParam double b) {
        return Map.of("a", a, "b", b, "suma", nativo.suma(a, b));
    }
}
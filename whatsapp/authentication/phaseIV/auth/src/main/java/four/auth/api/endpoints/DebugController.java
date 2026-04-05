package four.auth.api.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    @Autowired
    private Environment env;

    @GetMapping("/whoami")
    public String whoami() {
        return "Handled by instance: " + env.getProperty("local.server.port");
    }
}
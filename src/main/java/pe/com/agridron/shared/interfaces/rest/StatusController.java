package pe.com.agridron.shared.interfaces.rest;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.agridron.shared.interfaces.rest.resources.MessageResource;

/**
 * Simple status endpoint used as a smoke test for the REST stack and i18n support.
 */
@RestController
@RequestMapping(value = "/api/v1/status", produces = MediaType.APPLICATION_JSON_VALUE)
public class StatusController {

    private final MessageSource messageSource;

    public StatusController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @GetMapping
    public MessageResource getStatus() {
        var message = messageSource.getMessage(
                "status.ok",
                null,
                "AgriDron API is running",
                LocaleContextHolder.getLocale());
        return new MessageResource(message);
    }
}

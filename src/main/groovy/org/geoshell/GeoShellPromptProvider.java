package org.geoshell;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultPromptProvider;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GeoShellPromptProvider extends DefaultPromptProvider {

    @Override
    public String getPrompt() {
        return "geo-shell>";
    }

    @Override
    public String getProviderName() {
        return "geo shell prompt provider";
    }
}

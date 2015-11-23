package org.geoshell;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultHistoryFileNameProvider;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GeoShellHistoryFileNameProvider extends DefaultHistoryFileNameProvider {

    @Override
    public String getHistoryFileName() {
        return "geo-shell.log";
    }

    @Override
    public String getProviderName() {
        return "geo shell history file name provider";
    }
}

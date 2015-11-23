package org.geoshell;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.shell.support.util.OsUtils;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GeoShellBannerProvider extends DefaultBannerProvider implements CommandMarker {

    @Override
    public String getBanner() {
        StringBuilder builder = new StringBuilder();
        builder.append("   ______              _____ __         ____" + OsUtils.LINE_SEPARATOR);
        builder.append("  / ____/__  ____     / ___// /_  ___  / / /" + OsUtils.LINE_SEPARATOR);
        builder.append(" / / __/ _ \\/ __ \\    \\__ \\/ __ \\/ _ \\/ / / " + OsUtils.LINE_SEPARATOR);
        builder.append("/ /_/ /  __/ /_/ /   ___/ / / / /  __/ / /  " + OsUtils.LINE_SEPARATOR);
        builder.append("\\____/\\___/\\____/   /____/_/ /_/\\___/_/_/   " + OsUtils.LINE_SEPARATOR);
        return builder.toString();
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome to the Geo Shell!";
    }

    @Override
    public String getProviderName() {
        return "geo shell";
    }
}

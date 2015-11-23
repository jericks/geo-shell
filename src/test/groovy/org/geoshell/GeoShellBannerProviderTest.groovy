package org.geoshell

import org.junit.Test
import static org.junit.Assert.assertEquals
import org.springframework.shell.support.util.OsUtils

class GeoShellBannerProviderTest {

    @Test
    public void getBanner() {
        GeoShellBannerProvider provider = new GeoShellBannerProvider()
        StringBuilder builder = new StringBuilder()
        builder.append("   ______              _____ __         ____" + OsUtils.LINE_SEPARATOR)
        builder.append("  / ____/__  ____     / ___// /_  ___  / / /" + OsUtils.LINE_SEPARATOR)
        builder.append(" / / __/ _ \\/ __ \\    \\__ \\/ __ \\/ _ \\/ / / " + OsUtils.LINE_SEPARATOR)
        builder.append("/ /_/ /  __/ /_/ /   ___/ / / / /  __/ / /  " + OsUtils.LINE_SEPARATOR)
        builder.append("\\____/\\___/\\____/   /____/_/ /_/\\___/_/_/   " + OsUtils.LINE_SEPARATOR)
        assertEquals builder.toString(), provider.getBanner()
    }

    @Test
    public void getVersion() {
        GeoShellBannerProvider provider = new GeoShellBannerProvider()
        assertEquals "0.0.1", provider.getVersion()
    }

    @Test
    public void getWelcomeMessage() {
        GeoShellBannerProvider provider = new GeoShellBannerProvider()
        assertEquals "Welcome to the Geo Shell!", provider.getWelcomeMessage()
    }

    @Test
    public void getProviderName() {
        GeoShellBannerProvider provider = new GeoShellBannerProvider()
        assertEquals "geo shell", provider.getProviderName()
    }

}

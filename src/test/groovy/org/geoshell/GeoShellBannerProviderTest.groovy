package org.geoshell

import org.junit.Test
import static org.junit.Assert.assertEquals
import org.springframework.shell.support.util.OsUtils

import static org.junit.Assert.assertNotEquals

class GeoShellBannerProviderTest {

    @Test
    void getBanner() {
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
    void getVersion() {
        GeoShellBannerProvider provider = new GeoShellBannerProvider()
        assertNotEquals "", provider.getVersion()
        assertEquals getExpectedVersion(), provider.getVersion()
    }

    String getExpectedVersion() {
        String version = ""
        GeoShellBannerProvider.class.getClassLoader().getResource("application.properties").withInputStream { InputStream inputStream ->
            Properties properties = new Properties()
            properties.load(inputStream)
            version = properties.getProperty("version")
        }
        version
    }

    @Test
    void getWelcomeMessage() {
        GeoShellBannerProvider provider = new GeoShellBannerProvider()
        assertEquals "Welcome to the Geo Shell!", provider.getWelcomeMessage()
    }

    @Test
    void getProviderName() {
        GeoShellBannerProvider provider = new GeoShellBannerProvider()
        assertEquals "geo shell", provider.getProviderName()
    }

}

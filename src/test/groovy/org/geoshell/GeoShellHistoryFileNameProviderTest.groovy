package org.geoshell

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.assertEquals

class GeoShellHistoryFileNameProviderTest {

    @Test
    public void getHistoryFileName() {
        GeoShellHistoryFileNameProvider provider = new GeoShellHistoryFileNameProvider()
        assertEquals "geo-shell.log", provider.getHistoryFileName()
    }

    @Test
    public void getProviderName() {
        GeoShellHistoryFileNameProvider provider = new GeoShellHistoryFileNameProvider()
        assertEquals "geo shell history file name provider", provider.getProviderName()
    }
}

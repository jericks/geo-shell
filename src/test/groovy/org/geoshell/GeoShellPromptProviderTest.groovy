package org.geoshell

import org.junit.Test
import static org.junit.Assert.assertEquals

class GeoShellPromptProviderTest {

    @Test
    public void getPrompt() {
        GeoShellPromptProvider provider = new GeoShellPromptProvider()
        assertEquals provider.getPrompt(), "geo-shell>"
    }

    @Test
    public void getProviderName() {
        GeoShellPromptProvider provider = new GeoShellPromptProvider()
        assertEquals provider.getProviderName(), "geo shell prompt provider"
    }

}

package org.geoshell

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.assertEquals

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

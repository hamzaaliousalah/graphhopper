package com.graphhopper.routing;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.PMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests de la classe DefaultWeightingFactory avec des mocks mockito.
 */
public class DefaultWeightingFactoryTest {

    /**
     * Vérifie qu'un profil avec weighting = "shortest" provoque une IllegalArgumentException.
     */
    @Test
    public void testShortestWeightingThrows() {
        BaseGraph graph = mock(BaseGraph.class);
        EncodingManager encoding = mock(EncodingManager.class);
        Profile profile = mock(Profile.class);

        when(profile.getHints()).thenReturn(new PMap());
        when(profile.hasTurnCosts()).thenReturn(false);
        when(profile.getWeighting()).thenReturn("shortest");
        when(profile.getName()).thenReturn("car");

        DefaultWeightingFactory factory = new DefaultWeightingFactory(graph, encoding);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> factory.createWeighting(profile, new PMap(), false)
        );

        assertTrue(ex.getMessage().contains("weighting=shortest"));
    }

    /**
     * Vérifie qu'un profil avec turn-costs activés mais sans encodage
     * de restrictions de virage lève une IllegalArgumentException.
     */
    @Test
    public void testMissingTurnRestrictionThrows() {
        BaseGraph graph = mock(BaseGraph.class);
        EncodingManager encoding = mock(EncodingManager.class);
        Profile profile = mock(Profile.class);

        when(profile.getHints()).thenReturn(new PMap());
        when(profile.hasTurnCosts()).thenReturn(true);
        when(profile.getName()).thenReturn("car");
        when(profile.getWeighting()).thenReturn("custom");

        when(profile.getCustomModel()).thenReturn(new com.graphhopper.util.CustomModel());
        when(profile.getTurnCostsConfig()).thenReturn(new com.graphhopper.util.TurnCostsConfig());

        when(encoding.getTurnBooleanEncodedValue(anyString())).thenReturn(null);

        DefaultWeightingFactory factory = new DefaultWeightingFactory(graph, encoding);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> factory.createWeighting(profile, new PMap(), false)
        );

        assertTrue(ex.getMessage().contains("Cannot find turn restriction encoded value for car"));
    }
}

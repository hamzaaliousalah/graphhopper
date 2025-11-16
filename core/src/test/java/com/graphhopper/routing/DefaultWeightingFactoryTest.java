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
 * Tests de DefaultWeightingFactory utilisant des mocks Mockito.
 * Tâche 3 : au moins 2 classes simulées avec des mocks.
 */
public class DefaultWeightingFactoryTest {

    /**
     * Cas 1 : on simule un profil avec weighting="shortest".
     * On vérifie que la factory lève bien IllegalArgumentException,
     * en utilisant des dépendances simulées (BaseGraph, EncodingManager, Profile).
     */
    @Test
    public void testCreateWeighting_InvalidShortestWeighting_UsingMocks() {
        // Dépendances simulées
        BaseGraph mockGraph = mock(BaseGraph.class);
        EncodingManager mockEncoding = mock(EncodingManager.class);
        Profile mockProfile = mock(Profile.class);

        // Profil simulé : aucun turn-cost, weighting=shortest
        when(mockProfile.getHints()).thenReturn(new PMap());
        when(mockProfile.hasTurnCosts()).thenReturn(false); // -> NO_TURN_COST_PROVIDER
        when(mockProfile.getWeighting()).thenReturn("shortest");
        when(mockProfile.getName()).thenReturn("car");

        // Hints de la requête (réel, ce n’est pas un problème)
        PMap requestHints = new PMap();

        DefaultWeightingFactory factory = new DefaultWeightingFactory(mockGraph, mockEncoding);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> factory.createWeighting(mockProfile, requestHints, false)
        );

        // On vérifie qu'on tombe bien dans le message "shortest interdit"
        assertTrue(ex.getMessage().contains("weighting=shortest"));
    }

    /**
     * Cas 2 : on simule un profil avec turn-costs activés, mais sans
     * encodage de restrictions de virage dans l'EncodingManager.
     *
     * On vérifie que la factory lève IllegalArgumentException :
     * "Cannot find turn restriction encoded value for car".
     */
    @Test
    public void testCreateWeighting_MissingTurnRestrictionEncodedValue_UsingMocks() {
        BaseGraph mockGraph = mock(BaseGraph.class);
        EncodingManager mockEncoding = mock(EncodingManager.class);
        Profile mockProfile = mock(Profile.class);

        // Profil simulé
        when(mockProfile.getHints()).thenReturn(new PMap());
        when(mockProfile.hasTurnCosts()).thenReturn(true);
        when(mockProfile.getName()).thenReturn("car");
        when(mockProfile.getWeighting()).thenReturn("custom");

        // CustomModel / TurnCostsConfig : objets réels mais simples
        when(mockProfile.getCustomModel()).thenReturn(new com.graphhopper.util.CustomModel());
        when(mockProfile.getTurnCostsConfig()).thenReturn(new com.graphhopper.util.TurnCostsConfig());

        // EncodingManager simulé : pas de TurnRestriction EV -> retourne null
        when(mockEncoding.getTurnBooleanEncodedValue(anyString())).thenReturn(null);

        PMap requestHints = new PMap();

        DefaultWeightingFactory factory = new DefaultWeightingFactory(mockGraph, mockEncoding);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> factory.createWeighting(mockProfile, requestHints, false)
        );

        assertTrue(ex.getMessage().contains("Cannot find turn restriction encoded value for car"));
    }
}

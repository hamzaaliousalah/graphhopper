/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.routing;

import com.carrotsearch.hppc.IntArrayList;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValueImpl;
import com.graphhopper.routing.ev.TurnCost;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.SpeedWeighting;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.TurnCostStorage;
import org.junit.jupiter.api.Test;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Karich
 * @author easbar
 */
public class DefaultBidirPathExtractorTest {
    private final DecimalEncodedValue speedEnc = new DecimalEncodedValueImpl("speed", 5, 5, true);
    private final DecimalEncodedValue turnCostEnc = TurnCost.create("car", 10);
    private final EncodingManager encodingManager = EncodingManager.start().add(speedEnc).addTurnCostEncodedValue(turnCostEnc).build();

    BaseGraph createGraph() {
        return new BaseGraph.Builder(encodingManager).withTurnCosts(true).create();
    }

    @Test
    public void testExtract() {
        Graph graph = createGraph();
        graph.edge(1, 2).setDistance(10).set(speedEnc, 60, 60);
        SPTEntry fwdEntry = new SPTEntry(0, 2, 0, new SPTEntry(1, 10));
        SPTEntry bwdEntry = new SPTEntry(2, 0);
        Path p = DefaultBidirPathExtractor.extractPath(graph, new SpeedWeighting(speedEnc), fwdEntry, bwdEntry, 0);
        assertEquals(IntArrayList.from(1, 2), p.calcNodes());
        assertEquals(10, p.getDistance(), 1e-4);
    }

    @Test
    public void testExtract2() {
        // 1->2->3
        Graph graph = createGraph();
        graph.edge(1, 2).setDistance(10).set(speedEnc, 10, 0);
        graph.edge(2, 3).setDistance(20).set(speedEnc, 10, 0);
        // add some turn costs at node 2 where fwd&bwd searches meet. these costs have to be included in the
        // weight and the time of the path
        TurnCostStorage turnCostStorage = graph.getTurnCostStorage();
        turnCostStorage.set(turnCostEnc, 0, 2, 1, 5);

        SPTEntry fwdEntry = new SPTEntry(0, 2, 0.6, new SPTEntry(1, 0));
        SPTEntry bwdEntry = new SPTEntry(1, 2, 1.2, new SPTEntry(3, 0));

        Path p = DefaultBidirPathExtractor.extractPath(graph, new SpeedWeighting(speedEnc, turnCostEnc, turnCostStorage, Double.POSITIVE_INFINITY), fwdEntry, bwdEntry, 0);
        p.setWeight(5 + 3);

        assertEquals(IntArrayList.from(1, 2, 3), p.calcNodes());
        assertEquals(30, p.getDistance(), 1e-4);
        assertEquals(8, p.getWeight(), 1e-4);
        assertEquals(8000, p.getTime(), 1.e-6);
    }
    /**
     * Petite sous-classe pour exposer les méthodes protégées onEdge/onMeetingPoint
     * sans modifier la classe de production.
     */
    static class TestExtractor extends DefaultBidirPathExtractor {

        TestExtractor(Graph graph, Weighting weighting) {
            super(graph, weighting);
        }

        void callOnEdge(int edge, int adjNode, boolean reverse, int prevOrNextEdge) {
            onEdge(edge, adjNode, reverse, prevOrNextEdge);
        }

        void callOnMeetingPoint(int inEdge, int viaNode, int outEdge) {
            onMeetingPoint(inEdge, viaNode, outEdge);
        }
    }

    // Voici les 2 nouveaux cas de tests basés sur des mocks qu'on a ajouté
    /**
     * Vérifie que "onEdge" utilise bien Graph et Weighting
     * pour construire un Path avec une distance, temps et arêtes.
     */
    @Test
    public void testOnEdgeWithMocks() {
        Graph mockGraph = mock(Graph.class);
        Weighting mockWeighting = mock(Weighting.class);
        EdgeIteratorState mockEdge = mock(EdgeIteratorState.class);
        // Valeurs simulées
        when(mockGraph.getEdgeIteratorState(42, 7)).thenReturn(mockEdge);
        when(mockEdge.getDistance()).thenReturn(15.0);
        when(mockWeighting.calcEdgeMillis(mockEdge, false)).thenReturn(300L);
        when(mockWeighting.calcTurnMillis(anyInt(), anyInt(), anyInt())).thenReturn(0L);

        TestExtractor extractor = new TestExtractor(mockGraph, mockWeighting);
        extractor.callOnEdge(42, 7, false, -1);

        Path path = extractor.path;

        // Vérifie le contenu du Path
        assertEquals(15.0, path.getDistance(), 1e-3);
        assertEquals(300L, path.getTime());
        assertEquals(1, path.getEdges().size());
        assertEquals(42, path.getEdges().get(0));

        // Vérifie les appels aux mocks
        verify(mockGraph).getEdgeIteratorState(42, 7);
        verify(mockEdge).getDistance();
        verify(mockWeighting).calcEdgeMillis(mockEdge, false);
    }

    /**
     * Vérifie que "onMeetingPoint" ajoute le temps de virage
     * fourni par Weighting.calcTurnMillis.
     */
    @Test
    public void testOnMeetingPointWithMocks() {
        Graph mockGraph = mock(Graph.class);
        Weighting mockWeighting = mock(Weighting.class);
        // Valeur simulée pour le temps de virage
        when(mockWeighting.calcTurnMillis(5, 10, 7)).thenReturn(400L);

        TestExtractor extractor = new TestExtractor(mockGraph, mockWeighting);
        extractor.callOnMeetingPoint(5, 10, 7);

        Path path = extractor.path;
        // Vérifie que le temps du Path correspond au temps simulé
        assertEquals(400L, path.getTime());
        verify(mockWeighting).calcTurnMillis(5, 10, 7);
    }
}

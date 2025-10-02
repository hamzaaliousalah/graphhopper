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

package com.graphhopper.util;

import com.github.javafaker.Faker;
import com.graphhopper.util.shapes.GHPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DistanceCalcEuclideanTest {

    @Test
    public void testCrossingPointToEdge() {
        DistanceCalcEuclidean distanceCalc = new DistanceCalcEuclidean();
        GHPoint point = distanceCalc.calcCrossingPointToEdge(0, 10, 0, 0, 10, 10);
        assertEquals(5, point.getLat(), 0);
        assertEquals(5, point.getLon(), 0);
    }

    @Test
    public void testCalcNormalizedEdgeDistance() {
        DistanceCalcEuclidean distanceCalc = new DistanceCalcEuclidean();
        double distance = distanceCalc.calcNormalizedEdgeDistance(0, 10, 0, 0, 10, 10);
        assertEquals(50, distance, 0);
    }

    @Test
    public void testCalcNormalizedEdgeDistance3dStartEndSame() {
        DistanceCalcEuclidean distanceCalc = new DistanceCalcEuclidean();
        double distance = distanceCalc.calcNormalizedEdgeDistance3D(0, 3, 4, 0, 0, 0, 0, 0, 0);
        assertEquals(25, distance, 0);
    }

    @Test
    public void testValidEdgeDistance() {
        DistanceCalcEuclidean distanceCalc = new DistanceCalcEuclidean();
        boolean validEdgeDistance = distanceCalc.validEdgeDistance(5, 15, 0, 0, 10, 10);
        assertEquals(false, validEdgeDistance);
        validEdgeDistance = distanceCalc.validEdgeDistance(15, 5, 0, 0, 10, 10);
        assertEquals(false, validEdgeDistance);
    }

    @Test
    public void testDistance3dEuclidean() {
        DistanceCalcEuclidean distCalc = new DistanceCalcEuclidean();
        assertEquals(1, distCalc.calcDist3D(
                0, 0, 0,
                0, 0, 1
        ), 1e-6);
        assertEquals(10, distCalc.calcDist3D(
                0, 0, 0,
                0, 0, 10
        ), 1e-6);
    }


    // Voici les tests qu'on a ajouté
    @Test
    public void testCalcDenormalizedDist() {
        DistanceCalcEuclidean calc = new DistanceCalcEuclidean();
        assertEquals(5.0, calc.calcDenormalizedDist(25.0), 1e-6);
    }

    @Test
    public void testCalcNormalizedDistSingleValue() {
        DistanceCalcEuclidean calc = new DistanceCalcEuclidean();
        assertEquals(25.0, calc.calcNormalizedDist(5.0), 1e-6);
    }

    @Test
    public void testIntermediatePointHalfway() {
        DistanceCalcEuclidean calc = new DistanceCalcEuclidean();
        GHPoint mid = calc.intermediatePoint(0.5, 0, 0, 10, 10);
        assertEquals(5.0, mid.getLat(), 1e-6);
        assertEquals(5.0, mid.getLon(), 1e-6);
    }

    @Test
    public void testToStringReturns2D() {
        DistanceCalcEuclidean calc = new DistanceCalcEuclidean();
        assertEquals("2D", calc.toString());
    }

    @Test
    public void testCalcNormalizedDistZeroDistance() {
        DistanceCalcEuclidean calc = new DistanceCalcEuclidean();
        assertEquals(0.0, calc.calcNormalizedDist(0, 0, 0, 0), 1e-6);
    }

    @Test
    public void testCalcShrinkFactorAlwaysOne() {
        DistanceCalcEuclidean calc = new DistanceCalcEuclidean();
        assertEquals(1.0, calc.calcShrinkFactor(45.0, 60.0), 1e-6);
    }

    @Test
    public void testValidEdgeDistanceTrueCase() {
        DistanceCalcEuclidean calc = new DistanceCalcEuclidean();
        boolean result = calc.validEdgeDistance(5, 5, 0, 0, 10, 10);
        assertEquals(true, result);
    }


}


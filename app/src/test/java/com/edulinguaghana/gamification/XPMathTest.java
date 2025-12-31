package com.edulinguaghana.gamification;

import org.junit.Test;
import static org.junit.Assert.*;

public class XPMathTest {

    @Test
    public void xpRequiredAndLevelCalculation() {
        // Check xp required is positive and increases
        int req1 = XPManager.xpRequiredForLevel(1);
        int req2 = XPManager.xpRequiredForLevel(2);
        int req3 = XPManager.xpRequiredForLevel(3);
        assertTrue(req1 > 0);
        assertTrue(req2 > req1);
        assertTrue(req3 > req2);

        // Check level calculation for cumulative xp
        int totalToReach3 = XPManager.totalXpForLevelsBelow(3);
        // totalXpForLevelsBelow(3) returns XP required to reach start of level 3
        int level = XPManager.getLevelForXp(totalToReach3);
        assertTrue(level >= 3);

        // If we have less than threshold, level should be 2 or lower
        int justBelow = totalToReach3 - 1;
        int levelBelow = XPManager.getLevelForXp(justBelow);
        assertTrue(levelBelow < 3);
    }
}


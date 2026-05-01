package de.geheimagentnr1.easier_sleeping;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EasierSleepingTest {

    @Test
    void modIdIsValid() {

        String modId = "easier_sleeping";
        assertTrue( modId.matches( "[a-z][a-z0-9_]{1,63}" ) );
    }
}

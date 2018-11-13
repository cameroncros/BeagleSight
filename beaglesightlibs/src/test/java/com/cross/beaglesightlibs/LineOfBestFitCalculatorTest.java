package com.cross.beaglesightlibs;

import com.cross.beaglesightlibs.exceptions.InvalidNumberFormatException;

import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class LineOfBestFitCalculatorTest {
    @Test
    public void calcPosition() throws InvalidNumberFormatException {
        {
            List<PositionPair> pos = new ArrayList<>();
            pos.add(new PositionPair("10", "10"));
            LineOfBestFitCalculator calc = new LineOfBestFitCalculator();
            calc.setPositions(pos);
            assertEquals(Float.NaN, calc.calcPosition(11), 0.001);
        }
        {
            List<PositionPair> pos = new ArrayList<>();
            pos.add(new PositionPair("10", "10"));
            pos.add(new PositionPair("20", "20"));
            LineOfBestFitCalculator calc = new LineOfBestFitCalculator();
            calc.setPositions(pos);
            assertEquals(15.0, calc.calcPosition(15), 0.001);
        }
    }
}
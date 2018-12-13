package com.cross.beaglesightlibs;

public class MockBowConfig extends BowConfig {
    public MockBowConfig()
    {
        super();
    }

    @Override
    public PositionCalculator getPositionCalculator()
    {
        return new MockPositionCalculator();
    }

    private class MockPositionCalculator extends PositionCalculator {
        @Override
        public float calcPosition(float distance) {
            return -(distance) * (distance - 60) * (distance - 1000) / 60000;
        }
    }
}

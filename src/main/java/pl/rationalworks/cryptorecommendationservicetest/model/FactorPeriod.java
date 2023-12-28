package pl.rationalworks.cryptorecommendationservicetest.model;

public enum FactorPeriod {
    DAY(0), WEEK(-7), MONTH(-30);
    private final int daysBack;

    FactorPeriod(int daysBack) {
        this.daysBack = daysBack;
    }

    public int getDaysBack() {
        return daysBack;
    }
}

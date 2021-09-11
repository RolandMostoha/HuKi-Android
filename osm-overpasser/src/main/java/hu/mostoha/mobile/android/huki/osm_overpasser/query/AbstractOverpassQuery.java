package hu.mostoha.mobile.android.huki.osm_overpasser.query;

abstract class AbstractOverpassQuery {
    protected OverpassQueryBuilder builder;

    AbstractOverpassQuery() {
        this(new OverpassQueryBuilderImpl());
    }

    AbstractOverpassQuery(OverpassQueryBuilder builder) {
        this.builder = builder;
    }

    public void onSubQueryResult(AbstractOverpassSubQuery subQuery) {
        builder.append(subQuery.build());
    }

    protected abstract String build();
}

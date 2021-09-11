package hu.mostoha.mobile.android.huki.osm_overpasser.query;

abstract class AbstractOverpassSubQuery extends AbstractOverpassQuery {
    private OverpassQuery parent;

    public AbstractOverpassSubQuery(OverpassQuery parent) {
        super();
        this.parent = parent;
    }

    AbstractOverpassSubQuery(OverpassQuery parent, OverpassQueryBuilder builder) {
        super(builder);
        this.parent = parent;
    }

    public final OverpassQuery end() {
        parent.onSubQueryResult(this);

        return parent;
    }
}

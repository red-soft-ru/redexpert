package org.executequery.sql.sqlbuilder;

import org.executequery.databasemediators.DatabaseConnection;
import org.underworldlabs.util.MiscUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GrantBuilder extends SQLBuilder {

    boolean isGrant = true;
    String grantType;
    String grantor;
    String relation;

    List<String> fields;
    String relationType;
    String grantorType;
    boolean isGrantOption = false;

    public GrantBuilder(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    public static GrantBuilder createGrantBuilder(DatabaseConnection dc) {
        return new GrantBuilder(dc);
    }

    public GrantBuilder appendFields(String... fields) {
        if (this.fields == null)
            this.fields = new ArrayList<>();
        this.fields.addAll(Arrays.asList(fields));
        return this;
    }

    public boolean isGrant() {
        return isGrant;
    }

    public GrantBuilder setGrant(boolean grant) {
        isGrant = grant;
        return this;
    }

    public String getGrantType() {
        return grantType;
    }

    public GrantBuilder setGrantType(String grantType) {
        this.grantType = grantType;
        return this;
    }

    public String getGrantor() {
        return grantor;
    }

    public GrantBuilder setGrantor(String grantor) {
        this.grantor = grantor;
        return this;
    }

    public String getRelation() {
        return relation;
    }

    public GrantBuilder setRelation(String relation) {
        this.relation = relation;
        return this;
    }

    public boolean isGrantOption() {
        return isGrantOption;
    }

    public GrantBuilder setGrantOption(boolean grantOption) {
        isGrantOption = grantOption;
        return this;
    }

    public String getRelationType() {
        return relationType;
    }

    public GrantBuilder setRelationType(String relationType) {
        this.relationType = relationType;
        return this;
    }

    public String getGrantorType() {
        return grantorType;
    }

    public void setGrantorType(String grantorType) {
        this.grantorType = grantorType;
    }

    @Override
    public String getSQLQuery() {
        StringBuilder sb = new StringBuilder();
        if (isGrant)
            sb.append("GRANT ");
        else sb.append("REVOKE ");
        sb.append(grantType);
        if (fields != null) {
            sb.append(" (");
            boolean first = true;
            for (String field : fields) {
                if (!first)
                    sb.append(", ");
                first = false;
                sb.append(MiscUtils.getFormattedObject(field, connection));
            }
            sb.append(")");
        }
        sb.append(" ON ");
        if (relationType != null)
            sb.append(relationType).append(" ");
        sb.append("\"").append(relation).append("\"\n");
        if (isGrant)
            sb.append("TO ");
        else sb.append("FROM ");
        if (grantorType != null)
            sb.append(grantorType).append(" ");
        sb.append("\"").append(grantor).append("\"");
        if (isGrantOption)
            sb.append(" WITH GRANT OPTION");
        return sb.toString();
    }
}

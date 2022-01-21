package TKKG.DATA;

/**
 * Diese Klasse wrappt die benötigten Meta Daten von den verschiedenen Spalten
 */

public class MetaData {
    private final String name;
    private final String typeName;
    private final int typeNo;
    private final boolean nullable;
    private final boolean primaryKey;

    /**
     *
     * @param name Der Name der aberfufenen Spalte
     * @param type Der SQL Datentyp der in der Spalte benutzt wird
     * @param typeNo Die Nummer des SQL Datentypen der in der Spalte benutzt wird
     * @see java.sql.Types
     * @param nullable Zeigt an ob die Spalte null sein darf oder nicht
     * @param primaryKey Zeigt an ob die Spalte ein primary Key ist oder nicht
     */

    public MetaData(String name, String type, int typeNo, boolean nullable, boolean primaryKey) {
        this.name = name;
        this.typeName = type;
        this.nullable = nullable;
        this.typeNo = typeNo;
        this.primaryKey = primaryKey;
    }

    /**
     *
     * @return Den Namen der Spalte wird für die Ausgabe benötigt
     */

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public int getTypeNo() {
        return typeNo;
    }
}
package TKKG.DATA;

import TKKG.UI.UserInput;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Managed die Datenbank die ihm im DatabaseInit übergeben wurde.
 */

public class DatabaseManager {
    private UserInput userInput;
    private DatabaseInit dbInit;


    /**
     * Der Konstruktor legt eine neue UserInput Instanz um die Inputs des Users zu verwalten
     * und eine neue dbInit instanz um eine Verbindung zur Datenbank aufzubauen.
     * @param url der Connection
     * @param user der Connected
     * @param pass um zu Connecten
     */
    public DatabaseManager(String url, String user, String pass) {
        userInput = new UserInput();
        dbInit = new DatabaseInit(url,user,pass);
    }


    /**
     * Executed ein SelectStatement, das Dynamisch aus table attribute einliest.
     * After executing a Prepared Statement from @see{#buildShowAllStatement(table)},
     * a set method and input scanner is determined through the corresponding methods in:
     * link{#getColNamesNTypes(String)},
     * and executes them through the invoke method
     * Null cases are optional and do not to be inputted.
     * @param table der Tabellenname aus den etwas Gelöscht werden soll
     */
    public void showTable(String table) {
        try {
            HashMap<Integer, MetaData> colNamesNTypes = dbInit.getColNamesNTypes(table);

            PreparedStatement statement = buildShowAllStatement(table);
            ResultSet rs = statement.executeQuery();
            System.out.println("----" + table + "----");
            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append(table).append("[");
                for (int i = 0; i < colNamesNTypes.size(); i++) {
                    sb.append(colNamesNTypes.get(i).getName()).append(": ");
                    Method get = dbInit.getSQLMethodGet(colNamesNTypes.get(i).getTypeName());
                    Object reading = get.invoke(rs,i+1);

                    if(rs.wasNull())
                        reading = null;

                    sb.append(reading);
                    if(i + 1 < colNamesNTypes.size())
                        sb.append(", ");
                }
                sb.append("]");
                System.out.println(sb);
            }

        } catch (SQLException e) {
            handleSqlException(e);
        } catch (InvocationTargetException | IllegalAccessException ignored) {

        }
    }

    /**
     * Executed ein InsertStatement, das Dynamisch aus table A einliest.
     * a set method and input scanner is determined through the corresponding methods in:
     * link{#getColNamesNTypes(String)},
     * and executes them through the invoke method
     * Null cases are optional and do not to be inputted.
     * @param table der Tabellenname aus den etwas Gelöscht werden soll
     */

    public void insertIntoTable(String table) {
        try {

            HashMap<Integer, MetaData> colNamesNTypes = dbInit.getColNamesNTypes(table);
            PreparedStatement statement = buildInsertStatement(table);

            for (int i = 0; i < colNamesNTypes.size(); i++) {
                MetaData data = colNamesNTypes.get(i);
                String rowName = data.getName();
                String type = data.getTypeName();
                char auswahl = 'y';

                if (data.isNullable()) {
                    do {
                        System.out.printf("%s ist Optional wollen Sie diesen angeben? (y/n)%n ", rowName);
                        auswahl = userInput.readChar().charAt(0);
                    } while (auswahl != 'y' && auswahl != 'n');
                }
                if (auswahl == 'n') {
                    statement.setNull(i + 1, data.getTypeNo());
                } else {
                    System.out.println("Bitte geben Sie " + rowName + " ein. ");

                    Method sql = dbInit.getSQLMethodInsert(type);

                    Method userInputMethod = dbInit.getUserInputMethod(type);

                    sql.invoke(statement, i + 1, userInputMethod.invoke(userInput));
                    //statement.setString(3, userInput.scan());

                }
            }
            System.out.println(statement.executeUpdate() == 1 ? "Erfolgreich eingefügt" : "Bitte überprüfe deine Eingabe, da kann etwas nicht stimmmen! (Tabelle wurde nicht verändert)");
        } catch (SQLException e) {
            handleSqlException(e);
        } catch (InvocationTargetException | IllegalAccessException ignored) {

        }
    }

    /**
     * Executed ein UpdateStatement, das Dynamisch aus table attribute einliest.
     * a set method and input scanner is determined through the corresponding methods in:
     * link{#getColNamesNTypes(String)},
     * and executes them through the invoke method
     * Null cases are optional and do not to be inputted.
     * @param table der Tabellenname aus den etwas Gelöscht werden soll
     */
    public void updateTable(String table){
        try{

            HashMap<Integer, MetaData> colNamesNTypes = dbInit.getColNamesNTypes(table);
            PreparedStatement statement = buildUpdateStatement(table);
            ArrayList<MetaData> primaryKeys = new ArrayList<>();
            int statementCount = 1;

            for (int i = 0; i < colNamesNTypes.size(); i++) {
                MetaData data = colNamesNTypes.get(i);
                String rowName = data.getName();
                String type = data.getTypeName();

                char auswahl = 'y';

                if (data.isNullable()) {
                    do {
                        System.out.printf("%s ist Optional wollen Sie diesen angeben? (y/n) %n", rowName);
                        auswahl = UserInput.sc.nextLine().charAt(0);
                    } while (auswahl != 'y' && auswahl != 'n');
                }
                if (auswahl == 'n') {
                    statement.setNull(statementCount++, data.getTypeNo());
                } else {
                    if (data.isPrimaryKey()) {
                        primaryKeys.add(data);
                    } else {
                        System.out.println("Bitte geben Sie " + rowName + " ein. ");
                        Method sql = dbInit.getSQLMethodInsert(type);
                        Method userInputMethod = dbInit.getUserInputMethod(type);
                        Object input = userInputMethod.invoke(userInput);

                        sql.invoke(statement, statementCount++, input);
                    }
                }
            }

            for (int i = 0; i < primaryKeys.size(); i++) {
                System.out.println(primaryKeys.get(i).getName() + " vom zu bearbeitenden " + table + " eingeben: ");
                Method sql = dbInit.getSQLMethodInsert(primaryKeys.get(i).getTypeName());
                Method userInputMethod = dbInit.getUserInputMethod(primaryKeys.get(i).getTypeName());
                Object input = userInputMethod.invoke(userInput);
                sql.invoke(statement, statementCount++, input);
            }

            System.out.println(statement.executeUpdate() == 1 ? "Erfolgreich geupdatet" : "Bitte überprüfe deine Eingabe, da kann etwas nicht stimmmen! (Tabelle wurde nicht verändert)");

        }catch(SQLException e){
            handleSqlException(e);
        } catch (InvocationTargetException | IllegalAccessException ignored) {

        }
    }


    /**
     * Zählt die rekursiven Aufrufe (ModeratorID(ID)) in der Tabelle Accounts und gruppiert nach Username.
     * Create a PreparedStatement statically and extracts the Data from ACCOUNTS
     * and print it.
     */
    public void countRecusriveFromAccounts() {
        try {
            System.out.println("---RECURSIVE COUNT FROM ACCOUNTS---");
            StringBuilder recursiveOutput = new StringBuilder();
            String statement = "SELECT a.username, COUNT(*) FROM accounts a, accounts b WHERE a.id=b.moderatorid GROUP BY a.username";
            PreparedStatement stmt = dbInit.getCon().prepareStatement(statement);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String result = "Moderator: " + rs.getString(1) +" | ModeratorCount: " + rs.getInt(2);
                recursiveOutput.append(result).append("\n");
            }
            System.out.println(recursiveOutput);
        } catch (SQLException e) {
            handleSqlException(e);
        }
    }

    /**
     * Executed ein DeleteStatement, das Dynamisch aus table attribute einliest
     * a set method and input scanner is determined throught the corresponing methods in:
     * link{#getColNamesNTypes(String)},
     * and executes them through the invoke method
     * @param table der Tabellenname aus den etwas Gelöscht werden soll
     */

    public void deleteFromTable(String table) {
        try {
            HashMap<Integer, MetaData> colNamesNTypes = dbInit.getColNamesNTypes(table);
            PreparedStatement statement = buildDeleteStatement(table);
            StringBuilder sb = new StringBuilder();
            System.out.println("---LÖSCHE AUS " + table + "---");
            int statementCount = 1;

            for (int i = 0; i < colNamesNTypes.size(); i++) {
                if(colNamesNTypes.get(i).isPrimaryKey()) {
                    String rowName = colNamesNTypes.get(i).getName();
                    String type = colNamesNTypes.get(i).getTypeName();

                    System.out.println("Bitte geben Sie " + rowName + " ein vom zu löschendem " + table);
                    Method sql = dbInit.getSQLMethodInsert(type);
                    Method userInputMethod = dbInit.getUserInputMethod(type);
                    Object input = userInputMethod.invoke(userInput);

                    sql.invoke(statement, statementCount++, input);
                }
            }

            System.out.println(statement.executeUpdate() == 1 ? "Erfolgreich gelöscht" : "Bitte überprüfe deine Eingabe, da kann etwas nicht stimmmen! (Tabelle wurde nicht verändert)");

        } catch (SQLException e) {
            handleSqlException(e);
        } catch (InvocationTargetException | IllegalAccessException ignored) {

        }
    }

    /**
     * Generiert ein Prepared Insert Statement Dynamisch, abhängig von der Tabelle in der Man Inserten will
     * Es werden die Meta Daten der Tabelle in der Hashmap meta gespeichert, und so die Primary Keys festgehalten.
     * Das versichert einlesen von unterschiedlichen Anzahlen an Primary Keys.
     * @param table Der Tabellenname auf den das InsertStatement Aufgebaut werden soll
     * @return SQL Query welches uns alle Datensätze einer Tabelle anzeigen kann
     * @throws SQLException Wenn Prepared Statement nicht generiert werden kann oder ein Fehler auftritt
     */

    private PreparedStatement buildInsertStatement(String table) throws SQLException{
        HashMap<Integer, MetaData> meta = dbInit.getColNamesNTypes(table);
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(table).append(" VALUES(");
        sb.append("?");
        for (int i = 1; i < meta.size(); i++) {
            sb.append(",?");
        }
        sb.append(")");
        System.out.println(sb);
        PreparedStatement stmt = dbInit.getCon().prepareStatement(sb.toString());
        return stmt;
    }

    /**
     * Generiert ein Prepared Update Statement Dynamisch, abhängig von der Tabelle in der Man Updaten will
     * Es werden die Meta Daten der Tabelle in der Hashmap meta gespeichert, und so die Primary Keys festgehalten.
     * Das versichert einlesen von unterschiedlichen Anzahlen an Primary Keys.
     * @param table Der Tabellenname auf den das UpdateStatement Aufgebaut werden soll
     * @return SQL Query welches uns alle Datensätze einer Tabelle anzeigen kann
     * @throws SQLException Wenn Prepared Statement nicht generiert werden kann oder ein Fehler auftritt
     */
    private PreparedStatement buildUpdateStatement(String table) throws SQLException{
        HashMap<Integer, MetaData> meta = dbInit.getColNamesNTypes(table);
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        ArrayList<String> primaryKeys = new ArrayList<>();
        sb.append("UPDATE ").append(table).append(" SET ");

        for (int i = 0; i < meta.size(); i++) {
            if(meta.get(i).isPrimaryKey()) {
                primaryKeys.add(meta.get(i).getName());
            } else {
                if(first) {
                    sb.append(meta.get(i).getName()).append(" = ?");
                    first = false;
                } else {
                    sb.append(",").append(meta.get(i).getName()).append(" = ?");
                }
            }
        }
        sb.append(" WHERE ");
        sb.append(primaryKeys.get(0)).append(" = ?");
        if(primaryKeys.size() > 1) {
            for (int i = 1; i < primaryKeys.size(); i++) {
                sb.append(" AND ").append(primaryKeys.get(i)).append(" = ?");
            }
        }
        System.out.println(sb);
                //UPDATE FOLLOWER SET START_FOLLOWING = ?, WHERE FOLLOWING_ACCOUNT_ID = ? AND FOLLOWER_ACCOUNT_ID = ?
                // UPDATE TABLE SET START_FOLLWING = ?,
        return dbInit.getCon().prepareStatement(sb.toString());
    }

    /**
     * Generiert ein Prepared Delete Statement Dynamisch, abhängig von der Tabelle in der Man Deleten will.
     *
     * Es werden die Meta Daten der Tabelle in der Hashmap meta gespeichert, und so die Primary Keys festgehalten.
     * Das versichert einlesen von unterschiedlichen Anzahlen an Primary Keys.
     * Ein String Builder fügt alle Komponenten zusammen
     * @param table Der Tabellenname auf den das DeleteStatement Aufgebaut werden soll
     * @return SQL Query welches uns alle Datensätze einer Tabelle anzeigen kann
     * @throws SQLException Wenn Prepared Statement nicht generiert werden kann oder ein Fehler auftritt
     */
    private PreparedStatement buildDeleteStatement(String table) throws SQLException{
        boolean first = true;
        HashMap<Integer, MetaData> meta = dbInit.getColNamesNTypes(table);
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(table).append(" WHERE ");
        for (int i = 0; i < meta.size(); i++) {
            if(first && meta.get(i).isPrimaryKey()) {
                sb.append(meta.get(i).getName()).append(" = ?");
                first = false;
            } else if(meta.get(i).isPrimaryKey()){
                sb.append(" AND ").append(meta.get(i).getName()).append(" = ?");
            }
        }

        return dbInit.getCon().prepareStatement(sb.toString());
    }

    /**
     * Es wird ein Show Statement generiert, also ein Statement wie SELECT * FROM ACCOUNTS um alle Einträge der Tabelle anzeigen zu können <br>
     * In Der Methode werden sich alle Meta Daten der Tabellen Spalten geholt (HashMap<Integer, TKKG.MetaData> meta) um Primary key herauszufinden
     * und nach diesem Ordnen zu können. (Es wird nur der erste Primary Key genommen)
     * @param table Der Tabellen Name von dem das Show Statement generiert werden soll
     * @return SQL Query welches uns alle Datensätze einer Tabelle anzeigen kann
     * @throws SQLException Wenn Prepared Statement nicht generiert werden kann und ein fehler auftritt.
     */
    private PreparedStatement buildShowAllStatement(String table) throws SQLException{
        HashMap<Integer, MetaData> meta = dbInit.getColNamesNTypes(table);
        StringBuilder statement = new StringBuilder();
        statement.append("SELECT * FROM ").append(table).append(" ORDER BY ");
        for (int i = 0; i < meta.size(); i++) {
            //Break immer sehr unschön hier jedoch unserer Meinung nach keine andere Möglichkeit
            //Außer man returnt direkt in der If Anweisung und returnt Standardmäßig null
            if(meta.get(i).isPrimaryKey()) {
                statement.append(meta.get(i).getName());
                break;
            }
        }
        return dbInit.getCon().prepareStatement(statement.toString());
    }


    /**
     * @return returns user Input {@link #getDbInit()}
     */
    public UserInput getUserInput() {
        return userInput;
    }

    /**
     * @return returns dbInit
     */

    public DatabaseInit getDbInit() {
        return dbInit;
    }

    /**
     * Handles all SQL Exceptions
     * Code 1: No such Key found
     * Code 2290: Invalid input
     * Code 1400: Tried inputting NULL when NULL is not permitted
     * @param e caught Exception to be handled
     */
    private void handleSqlException(SQLException e) {
        System.out.println();
        System.err.println("Es wurde nichts in der Datenbank verändert");
        if(e.getErrorCode() == 1) {
            System.err.println("Eine eingabe die du getroffen hast und unique ist bereits in der Datenbank");
        }
        if(e.getErrorCode() == 2290) {
            System.err.println("Eine Check Bedingung wurde nicht erfüllt");
        }
        if(e.getErrorCode() == 1400) {
            System.err.println("Du hast eine NULL Value angegeben bei einer Value die nicht NULL sein darf");
        }
        char eingabe;
        do {
            System.out.println("Möchtest du den vollständigen error code sehen? (y/n)");
            eingabe = userInput.readChar().charAt(0);
        } while (eingabe != 'y' && eingabe != 'n');

        if(eingabe == 'y') {
            e.printStackTrace();
        }
    }

    public void close() {
        dbInit.close();
        userInput.close();
    }
}

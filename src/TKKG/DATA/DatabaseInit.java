package TKKG.DATA;
import TKKG.UI.UserInput;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Database initalisierung
 * Es werden Methoden aufgerufen die einmalig beim Programmstart aufgerufen werden müssen <br>
 * <code>meethodByTypeSQLInsert</code> Hier werden die methoden in eine HashMap gepackt die je nach Typ der SQL Zeile aufgerufen werden müssen
 * um prepared Statements zu setzen <br>
 * <code>methodByTypeUI</code> Hier werden die Methoden in eine Hashmap gepackt die je nach der der SQL Zeile aufgerufen werden müssen
 * um den User dazu aufzufordern den richtigen Typen in die Konsole zu tippen <br>
 * <code>methodByTypeSqlGet</code> Hier werden die Methoden in eine Hashmap gepackt die je nach der SQL Zeile aufgerufen werden müssen
 * um die Daten die sich in der Column befinden auszugebebn <br>
 * <code>tableData</code> Hier werden zu den Tabellen die unterschiedlichen Meta Daten der Columns gepackt
 * <code>tableNames</code> Hier befinden sich die Table Names der zugehörigen Connection, diese werden dynamisch generiert (je nach Connection) <br>
 * <code>Con</code> Hier wird die Connection zur Datenabank gespeichert
 * @see MetaData
 **/

public class DatabaseInit {
    private HashMap<String, Method> methodByTypeSQLInsert;
    private HashMap<String, Method> methodByTypeUI;
    private HashMap<String, Method> methodByTypeSQLGet;
    private HashMap<String, HashMap<Integer, MetaData>> tableData;
    private ArrayList<String> tableNames;
    private Connection con;

    /**
     * @param url Url der Datenbank mit der sich verbunden werden soll
     * @param user User mit dem sich in der Datenbank eingeloggt werden soll
     * @param pass Passwort des Benutzers der Datenbank
     * <code>DatabaseInit</code> initialisierung der Verbindung und arbeitsnotwendigen Daten
     */

    public DatabaseInit(String url, String user, String pass) {
        this.tableNames = new ArrayList<>();
        this.tableData = new HashMap<>();
        this.methodByTypeSQLInsert = new HashMap<>();
        this.methodByTypeUI = new HashMap<>();
        this.methodByTypeSQLGet = new HashMap<>();

        initConnection(url, user, pass);
        initTableNames();
        initMethodSet();
    }


    /**
     * @param url
     * @param user
     * @param pass
     * Initialisierung der Verbindung mittels url, username und password und entsprechendem Treiber
     */
    private void initConnection(String url, String user, String pass){
        try {
            con = DriverManager.getConnection(url, user, pass);
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            //TODO
        }
    }

    /**
     * Das Set der Methoden wird initialisiert, indem wir die Methoden aus der entsprechenden Klasse wie PreparedStatement holen
     * und das ganze auf den jeweiligen Key, in dem Fall der SQL-Datentyp mappen.
     * So können wir immer wenn wir einen Datentyp zur hand haben dynamisch die entsprechende Methode invoken
     * see:
     * {@link #getSQLMethodGet(String)},
     * {@link #getSQLMethodGet(String)},
     * {@link #getUserInputMethod(String)}
     */
    private void initMethodSet(){
        try {
            methodByTypeUI.put("NUMBER", UserInput.class.getMethod("readLong"));
            methodByTypeUI.put("VARCHAR2", UserInput.class.getMethod("readString"));
            methodByTypeUI.put("DATE", UserInput.class.getMethod("readDate"));
            methodByTypeUI.put("TIMESTAMP", UserInput.class.getMethod("readTimestamp"));
            methodByTypeUI.put("CHAR", UserInput.class.getMethod("readChar"));

            methodByTypeSQLInsert.put("NUMBER", PreparedStatement.class.getMethod("setLong", int.class, long.class));
            methodByTypeSQLInsert.put("VARCHAR2", PreparedStatement.class.getMethod("setString", int.class, String.class));
            methodByTypeSQLInsert.put("DATE", PreparedStatement.class.getMethod("setDate", int.class, Date.class));
            methodByTypeSQLInsert.put("TIMESTAMP", PreparedStatement.class.getMethod("setTimestamp", int.class, Timestamp.class));
            methodByTypeSQLInsert.put("CHAR", PreparedStatement.class.getMethod("setString", int.class, String.class));

            methodByTypeSQLGet.put("NUMBER", ResultSet.class.getMethod("getLong", int.class));
            methodByTypeSQLGet.put("VARCHAR2", ResultSet.class.getMethod("getString", int.class));
            methodByTypeSQLGet.put("DATE", ResultSet.class.getMethod("getDate", int.class));
            methodByTypeSQLGet.put("TIMESTAMP", ResultSet.class.getMethod("getTimestamp", int.class));
            methodByTypeSQLGet.put("CHAR", ResultSet.class.getMethod("getString", int.class));

        } catch (Exception e) {

        }
    }

    /**
     * Initialisierung der Table names und Metadaten für unsere Hash-Map mit dem Table Name als Key und relevante Table-Metadaten als Value.
     * sodass wir später nur noch einen Table angeben müssen den bei dem wir etwas inserten, updaten, deleten oder showen wollen und dann
     * über unsere Hashmap die nötigen Column Names und Datatypes fetchen können um unseren SQL Query dynamisch zusammenzubauen und auch
     * über die Method Hashmap die jeweilige Methode dynamisch (bspw. setLong oder setInt) ausführen können.
     */

    private void initTableNames(){
        try {

            DatabaseMetaData tables = con.getMetaData();

            ResultSet dbResult = tables.getTables(con.getCatalog(), con.getSchema(), "%", null);

            while (dbResult.next()) {
                tableNames.add(dbResult.getString(3));
            }


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Herzlichen Glückwunsch du hast ein Easter Egg gefunden!");
        }

        for (int i = 0; i < tableNames.size(); i++) {
            String tableName = tableNames.get(i);
            tableData.put(tableName, initColNamesNTypes(tableName));
        }

    }

    /**
     * @param tableName
     * @return list of Primary Keys in
     * @throws Exception
     * Hier holen wir uns eine Liste der Primarykeys des Tables aus den Metadaten der Tabellen.
     */

    private ArrayList<String> getPrimaryKeys(String tableName) throws Exception{
        DatabaseMetaData tables = getCon().getMetaData();
        ResultSet primaryKeys = tables.getPrimaryKeys(con.getCatalog(), con.getSchema(), tableName);

        ArrayList<String> primaryKeysList = new ArrayList<>();
        while (primaryKeys.next()) {
            primaryKeysList.add(primaryKeys.getString("COLUMN_NAME"));
        }

        return primaryKeysList;
    }

    /**
     *
     * @param tableName
     * @return
     * Hier initialisieren wir die Spaltennamen und Datentypen der Spalten sowie auch welche Spalten Primary-Keys sind und
     * erstellen für jeweils eine Spalte ein MetaData Objekt:
     * <code> new MetaData(String name, String type, int typeNo, boolean nullable, boolean primaryKey) </code>
     * auf welches wir die von uns gewünschten Metadaten mappen.
     * Genutzt werden diese Metadatenobjekte um sie auf das jeweilige Table in der von uns dafür vorgesehenen Hashmap zu mappen.
     * See
     * {@link #initTableNames()},
     * {@link #tableData}
     *  @see MetaData
     */

    private HashMap<Integer, MetaData> initColNamesNTypes(String tableName) {

        HashMap<Integer, MetaData> map = new HashMap<>();

        try {
            ArrayList<String> primaryKeysList = getPrimaryKeys(tableName);
            String statement = "SELECT * FROM " + tableName;
            PreparedStatement stmt = con.prepareStatement(statement);

            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();

            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                boolean isPrimaryKey = false;
                for (String primaryKey :
                        primaryKeysList) {
                    if (metaData.getColumnName(i).equals(primaryKey))
                        isPrimaryKey = true;
                }

                MetaData meta = new MetaData(metaData.getColumnName(i), metaData.getColumnTypeName(i),
                        metaData.getColumnType(i), metaData.isNullable(i) == ResultSetMetaData.columnNullable, isPrimaryKey);
                map.put(i - 1, meta);
            }
        } catch (Exception e) {
            //Passiert hoffentlich nie
            e.printStackTrace();
        }

        return map;
    }

    public Connection getCon() {
        return con;
    }

    /**
     * @param dataType
     * @return Passende SQL Insert-methode für den angegebenen Datentyp
     */

    public Method getSQLMethodInsert(String dataType) {
        return methodByTypeSQLInsert.get(dataType);
    }

    /**
     * @param dataType
     * @return Passende SQL Get-methode für angegebenen Datentyp
     */

    public Method getSQLMethodGet(String dataType) {
        return methodByTypeSQLGet.get(dataType);
    }

    /**
     * @param dataType
     * @return Passende UI methode für angegebenen Datentyp
     */

    public Method getUserInputMethod(String dataType) {
        return methodByTypeUI.get(dataType);
    }

    /**
     *
     * @param table
     * @return für uns relevante Metadaten der Tabellen aus der Hashmap.
     * {@link #tableData}
     */

    public HashMap<Integer, MetaData> getColNamesNTypes(String table){
        return tableData.get(table);
    }

    public ArrayList<String> getTableNames() {
        return tableNames;
    }


    /**
     * Schließt die Verbindung zur Datenbank
     * @throws SQLException Wenn conncetion nicht geschlossen werden soll
     */
    public void close() {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

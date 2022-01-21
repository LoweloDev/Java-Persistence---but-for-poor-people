package TKKG;

import TKKG.DATA.DatabaseManager;
import TKKG.UI.Menu;

/**
 *
 * Der gebuchte Anwendungskontext ist eine Live Streaming Plattform Datenbank wie bspw. Twitch <br>
 * Das Programm erzeugt dynamisch insert delete und show Querys für eine Datenbank auf die sich eingeloggt wird <br>
 * Viel von dem Code ist spät Nachts entstanden, deshalb kann es sein, dass der Code an einigen Stellen sehr
 * unübersichtlich und verworren ist <br>
 * Es praktisch kein Error Handling aus try-catch und error printen
 * @author Lars Koenigsmann Matrikelnummer: 7209439 <br>
 * @author Tobias Barthold  Matrikelnummer: 7209370 <br>
 * @author Mika Bredehoeft  Matrikelnummer: 7209429 <br>
 * @version 1.0 Benutz wurde openjdk17
 */

public class Main {


    /**
     * Datenbankverbindung wird hier aufgebaut <br>
     * <code>Url</code> Bitte hier mit der URL der Datenbank ersetzen<br>
     * <code>User</code> Bitte hier mit dem Username ersetzen mit dem sich auf der Datenbank eingeloggt werden soll<br>
     * <code>Pass</code> Bitte hier mit dem Passwort des Users der Datenbank ersetzen <br>
     */
    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@172.22.112.100:1521:fbpool";
        String user = "C##FBPOOL101";
        String pass = "oracle";
        Menu menu = new Menu(new DatabaseManager(url,user,pass));
        menu.mainMenu();
        menu.close();

        System.out.println();
    }
}
Lars Koenigsmann - 7209439 <br>
Mika Bredehoeft  - 7209429 <br>
Tobias Barthold  - 7209370
## Aufgabe 1 a)
### Anwendungskontext

Der von uns gewählte Anwendungskontext ist eine Streamingplatform die Menschen
besuchen können um die Live-Übertragungen anderer anschauen zu können, oder um
selber eine Live-Übertragung zu starten.

### Anwendungsszenario der Datenbankanwendung

Das Anwendungsszenario unserer Datenbankanwendung setzt sich aus vielen kleinen zusammen.
Die Datenbank soll es Nutzern ermöglichen Accounts anlegen und managen zu können, sowie
einen Stream starten oder einem anderen zuschauen zu können.
Darüber hinaus soll der User anderen Usern folgen und an einem Streamchat teilnehmen können.

### Statische Integritätsbedingungen

````sql
creation DATE DEFAULT CURRENT_DATE NOT NULL
````
oder anderes Beispiel:
````sql
message_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, 
````

Daten dürfen nicht NULL sein allein schon wegen der Semantik, dass es nicht "kein" Datum von der Erschaffung von etwas gibt. Standartmäßig wird das aktuelle Datum verwendet.

````sql
passwort varchar(64) NOT NULL CHECK(length(passwort) > 5),
````

Das Passwort darf nicht NULL sein da sich sonst jeder in den Account einloggen könnte. Aus sicherheitsgründen
darf es auch nicht weniger als 5 Zeichen enthalten, da es sonst zu einfach von entsprechenden Programmen gematched wird,
und somit "hackern" leichten zugriff auf den Account verschaffen könnte.

```sql
mail varchar(80) NOT NULL,
```

Mails dürfen nicht NULL sein weil unsere accounts in erster linie an einer Usermail hängen, ohne die ein User
sich nicht identifizieren kann, sollte er mal den username oder das passwort vergessen haben.

````sql
username varchar(20) UNIQUE NOT NULL,
````

Usernames dürfen nicht NULL enthalten um zu gewährleisten, dass sich User gegenseitig Identifizieren können, aus diesem grund müssen sie auch Unique sein, sonst könnten User
Identitätsklau betreiben.

````sql
tier CHARACTER(1) DEFAULT 1 NOT NULL CHECK (tier BETWEEN '1' AND '3'),
````

Tiers dürfen nicht null sein, weil jeder immer einen Tier/Rank hat, wenn er subscribed ist. Standardmäßig 1.

````sql
chat_message VARCHAR(500) NOT NULL,
````
Chat messages dürfen nicht null sein weil NULL kein Character ist.

````sql
streamer_id INTEGER NOT NULL,
````

Streamer ID's als Fremdschlüssel im Kontext eines Streams dürfen nicht null sein weil ein Stream
nicht ohne Streamer stattfinden kann.


/*
    Gebuchter Anwendungskontext: Live Streaming Plattform
    Lars Koenigsmann Matrikelnummer 7209439
    Tobias Barthold Matrikelnummer  7209370
    Mika Bredehoeft Matrikelnummer  7209429
*/

DROP TABLE accounts     CASCADE CONSTRAINTS;
DROP TABLE partner      CASCADE CONSTRAINTS;
DROP TABLE stream       CASCADE CONSTRAINTS;
DROP TABLE vod          CASCADE CONSTRAINTS;
DROP TABLE subscriber   CASCADE CONSTRAINTS;
DROP TABLE follower     CASCADE CONSTRAINTS;
DROP TABLE chat         CASCADE CONSTRAINTS; 

CREATE TABLE accounts(
    id INTEGER NOT NULL PRIMARY KEY,
    creation DATE DEFAULT CURRENT_DATE NOT NULL,
    username varchar(20) UNIQUE NOT NULL, 
    passwort varchar(64) NOT NULL CHECK(length(passwort) > 5), 
    mail varchar(80) NOT NULL,
    streamkey varchar(44) DEFAULT 'LIVE_NOKEY',
    moderatorId INTEGER DEFAULT NULL,

    FOREIGN KEY (moderatorId) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE TABLE partner(
    accounts_id INTEGER NOT NULL PRIMARY KEY,
    partner_macro varchar(3) UNIQUE NOT NULL, 
    emotes_max INTEGER DEFAULT 10 NOT NULL,
    emotes_current INTEGER DEFAULT 0 NOT NULL, 

    FOREIGN KEY (accounts_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE TABLE stream(
     id INTEGER NOT NULL PRIMARY KEY,
     streamer_id INTEGER NOT NULL,
     startdate TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
     stopdate TIMESTAMP, 

    FOREIGN KEY (streamer_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE TABLE vod(
    id INTEGER NOT NULL PRIMARY KEY,
    stream_id INTEGER NOT NULL,
    clicks INTEGER DEFAULT 0 NOT NULL, 
    status VARCHAR(7) DEFAULT 'public' NOT NULL CHECK (status in ('public', 'private')), 
    location VARCHAR(255) NOT NULL,

    FOREIGN KEY (stream_id) REFERENCES stream(id) ON DELETE CASCADE
); 

CREATE TABLE subscriber(
    partner_account_id INTEGER NOT NULL, 
    subscriber_account_id INTEGER, 
    tier CHARACTER(1) DEFAULT 1 NOT NULL CHECK (tier BETWEEN '1' AND '3'),
    subscriber_start DATE DEFAULT CURRENT_DATE NOT NULL,
    subscriber_end DATE NOT NULL,
    month_count INTEGER DEFAULT 1 NOT NULL,

    PRIMARY KEY(partner_account_id, subscriber_account_id),
    FOREIGN KEY (partner_account_id) REFERENCES partner(accounts_id) ON DELETE CASCADE,
    FOREIGN KEY (subscriber_account_id) REFERENCES accounts(id) ON DELETE SET NULL
);

CREATE TABLE follower(
    following_account_id INTEGER NOT NULL, 
    follower_account_id INTEGER NOT NULL, 
    start_following DATE DEFAULT CURRENT_DATE NOT NULL,

    PRIMARY KEY(following_account_id, follower_account_id),
    FOREIGN KEY(following_account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY(follower_account_id) REFERENCES accounts(id) ON DELETE CASCADE
); 

CREATE TABLE chat(
    id INTEGER NOT NULL PRIMARY KEY,
    streamer_account_id INTEGER NOT NULL,
    chatter_account_id INTEGER NOT NULL, 
    message_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, 
    chat_message VARCHAR(500) NOT NULL,

    FOREIGN KEY(streamer_account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY(chatter_account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

INSERT INTO accounts(id,username,passwort,mail, streamkey) values(1, 'Ammil', '23374288941621235993d3e474c08940bae7e13d7e07c3aaa7a077ab324b7cbd', 'ammil@gmail.com', 'live_1_r3A6lNq3nOUeINuEiuDT1LLyOqwbdU');
INSERT INTO accounts(id,username,passwort,mail, moderatorId) values(2, 'rearwindow', '39ae60dcfe2fe86e97c76b9c3aa4615fc9449a6f03ee6f3707b63b23e6e73f8c', 'rearwindow@twitch.com', 1);
INSERT INTO accounts(id,username,passwort,mail, moderatorId) values(3, 'batmanbegins', '161fe540610768de76dba6721eaab2a40a2b68ab394297afbb31f4e29e91add2', 'batman@live.com', 1);
INSERT INTO accounts(id,username,passwort,mail, streamkey, moderatorId) values(4, 'rye', '277d0488076f8ffdfd38a2e6a7a496c395bf0d7641a6681d27c7e39cb931b7e5', 'rye@gmx.com', 'live_4_VHS0YSVBETLAYLGvZ8DVGLB9jS0Z6a', 1);
INSERT INTO accounts(id,username,passwort,mail, streamkey, moderatorId) values(5, 'dasboot', 'a27cd870b15cc0747b32f1b4fba1186bb37a10d37646158f8261574417e18600', 'dasboot@boot.com', 'live_5_okWgC1FW2gH5AFIM1eW1yAhKqg01tS', 1);
INSERT INTO accounts(id,username,passwort,mail, streamkey) values(6, 'ragingbull', '25df79516e20707848bc75d6fd328bff4958ba712b154f6a626d4d26301ee511', 'ragingbull@outlook.de', 'live_6_LYyeeMlUF00ofyK8Lyv9Z3IIS5tk2j');
INSERT INTO accounts(id,username,passwort,mail) values(7, 'marram', '06e7a0a924901e4e9d6a53b22d4ce6fc4630a575c71ce259cb95e1a3e9027f07', 'marram@outlook.de');
INSERT INTO accounts(id,username,passwort,mail) values(8, 'sevensamurai', 'caef8b1b3f6622f9cfacace8102145e64603af3f3050d73be25c58808117a17d', 'sevensamurai@outlook.de');
INSERT INTO accounts(id,username,passwort,mail) values(9, 'ran', '825216a6bed848e9099122f5170c40dc7c30c64347412deeef4c6dc13f15fe52', 'ran@nice.com');
INSERT INTO accounts(id,username,passwort,mail) values(10, 'pool', 'b7489d12676f802ea87be57829f38dc8c4f1b974a4c6854fc0e38d78210656a8', 'pool@yahoo.com');

INSERT INTO partner(accounts_id, partner_macro) values(1, 'ami');
INSERT INTO partner(accounts_id, partner_macro) values(5, 'boo');
INSERT INTO partner(accounts_id, partner_macro) values(4, 'rye');

INSERT INTO stream(id, streamer_id, startdate, stopdate) values(1, 1, '23.11.2021 17:30:00', '23.11.2021 20:00:00');
INSERT INTO stream(id, streamer_id, startdate, stopdate) values(2, 5, '21.11.2021 16:00:00', '21.11.2021 18:23:11');
INSERT INTO stream(id, streamer_id, startdate, stopdate) values(3, 4, '14.11.2021 08:20:14', '14.11.2021 13:21:10');
INSERT INTO stream(id, streamer_id, startdate, stopdate) values(4, 6, '23.10.2021 14:00:02', '23.10.2021 15:22:42');
INSERT INTO stream(id, streamer_id) values(5,1);

INSERT INTO vod(id, stream_id, clicks, status, location) values(1, 1, 52, 'public', 'D:\TKKG_Streaming\VODs\1\1_ammil_stream.mp4');
INSERT INTO vod(id, stream_id, clicks, status, location) values(2, 2, 130, 'public', 'D:\TKKG_Streaming\VODs\5\5_dasboot_stream.mp4');
INSERT INTO vod(id, stream_id, clicks, status, location) values(3, 3, 1337, 'public', 'D:\TKKG_Streaming\VODs\4\4_rye_stream.mp4');
INSERT INTO vod(id, stream_id, clicks, status, location) values(4, 4, 69, 'private', 'D:\TKKG_Streaming\VODs\6\6_ragingbull_stream.mp4');

INSERT INTO subscriber(partner_account_id, subscriber_account_id, tier, subscriber_start,subscriber_end, month_count) values(1,9,1,'10.09.2021','10.12.2021', 3);
INSERT INTO subscriber(partner_account_id, subscriber_account_id, tier, subscriber_start,subscriber_end, month_count) values(1,10,3,'04.06.2021','04.12.2021', 6);
INSERT INTO subscriber(partner_account_id, subscriber_account_id, tier, subscriber_start,subscriber_end, month_count) values(4,9,1,'11.11.2021','11.12.2021', 3);

INSERT INTO follower(following_account_id, follower_account_id) values(1,10);
INSERT INTO follower(following_account_id, follower_account_id) values(1,9);
INSERT INTO follower(following_account_id, follower_account_id) values(1,2);
INSERT INTO follower(following_account_id, follower_account_id) values(1,3);
INSERT INTO follower(following_account_id, follower_account_id) values(4,9);
INSERT INTO follower(following_account_id, follower_account_id) values(4,8);
INSERT INTO follower(following_account_id, follower_account_id) values(4,3);
INSERT INTO follower(following_account_id, follower_account_id) values(5,2);
INSERT INTO follower(following_account_id, follower_account_id) values(5,9);
INSERT INTO follower(following_account_id, follower_account_id) values(5,1);

INSERT INTO chat(id, streamer_account_id, chatter_account_id, chat_message) values(1, 1, 9, 'Hallo Ammil! :)');
INSERT INTO chat(id, streamer_account_id, chatter_account_id, chat_message) values(2, 1, 1, 'Moin, ran! Geht gleich los :D');
INSERT INTO chat(id, streamer_account_id, chatter_account_id, chat_message) values(3, 1, 9, 'Cool, freue mich schon!');
INSERT INTO chat(id, streamer_account_id, chatter_account_id, chat_message) values(4, 4, 9, 'Moin rye!');
INSERT INTO chat(id, streamer_account_id, chatter_account_id, chat_message) values(5, 4, 1, 'Hi ran, du auch hier? xD');
INSERT INTO chat(id, streamer_account_id, chatter_account_id, chat_message) values(6, 4, 9, 'Jo');
INSERT INTO chat(id, streamer_account_id, chatter_account_id, chat_message) values(7, 5, 10, 'Wann gehts los? xD');
INSERT INTO chat(id, streamer_account_id, chatter_account_id, chat_message) values(8, 5, 7, 'Hoffe bald, warte schon 10 minuten');
INSERT INTO chat(id, streamer_account_id, chatter_account_id, chat_message) values(9, 5, 6, 'Endlich gehts los xD');
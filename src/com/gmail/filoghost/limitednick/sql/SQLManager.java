/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gmail.filoghost.limitednick.sql;

import java.sql.SQLException;

import wild.api.mysql.MySQL;
import wild.api.mysql.SQLResult;
import lombok.Cleanup;

public class SQLManager {
	
	private static MySQL mysql;

	public static void connect(String host, int port, String database, String user, String pass) throws SQLException, ClassNotFoundException {
		mysql = new MySQL(host, port, database, user, pass);
		mysql.connect();
	}
	
	public void close() {
		mysql.close();
	}
	
	public static void createTable() throws SQLException {
		mysql.update("CREATE TABLE IF NOT EXISTS " + SQLColumns.TABLE + " ("
				+ SQLColumns.NAME + " varchar(20) NOT NULL, "
				+ SQLColumns.NICK + " varchar(100) NOT NULL, "
				+ " PRIMARY KEY (" + SQLColumns.NAME + ")"
				+ ") ENGINE = InnoDB DEFAULT CHARSET = UTF8;");
	}
	
	public static boolean playerExists(String playerName) throws SQLException {
		@Cleanup SQLResult result = mysql.preparedQuery("SELECT null FROM " + SQLColumns.TABLE + " WHERE " + SQLColumns.NAME + " = ?;", playerName);
		
		return result.next();
	}
	
	public static String getNick(String playerName) throws SQLException {
		@Cleanup SQLResult result = mysql.preparedQuery("SELECT * FROM " + SQLColumns.TABLE + " WHERE " + SQLColumns.NAME + " = ?;", playerName);
		
		if (result.next()) {
			return result.getString(SQLColumns.NICK);
		} else {
			return null;
		}
	}
	
	public static void setNick(String playerName, String nick) throws SQLException {
		mysql.preparedUpdate("INSERT INTO " + SQLColumns.TABLE + " (" + SQLColumns.NAME + ", " + SQLColumns.NICK + ") VALUES (?, ?) "
				+ " ON DUPLICATE KEY UPDATE " + SQLColumns.NICK + " = VALUES(" + SQLColumns.NICK + ");",
				playerName,	nick);
	}
	
	public static void removeNick(String playerName) throws SQLException {
		mysql.preparedUpdate("DELETE FROM " + SQLColumns.TABLE + " WHERE " + SQLColumns.NAME + " = ?;", playerName);
	}

	public static void checkConnection() {
		mysql.isConnectionValid();
	}

}

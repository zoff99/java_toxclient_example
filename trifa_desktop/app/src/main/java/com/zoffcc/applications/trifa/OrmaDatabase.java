package com.zoffcc.applications.trifa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.SQLException;
import java.sql.Statement;

import static com.zoffcc.applications.trifa.MainActivity.sqldb;

public class OrmaDatabase
{
    private static final String TAG = "trifa.OrmaDatabase";

    public OrmaDatabase()
    {
    }

    /* escape to prevent SQL injection, very basic and bad! */
    public static String s(String str)
    {
        // TODO: bad!! use prepared statements
        String data = "";

        if (str == null || str.length() == 0)
        {
            return "";
        }

        if (str != null && str.length() > 0)
        {
            str = str.
                    // replace("\\", "\\\\"). // \ -> \\
                    // replace("%", "\\%"). // % -> \%
                    // replace("_", "\\_"). // _ -> \_
                            replace("'", "''"). // ' -> ''
                    replace("\\x1a", "\\Z");
            data = str;
        }
        return data;
    }

    public static String s(int i)
    {
        return "" + i;
    }

    public static String s(long l)
    {
        return "" + l;
    }

    public static int b(boolean in)
    {
        if (in == true)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public static String readSQLFileAsString(String filePath) throws java.io.IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line, results = "";
        while ((line = reader.readLine()) != null)
        {
            results += line;
        }
        reader.close();
        return results;
    }

    public static void create_db()
    {
        try
        {
            String asset_filename = "." + File.separator + "assets" + File.separator + "main.db.txt";
            String create_db_sqls = readSQLFileAsString(asset_filename);
            run_multi_sql(create_db_sqls);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public static void run_multi_sql(String sql_multi)
    {
        try
        {
            Statement statement = null;

            try
            {
                statement = sqldb.createStatement();
                statement.setQueryTimeout(10);  // set timeout to x sec.
            }
            catch (SQLException e)
            {
                System.err.println(e.getMessage());
            }

            String[] queries = sql_multi.split(";");
            for (String query : queries)
            {
                try
                {
                    statement.executeUpdate(query);
                }
                catch (SQLException e)
                {
                    System.err.println(e.getMessage());
                }
            }
        }
        catch (Exception e)
        {
        }
    }

    /**
     * Starts building a query: {@code SELECT * FROM FriendList ...}.
     */
    public FriendList selectFromFriendList()
    {
        FriendList ret = new FriendList();
        ret.sql_start = "SELECT * FROM FriendList";
        return ret;
    }

    public long insertIntoFriendList(FriendList f)
    {
        return f.insert();
    }

    /**
     * Starts building a query: {@code SELECT * FROM Message ...}.
     */
    public Message selectFromMessage()
    {
        Message ret = new Message();
        ret.sql_start = "SELECT * FROM Message";
        return ret;
    }

    /**
     * Starts building a query: {@code UPDATE Message ...}.
     */
    public Message updateMessage()
    {
        Message ret = new Message();
        ret.sql_start = "UPDATE Message";
        return ret;
    }

    /**
     * Starts building a query: {@code SELECT * FROM ConferenceDB ...}.
     */
    public ConferenceDB selectFromConferenceDB()
    {
        ConferenceDB ret = new ConferenceDB();
        ret.sql_start = "SELECT * FROM ConferenceDB";
        return ret;
    }

    /**
     * Starts building a query: {@code UPDATE ConferenceDB ...}.
     */
    public ConferenceDB updateConferenceDB()
    {
        ConferenceDB ret = new ConferenceDB();
        ret.sql_start = "UPDATE ConferenceDB";
        return ret;
    }

    public long insertIntoConferenceDB(ConferenceDB conf_new)
    {
        return conf_new.insert();
    }

    /**
     * Starts building a query: {@code UPDATE ConferenceMessage ...}.
     */
    public ConferenceMessage updateConferenceMessage()
    {
        ConferenceMessage ret = new ConferenceMessage();
        ret.sql_start = "UPDATE ConferenceMessage";
        return ret;
    }

    /**
     * Starts building a query: {@code SELECT * FROM ConferenceMessage ...}.
     */
    public ConferenceMessage selectFromConferenceMessage()
    {
        ConferenceMessage ret = new ConferenceMessage();
        ret.sql_start = "SELECT * FROM ConferenceMessage";
        return ret;
    }

    public long insertIntoConferenceMessage(ConferenceMessage m)
    {
        return m.insert();
    }

    /**
     * Starts building a query: {@code UPDATE FriendList ...}.
     */
    public FriendList updateFriendList()
    {
        FriendList ret = new FriendList();
        ret.sql_start = "UPDATE FriendList";
        return ret;
    }

    /**
     * Starts building a query: {@code SELECT * FROM Filetransfer ...}.
     */
    public Filetransfer selectFromFiletransfer()
    {
        Filetransfer ret = new Filetransfer();
        ret.sql_start = "SELECT * FROM Filetransfer";
        return ret;
    }

    public long insertIntoFiletransfer(Filetransfer f)
    {
        return f.insert();
    }

    /**
     * Starts building a query: {@code UPDATE Filetransfer ...}.
     */
    public Filetransfer updateFiletransfer()
    {
        Filetransfer ret = new Filetransfer();
        ret.sql_start = "UPDATE Filetransfer";
        return ret;
    }

    public long insertIntoFileDB(FileDB f)
    {
        return f.insert();
    }

    /**
     * Starts building a query: {@code SELECT * FROM FileDB ...}.
     */
    public FileDB selectFromFileDB()
    {
        FileDB ret = new FileDB();
        ret.sql_start = "SELECT * FROM FileDB";
        return ret;
    }

    public Filetransfer deleteFromFiletransfer()
    {
        Filetransfer ret = new Filetransfer();
        ret.sql_start = "DELETE FROM Filetransfer";
        return ret;
    }

    public long insertIntoMessage(Message m)
    {
        return m.insert();
    }

    /**
     * Starts building a query: {@code SELECT * FROM RelayListDB ...}.
     */
    public RelayListDB selectFromRelayListDB()
    {
        RelayListDB ret = new RelayListDB();
        ret.sql_start = "SELECT * FROM RelayListDB";
        return ret;
    }

    public long insertIntoRelayListDB(RelayListDB f)
    {
        return f.insert();
    }
}


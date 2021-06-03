package com.zoffcc.applications.trifa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.zoffcc.applications.trifa.HelperOSFile.sha256sum_of_file;
import static com.zoffcc.applications.trifa.MainActivity.sqldb;

public class OrmaDatabase
{
    private static final String TAG = "trifa.OrmaDatabase";

    private static final String CREATE_DB_FILE_SHA256SUM = "GE/avgqgDL4L1v35QvL2DIXdFMVOVKm8Ic8hG7v1BeA=";

    public OrmaDatabase()
    {
    }

    /*
     * escape to prevent SQL injection, very basic and bad!
     * TODO: make me better (and later use prepared statements)
     */
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

    public static int get_current_db_version()
    {
        int ret = 0;

        try
        {
            Statement statement = sqldb.createStatement();
            ResultSet rs = statement.executeQuery(
                    "select db_version from orma_schema order by db_version desc limit 1");
            if (rs.next())
            {
                ret = rs.getInt("db_version");
            }

            return ret;
        }
        catch (Exception e)
        {
            ret = 0;

            try
            {
                final String update_001 = "CREATE TABLE orma_schema (db_version INTEGER NOT NULL);";
                run_multi_sql(update_001);
                final String update_002 = "insert into orma_schema values ('0');";
                run_multi_sql(update_002);
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }
        }

        return ret;
    }

    public static void set_new_db_version(int new_version)
    {
        try
        {
            final String update_001 = "update orma_schema set db_version='" + new_version + "';";
            run_multi_sql(update_001);
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }
    }

    public static int update_db(int current_db_version)
    {
        if (current_db_version < 1)
        {
            try
            {
                final String update_001 = "CREATE UNIQUE INDEX ux_tox_public_key_string_of_owner ON RelayListDB(tox_public_key_string_of_owner);";
                run_multi_sql(update_001);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (current_db_version < 2)
        {
            try
            {
                final String update_001 =
                        "alter table Message add ft_outgoing_queued BOOLEAN NOT NULL DEFAULT false;" + "\n" +
                        "CREATE INDEX index_ft_outgoing_queued_on_Message ON Message (ft_outgoing_queued);";
                run_multi_sql(update_001);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (current_db_version < 3)
        {
            try
            {
                final String update_001 =
                        "alter table Message add msg_at_relay BOOLEAN NOT NULL DEFAULT false;" + "\n" +
                        "CREATE INDEX index_msg_at_relay_on_Message ON Message (msg_at_relay);";
                run_multi_sql(update_001);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        final int new_db_version = 3;
        set_new_db_version(new_db_version);
        // return the updated DB VERSION
        return new_db_version;
    }

    public static void create_db()
    {
        try
        {
            String asset_filename = "." + File.separator + "assets" + File.separator + "main.db.txt";
            String sha256sum_of_create_db_file = sha256sum_of_file(asset_filename);
            Log.i(TAG, "create_db:sha256sum_of_create_db_file=" + sha256sum_of_create_db_file);
            // TODO: on some windows systems the checksum does not seem to match?
            // maybe "\r\n" or the file is not read as UTF-8 ?
            if ((sha256sum_of_create_db_file.equals(CREATE_DB_FILE_SHA256SUM)) ||
                (OperatingSystem.getCurrent() == OperatingSystem.WINDOWS))
            {
                String create_db_sqls = readSQLFileAsString(asset_filename);
                run_multi_sql(create_db_sqls);
            }
            else
            {
                Log.i(TAG, "create_db:input file sha256 hash does not match!");
                System.exit(5);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /*
     * Runs SQL statements that are seperated by ";" character
     */
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

    public long insertIntoTRIFADatabaseGlobalsNew(TRIFADatabaseGlobalsNew o)
    {
        return o.insert();
    }

    /**
     * Starts building a query: {@code UPDATE TRIFADatabaseGlobalsNew ...}.
     */
    public TRIFADatabaseGlobalsNew updateTRIFADatabaseGlobalsNew()
    {
        TRIFADatabaseGlobalsNew ret = new TRIFADatabaseGlobalsNew();
        ret.sql_start = "UPDATE TRIFADatabaseGlobalsNew";
        return ret;
    }

    public TRIFADatabaseGlobalsNew deleteFromTRIFADatabaseGlobalsNew()
    {
        TRIFADatabaseGlobalsNew ret = new TRIFADatabaseGlobalsNew();
        ret.sql_start = "DELETE FROM TRIFADatabaseGlobalsNew";
        return ret;
    }

    /**
     * Starts building a query: {@code SELECT * FROM TRIFADatabaseGlobalsNew ...}.
     */
    public TRIFADatabaseGlobalsNew selectFromTRIFADatabaseGlobalsNew()
    {
        TRIFADatabaseGlobalsNew ret = new TRIFADatabaseGlobalsNew();
        ret.sql_start = "SELECT * FROM TRIFADatabaseGlobalsNew";
        return ret;
    }
}


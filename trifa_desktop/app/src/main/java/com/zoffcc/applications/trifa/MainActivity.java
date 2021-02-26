/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 - 2021 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.trifa;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import static com.zoffcc.applications.trifa.HelperFriend.main_get_friend;
import static com.zoffcc.applications.trifa.TRIFAGlobals.bootstrapping;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;

public class MainActivity extends JFrame
{
    private static final String TAG = "trifa.MainActivity";
    private static final String Version = "1.0.2";
    // --------- global config ---------
    // --------- global config ---------
    // --------- global config ---------
    final static boolean CTOXCORE_NATIVE_LOGGING = true; // set "false" for release builds
    final static boolean ORMA_TRACE = false; // set "false" for release builds
    final static boolean DB_ENCRYPT = true; // set "true" always!
    final static boolean VFS_ENCRYPT = true; // set "true" always!
    // --------- global config ---------
    // --------- global config ---------
    // --------- global config ---------

    static TrifaToxService tox_service_fg = null;
    static boolean native_lib_loaded = false;
    static long[] friends = null;
    static String app_files_directory = "./";
    static String password_hash = "pass";
    static Semaphore semaphore_tox_savedata = new Semaphore(1);

    static Connection sqldb = null;

    static JFrame MainFrame = null;

    static JSplitPane splitPane;
    static FriendListFragmentJ FriendPanel;
    static JPanel MessagePanel;
    static JScrollPane MessageScrollPane;
    static JTextPane MessageTextArea;
    static JPanel MessageTextInputPanel;
    static JTextArea sendTextField;
    static JButton sendButton;
    static Style blueStyle;
    static Style redStyle;
    static Style defaultStyle;

    // ---- lookup cache ----
    static Map<String, Long> cache_pubkey_fnum = new HashMap<String, Long>();
    static Map<Long, String> cache_fnum_pubkey = new HashMap<Long, String>();
    static Map<String, String> cache_peernum_pubkey = new HashMap<String, String>();
    // static Map<String, String> cache_peername_pubkey = new HashMap<String, String>();
    static Map<String, String> cache_peername_pubkey2 = new HashMap<String, String>();
    static Map<String, Long> cache_confid_confnum = new HashMap<String, Long>();
    // ---- lookup cache ----

    // ---- lookup cache for conference drawer ----
    static Map<String, Long> lookup_peer_listnum_pubkey = new HashMap<String, Long>();
    // ---- lookup cache for conference drawer ----

    static class Log
    {
        public static void i(String tag, String message)
        {
            message = message.replace("\r", "").replace("\n", "");
            System.out.println("" + tag + ":" + message + "");
        }
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
            str = str.replace("\\", "\\\\");
            str = str.replace("'", "\\'");
            str = str.replace("\0", "\\0");
            str = str.replace("\n", "\\n");
            str = str.replace("\r", "\\r");
            str = str.replace("\"", "\\\"");
            str = str.replace("\\x1a", "\\Z");
            data = str;
        }
        return data;
    }

    public static void add_message(String datetime, String username, String message)
    {
        MessageTextArea.setSelectionStart(MessageTextArea.getText().length());
        MessageTextArea.setSelectionEnd(MessageTextArea.getText().length());
        MessageTextArea.setCharacterAttributes(blueStyle, true);
        MessageTextArea.replaceSelection(datetime);

        MessageTextArea.setSelectionStart(MessageTextArea.getText().length());
        MessageTextArea.setSelectionEnd(MessageTextArea.getText().length());
        MessageTextArea.setCharacterAttributes(defaultStyle, true);
        MessageTextArea.replaceSelection(":");

        MessageTextArea.setSelectionStart(MessageTextArea.getText().length());
        MessageTextArea.setSelectionEnd(MessageTextArea.getText().length());
        MessageTextArea.setCharacterAttributes(redStyle, true);
        MessageTextArea.replaceSelection(username);

        MessageTextArea.setSelectionStart(MessageTextArea.getText().length());
        MessageTextArea.setSelectionEnd(MessageTextArea.getText().length());
        MessageTextArea.setCharacterAttributes(defaultStyle, true);
        MessageTextArea.replaceSelection(":" + message + "\n");
    }

    public MainActivity()
    {
        super("TRIfA - Desktop - " + Version + "   ");
        initComponents();
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MainFrame = this;

        splitPane = new JSplitPane();

        FriendPanel = new FriendListFragmentJ();
        MessagePanel = new JPanel();
        MessageScrollPane = new JScrollPane();

        // ------------------
        // ------------------
        // ------------------
        StyleContext sc = new StyleContext();
        final DefaultStyledDocument doc = new DefaultStyledDocument(sc);

        // Create and add the main document style
        defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        final Style mainStyle = sc.addStyle("MainStyle", defaultStyle);
        StyleConstants.setFontFamily(mainStyle, "monospaced");
        StyleConstants.setFontSize(mainStyle, 12);

        // Create and add the constant width style
        redStyle = sc.addStyle("ConstantWidthRed", null);
        StyleConstants.setFontFamily(redStyle, "monospaced");
        StyleConstants.setFontSize(redStyle, 12);
        StyleConstants.setForeground(redStyle, Color.red);

        // Create and add the constant width style
        blueStyle = sc.addStyle("ConstantWidthBlue", null);
        StyleConstants.setFontFamily(blueStyle, "monospaced");
        StyleConstants.setFontSize(blueStyle, 12);
        StyleConstants.setForeground(blueStyle, Color.blue);

        // Create and add the heading style
        /*
        final Style heading2Style = sc.addStyle("Heading2", null);
        StyleConstants.setForeground(heading2Style, Color.red);
        StyleConstants.setFontSize(heading2Style, 16);
        StyleConstants.setFontFamily(heading2Style, "serif");
        StyleConstants.setBold(heading2Style, true);
        StyleConstants.setLeftIndent(heading2Style, 8);
        StyleConstants.setFirstLineIndent(heading2Style, 0);
        */
        // ------------------
        // ------------------
        // ------------------

        MessageTextArea = new JTextPane(doc);

        MessageTextInputPanel = new JPanel();
        sendTextField = new JTextArea();
        sendButton = new JButton("send");

        getContentPane().setLayout(new GridLayout());
        getContentPane().add(splitPane);

        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(80);
        splitPane.setLeftComponent(FriendPanel);
        splitPane.setRightComponent(MessagePanel);

        MessagePanel.setLayout(new BoxLayout(MessagePanel, BoxLayout.Y_AXIS));
        MessagePanel.add(MessageScrollPane);
        MessageScrollPane.setViewportView(MessageTextArea);
        // MessageTextArea.setEditable(false);
        MessagePanel.add(MessageTextInputPanel);

        MessageTextInputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        MessageTextInputPanel.setLayout(new BoxLayout(MessageTextInputPanel, BoxLayout.X_AXIS));

        MessageTextInputPanel.add(sendTextField);
        sendTextField.setEditable(true);
        MessageTextInputPanel.add(sendButton);


        doc.setLogicalStyle(0, mainStyle);
        try
        {
            doc.insertString(0, "", null);
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }

        add_message("2021-02-02 15:30", "user1", "mesafoejwr jw3r3 krk3rk32ißrk2kß0 ßk0k0rß3irß03 kßrß03r kß0");
        add_message("2021-02-02 15:30", "user1", "mesafoejwr jw3r3 krk3rk32ißrk2kß0 ßk0k0rß3irß03 kßrß03r kß0");
        add_message("2021-02-02 15:30", "user1", "mesafoejwr jw3r3 krk3rk32ißrk2kß0 ßk0k0rß3irß03 kßrß03r kß0");
        add_message("2021-02-02 15:30", "user1", "mesafoejwr jw3r3 krk3rk32ißrk2kß0 ßk0k0rß3irß03 kßrß03r kß0");
        add_message("2021-02-02 15:30", "user1", "mesafoejwr jw3r3 krk3rk32ißrk2kß0 ßk0k0rß3irß03 kßrß03r kß0");
        add_message("2021-02-02 15:30", "user1", "mesafoejwr jw3r3 krk3rk32ißrk2kß0 ßk0k0rß3irß03 kßrß03r kß0");
        add_message("2021-02-02 15:30", "user1", "mesafoejwr jw3r3 krk3rk32ißrk2kß0 ßk0k0rß3irß03 kßrß03r kß0");
        add_message("2021-02-02 15:30", "user1", "mesafoejwr jw3r3 krk3rk32ißrk2kß0 ßk0k0rß3irß03 kßrß03r kß0");
        add_message("2021-02-02 15:30", "user1", "mesafoejwr jw3r3 krk3rk32ißrk2kß0 ßk0k0rß3irß03 kßrß03r kß0");
        add_message("2021-02-02 15:30", "user1", "mesafoejwr jw3r3 krk3rk32ißrk2kß0 ßk0k0rß3irß03 kßrß03r kß0");
        add_message("2021-02-02 15:30", "user1", "mesafoejwr jw3r3 krk3rk32ißrk2kß0 ßk0k0rß3irß03 kßrß03r kß0");
        add_message("2021-02-02 15:30", "user1", "mesafoejwr jw3r3 krk3rk32ißrk2kß0 ßk0k0rß3irß03 kßrß03r kß0");
        add_message("2021-02-02 15:30", "user1", "mesafoejwr jw3r3 krk3rk32ißrk2kß0 ßk0k0rß3irß03 kßrß03r kß0");
    }

    private void initComponents()
    {
        setLayout(new FlowLayout());
    }

    public static void main(String[] args)
    {
        System.out.println("Version:" + Version);

        TrifaToxService.TOX_SERVICE_STARTED = false;
        bootstrapping = false;
        Log.i(TAG, "java.library.path:" + System.getProperty("java.library.path"));

        Log.i(TAG, "loaded:c-toxcore:v" + tox_version_major() + "." + tox_version_minor() + "." + tox_version_patch());
        Log.i(TAG, "loaded:jni-c-toxcore:v" + jnictoxcore_version());


        // create a database connection
        try
        {
            // Class.forName("org.sqlite.JDBC");
            sqldb = DriverManager.getConnection("jdbc:sqlite:main.db");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            Statement statement = sqldb.createStatement();
            statement.setQueryTimeout(10);  // set timeout to 30 sec.

            statement.executeUpdate(
                    "create table TRIFADatabaseGlobalsNew (" + "key string NOT NULL PRIMARY KEY," + "value string" +
                    ")");

            statement.executeUpdate(
                    "create table FriendList (" + "tox_public_key_string string NOT NULL PRIMARY KEY , " +
                    "name string," + "alias_name string," + "status_message string," + "TOX_CONNECTION integer," +
                    "TOX_CONNECTION_real integer," + "TOX_CONNECTION_on_off integer," + "TOX_USER_STATUS integer," +
                    "avatar_pathname string," + "avatar_filename string," + "avatar_update integer," +
                    "avatar_update_timestamp integer," + "notification_silent integer," + "sort integer," +
                    "last_online_timestamp integer," + "last_online_timestamp_real integer," +
                    "added_timestamp integer," + "is_relay integer )");
        }
        catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }

        tox_service_fg = new TrifaToxService();

        if (!TrifaToxService.TOX_SERVICE_STARTED)
        {
            int PREF__udp_enabled = 1;
            int PREF__orbot_enabled_to_int = 0;
            String ORBOT_PROXY_HOST = "";
            long ORBOT_PROXY_PORT = 0;
            int PREF__local_discovery_enabled = 1;
            int PREF__ipv6_enabled = 1;
            int PREF__force_udp_only = 0;

            app_files_directory = "./";

            init(app_files_directory, PREF__udp_enabled, PREF__local_discovery_enabled, PREF__orbot_enabled_to_int,
                 ORBOT_PROXY_HOST, ORBOT_PROXY_PORT, password_hash, PREF__ipv6_enabled, PREF__force_udp_only);
            tox_service_fg.tox_thread_start_fg();
        }

        String my_tox_id_temp = get_my_toxid();
        Log.i(TAG, "MyToxID:" + my_tox_id_temp);

        new MainActivity().setVisible(true);
    }

    static
    {
        try
        {
            System.loadLibrary("jni-c-toxcore");
            native_lib_loaded = true;
            Log.i(TAG, "successfully loaded native library");
        }
        catch (java.lang.UnsatisfiedLinkError e)
        {
            native_lib_loaded = false;
            Log.i(TAG, "loadLibrary jni-c-toxcore failed!");
            e.printStackTrace();
        }
    }

    // -------- native methods --------
    // -------- native methods --------
    // -------- native methods --------
    public static native void init(String data_dir, int udp_enabled, int local_discovery_enabled, int orbot_enabled, String orbot_host, long orbot_port, String tox_encrypt_passphrase_hash, int enable_ipv6, int force_udp_only_mode);

    public static native void update_savedata_file(String tox_encrypt_passphrase_hash);

    public static native String get_my_toxid();

    public static native void bootstrap();

    public static native int add_tcp_relay_single(String ip, String key_hex, long port);

    public static native int bootstrap_single(String ip, String key_hex, long port);

    public static native void init_tox_callbacks();

    public static native long tox_iteration_interval();

    public static native long tox_iterate();

    public static native long tox_kill();

    public static native void exit();

    public static native long tox_friend_send_message(long friendnum, int a_TOX_MESSAGE_TYPE, String message);

    public static native long tox_version_major();

    public static native long tox_version_minor();

    public static native long tox_version_patch();

    public static native String jnictoxcore_version();

    public static native long tox_max_filename_length();

    public static native long tox_file_id_length();

    public static native long tox_max_message_length();

    public static native long tox_friend_add(String toxid_str, String message);

    public static native long tox_friend_add_norequest(String public_key_str);

    public static native long tox_self_get_friend_list_size();

    public static native void tox_self_set_nospam(long nospam); // this actually needs an "uint32_t" which is an unsigned 32bit integer value

    public static native long tox_self_get_nospam(); // this actually returns an "uint32_t" which is an unsigned 32bit integer value

    public static native long tox_friend_by_public_key(String friend_public_key_string);

    public static native String tox_friend_get_public_key(long friend_number);

    public static native long[] tox_self_get_friend_list();

    public static native int tox_self_set_name(String name);

    public static native int tox_self_set_status_message(String status_message);

    public static native void tox_self_set_status(int a_TOX_USER_STATUS);

    public static native int tox_self_set_typing(long friend_number, int typing);

    public static native int tox_friend_get_connection_status(long friend_number);

    public static native int tox_friend_delete(long friend_number);

    public static native String tox_self_get_name();

    public static native long tox_self_get_name_size();

    public static native long tox_self_get_status_message_size();

    public static native String tox_self_get_status_message();

    public static native int tox_file_control(long friend_number, long file_number, int a_TOX_FILE_CONTROL);

    public static native int tox_hash(java.nio.ByteBuffer hash_buffer, java.nio.ByteBuffer data_buffer, long data_length);

    public static native int tox_file_seek(long friend_number, long file_number, long position);

    public static native int tox_file_get_file_id(long friend_number, long file_number, java.
            nio.ByteBuffer file_id_buffer);

    public static native long tox_file_send(long friend_number, long kind, long file_size, java.
            nio.ByteBuffer file_id_buffer, String file_name, long filename_length);

    public static native int tox_file_send_chunk(long friend_number, long file_number, long position, java.
            nio.ByteBuffer data_buffer, long data_length);

    // --------------- Conference -------------
    // --------------- Conference -------------
    // --------------- Conference -------------

    public static native long tox_conference_join(long friend_number, java.nio.ByteBuffer cookie_buffer, long cookie_length);

    public static native String tox_conference_peer_get_public_key(long conference_number, long peer_number);

    public static native long tox_conference_peer_count(long conference_number);

    public static native long tox_conference_peer_get_name_size(long conference_number, long peer_number);

    public static native String tox_conference_peer_get_name(long conference_number, long peer_number);

    public static native int tox_conference_peer_number_is_ours(long conference_number, long peer_number);

    public static native long tox_conference_get_title_size(long conference_number);

    public static native String tox_conference_get_title(long conference_number);

    public static native int tox_conference_get_type(long conference_number);

    public static native int tox_conference_send_message(long conference_number, int a_TOX_MESSAGE_TYPE, String message);

    public static native int tox_conference_delete(long conference_number);
    // --------------- Conference -------------
    // --------------- Conference -------------
    // --------------- Conference -------------


    // --------------- AV -------------
    // --------------- AV -------------
    // --------------- AV -------------
    public static native int toxav_answer(long friendnum, long audio_bit_rate, long video_bit_rate);

    public static native long toxav_iteration_interval();

    public static native int toxav_call(long friendnum, long audio_bit_rate, long video_bit_rate);

    public static native int toxav_bit_rate_set(long friendnum, long audio_bit_rate, long video_bit_rate);

    public static native int toxav_call_control(long friendnum, int a_TOXAV_CALL_CONTROL);

    public static native int toxav_video_send_frame_uv_reversed(long friendnum, int frame_width_px, int frame_height_px);

    public static native int toxav_video_send_frame(long friendnum, int frame_width_px, int frame_height_px);

    public static native long set_JNI_video_buffer(java.nio.ByteBuffer buffer, int frame_width_px, int frame_height_px);

    public static native void set_JNI_video_buffer2(java.nio.ByteBuffer buffer2, int frame_width_px, int frame_height_px);

    public static native void set_JNI_audio_buffer(java.nio.ByteBuffer audio_buffer);

    // buffer2 is for incoming audio
    public static native void set_JNI_audio_buffer2(java.nio.ByteBuffer audio_buffer2);

    /**
     * Send an audio frame to a friend.
     * <p>
     * The expected format of the PCM data is: [s1c1][s1c2][...][s2c1][s2c2][...]...
     * Meaning: sample 1 for channel 1, sample 1 for channel 2, ...
     * For mono audio, this has no meaning, every sample is subsequent. For stereo,
     * this means the expected format is LRLRLR... with samples for left and right
     * alternating.
     *
     * @param friend_number The friend number of the friend to which to send an
     *                      audio frame.
     * @param sample_count  Number of samples in this frame. Valid numbers here are
     *                      ((sample rate) * (audio length) / 1000), where audio length can be
     *                      2.5, 5, 10, 20, 40 or 60 millseconds.
     * @param channels      Number of audio channels. Supported values are 1 and 2.
     * @param sampling_rate Audio sampling rate used in this frame. Valid sampling
     *                      rates are 8000, 12000, 16000, 24000, or 48000.
     */
    public static native int toxav_audio_send_frame(long friend_number, long sample_count, int channels, long sampling_rate);
    // --------------- AV -------------
    // --------------- AV -------------
    // --------------- AV -------------

    // -------- native methods --------
    // -------- native methods --------
    // -------- native methods --------

    // -------- called by AV native methods --------
    // -------- called by AV native methods --------
    // -------- called by AV native methods --------

    static void android_toxav_callback_call_cb_method(long friend_number, int audio_enabled, int video_enabled)
    {
    }

    static void android_toxav_callback_video_receive_frame_cb_method(long friend_number, long frame_width_px, long frame_height_px, long ystride, long ustride, long vstride)
    {
    }

    static void android_toxav_callback_call_state_cb_method(long friend_number, int a_TOXAV_FRIEND_CALL_STATE)
    {
    }

    static void android_toxav_callback_bit_rate_status_cb_method(long friend_number, long audio_bit_rate, long video_bit_rate)
    {
    }

    static void android_toxav_callback_audio_receive_frame_cb_method(long friend_number, long sample_count, int channels, long sampling_rate)
    {
    }

    static void android_toxav_callback_video_receive_frame_pts_cb_method(long friend_number, long frame_width_px, long frame_height_px, long ystride, long ustride, long vstride, long pts)
    {
    }

    static void android_toxav_callback_video_receive_frame_h264_cb_method(long friend_number, long buf_size)
    {
    }

    static void android_toxav_callback_audio_receive_frame_pts_cb_method(long friend_number, long sample_count, int channels, long sampling_rate, long pts)
    {
    }

    static void android_toxav_callback_group_audio_receive_frame_cb_method(long conference_number, long peer_number, long sample_count, int channels, long sampling_rate)
    {
    }

    static void android_toxav_callback_call_comm_cb_method(long friend_number, long a_TOXAV_CALL_COMM_INFO, long comm_number)
    {
    }

    // -------- called by AV native methods --------
    // -------- called by AV native methods --------
    // -------- called by AV native methods --------


    // -------- called by native methods --------
    // -------- called by native methods --------
    // -------- called by native methods --------

    static void android_tox_callback_self_connection_status_cb_method(int a_TOX_CONNECTION)
    {
        Log.i(TAG, "self_connection_status:status:" + a_TOX_CONNECTION);
    }

    static void android_tox_callback_friend_name_cb_method(long friend_number, String friend_name, long length)
    {
        // Log.i(TAG, "friend_alias_name:friend:" + friend_number + " name:" + friend_alias_name);
        FriendList f = main_get_friend(friend_number);

        // Log.i(TAG, "friend_alias_name:002:" + f);
        if (f != null)
        {
            f.name = friend_name;
            HelperFriend.update_friend_in_db_name(f);
            HelperFriend.update_single_friend_in_friendlist_view(f);
        }
    }

    static void android_tox_callback_friend_status_message_cb_method(long friend_number, String status_message, long length)
    {
    }

    static void android_tox_callback_friend_lossless_packet_cb_method(long friend_number, byte[] data, long length)
    {
    }

    static void android_tox_callback_friend_status_cb_method(long friend_number, int a_TOX_USER_STATUS)
    {
    }

    static void android_tox_callback_friend_connection_status_cb_method(long friend_number, int a_TOX_CONNECTION)
    {
        Log.i(TAG, "friend_connection_status:friend:" + friend_number + " status:" + a_TOX_CONNECTION);
    }

    static void android_tox_callback_friend_typing_cb_method(long friend_number, final int typing)
    {
    }

    static void android_tox_callback_friend_read_receipt_cb_method(long friend_number, long message_id)
    {
    }

    static void android_tox_callback_friend_request_cb_method(String friend_public_key, String friend_request_message, long length)
    {
        Log.i(TAG, "friend_request:friend:" + friend_public_key + " friend request message:" + friend_request_message);
        Log.i(TAG, "friend_request:friend:" + friend_public_key.substring(0, TOX_PUBLIC_KEY_SIZE * 2) +
                   " friend request message:" + friend_request_message);

        final String friend_public_key__final = friend_public_key.substring(0, TOX_PUBLIC_KEY_SIZE * 2);
        long friendnum = tox_friend_add_norequest(friend_public_key__final);

        HelperGeneric.update_savedata_file_wrapper(MainActivity.password_hash);
    }

    static void android_tox_callback_friend_message_cb_method(long friend_number, int message_type, String friend_message, long length)
    {
        Log.i(TAG, "friend_message:friendnum:" + friend_number + " message:" + friend_message);
    }

    static void android_tox_callback_friend_message_v2_cb_method(long friend_number, String friend_message, long length, long ts_sec, long ts_ms, byte[] raw_message, long raw_message_length)
    {
        Log.i(TAG, "friend_message_v2:friendnum:" + friend_number + " message:" + friend_message);
    }

    static void android_tox_callback_friend_sync_message_v2_cb_method(long friend_number, long ts_sec, long ts_ms, byte[] raw_message, long raw_message_length, byte[] raw_data, long raw_data_length)
    {
    }

    static void android_tox_callback_friend_read_receipt_message_v2_cb_method(final long friend_number, long ts_sec, byte[] msg_id)
    {
    }

    static void android_tox_callback_file_recv_control_cb_method(long friend_number, long file_number, int a_TOX_FILE_CONTROL)
    {
    }

    static void android_tox_callback_file_chunk_request_cb_method(long friend_number, long file_number, long position, long length)
    {
    }

    static void android_tox_callback_file_recv_cb_method(long friend_number, long file_number, int a_TOX_FILE_KIND, long file_size, String filename, long filename_length)
    {
    }

    static void android_tox_callback_file_recv_chunk_cb_method(long friend_number, long file_number, long position, byte[] data, long length)
    {
    }

    static void android_tox_log_cb_method(int a_TOX_LOG_LEVEL, String file, long line, String function, String message)
    {
        if (CTOXCORE_NATIVE_LOGGING)
        {
            Log.i(TAG, "C-TOXCORE:" + ToxVars.TOX_LOG_LEVEL.value_str(a_TOX_LOG_LEVEL) + ":file=" + file + ":linenum=" +
                       line + ":func=" + function + ":msg=" + message);
        }
    }

    // -------- called by native methods --------
    // -------- called by native methods --------
    // -------- called by native methods --------

    // -------- called by native Conference methods --------
    // -------- called by native Conference methods --------
    // -------- called by native Conference methods --------

    static void android_tox_callback_conference_invite_cb_method(long friend_number, int a_TOX_CONFERENCE_TYPE, byte[] cookie_buffer, long cookie_length)
    {
    }

    static void android_tox_callback_conference_connected_cb_method(long conference_number)
    {
    }

    static void android_tox_callback_conference_message_cb_method(long conference_number, long peer_number, int a_TOX_MESSAGE_TYPE, String message, long length)
    {
    }

    static void android_tox_callback_conference_title_cb_method(long conference_number, long peer_number, String title, long title_length)
    {
    }

    static void android_tox_callback_conference_peer_name_cb_method(long conference_number, long peer_number, String name, long name_length)
    {
    }

    static void android_tox_callback_conference_peer_list_changed_cb_method(long conference_number)
    {
    }

    static void android_tox_callback_conference_namelist_change_cb_method(long conference_number, long peer_number, int a_TOX_CONFERENCE_STATE_CHANGE)
    {
    }

    // -------- called by native Conference methods --------
    // -------- called by native Conference methods --------
    // -------- called by native Conference methods --------

    static int add_tcp_relay_single_wrapper(String ip, long port, String key_hex)
    {
        return add_tcp_relay_single(ip, key_hex, port);
    }

    static int bootstrap_single_wrapper(String ip, long port, String key_hex)
    {
        return bootstrap_single(ip, key_hex, port);
    }
}


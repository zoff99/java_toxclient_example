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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import static com.zoffcc.applications.trifa.AudioBar.audio_vu;
import static com.zoffcc.applications.trifa.AudioFrame.set_audio_out_bar_level;
import static com.zoffcc.applications.trifa.AudioSelectInBox.AUDIO_VU_MIN_VALUE;
import static com.zoffcc.applications.trifa.HelperFriend.main_get_friend;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.TYPING_FLAG_DEACTIVATE_DELAY_IN_MILLIS;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.friendnum;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.global_typing;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.send_message_onclick;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.typing_flag_thread;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_CODEC_H264;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_CODEC_VP8;
import static com.zoffcc.applications.trifa.TRIFAGlobals.bootstrapping;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_for_battery_savings_ts;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_DECODER_CURRENT_BITRATE;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_DECODER_IN_USE_H264;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_DECODER_IN_USE_VP8;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_ENCODER_CURRENT_BITRATE;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_ENCODER_IN_USE_H264;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_ENCODER_IN_USE_VP8;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_NETWORK_ROUND_TRIP_MS;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_PLAY_BUFFER_ENTRIES;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_CALL_COMM_INFO.TOXAV_CALL_COMM_PLAY_DELAY;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_ACCEPTING_A;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_ACCEPTING_V;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_ERROR;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_FINISHED;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_NONE;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_SENDING_A;
import static com.zoffcc.applications.trifa.ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_SENDING_V;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;
import static com.zoffcc.applications.trifa.VideoInFrame.new_video_in_frame;
import static com.zoffcc.applications.trifa.VideoInFrame.on_call_ended_actions;
import static com.zoffcc.applications.trifa.VideoInFrame.on_call_started_actions;
import static com.zoffcc.applications.trifa.VideoInFrame.setup_video_in_resolution;
import static java.awt.Font.PLAIN;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;

public class MainActivity extends JFrame
{
    private static final String TAG = "trifa.MainActivity";
    static final String Version = "1.0.5";
    // --------- global config ---------
    // --------- global config ---------
    // --------- global config ---------
    final static boolean CTOXCORE_NATIVE_LOGGING = false; // set "false" for release builds
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
    static VideoInFrame VideoInFrame1 = null;
    static VideoOutFrame VideoOutFrame1 = null;
    static AudioFrame AudioFrame1 = null;

    static JSplitPane splitPane = null;
    static FriendListFragmentJ FriendPanel;
    static JPanel leftPanel = null;
    static MessageListFragmentJ MessagePanel;
    static JScrollPane MessageScrollPane;
    static JTextPane MessageTextArea;
    static JPanel MessageTextInputPanel;
    static JTextArea sendTextField;
    static JButton sendButton;
    static JTextField myToxID = null;
    static Style blueStyle;
    static Style blueSmallStyle;
    static Style redStyle;
    static Style mainStyle;
    static Style defaultStyle;
    static JTextArea ownProfileShort;

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

    static boolean PREF__X_battery_saving_mode = false;
    final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    static long update_all_messages_global_timestamp = -1;
    final static long UPDATE_MESSAGES_NORMAL_MILLIS = 500; // ~0.5 seconds
    final static SimpleDateFormat df_date_time_long = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    final static SimpleDateFormat df_date_only = new SimpleDateFormat("yyyy-MM-dd");
    static ResourceBundle lo = null;
    static ByteBuffer video_buffer_1 = null;
    static ByteBuffer video_buffer_2 = null;
    static int buffer_size_in_bytes = 0;
    static ByteBuffer _recBuffer = null;

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
                    replace("\\", "\\\\"). // \ -> \\
                    replace("%", "\\%"). // % -> \%
                    replace("_", "\\_"). // _ -> \_
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
        if (in)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    static class send_message_result
    {
        long msg_num;
        boolean msg_v2;
        String msg_hash_hex;
        String raw_message_buf_hex;
        long error_num;
    }

    public static void add_message_ml(String datetime, String username, String message, boolean self)
    {
        try
        {
            MessageTextArea.setSelectionStart(MessageTextArea.getText().length());
            MessageTextArea.setSelectionEnd(MessageTextArea.getText().length());
            MessageTextArea.setCharacterAttributes(blueStyle, true);
            MessageTextArea.replaceSelection(datetime);

            MessageTextArea.setSelectionStart(MessageTextArea.getText().length());
            MessageTextArea.setSelectionEnd(MessageTextArea.getText().length());
            MessageTextArea.setCharacterAttributes(mainStyle, true);
            MessageTextArea.replaceSelection("|");

            if (self)
            {
                MessageTextArea.setSelectionStart(MessageTextArea.getText().length());
                MessageTextArea.setSelectionEnd(MessageTextArea.getText().length());
                MessageTextArea.setCharacterAttributes(redStyle, true);
                MessageTextArea.replaceSelection("self");
            }
            else
            {
                MessageTextArea.setSelectionStart(MessageTextArea.getText().length());
                MessageTextArea.setSelectionEnd(MessageTextArea.getText().length());
                MessageTextArea.setCharacterAttributes(blueStyle, true);
                MessageTextArea.replaceSelection("user");
            }

            MessageTextArea.setSelectionStart(MessageTextArea.getText().length());
            MessageTextArea.setSelectionEnd(MessageTextArea.getText().length());
            MessageTextArea.setCharacterAttributes(mainStyle, true);
            MessageTextArea.replaceSelection("|" + message + "\n");
        }
        catch (Exception e)
        {
        }
    }

    public MainActivity()
    {
        super("TRIfA - Desktop - " + Version + "   ");
        MainFrame = this;

        VideoInFrame1 = new VideoInFrame();
        VideoOutFrame1 = new VideoOutFrame();
        AudioFrame1 = new AudioFrame();

        initComponents();
        setSize(600, 400);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                int selected_answer = JOptionPane.showConfirmDialog(null, lo.getString("exit_app_msg"),
                                                                    lo.getString("exit_app_title"), YES_NO_OPTION);
                if (selected_answer == YES_OPTION)
                {
                    try
                    {
                        HelperGeneric.update_savedata_file_wrapper(MainActivity.password_hash);
                    }
                    catch (Exception e3)
                    {
                        e3.printStackTrace();
                    }

                    tox_service_fg.stop_me = true;

                    try
                    {
                        sqldb.close();
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }

                    System.exit(0);
                }
            }
        });

        this.setVisible(true);

        splitPane = new JSplitPane();

        FriendPanel = new FriendListFragmentJ();
        MessagePanel = new MessageListFragmentJ();
        MessagePanel.setCurrentPK(null);

        MessageScrollPane = new JScrollPane();

        // ------------------
        // ------------------
        // ------------------
        StyleContext sc = new StyleContext();
        final DefaultStyledDocument doc = new DefaultStyledDocument(sc);

        // Create and add the main document style
        defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        mainStyle = sc.addStyle("MainStyle", defaultStyle);
        StyleConstants.setFontFamily(mainStyle, "monospaced");
        StyleConstants.setFontSize(mainStyle, 9);

        // Create and add the constant width style
        redStyle = sc.addStyle("ConstantWidthRed", null);
        StyleConstants.setFontFamily(redStyle, "monospaced");
        StyleConstants.setFontSize(redStyle, 9);
        StyleConstants.setForeground(redStyle, Color.red);

        // Create and add the constant width style
        blueStyle = sc.addStyle("ConstantWidthBlue", null);
        StyleConstants.setFontFamily(blueStyle, "monospaced");
        StyleConstants.setFontSize(blueStyle, 9);
        StyleConstants.setForeground(blueStyle, Color.blue);

        blueSmallStyle = sc.addStyle("ConstantWidthBlue", null);
        StyleConstants.setFontFamily(blueSmallStyle, "monospaced");
        StyleConstants.setFontSize(blueSmallStyle, 7);
        StyleConstants.setForeground(blueSmallStyle, Color.blue);

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
        leftPanel = new JPanel();
        ownProfileShort = new JTextArea();

        myToxID = new JTextField();
        myToxID.setVisible(true);

        getContentPane().setLayout(new GridLayout());
        getContentPane().add(splitPane);
        splitPane.setVisible(true);

        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(80);
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(MessagePanel);

        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(ownProfileShort);
        leftPanel.add(FriendPanel);
        leftPanel.setVisible(true);

        ownProfileShort.setFont(new java.awt.Font("monospaced", PLAIN, 9));
        ownProfileShort.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        ownProfileShort.setEditable(false);

        MessagePanel.setLayout(new BoxLayout(MessagePanel, BoxLayout.Y_AXIS));
        MessagePanel.add(MessageScrollPane);
        MessageScrollPane.setViewportView(MessageTextArea);
        // MessageTextArea.setEditable(false);
        MessagePanel.add(MessageTextInputPanel);
        myToxID.setFont(new java.awt.Font("monospaced", PLAIN, 9));
        myToxID.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        MessagePanel.add(myToxID, BorderLayout.SOUTH);

        MessageTextInputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        MessageTextInputPanel.setLayout(new BoxLayout(MessageTextInputPanel, BoxLayout.X_AXIS));

        MessageTextInputPanel.add(sendTextField);
        sendTextField.setEditable(true);

        sendTextField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    if (e.isShiftDown())
                    {
                        Log.i(TAG, "SHIFT Enter pressed");
                        sendTextField.append("\n");
                        return;
                    }
                    Log.i(TAG, "Enter key pressed");
                    send_message_onclick();
                }
            }
        });


        MessageTextInputPanel.add(sendButton);

        sendButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Log.i(TAG, "sendButton pressed");
                send_message_onclick();
            }
        });

        doc.setLogicalStyle(0, mainStyle);
        try
        {
            doc.insertString(0, "", null);
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }

        sendTextField.getDocument().addDocumentListener(new DocumentListener()
        {

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                updated();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                updated();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                updated();
            }

            void updated()
            {
                if (global_typing == 0)
                {
                    global_typing = 1;  // typing = 1

                    Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                tox_self_set_typing(friendnum, global_typing);
                                Log.i(TAG, "typing:fn#" + friendnum + ":activated");
                            }
                            catch (Exception e)
                            {
                                Log.i(TAG, "typing:fn#" + friendnum + ":EE1" + e.getMessage());
                            }
                        }
                    };

                    Log.i(TAG, "invokeLater:002:s");
                    SwingUtilities.invokeLater(myRunnable);
                    Log.i(TAG, "invokeLater:002:e");

                    try
                    {
                        typing_flag_thread.interrupt();
                    }
                    catch (Exception e)
                    {
                        // e.printStackTrace();
                    }

                    typing_flag_thread = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            boolean skip_flag_update = false;
                            try
                            {
                                Thread.sleep(TYPING_FLAG_DEACTIVATE_DELAY_IN_MILLIS); // sleep for n seconds
                            }
                            catch (Exception e)
                            {
                                // e.printStackTrace();
                                // ok, dont update typing flag
                                skip_flag_update = true;
                            }

                            if (global_typing == 1)
                            {
                                if (skip_flag_update == false)
                                {
                                    global_typing = 0;  // typing = 0
                                    Runnable myRunnable = new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            try
                                            {
                                                tox_self_set_typing(friendnum, global_typing);
                                                Log.i(TAG, "typing:fn#" + friendnum + ":DEactivated");
                                            }
                                            catch (Exception e)
                                            {
                                                Log.i(TAG, "typing:fn#" + friendnum + ":EE2" + e.getMessage());
                                            }
                                        }
                                    };

                                    Log.i(TAG, "invokeLater:003:s");
                                    SwingUtilities.invokeLater(myRunnable);
                                    Log.i(TAG, "invokeLater:003:e");
                                }
                            }
                        }
                    };
                    typing_flag_thread.start();
                }
            }
        });


        final Thread set_focus_on_textinput = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    while (!sendTextField.isValid())
                    {
                        Thread.sleep(10);
                    }

                    while (!sendTextField.isShowing())
                    {
                        Thread.sleep(10);
                    }

                    Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            sendTextField.requestFocus();
                            // sendTextField.revalidate();
                        }
                    };
                    SwingUtilities.invokeLater(myRunnable);
                }
                catch (Exception e)
                {
                }
            }
        };
        set_focus_on_textinput.start();

        VideoInFrame1.setLocationRelativeTo(null);
        final Rectangle bounds = this.getBounds();
        VideoInFrame1.setLocation(bounds.x + bounds.width, bounds.y);

        VideoOutFrame1.setLocationRelativeTo(null);
        final Rectangle bounds2 = VideoInFrame1.getBounds();
        VideoOutFrame1.setLocation(bounds2.x, bounds2.y + bounds2.height);

        AudioFrame1.setLocationRelativeTo(null);
        final Rectangle bounds3 = VideoOutFrame1.getBounds();
        AudioFrame1.setLocation(bounds3.x, bounds3.y + bounds3.height);

        MessageListFragmentJ.show_info_text();

        this.toFront();
        this.revalidate();
    }

    private void initComponents()
    {
        setLayout(new FlowLayout());
    }

    public static void main(String[] args)
    {
        System.out.println("Version:" + Version);

        // // how to change locale ---------------
        // Locale.setDefault(Locale.GERMAN);
        // ResourceBundle.clearCache();
        // // how to change locale ---------------

        Locale locale = Locale.getDefault();
        Log.i(TAG, locale.getDisplayCountry());
        Log.i(TAG, locale.getDisplayLanguage());
        Log.i(TAG, locale.getDisplayName());
        Log.i(TAG, locale.getISO3Country());
        Log.i(TAG, locale.getISO3Language());
        Log.i(TAG, locale.getLanguage());
        Log.i(TAG, locale.getCountry());

        lo = ResourceBundle.getBundle("i18n.ResourceBundle", locale);
        Log.i(TAG, "locale_test:" + lo.getString("locale_test"));

        TrifaToxService.TOX_SERVICE_STARTED = false;
        bootstrapping = false;
        Log.i(TAG, "java.library.path:" + System.getProperty("java.library.path"));

        Log.i(TAG, "loaded:c-toxcore:v" + tox_version_major() + "." + tox_version_minor() + "." + tox_version_patch());
        Log.i(TAG, "loaded:jni-c-toxcore:v" + jnictoxcore_version());

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            public void run()
            {
                try
                {
                    System.out.println("Shutting down ...");
                    try
                    {
                        HelperGeneric.update_savedata_file_wrapper(MainActivity.password_hash);
                    }
                    catch (Exception e3)
                    {
                        e3.printStackTrace();
                    }

                    tox_service_fg.stop_me = true;
                    Thread.sleep(500);

                    try
                    {
                        sqldb.close();
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                }
                catch (Exception e)
                {
                    System.out.println("ERROR in Shutdown");
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        });


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

        Statement statement = null;

        try
        {
            statement = sqldb.createStatement();
            statement.setQueryTimeout(10);  // set timeout to 30 sec.
        }
        catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }

        // @formatter:off
        try
        {
            statement.executeUpdate(
                    "create table TRIFADatabaseGlobalsNew (" +
                    "key string NOT NULL PRIMARY KEY," +
                    "value string" +
                    ")");
        }
        catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }

        try
        {
            statement.executeUpdate(
                    "CREATE TABLE `BootstrapNodeEntryDB` \n" +
                    "(\n" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "`num` INTEGER NOT NULL,\n" +
                    "`udp_node` BOOLEAN NOT NULL,\n" +
                    "`ip` TEXT NOT NULL,\n" +
                    "`port` INTEGER NOT NULL,\n" +
                    "`key_hex` TEXT NOT NULL\n" +
                    ")");
        }
        catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }

        try
        {
            statement.executeUpdate(
                    "create table FriendList (" +
                    "tox_public_key_string string NOT NULL PRIMARY KEY , " +
                    "name string," +
                    "alias_name string," +
                    "status_message string," +
                    "TOX_CONNECTION integer DEFAULT '0' CHECK (TOX_CONNECTION IN ('0', '1', '2'))," +
                    "TOX_CONNECTION_real integer DEFAULT '0' CHECK (TOX_CONNECTION_real IN ('0', '1', '2'))," +
                    "TOX_CONNECTION_on_off integer DEFAULT '0' CHECK (TOX_CONNECTION_on_off IN ('0', '1'))," +
                    "TOX_CONNECTION_on_off_real integer DEFAULT '0' CHECK (TOX_CONNECTION_on_off_real IN ('0', '1'))," +
                    "TOX_USER_STATUS integer DEFAULT '0' CHECK (TOX_USER_STATUS IN ('0', '1', '2'))," +
                    "avatar_pathname string," +
                    "avatar_filename string," +
                    "avatar_update integer DEFAULT '0' CHECK (avatar_update IN ('0', '1'))," +
                    "avatar_update_timestamp integer DEFAULT '-1'," +
                    "notification_silent integer DEFAULT '0' CHECK (notification_silent IN ('0', '1'))," +
                    "sort integer DEFAULT '0'," +
                    "last_online_timestamp integer DEFAULT '-1'," +
                    "last_online_timestamp_real integer DEFAULT '-1'," +
                    "added_timestamp integer DEFAULT '-1'," +
                    "is_relay integer DEFAULT '0' CHECK (is_relay IN ('0', '1'))  )");
        }
        catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }

        try
        {
            statement.executeUpdate("create table Message("+
                                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "+
                                    "message_id integer DEFAULT '-1', "+
                                    "tox_friendpubkey string NOT NULL, "+
                                    "direction INTEGER NOT NULL DEFAULT '0' CHECK (direction IN ('0', '1')), "+
                                    "TOX_MESSAGE_TYPE INTEGER NOT NULL DEFAULT '0' CHECK (TOX_MESSAGE_TYPE IN ('0', '1')), "+
                                    "TRIFA_MESSAGE_TYPE integer DEFAULT '0', "+
                                    "state integer DEFAULT '1', "+
                                    "ft_accepted integer DEFAULT '0' CHECK (ft_accepted IN ('0', '1')), "+
                                    "ft_outgoing_started integer DEFAULT '0' CHECK (ft_outgoing_started IN ('0', '1')), "+
                                    "filedb_id integer DEFAULT '-1', "+
                                    "filetransfer_id integer DEFAULT '-1', "+
                                    "sent_timestamp integer DEFAULT '0', "+
                                    "sent_timestamp_ms integer DEFAULT '0', "+
                                    "rcvd_timestamp integer DEFAULT '0', "+
                                    "rcvd_timestamp_ms integer DEFAULT '0', "+
                                    "read integer DEFAULT '0' CHECK (read IN ('0', '1')), "+
                                    "send_retries integer DEFAULT '0', "+
                                    "is_new DEFAULT '0' CHECK (is_new IN ('0', '1')), "+
                                    "text string, "+
                                    "filename_fullpath string, "+
                                    "msg_id_hash string, "+
                                    "raw_msgv2_bytes string, "+
                                    "msg_version integer DEFAULT '0' CHECK (msg_version IN ('0', '1')), "+
                                    "resend_count integer DEFAULT '2' CHECK (resend_count IN ('0', '1', '2')) "+
               ")"
            );
        }
        catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }

        try
        {
            statement.executeUpdate(
                    "create table RelayListDB (" +
                    "tox_public_key_string string NOT NULL PRIMARY KEY , " +
                    "TOX_CONNECTION integer DEFAULT '0' CHECK (TOX_CONNECTION IN ('0', '1', '2')), " +
                    "TOX_CONNECTION_on_off integer DEFAULT '0' CHECK (TOX_CONNECTION_on_off IN ('0', '1')), " +
                    "own_relay integer DEFAULT '0' CHECK (own_relay IN ('0', '1')), " +
                    "last_online_timestamp integer DEFAULT '-1', " +
                    "tox_public_key_string_of_owner string " +
                    ")"
            );
        }
        catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }

        try
        {
            statement.executeUpdate(
                    "create table ConferenceDB" +
                    "(" +
                    "    conference_identifier string NOT NULL PRIMARY KEY ," +
                    "    who_invited__tox_public_key_string string," +
                    "    name string," +
                    "    peer_count integer DEFAULT '-1'," +
                    "    own_peer_number integer DEFAULT '-1'," +
                    "    kind integer DEFAULT '0'," +
                    "    tox_conference_number integer DEFAULT '-1'," +
                    "    conference_active integer DEFAULT '0' CHECK (conference_active IN ('0', '1'))," +
                    "    notification_silent integer DEFAULT '0' CHECK (notification_silent IN ('0', '1'))" +
                    ")"
            );
        }
        catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }

        try
        {
            statement.executeUpdate(
                    "CREATE TABLE ConferencePeerCacheDB\n" +
                    "(\n" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "conference_identifier TEXT NOT NULL, \n" +
                    "peer_pubkey TEXT NOT NULL, \n" +
                    "peer_name TEXT NOT NULL, \n" +
                    "last_update_timestamp INTEGER NOT NULL DEFAULT '-1'\n" +
                    ")"
            );
        }
        catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }

        try
        {
            statement.executeUpdate(
                    "CREATE TABLE `ConferenceMessage`\n" +
                    "(\n" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "`conference_identifier` TEXT NOT NULL DEFAULT -1,\n" +
                    "`tox_peerpubkey` TEXT NOT NULL,\n" +
                    "`tox_peername` TEXT ,\n" +
                    "`direction` INTEGER NOT NULL,\n" +
                    "`TOX_MESSAGE_TYPE` INTEGER NOT NULL,\n" +
                    "`TRIFA_MESSAGE_TYPE` INTEGER NOT NULL DEFAULT 0,\n" +
                    "`sent_timestamp` INTEGER ,\n" +
                    "`rcvd_timestamp` INTEGER ,\n" +
                    "`read` BOOLEAN NOT NULL,\n" +
                    "`is_new` BOOLEAN NOT NULL,\n" +
                    "`text` TEXT \n" +
                    ")\n"
            );
        }
        catch (SQLException e)
        {
            System.err.println(e.getMessage());
        }
        // @formatter:on

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

        new MainActivity();

        String my_tox_id_temp = get_my_toxid();
        Log.i(TAG, "MyToxID:" + my_tox_id_temp);
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

    // ----------- TRIfA internal -----------
    public static native int jni_iterate_group_audio(int delta_new, int want_ms_output);

    public static native int jni_iterate_videocall_audio(int delta_new, int want_ms_output, int channels, int sample_rate, int send_emtpy_buffer);

    public static native void tox_set_do_not_sync_av(int do_not_sync_av);

    public static native void tox_set_onion_active(int active);
    // ----------- TRIfA internal -----------

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

    // --------------- Message V2 -------------
    // --------------- Message V2 -------------
    // --------------- Message V2 -------------
    public static native long tox_messagev2_size(long text_length, long type, long alter_type);

    public static native int tox_messagev2_wrap(long text_length, long type, long alter_type, ByteBuffer message_text_buffer, long ts_sec, long ts_ms, ByteBuffer raw_message_buffer, ByteBuffer msgid_buffer);

    public static native int tox_messagev2_get_message_id(ByteBuffer raw_message_buffer, ByteBuffer msgid_buffer);

    public static native long tox_messagev2_get_ts_sec(ByteBuffer raw_message_buffer);

    public static native long tox_messagev2_get_ts_ms(ByteBuffer raw_message_buffer);

    public static native long tox_messagev2_get_message_text(ByteBuffer raw_message_buffer, long raw_message_len, int is_alter_msg, long alter_type, ByteBuffer message_text_buffer);

    public static native String tox_messagev2_get_sync_message_pubkey(ByteBuffer raw_message_buffer);

    public static native long tox_messagev2_get_sync_message_type(ByteBuffer raw_message_buffer);

    public static native int tox_util_friend_send_msg_receipt_v2(long friend_number, long ts_sec, ByteBuffer msgid_buffer);

    public static native long tox_util_friend_send_message_v2(long friend_number, int type, long ts_sec, String message, long length, ByteBuffer raw_message_back_buffer, ByteBuffer raw_message_back_buffer_length, ByteBuffer msgid_back_buffer);

    public static native int tox_util_friend_resend_message_v2(long friend_number, ByteBuffer raw_message_buffer, long raw_msg_len);
    // --------------- Message V2 -------------
    // --------------- Message V2 -------------
    // --------------- Message V2 -------------

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

    public static native int toxav_video_send_frame_h264(long friendnum, int frame_width_px, int frame_height_px, long data_len);

    public static native int toxav_video_send_frame_h264_age(long friendnum, int frame_width_px, int frame_height_px, long data_len, int age_ms);

    public static native int toxav_option_set(long friendnum, long a_TOXAV_OPTIONS_OPTION, long value);

    public static native void set_av_call_status(int status);

    public static native void set_audio_play_volume_percent(int volume_percent);

    // buffer is for incoming video (call)
    public static native long set_JNI_video_buffer(java.nio.ByteBuffer buffer, int frame_width_px, int frame_height_px);

    // buffer2 is for sending video (call)
    public static native void set_JNI_video_buffer2(java.nio.ByteBuffer buffer2, int frame_width_px, int frame_height_px);

    // audio_buffer is for sending audio (group and call)
    public static native void set_JNI_audio_buffer(java.nio.ByteBuffer audio_buffer);

    // audio_buffer2 is for incoming audio (group and call)
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
        if (Callstate.state != 0)
        {
            // don't accept a new call if we already are in a call
            Log.i(TAG, "android_toxav_callback_call_cb_method:already in a call:fn calling=" + friend_number +
                       " fn in call=" + tox_friend_by_public_key__wrapper(Callstate.friend_pubkey));
            return;
        }

        int res1 = toxav_answer(friend_number, GLOBAL_AUDIO_BITRATE, GLOBAL_VIDEO_BITRATE);
        Callstate.state = 1;
        Callstate.friend_pubkey = tox_friend_get_public_key__wrapper(friend_number);
        Callstate.accepted_call = 1;
        set_av_call_status(Callstate.state);
        Log.i(TAG, "android_toxav_callback_call_cb_method:Callstate.state=" + Callstate.state);
    }

    static void android_toxav_callback_video_receive_frame_cb_method(long friend_number, long frame_width_px, long frame_height_px, long ystride, long ustride, long vstride)
    {
        // Log.i(TAG, "android_toxav_callback_video_receive_frame_cb_method");
        int y_layer_size = (int) Math.max(frame_width_px, Math.abs(ystride)) * (int) frame_height_px;
        int u_layer_size = (int) Math.max((frame_width_px / 2), Math.abs(ustride)) * ((int) frame_height_px / 2);
        int v_layer_size = (int) Math.max((frame_width_px / 2), Math.abs(vstride)) * ((int) frame_height_px / 2);
        int frame_width_px1 = (int) Math.max(frame_width_px, Math.abs(ystride));
        int frame_height_px1 = (int) frame_height_px;
        buffer_size_in_bytes = y_layer_size + v_layer_size + u_layer_size;
        if (video_buffer_1 == null)
        {
            Log.i(TAG, "android_toxav_callback_video_receive_frame_cb_method:11:1");
            video_buffer_1 = ByteBuffer.allocateDirect(buffer_size_in_bytes);
            set_JNI_video_buffer(video_buffer_1, frame_width_px1, frame_height_px1);
            setup_video_in_resolution(frame_width_px1, frame_height_px1, buffer_size_in_bytes);
            Log.i(TAG, "android_toxav_callback_video_receive_frame_cb_method:11:2");
        }
        else
        {
            if ((VideoInFrame1.width != frame_width_px1) || (VideoInFrame1.height != frame_height_px1))
            {
                Log.i(TAG, "android_toxav_callback_video_receive_frame_cb_method:22:1");
                video_buffer_1 = ByteBuffer.allocateDirect(buffer_size_in_bytes);
                set_JNI_video_buffer(video_buffer_1, frame_width_px1, frame_height_px1);
                setup_video_in_resolution(frame_width_px1, frame_height_px1, buffer_size_in_bytes);
                Log.i(TAG, "android_toxav_callback_video_receive_frame_cb_method:22:2");
            }
        }

        new_video_in_frame(video_buffer_1, frame_width_px1, frame_height_px1);
        // Log.i(TAG, "android_toxav_callback_video_receive_frame_cb_method:099");
    }

    static void android_toxav_callback_call_state_cb_method(long friend_number, int a_TOXAV_FRIEND_CALL_STATE)
    {
        if (tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
        {
            // not the friend we are in call with now
            return;
        }

        Log.i(TAG, "toxav_call_state:INCOMING_CALL:from=" + friend_number + " state=" + a_TOXAV_FRIEND_CALL_STATE);
        Log.i(TAG, "Callstate.tox_call_state:INCOMING_CALL=" + a_TOXAV_FRIEND_CALL_STATE + " old=" +
                   Callstate.tox_call_state);

        if (Callstate.state == 1)
        {
            int old_value = Callstate.tox_call_state;
            Callstate.tox_call_state = a_TOXAV_FRIEND_CALL_STATE;

            if ((a_TOXAV_FRIEND_CALL_STATE &
                 (TOXAV_FRIEND_CALL_STATE_SENDING_A.value + TOXAV_FRIEND_CALL_STATE_SENDING_V.value +
                  TOXAV_FRIEND_CALL_STATE_ACCEPTING_A.value + TOXAV_FRIEND_CALL_STATE_ACCEPTING_V.value)) > 0)
            {
                Log.i(TAG, "toxav_call_state:from=" + friend_number + " call starting");
                on_call_started_actions();
            }
            else if ((a_TOXAV_FRIEND_CALL_STATE & (TOXAV_FRIEND_CALL_STATE_FINISHED.value)) > 0)
            {
                Log.i(TAG, "toxav_call_state:from=" + friend_number + " call ending(1)");
                on_call_ended_actions();
            }
            else if ((old_value > TOXAV_FRIEND_CALL_STATE_NONE.value) &&
                     (a_TOXAV_FRIEND_CALL_STATE == TOXAV_FRIEND_CALL_STATE_NONE.value))
            {
                Log.i(TAG, "toxav_call_state:from=" + friend_number + " call ending(2)");
                on_call_ended_actions();
            }
            else if ((a_TOXAV_FRIEND_CALL_STATE & (TOXAV_FRIEND_CALL_STATE_ERROR.value)) > 0)
            {
                Log.i(TAG, "toxav_call_state:from=" + friend_number + " call ERROR(3)");
                on_call_ended_actions();
            }
        }
    }

    static void android_toxav_callback_bit_rate_status_cb_method(long friend_number, long audio_bit_rate, long video_bit_rate)
    {
    }

    static void android_toxav_callback_audio_receive_frame_cb_method(long friend_number, long sample_count, int channels, long sampling_rate)
    {
        // Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:001:1");

        if (tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
        {
            Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:001a:ret01");
            return;
        }

        if ((sampling_rate != AudioSelectOutBox.SAMPLE_RATE) || (channels != AudioSelectOutBox.CHANNELS) ||
            (_recBuffer == null) || (sample_count == 0))
        {
            Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:11:1");
            _recBuffer = ByteBuffer.allocateDirect((int) (10000 * 2 * channels));
            set_JNI_audio_buffer2(_recBuffer);
            Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:11:2");
        }

        if ((sampling_rate != AudioSelectOutBox.SAMPLE_RATE) || (channels != AudioSelectOutBox.CHANNELS))
        {
            Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:22:1");
            AudioSelectOutBox.change_audio_format((int) sampling_rate, channels);
            Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:22:2");
        }

        if (sample_count == 0)
        {
            return;
        }

        try
        {
            _recBuffer.rewind();
            int want_bytes = (int) (sample_count * 2 * channels);
            byte[] audio_out_byte_buffer = new byte[want_bytes];
            _recBuffer.get(audio_out_byte_buffer, 0, want_bytes);
            // Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:sourceDataLine.write:1:" + want_bytes +
            //            " sample_count=" + sample_count + " channels=" + channels +
            //            " AudioSelectOutBox.sourceDataLine.getFormat().getChannels()=" +
            //            AudioSelectOutBox.sourceDataLine.getFormat().getChannels());


            int actual_written_bytes = AudioSelectOutBox.sourceDataLine.write(audio_out_byte_buffer, 0, want_bytes);
            // Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:sourceDataLine.write:2:" +
            //           actual_written_bytes);

            float global_audio_out_vu = AUDIO_VU_MIN_VALUE;
            if (sample_count > 0)
            {
                float vu_value = audio_vu(audio_out_byte_buffer, (int) sample_count);
                if (vu_value > AUDIO_VU_MIN_VALUE)
                {
                    global_audio_out_vu = vu_value;
                }
                else
                {
                    global_audio_out_vu = 0;
                }
            }

            final float global_audio_out_vu_ = global_audio_out_vu;
            final Thread t_audio_bar_set_play = new Thread()
            {
                @Override
                public void run()
                {
                    // Log.i(TAG, "set_audio_in_bar_level:" + global_audio_out_vu_);
                    set_audio_out_bar_level((int) global_audio_out_vu_);
                }
            };
            t_audio_bar_set_play.start();
        }
        catch (Exception e)
        {
            Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:EE01:" + e.getMessage());
        }
    }

    static void android_toxav_callback_video_receive_frame_pts_cb_method(long friend_number, long frame_width_px, long frame_height_px, long ystride, long ustride, long vstride, long pts)
    {
        if (tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
        {
            return;
        }
        // Log.i(TAG, "android_toxav_callback_video_receive_frame_pts_cb_method");
        android_toxav_callback_video_receive_frame_cb_method(friend_number, frame_width_px, frame_height_px, ystride,
                                                             ustride, vstride);
    }

    static void android_toxav_callback_video_receive_frame_h264_cb_method(long friend_number, long buf_size)
    {
        Log.i(TAG, "android_toxav_callback_video_receive_frame_h264_cb_method");
        // HINT: Disabled. this is now handled by c-toxcore. how nice.
    }

    static void android_toxav_callback_audio_receive_frame_pts_cb_method(long friend_number, long sample_count, int channels, long sampling_rate, long pts)
    {
        // Log.i(TAG, "android_toxav_callback_audio_receive_frame_cb_method:001:0");
        android_toxav_callback_audio_receive_frame_cb_method(friend_number, sample_count, channels, sampling_rate);
    }

    static void android_toxav_callback_group_audio_receive_frame_cb_method(long conference_number, long peer_number, long sample_count, int channels, long sampling_rate)
    {
    }

    static void android_toxav_callback_call_comm_cb_method(long friend_number, long a_TOXAV_CALL_COMM_INFO, long comm_number)
    {
        // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:" + a_TOXAV_CALL_COMM_INFO + ":" + comm_number);
        if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_DECODER_IN_USE_VP8.value)
        {
            // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:3:" + a_TOXAV_CALL_COMM_INFO + ":" + comm_number);
            Callstate.video_in_codec = VIDEO_CODEC_VP8;
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_DECODER_IN_USE_H264.value)
        {
            // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:4:" + a_TOXAV_CALL_COMM_INFO + ":" + comm_number);
            Callstate.video_in_codec = VIDEO_CODEC_H264;
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_ENCODER_IN_USE_VP8.value)
        {
            Callstate.video_out_codec = VIDEO_CODEC_VP8;
            // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:1:" + a_TOXAV_CALL_COMM_INFO + ":" + comm_number);
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_ENCODER_IN_USE_H264.value)
        {
            Callstate.video_out_codec = VIDEO_CODEC_H264;
            // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:2:" + a_TOXAV_CALL_COMM_INFO + ":" + comm_number);
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_DECODER_CURRENT_BITRATE.value)
        {
            Callstate.video_in_bitrate = comm_number;
            // Log.i(TAG,
            //      "android_toxav_callback_call_comm_cb_method:TOXAV_CALL_COMM_DECODER_CURRENT_BITRATE:" + comm_number);
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_ENCODER_CURRENT_BITRATE.value)
        {
            Callstate.video_bitrate = comm_number;
            // Log.i(TAG,
            //      "android_toxav_callback_call_comm_cb_method:TOXAV_CALL_COMM_ENCODER_CURRENT_BITRATE:" + comm_number);
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_PLAY_BUFFER_ENTRIES.value)
        {
            if (comm_number < 0)
            {
                Callstate.play_buffer_entries = 0;
            }
            else if (comm_number > 9900)
            {
                Callstate.play_buffer_entries = 99;
            }
            else
            {
                Callstate.play_buffer_entries = (int) comm_number;
                // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:play_buffer_entries=:" + comm_number);
            }
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_NETWORK_ROUND_TRIP_MS.value)
        {
            if (comm_number < 0)
            {
                Callstate.round_trip_time = 0;
            }
            else if (comm_number > 9900)
            {
                Callstate.round_trip_time = 9900;
            }
            else
            {
                Callstate.round_trip_time = comm_number;
                // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:round_trip_time=:" + Callstate.round_trip_time);
            }
        }
        else if (a_TOXAV_CALL_COMM_INFO == TOXAV_CALL_COMM_PLAY_DELAY.value)
        {
            if (comm_number < 0)
            {
                Callstate.play_delay = 0;
            }
            else if (comm_number > 9900)
            {
                Callstate.play_delay = 9900;
            }
            else
            {
                Callstate.play_delay = comm_number;
                // Log.i(TAG, "android_toxav_callback_call_comm_cb_method:play_delay=:" + Callstate.play_delay);
            }
        }

        try
        {
            HelperGeneric.update_bitrates();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "android_toxav_callback_call_comm_cb_method:EE:" + e.getMessage());
        }
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

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // ownProfileShort.setEditable(true);
                    if (a_TOX_CONNECTION == 2)
                    {
                        ownProfileShort.setBackground(Color.GREEN);
                    }
                    else if (a_TOX_CONNECTION == 1)
                    {
                        ownProfileShort.setBackground(Color.ORANGE);
                    }
                    else
                    {
                        ownProfileShort.setBackground(Color.LIGHT_GRAY);
                    }
                    // ownProfileShort.setEditable(false);
                }
                catch (Exception e)
                {
                    Log.i(TAG, "android_tox_callback_self_connection_status_cb_method:005:EE:" + e.getMessage());
                }
            }
        };

        long loop = 0;
        while ((ownProfileShort == null) || (!ownProfileShort.isShowing()))
        {
            try
            {
                // Log.i(TAG, "myToxID.setText:sleep");
                Thread.sleep(10);
                loop++;
                if (loop > 20)
                {
                    break;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(myRunnable);
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
        FriendList f = main_get_friend(friend_number);
        //Log.i(TAG,
        //      "friend_connection_status:friend:" + friend_number + " connection status:" + a_TOX_CONNECTION + " f=" +
        //      f);

        if (f != null)
        {
            if (f.TOX_CONNECTION_real != a_TOX_CONNECTION)
            {
                f.TOX_CONNECTION_real = a_TOX_CONNECTION;
                f.TOX_CONNECTION_on_off_real = HelperGeneric.get_toxconnection_wrapper(f.TOX_CONNECTION);
                HelperFriend.update_friend_in_db_connection_status_real(f);
            }

            HelperGeneric.update_friend_connection_status_helper(a_TOX_CONNECTION, f, false);

            if (f.TOX_CONNECTION_real != a_TOX_CONNECTION)
            {
                f.TOX_CONNECTION_real = a_TOX_CONNECTION;
                f.TOX_CONNECTION_on_off_real = HelperGeneric.get_toxconnection_wrapper(f.TOX_CONNECTION);
                HelperFriend.update_friend_in_db_connection_status_real(f);
            }
        }
    }

    static void android_tox_callback_friend_typing_cb_method(long friend_number, final int typing)
    {
    }

    static void android_tox_callback_friend_read_receipt_cb_method(long friend_number, long message_id)
    {
    }

    static void android_tox_callback_friend_request_cb_method(String friend_public_key, String friend_request_message, long length)
    {
        // Log.i(TAG, "friend_request:friend:" + friend_public_key + " friend request message:" + friend_request_message);
        // Log.i(TAG, "friend_request:friend:" + friend_public_key.substring(0, TOX_PUBLIC_KEY_SIZE * 2) +
        //            " friend request message:" + friend_request_message);
        String friend_public_key__ = friend_public_key.substring(0, TOX_PUBLIC_KEY_SIZE * 2);
        HelperFriend.add_friend_to_system(friend_public_key__.toUpperCase(), false, null);
    }

    static void android_tox_callback_friend_message_cb_method(long friend_number, int message_type, String friend_message, long length)
    {
        if (PREF__X_battery_saving_mode)
        {
            Log.i(TAG, "global_last_activity_for_battery_savings_ts:007:*PING*");
        }
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        HelperGeneric.receive_incoming_message(0, friend_number, friend_message, null, 0, null);
    }

    static void android_tox_callback_friend_message_v2_cb_method(long friend_number, String friend_message, long length, long ts_sec, long ts_ms, byte[] raw_message, long raw_message_length)
    {
        if (PREF__X_battery_saving_mode)
        {
            Log.i(TAG, "global_last_activity_for_battery_savings_ts:005:*PING*");
        }
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        HelperGeneric.receive_incoming_message(1, friend_number, friend_message, raw_message, raw_message_length, null);
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


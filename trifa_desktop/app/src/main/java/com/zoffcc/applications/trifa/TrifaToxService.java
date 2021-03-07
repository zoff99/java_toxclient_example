/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
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

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import static com.zoffcc.applications.trifa.HelperConference.new_or_updated_conference;
import static com.zoffcc.applications.trifa.HelperConference.set_all_conferences_inactive;
import static com.zoffcc.applications.trifa.HelperFriend.add_friend_real;
import static com.zoffcc.applications.trifa.HelperFriend.get_friend_name_from_pubkey;
import static com.zoffcc.applications.trifa.HelperFriend.is_friend_online;
import static com.zoffcc.applications.trifa.HelperFriend.set_all_friends_offline;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.bytes_to_hex;
import static com.zoffcc.applications.trifa.HelperGeneric.get_g_opts;
import static com.zoffcc.applications.trifa.HelperGeneric.hex_to_bytes;
import static com.zoffcc.applications.trifa.HelperGeneric.set_g_opts;
import static com.zoffcc.applications.trifa.HelperGeneric.tox_friend_send_message_wrapper;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_messageid;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_no_read_recvedts;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_resend_count;
import static com.zoffcc.applications.trifa.HelperMessage.update_single_message;
import static com.zoffcc.applications.trifa.MainActivity.MainFrame;
import static com.zoffcc.applications.trifa.MainActivity.get_my_toxid;
import static com.zoffcc.applications.trifa.MainActivity.myToxID;
import static com.zoffcc.applications.trifa.MainActivity.ownProfileShort;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_get_chatlist;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_get_chatlist_size;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_get_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_get_type;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_name_size;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_status_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_get_status_message_size;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_status_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_util_friend_resend_message_v2;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ADD_BOTS_ON_STARTUP;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONFERENCE_ID_LENGTH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ECHOBOT_TOXID;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.bootstrapping;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_name;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_status_message;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_toxid;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_self_connection_status;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;

public class TrifaToxService
{
    static final String TAG = "trifa.ToxService";
    static Thread ToxServiceThread = null;
    static boolean stop_me = false;
    static boolean is_tox_started = false;
    static boolean global_toxid_text_set = false;
    static boolean TOX_SERVICE_STARTED = false;
    static OrmaDatabase orma = null;
    static long last_resend_pending_messages_ms = -1;
    static long last_resend_pending_messages2_ms = -1;

    void tox_thread_start_fg()
    {
        Log.i(TAG, "tox_thread_start_fg");

        ToxServiceThread = new Thread()
        {
            @Override
            public void run()
            {

                // ------ correct startup order ------
                boolean old_is_tox_started = is_tox_started;
                Log.i(TAG, "is_tox_started:==============================");
                Log.i(TAG, "is_tox_started=" + is_tox_started);
                Log.i(TAG, "is_tox_started:==============================");

                is_tox_started = true;

                // --------------------------------------------------
                // --- wait for UI to finish layouting
                // --------------------------------------------------
                while ((MainFrame == null) || (!MainFrame.isShowing()))
                {
                    try
                    {
                        Log.i(TAG, "waiting for UI to finish layout ...");
                        Thread.sleep(30);
                    }
                    catch (Exception e)
                    {
                    }
                }

                Log.i(TAG, "waiting for UI to finish layout ... DONE");
                // --------------------------------------------------
                // --- wait for UI to finish layouting
                // --------------------------------------------------


                if (!old_is_tox_started)
                {
                    set_all_friends_offline();
                    set_all_conferences_inactive();
                    MainActivity.init_tox_callbacks();
                    HelperGeneric.update_savedata_file_wrapper(MainActivity.password_hash);
                }
                // ------ correct startup order ------

                // TODO --------
                String my_tox_id_local = get_my_toxid();
                global_my_toxid = my_tox_id_local;
                if (tox_self_get_name_size() > 0)
                {
                    global_my_name = tox_self_get_name().substring(0, (int) tox_self_get_name_size());
                    Log.i(TAG, "AAA:003:" + global_my_name + " size=" + tox_self_get_name_size());
                }
                else
                {
                    tox_self_set_name("TRIfA " + my_tox_id_local.substring(my_tox_id_local.length() - 5,
                                                                           my_tox_id_local.length()));
                    global_my_name = ("TRIfA " + my_tox_id_local.substring(my_tox_id_local.length() - 5,
                                                                           my_tox_id_local.length()));
                    Log.i(TAG, "AAA:005");
                }

                if (tox_self_get_status_message_size() > 0)
                {
                    global_my_status_message = tox_self_get_status_message().substring(0,
                                                                                       (int) tox_self_get_status_message_size());
                    Log.i(TAG, "AAA:008:" + global_my_status_message + " size=" + tox_self_get_status_message_size());
                }
                else
                {
                    tox_self_set_status_message("this is TRIfA");
                    global_my_status_message = "this is TRIfA";
                    Log.i(TAG, "AAA:010");
                }
                Log.i(TAG, "AAA:011");

                HelperGeneric.update_savedata_file_wrapper(MainActivity.password_hash);

                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                if (!old_is_tox_started)
                {
                    bootstrapping = true;
                    Log.i(TAG, "bootrapping:set to true");

                    // ----- UDP ------
                    Log.i(TAG, "bootstrap_single:res=" + MainActivity.bootstrap_single_wrapper("tox.verdict.gg", 33445,
                                                                                               "1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976"));
                    Log.i(TAG, "bootstrap_single:res=" +
                               MainActivity.bootstrap_single_wrapper("tox.initramfs.io", 33445,
                                                                     "3F0A45A268367C1BEA652F258C85F4A66DA76BCAA667A49E770BCC4917AB6A25"));
                    Log.i(TAG, "bootstrap_single:res=" + MainActivity.bootstrap_single_wrapper("205.185.115.131", 53,
                                                                                               "3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68"));
                    // ----- UDP ------
                    //
                    // ----- TCP ------
                    Log.i(TAG, "add_tcp_relay_single:res=" +
                               MainActivity.add_tcp_relay_single_wrapper("tox.verdict.gg", 33445,
                                                                         "1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976"));
                    Log.i(TAG, "add_tcp_relay_single:res=" +
                               MainActivity.add_tcp_relay_single_wrapper("tox.initramfs.io", 33445,
                                                                         "3F0A45A268367C1BEA652F258C85F4A66DA76BCAA667A49E770BCC4917AB6A25"));
                    Log.i(TAG, "add_tcp_relay_single:res=" +
                               MainActivity.add_tcp_relay_single_wrapper("205.185.115.131", 443,
                                                                         "3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68"));
                    // ----- TCP ------
                }

                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------
                // --------------- bootstrap ---------------

                long tox_iteration_interval_ms = MainActivity.tox_iteration_interval();
                Log.i(TAG, "tox_iteration_interval_ms=" + tox_iteration_interval_ms);

                MainActivity.tox_iterate();

                Log.i(TAG, "myToxID.setText:001");
                long loop = 0;
                while ((myToxID == null) || (!myToxID.isShowing()))
                {
                    try
                    {
                        // Log.i(TAG, "myToxID.setText:sleep");
                        Thread.sleep(10);
                        loop++;
                        if (loop > 1000)
                        {
                            break;
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Log.i(TAG, "myToxID.setText:003");
                            myToxID.setEditable(true);
                            myToxID.setText(my_tox_id_local);
                            myToxID.setEditable(false);
                            ownProfileShort.setEditable(true);
                            ownProfileShort.setText(global_my_name);
                            ownProfileShort.setEditable(false);
                            Log.i(TAG, "myToxID.setText:004");
                        }
                        catch (Exception e)
                        {
                            Log.i(TAG, "myToxID.setText:005:EE:" + e.getMessage());
                        }
                    }
                };

                SwingUtilities.invokeLater(myRunnable);
                Log.i(TAG, "myToxID.setText:002");

                if (ADD_BOTS_ON_STARTUP)
                {
                    boolean need_add_bots = true;

                    try
                    {
                        Log.i(TAG, "need_add_bots read:" + get_g_opts("ADD_BOTS_ON_STARTUP_done"));

                        if (get_g_opts("ADD_BOTS_ON_STARTUP_done") != null)
                        {
                            if (get_g_opts("ADD_BOTS_ON_STARTUP_done").equals("true"))
                            {
                                need_add_bots = false;
                                Log.i(TAG, "need_add_bots=false");
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    if (need_add_bots)
                    {
                        Log.i(TAG, "need_add_bots:start");
                        add_friend_real(ECHOBOT_TOXID);
                        set_g_opts("ADD_BOTS_ON_STARTUP_done", "true");
                        Log.i(TAG, "need_add_bots=true (INSERT)");
                    }
                }

                try
                {
                    load_and_add_all_conferences();
                }
                catch (Exception e)
                {
                }

                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                while (!stop_me)
                {
                    try
                    {
                        if (tox_iteration_interval_ms < 2)
                        {
                            Log.i(TAG, "tox_iterate:(tox_iteration_interval_ms < 2ms!!):" + tox_iteration_interval_ms +
                                       "ms");
                            Thread.sleep(2);
                        }
                        else
                        {
                            // Log.i(TAG, "(tox_iteration_interval_ms):" + tox_iteration_interval_ms + "ms");
                            Thread.sleep(tox_iteration_interval_ms);
                        }
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    // --- send pending 1-on-1 text messages here --------------
                    // --- send pending 1-on-1 text messages here --------------
                    // --- send pending 1-on-1 text messages here --------------
                    if (global_self_connection_status != TOX_CONNECTION_NONE.value)
                    {

                        if ((last_resend_pending_messages_ms + (20 * 1000)) < System.currentTimeMillis())
                        {
                            // Log.i(TAG, "send_pending_1-on-1_messages ============================================");
                            last_resend_pending_messages_ms = System.currentTimeMillis();

                            // loop through all pending outgoing 1-on-1 text messages --------------
                            try
                            {
                                final int max_resend_count_per_iteration = 10;
                                int cur_resend_count_per_iteration = 0;

                                List<Message> m_v1 = orma.selectFromMessage().
                                        directionEq(1).
                                        TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value).
                                        resend_countEq(0).
                                        readEq(false).
                                        orderBySent_timestampAsc().
                                        toList();

                                if (m_v1.size() > 0)
                                {
                                    Iterator<Message> ii = m_v1.iterator();
                                    while (ii.hasNext())
                                    {
                                        Message m_resend_v1 = ii.next();

                                        if (is_friend_online(
                                                tox_friend_by_public_key__wrapper(m_resend_v1.tox_friendpubkey)) == 0)
                                        {
                                            //Log.i(TAG, "send_pending_1-on-1_messages:v1:fname=" +
                                            //           get_friend_name_from_pubkey(m_resend_v1.tox_friendpubkey) +
                                            //           " NOT online m=" + m_resend_v1.text);

                                            continue;
                                        }

                                        Log.i(TAG, "send_pending_1-on-1_messages:v1:fname=" +
                                                   get_friend_name_from_pubkey(m_resend_v1.tox_friendpubkey) + " m=" +
                                                   m_resend_v1.text);

                                        MainActivity.send_message_result result = tox_friend_send_message_wrapper(
                                                tox_friend_by_public_key__wrapper(m_resend_v1.tox_friendpubkey), 0,
                                                m_resend_v1.text);
                                        long res = result.msg_num;

                                        Log.i(TAG,
                                              "send_pending_1-on-1_messages:v1:res=" + res + " m=" + m_resend_v1.text);

                                        if (res > -1) // sending was OK
                                        {
                                            m_resend_v1.message_id = res;
                                            update_message_in_db_messageid(m_resend_v1);

                                            if (!result.raw_message_buf_hex.equalsIgnoreCase(""))
                                            {
                                                // save raw message bytes of this v2 msg into the database
                                                // we need it if we want to resend it later
                                                m_resend_v1.raw_msgv2_bytes = result.raw_message_buf_hex;
                                            }

                                            if (!result.msg_hash_hex.equalsIgnoreCase(""))
                                            {
                                                // msgV2 message -----------
                                                m_resend_v1.msg_id_hash = result.msg_hash_hex;
                                                m_resend_v1.msg_version = 1;
                                                // msgV2 message -----------
                                            }

                                            m_resend_v1.resend_count = 1; // we sent the message successfully
                                            update_message_in_db_no_read_recvedts(m_resend_v1);
                                            update_message_in_db_resend_count(m_resend_v1);
                                            update_single_message(m_resend_v1, true);

                                            cur_resend_count_per_iteration++;

                                            if (cur_resend_count_per_iteration >= max_resend_count_per_iteration)
                                            {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Log.i(TAG, "send_pending_1-on-1_messages:v1:EE:" + e.getMessage());
                            }
                            // loop through all pending outgoing 1-on-1 text messages --------------

                        }

                        if ((last_resend_pending_messages2_ms + (120 * 1000)) < System.currentTimeMillis())
                        {
                            // Log.i(TAG, "send_pending_1-on-1_messages 2 ============================================");
                            last_resend_pending_messages2_ms = System.currentTimeMillis();


                            // loop through all pending outgoing 1-on-1 text messages V2 (resend the resend) --------------
                            try
                            {
                                final int max_resend_count_per_iteration = 10;
                                int cur_resend_count_per_iteration = 0;

                                List<Message> m_v1 = orma.selectFromMessage().
                                        directionEq(1).
                                        TRIFA_MESSAGE_TYPEEq(TRIFA_MSG_TYPE_TEXT.value).
                                        resend_countEq(1).
                                        msg_versionEq(1).
                                        readEq(false).
                                        orderBySent_timestampAsc().
                                        toList();

                                if (m_v1.size() > 0)
                                {
                                    Iterator<Message> ii = m_v1.iterator();
                                    while (ii.hasNext())
                                    {
                                        Message m_resend_v2 = ii.next();

                                        if (is_friend_online(
                                                tox_friend_by_public_key__wrapper(m_resend_v2.tox_friendpubkey)) == 0)
                                        {
                                            continue;
                                        }

                                        Log.i(TAG, "send_pending_1-on-1_messages:v2:fname=" +
                                                   get_friend_name_from_pubkey(m_resend_v2.tox_friendpubkey) + " m=" +
                                                   m_resend_v2.text);

                                        // m_resend_v2.raw_msgv2_bytes

                                        final int raw_data_length = (m_resend_v2.raw_msgv2_bytes.length() / 2);
                                        byte[] raw_msg_resend_data = hex_to_bytes(m_resend_v2.raw_msgv2_bytes);

                                        ByteBuffer msg_text_buffer_resend_v2 = ByteBuffer.allocateDirect(
                                                raw_data_length);
                                        msg_text_buffer_resend_v2.put(raw_msg_resend_data, 0, raw_data_length);

                                        int res = tox_util_friend_resend_message_v2(
                                                tox_friend_by_public_key__wrapper(m_resend_v2.tox_friendpubkey),
                                                msg_text_buffer_resend_v2, raw_data_length);

                                        Log.i(TAG, "send_pending_1-on-1_messages:v2:res=" + res);

                                        cur_resend_count_per_iteration++;

                                        if (cur_resend_count_per_iteration >= max_resend_count_per_iteration)
                                        {
                                            break;
                                        }

                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Log.i(TAG, "send_pending_1-on-1_messages:v1:EE:" + e.getMessage());
                            }
                            // loop through all pending outgoing 1-on-1 text messages V2 (resend the resend) --------------


                        }
                    }
                    // --- send pending 1-on-1 text messages here --------------
                    // --- send pending 1-on-1 text messages here --------------
                    // --- send pending 1-on-1 text messages here --------------

                    // Log.i(TAG, "tox_iterate:--START--");
                    long s_time = System.currentTimeMillis();
                    MainActivity.tox_iterate();
                    if (s_time + 4000 < System.currentTimeMillis())
                    {
                        tox_iteration_interval_ms = MainActivity.tox_iteration_interval();
                        Log.i(TAG, "tox_iterate:--END--:took" +
                                   (long) (((float) (s_time - System.currentTimeMillis()) / 1000f)) +
                                   "s, new interval=" + tox_iteration_interval_ms + "ms");
                    }
                }
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------
                // ------- MAIN TOX LOOP ---------------------------------------------------------------


                try
                {
                    Thread.sleep(100); // wait a bit, for "something" to finish up in the native code
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    MainActivity.tox_kill();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    Thread.sleep(100); // wait a bit, for "something" to finish up in the native code
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        };

        ToxServiceThread.start();
    }

    // ------------------------------


    // --------------- JNI ---------------
    // --------------- JNI ---------------
    // --------------- JNI ---------------
    static void logger(int level, String text)
    {
        Log.i(TAG, text);
    }

    static String safe_string(byte[] in)
    {
        // Log.i(TAG, "safe_string:in=" + in);
        String out = "";

        try
        {
            out = new String(in, "UTF-8");  // Best way to decode using "UTF-8"
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "safe_string:EE:" + e.getMessage());
            try
            {
                out = new String(in);
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
                Log.i(TAG, "safe_string:EE2:" + e2.getMessage());
            }
        }

        // Log.i(TAG, "safe_string:out=" + out);
        return out;
    }
    // --------------- JNI ---------------
    // --------------- JNI ---------------
    // --------------- JNI ---------------

    void load_and_add_all_conferences()
    {
        long num_conferences = tox_conference_get_chatlist_size();
        Log.i(TAG, "load conferences at startup: num=" + num_conferences);

        long[] conference_numbers = tox_conference_get_chatlist();
        ByteBuffer cookie_buf3 = ByteBuffer.allocateDirect(CONFERENCE_ID_LENGTH * 2);

        int conf_ = 0;
        for (conf_ = 0; conf_ < num_conferences; conf_++)

        {
            cookie_buf3.clear();
            if (tox_conference_get_id(conference_numbers[conf_], cookie_buf3) == 0)
            {
                byte[] cookie_buffer = new byte[CONFERENCE_ID_LENGTH];
                cookie_buf3.get(cookie_buffer, 0, CONFERENCE_ID_LENGTH);
                String conference_identifier = bytes_to_hex(cookie_buffer);
                // Log.i(TAG, "load conference num=" + conference_numbers[conf_] + " cookie=" + conference_identifier +
                //           " offset=" + cookie_buf3.arrayOffset());

                final ConferenceDB conf2 = orma.selectFromConferenceDB().toList().get(0);
                //Log.i(TAG,
                //      "conference 0 in db:" + conf2.conference_identifier + " " + conf2.tox_conference_number + " " +
                //      conf2.name);

                new_or_updated_conference(conference_numbers[conf_], tox_friend_get_public_key__wrapper(0),
                                          conference_identifier, tox_conference_get_type(
                                conference_numbers[conf_])); // rejoin a saved conference

                //if (tox_conference_get_type(conference_numbers[conf_]) == TOX_CONFERENCE_TYPE_AV.value)
                //{
                //    // TODO: this returns error. check it
                //    long result = toxav_groupchat_disable_av(conference_numbers[conf_]);
                //    Log.i(TAG, "load conference num=" + conference_numbers[conf_] + " toxav_groupchat_disable_av res=" +
                //               result);
                //}

            }
        }
    }
}

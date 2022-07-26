/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2020 Zoff <zoff@zoff.cc>
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

import java.io.File;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Random;

import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.bytebuffer_to_hexstring;
import static com.zoffcc.applications.trifa.HelperGeneric.set_message_accepted_from_id;
import static com.zoffcc.applications.trifa.HelperMessage.set_message_queueing_from_id;
import static com.zoffcc.applications.trifa.HelperMessage.set_message_start_sending_from_id;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_filetransfer_kind;
import static com.zoffcc.applications.trifa.HelperMessage.update_single_message_from_messge_id;
import static com.zoffcc.applications.trifa.MainActivity.PREF__auto_accept_all_upto;
import static com.zoffcc.applications.trifa.MainActivity.PREF__auto_accept_image;
import static com.zoffcc.applications.trifa.MainActivity.PREF__auto_accept_video;
import static com.zoffcc.applications.trifa.MainActivity.sqldb;
import static com.zoffcc.applications.trifa.MainActivity.tox_file_control;
import static com.zoffcc.applications.trifa.MainActivity.tox_file_send;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_get_capabilities;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_INCOMING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_FILE_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_PREFIX;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_TMP_FILE_DIR;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CAPABILITY_DECODE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_ID_LENGTH;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_FTV2;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class HelperFiletransfer
{
    private static final String TAG = "trifa.Hlp.Filetransfer";

    public static String get_incoming_filetransfer_local_filename(String incoming_filename, String friend_pubkey_str)
    {
        String result = TrifaSetPatternActivity.filter_out_specials_from_filepath(incoming_filename);
        String wanted_full_filename_path = VFS_PREFIX + VFS_FILE_DIR + "/" + friend_pubkey_str;

        // Log.i(TAG, "check_auto_accept_incoming_filetransfer:start=" + incoming_filename + " " + result + " " +
        //           wanted_full_filename_path);

        File f1 = new File(wanted_full_filename_path + "/" + result);

        if (f1.exists())
        {
            Random random = new Random();
            long new_random_log = (long) random.nextInt() + (1L << 31);

            // Log.i(TAG, "check_auto_accept_incoming_filetransfer:new_random_log=" + new_random_log);

            String random_filename_addon = TrifaSetPatternActivity.filter_out_specials(
                    TrifaSetPatternActivity.bytesToString(TrifaSetPatternActivity.sha256(
                            TrifaSetPatternActivity.StringToBytes2("" + new_random_log))));

            // Log.i(TAG, "check_auto_accept_incoming_filetransfer:random_filename_addon=" + random_filename_addon);

            String extension = "";

            try
            {
                extension = result.substring(result.lastIndexOf("."));

                if (extension.equalsIgnoreCase("."))
                {
                    extension = "";
                }
            }
            catch (Exception e)
            {
                extension = "";
            }

            result = result + "_" + random_filename_addon + extension;

            // Log.i(TAG, "check_auto_accept_incoming_filetransfer:result=" + result);
        }

        return result;
    }

    static long insert_into_filetransfer_db(final Filetransfer f)
    {
        long row_id = -1;
        try
        {
            row_id = orma.insertIntoFiletransfer(f);
            // Log.i(TAG, "insert_into_filetransfer_db:row_id=" + row_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // Log.i(TAG, "insert_into_filetransfer_db:EE:" + e.getMessage());
            return -1;
        }

        try
        {
            long ft_id = -1;
            try
            {
                Statement statement = sqldb.createStatement();
                ResultSet rs = statement.executeQuery("SELECT id FROM Filetransfer where rowid='" + row_id + "'");
                if (rs.next())
                {
                    ft_id = rs.getLong("id");
                }

                try
                {
                    statement.close();
                }
                catch (Exception ignored)
                {
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return ft_id;
        }
        catch (Exception e)
        {
            // Log.i(TAG, "insert_into_filetransfer_db:EE:" + e.getMessage());
            // e.printStackTrace();
            return -1;
        }
    }

    static void update_filetransfer_db_full(final Filetransfer f)
    {
        orma.updateFiletransfer().
                idEq(f.id).
                tox_public_key_string(f.tox_public_key_string).
                direction(f.direction).
                file_number(f.file_number).
                kind(f.kind).
                state(f.state).
                path_name(f.path_name).
                message_id(f.message_id).
                file_name(f.file_name).
                fos_open(f.fos_open).
                filesize(f.filesize).
                current_position(f.current_position).
                tox_file_id_hex(f.tox_file_id_hex).
                execute();
    }

    public static boolean file_is_image(String filename_with_path)
    {
        try
        {
            String mimeType = URLConnection.guessContentTypeFromName(filename_with_path);

            if (mimeType != null)
            {
                if (mimeType.startsWith("image"))
                {
                    return true;
                }
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }

        return false;
    }

    public static boolean check_auto_accept_incoming_filetransfer(Message message)
    {
        try
        {
            String mimeType = URLConnection.guessContentTypeFromName(
                    get_filetransfer_filename_from_id(message.filetransfer_id).toLowerCase());
            // Log.i(TAG, "check_auto_accept_incoming_filetransfer:mime-type=" + mimeType);

            if (mimeType != null)
            {
                if (PREF__auto_accept_image)
                {
                    if (get_filetransfer_filesize_from_id(message.filetransfer_id) <=
                        6 * 1014 * 1024) // if file size is smaller than 6 MByte accept FT
                    {
                        if (mimeType.startsWith("image"))
                        {
                            if (get_filetransfer_state_from_id(message.filetransfer_id) == TOX_FILE_CONTROL_PAUSE.value)
                            {
                                // accept FT
                                set_filetransfer_accepted_from_id(message.filetransfer_id);
                                set_filetransfer_state_from_id(message.filetransfer_id, TOX_FILE_CONTROL_RESUME.value);
                                set_message_accepted_from_id(message.id);
                                set_message_state_from_id(message.id, TOX_FILE_CONTROL_RESUME.value);
                                tox_file_control(tox_friend_by_public_key__wrapper(message.tox_friendpubkey),
                                                 get_filetransfer_filenum_from_id(message.filetransfer_id),
                                                 TOX_FILE_CONTROL_RESUME.value);

                                // update message view
                                update_single_message_from_messge_id(message.id, true);
                                // Log.i(TAG, "check_auto_accept_incoming_filetransfer:image:accepted");
                                return true;
                            }
                        }
                    }
                }

                if (PREF__auto_accept_video)
                {
                    if (get_filetransfer_filesize_from_id(message.filetransfer_id) <=
                        20 * 1014 * 1024) // if file size is smaller than 20 MByte accept FT
                    {
                        if (mimeType.startsWith("video"))
                        {
                            if (get_filetransfer_state_from_id(message.filetransfer_id) == TOX_FILE_CONTROL_PAUSE.value)
                            {
                                // accept FT
                                set_filetransfer_accepted_from_id(message.filetransfer_id);
                                set_filetransfer_state_from_id(message.filetransfer_id, TOX_FILE_CONTROL_RESUME.value);
                                set_message_accepted_from_id(message.id);
                                set_message_state_from_id(message.id, TOX_FILE_CONTROL_RESUME.value);
                                tox_file_control(tox_friend_by_public_key__wrapper(message.tox_friendpubkey),
                                                 get_filetransfer_filenum_from_id(message.filetransfer_id),
                                                 TOX_FILE_CONTROL_RESUME.value);

                                // update message view
                                update_single_message_from_messge_id(message.id, true);
                                // Log.i(TAG, "check_auto_accept_incoming_filetransfer:video:accepted");
                                return true;
                            }
                        }
                    }
                }
            }

            if (PREF__auto_accept_all_upto)
            {
                if (get_filetransfer_filesize_from_id(message.filetransfer_id) <=
                    200 * 1014 * 1024) // if file size is smaller than 200 MByte accept FT
                {
                    if (get_filetransfer_state_from_id(message.filetransfer_id) == TOX_FILE_CONTROL_PAUSE.value)
                    {
                        // accept FT
                        set_filetransfer_accepted_from_id(message.filetransfer_id);
                        set_filetransfer_state_from_id(message.filetransfer_id, TOX_FILE_CONTROL_RESUME.value);
                        set_message_accepted_from_id(message.id);
                        set_message_state_from_id(message.id, TOX_FILE_CONTROL_RESUME.value);
                        tox_file_control(tox_friend_by_public_key__wrapper(message.tox_friendpubkey),
                                         get_filetransfer_filenum_from_id(message.filetransfer_id),
                                         TOX_FILE_CONTROL_RESUME.value);

                        // update message view
                        update_single_message_from_messge_id(message.id, true);
                        // Log.i(TAG, "check_auto_accept_incoming_filetransfer:video:accepted");
                        return true;
                    }
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    public static String get_filetransfer_filename_from_id(long filetransfer_id)
    {
        try
        {
            if (orma.selectFromFiletransfer().idEq(filetransfer_id).count() == 1)
            {
                return orma.selectFromFiletransfer().idEq(filetransfer_id).toList().get(0).file_name;
            }
            else
            {
                return "";
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static long get_filetransfer_filesize_from_id(long filetransfer_id)
    {
        try
        {
            if (orma.selectFromFiletransfer().idEq(filetransfer_id).count() == 1)
            {
                return orma.selectFromFiletransfer().idEq(filetransfer_id).toList().get(0).filesize;
            }
            else
            {
                return -1;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public static long get_filetransfer_state_from_id(long filetransfer_id)
    {
        try
        {
            if (orma.selectFromFiletransfer().idEq(filetransfer_id).count() == 1)
            {
                return orma.selectFromFiletransfer().idEq(filetransfer_id).toList().get(0).state;
            }
            else
            {
                return TOX_FILE_CONTROL_CANCEL.value;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return TOX_FILE_CONTROL_CANCEL.value;
        }
    }

    public static void set_filetransfer_accepted_from_id(long filetransfer_id)
    {
        try
        {
            orma.updateFiletransfer().idEq(filetransfer_id).ft_accepted(true).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_filetransfer_state_from_id(long filetransfer_id, int state)
    {
        try
        {
            orma.updateFiletransfer().idEq(filetransfer_id).state(state).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_message_state_from_id(long message_id, int state)
    {
        try
        {
            orma.updateMessage().idEq(message_id).state(state).execute();
            // Log.i(TAG, "set_message_state_from_id:message_id=" + message_id + " state=" + state);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_state_from_id:EE:" + e.getMessage());
        }
    }

    public static long get_filetransfer_filenum_from_id(long filetransfer_id)
    {
        try
        {
            if (orma.selectFromFiletransfer().idEq(filetransfer_id).count() == 1)
            {
                return orma.selectFromFiletransfer().idEq(filetransfer_id).toList().get(0).file_number;
            }
            else
            {
                return -1;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public static void set_filetransfer_for_message_from_friendnum_and_filenum(long friend_number, long file_number, long ft_id)
    {
        try
        {
            set_filetransfer_for_message_from_filetransfer_id(orma.selectFromFiletransfer().
                    tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    file_numberEq(file_number).
                    orderByIdDesc().toList().
                    get(0).id, ft_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_filetransfer_for_message_from_filetransfer_id(long filetransfer_id, long ft_id)
    {
        try
        {
            orma.updateMessage().filetransfer_idEq(filetransfer_id).filetransfer_id(ft_id).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void delete_filetransfers_from_friendnum_and_filenum(long friend_number, long file_number)
    {
        try
        {
            long del_ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    file_numberEq(file_number).
                    orderByIdDesc().toList().
                    get(0).id;
            // Log.i(TAG, "delete_ft:id=" + del_ft_id);
            delete_filetransfers_from_id(del_ft_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void delete_filetransfers_from_id(long filetransfer_id)
    {
        try
        {
            // Log.i(TAG, "delete_ft:id=" + filetransfer_id);
            orma.deleteFromFiletransfer().idEq(filetransfer_id).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void cancel_filetransfer_f(final Filetransfer f)
    {
        try
        {
            if (f == null)
            {
                return;
            }

            long ft_id = f.id;
            long msg_id = HelperMessage.get_message_id_from_filetransfer_id(ft_id);

            if (f.direction == TRIFA_FT_DIRECTION_INCOMING.value)
            {
                if ((f.kind == TOX_FILE_KIND_DATA.value) || (f.kind == TOX_FILE_KIND_FTV2.value))
                {
                    delete_filetransfer_tmpfile(ft_id);
                    HelperMessage.set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    set_filetransfer_for_message_from_filetransfer_id(ft_id, -1);
                    delete_filetransfers_from_id(ft_id);
                    try
                    {
                        if (f.id != -1)
                        {
                            HelperMessage.update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
                else // avatar FT
                {
                    set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_CANCEL.value);
                    HelperMessage.set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    delete_filetransfer_tmpfile(ft_id);
                    delete_filetransfers_from_id(ft_id);
                }
            }
            else // outgoing FT
            {
                if ((f.kind == TOX_FILE_KIND_DATA.value) || (f.kind == TOX_FILE_KIND_FTV2.value))
                {
                    HelperMessage.set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    set_filetransfer_for_message_from_filetransfer_id(ft_id, -1);
                    delete_filetransfers_from_id(ft_id);
                    try
                    {
                        if (f.id != -1)
                        {
                            HelperMessage.update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
                else // avatar FT
                {
                    set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_CANCEL.value);
                    if (msg_id > -1)
                    {
                        HelperMessage.set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    }
                    delete_filetransfer_tmpfile(ft_id);
                    delete_filetransfers_from_id(ft_id);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_filetransfer_db_current_position(final Filetransfer f)
    {
        orma.updateFiletransfer().
                tox_public_key_stringEq(f.tox_public_key_string).
                file_numberEq(f.file_number).
                stateNotEq(TOX_FILE_CONTROL_CANCEL.value).
                current_position(f.current_position).
                execute();
    }

    static void cancel_filetransfer(long friend_number, long file_number)
    {
        // Log.i(TAG, "FTFTFT:cancel_filetransfer");
        Filetransfer f = null;

        try
        {
            f = orma.selectFromFiletransfer().
                    file_numberEq(file_number).
                    tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    orderByIdDesc().
                    toList().get(0);

            if (f.direction == TRIFA_FT_DIRECTION_INCOMING.value)
            {
                if ((f.kind == TOX_FILE_KIND_DATA.value) || (f.kind == TOX_FILE_KIND_FTV2.value))
                {
                    long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
                    long msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
                    // delete tmp file
                    delete_filetransfer_tmpfile(friend_number, file_number);
                    // set state for FT in message
                    HelperMessage.set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    // remove link to any message
                    set_filetransfer_for_message_from_friendnum_and_filenum(friend_number, file_number, -1);
                    // delete FT in DB
                    // Log.i(TAG, "FTFTFT:002");
                    delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);

                    // update UI
                    // TODO: updates all messages, this is bad
                    // update_all_messages_global(false);
                    try
                    {
                        if (f.id != -1)
                        {
                            HelperMessage.update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
                else // avatar FT
                {
                    long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
                    long msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
                    set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_CANCEL.value);
                    HelperMessage.set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    // delete tmp file
                    delete_filetransfer_tmpfile(friend_number, file_number);
                    // delete FT in DB
                    // Log.i(TAG, "FTFTFT:003");
                    delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
                }
            }
            else // outgoing FT
            {
                if ((f.kind == TOX_FILE_KIND_DATA.value) || (f.kind == TOX_FILE_KIND_FTV2.value))
                {
                    long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
                    long msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
                    // set state for FT in message
                    HelperMessage.set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    // remove link to any message
                    set_filetransfer_for_message_from_friendnum_and_filenum(friend_number, file_number, -1);
                    // delete FT in DB
                    // Log.i(TAG, "FTFTFT:OGFT:002");
                    delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);

                    // update UI
                    try
                    {
                        if (f.id != -1)
                        {
                            HelperMessage.update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
                else // avatar FT
                {
                    long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
                    long msg_id = HelperMessage.get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
                    set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_CANCEL.value);

                    if (msg_id > -1)
                    {
                        HelperMessage.set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    }

                    // delete tmp file
                    delete_filetransfer_tmpfile(friend_number, file_number);
                    // delete FT in DB
                    // Log.i(TAG, "FTFTFT:OGFT:003");
                    delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void set_all_filetransfers_inactive()
    {
        try
        {
            List<Filetransfer> fts_active = orma.selectFromFiletransfer().file_numberNotEq(-1).toList();
            for (Filetransfer f : fts_active)
            {
                // Log.i(TAG, "set_all_filetransfers_inactive:cancel:id=" + f.tox_file_id_hex + " filename=" + f.file_name);
                cancel_filetransfer_f(f);
            }

            orma.updateFiletransfer().
                    file_number(-1).
                    state(TOX_FILE_CONTROL_CANCEL.value).
                    execute();
            Log.i(TAG, "set_all_filetransfers_inactive");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_all_filetransfers_inactive:EE:" + e.getMessage());
        }
    }


    public static long get_filetransfer_id_from_friendnum_and_filenum(long friend_number, long file_number)
    {
        try
        {
            // Log.i(TAG, "get_filetransfer_id_from_friendnum_and_filenum:friend_number=" + friend_number + " file_number=" + file_number);
            long ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    file_numberEq(file_number).
                    orderByIdDesc().
                    toList().
                    get(0).id;
            // Log.i(TAG, "get_filetransfer_id_from_friendnum_and_filenum:ft_id=" + ft_id);
            // ----- DEBUG -----
            //            try
            //            {
            //                Filetransfer ft_tmp = orma.selectFromFiletransfer().idEq(ft_id).get(0);
            //                //if (ft_tmp.kind != TOX_FILE_KIND_AVATAR.value)
            //                //{
            //                Log.i(TAG, "get_filetransfer_id_from_friendnum_and_filenum:ft full=" + ft_tmp);
            //                //}
            //            }
            //            catch (Exception e)
            //            {
            //                e.printStackTrace();
            //                Log.i(TAG, "get_filetransfer_id_from_friendnum_and_filenum:EE2:" + e.getMessage());
            //            }
            // ----- DEBUG -----
            return ft_id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_filetransfer_id_from_friendnum_and_filenum:EE:" + e.getMessage());
            return -1;
        }
    }

    public static void delete_filetransfer_tmpfile(long friend_number, long file_number)
    {
        try
        {
            delete_filetransfer_tmpfile(orma.selectFromFiletransfer().tox_public_key_stringEq(
                    HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).file_numberEq(
                    file_number).toList().get(0).id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void delete_filetransfer_tmpfile(long filetransfer_id)
    {
        try
        {
            Filetransfer ft = orma.selectFromFiletransfer().idEq(filetransfer_id).toList().get(0);

            File f1 = new File(VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + ft.tox_public_key_string + "/" + ft.file_name);
            f1.delete();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_filetransfer_start_sending_from_id(long filetransfer_id)
    {
        try
        {
            orma.updateFiletransfer().idEq(filetransfer_id).ft_outgoing_started(true).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void start_outgoing_ft(Message element)
    {
        try
        {
            set_message_queueing_from_id(element.id, false);

            // accept FT
            set_message_start_sending_from_id(element.id);
            set_filetransfer_start_sending_from_id(element.filetransfer_id);

            try
            {
                element._swing_ok.setVisible(false);
            }
            catch (Exception ee)
            {
            }

            // update message view
            update_single_message_from_messge_id(element.id, true);

            Filetransfer ft = orma.selectFromFiletransfer().
                    idEq(element.filetransfer_id).
                    orderByIdDesc().toList().get(0);

            Log.i(TAG, "MM2MM:8:ft.filesize=" + ft.filesize + " ftid=" + ft.id + " ft.mid=" + ft.message_id + " mid=" +
                       element.id);

            // ------ DEBUG ------
            Log.i(TAG, "MM2MM:8a:ft full=" + ft);
            // ------ DEBUG ------

            ByteBuffer file_id_buffer = ByteBuffer.allocateDirect(TOX_FILE_ID_LENGTH);
            MainActivity.tox_messagev3_get_new_message_id(file_id_buffer);

            final String file_id_buffer_hex = bytebuffer_to_hexstring(file_id_buffer, true);
            Log.i(TAG, "TOX_FILE_ID_LENGTH=" + TOX_FILE_ID_LENGTH + " file_id_buffer_hex=" + file_id_buffer_hex);
            ft.tox_file_id_hex = file_id_buffer_hex;

            // actually start sending the file to friend
            long file_number = -1;
            if (TOX_CAPABILITY_DECODE(
                    tox_friend_get_capabilities(tox_friend_by_public_key__wrapper(element.tox_friendpubkey))).ftv2)
            {
                Log.i(TAG, "TOX_FILE_KIND_FTV2");
                file_number = tox_file_send(tox_friend_by_public_key__wrapper(element.tox_friendpubkey),
                                            TOX_FILE_KIND_FTV2.value, ft.filesize, file_id_buffer, ft.file_name,
                                            ft.file_name.length());
                ft.kind = TOX_FILE_KIND_FTV2.value;
                element.filetransfer_kind = TOX_FILE_KIND_FTV2.value;
            }
            else
            {
                Log.i(TAG, "TOX_FILE_KIND_DATA");
                file_number = tox_file_send(tox_friend_by_public_key__wrapper(element.tox_friendpubkey),
                                            ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA.value, ft.filesize, file_id_buffer,
                                            ft.file_name, ft.file_name.length());
                ft.kind = ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA.value;
                element.filetransfer_kind = TOX_FILE_KIND_DATA.value;
            }
            // TODO: handle errors from tox_file_send() here -------

            update_message_in_db_filetransfer_kind(element);

            // @formatter:off
            Log.D(TAG,
                  "DEBUG_FT:OUT:file_chunk_request:file_number=" +
                  file_number +
                  " fn=" + tox_friend_by_public_key__wrapper(element.tox_friendpubkey) +
                  " filetransfer_id=" + element.filetransfer_id+
                  " pk="+element.tox_friendpubkey+
                  " path_name="+ft.path_name+
                  " file_name=" + ft.file_name
            );
            // @formatter:on

            if (file_number < 0)
            {
                Log.i(TAG, "tox_file_send:EE:" + file_number);

                // cancel FT
                set_filetransfer_state_from_id(element.filetransfer_id, TOX_FILE_CONTROL_CANCEL.value);
                set_message_state_from_id(element.id, TOX_FILE_CONTROL_CANCEL.value);
                // update message view
                update_single_message_from_messge_id(element.id, true);
            }
            else
            {

                Log.i(TAG, "MM2MM:9:new filenum=" + file_number);

                // update the tox file number in DB -----------
                ft.file_number = file_number;
                update_filetransfer_db_full(ft);
                // update the tox file number in DB -----------
            }

            Log.i(TAG, "button_ok:OnTouch:009:f_num=" + file_number);
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
            Log.i(TAG, "MM2MM:EE1:" + e2.getMessage());
        }
    }
}

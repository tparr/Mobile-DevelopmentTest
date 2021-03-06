package parrtim.applicationfundamentals.helper;

import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;

import parrtim.applicationfundamentals.models.ConversationInfo;
import parrtim.applicationfundamentals.models.InboxInfo;
import parrtim.applicationfundamentals.models.ThreadInfo;

import static android.content.Context.TELEPHONY_SERVICE;

public class SMSUtil {

    private static SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");;

    public static ArrayList<InboxInfo> getSMSInbox(Context context) {
        ArrayList<InboxInfo> messages = new ArrayList<>();
        Cursor cur = context.getContentResolver().query(Telephony.Sms.Inbox.CONTENT_URI, null, null, null, null);

        if (cur == null) return messages;

        int addressIndex = cur.getColumnIndex("address");
        int bodyIndex = cur.getColumnIndex("body");
        int dateIndex = cur.getColumnIndex("date");

        while (cur.moveToNext())
        {
            String address = cur.getString(addressIndex);
            String body = cur.getString(bodyIndex);
            try {
                Date date = simpleDate.parse(simpleDate.format(cur.getLong(dateIndex)));
                messages.add(new InboxInfo(address, body, date, true));
            } catch (ParseException e) {
                e.printStackTrace();
                Log.d("Error", e.toString());
            }
        }

        cur.close();
        return messages;
    }

    public static ArrayList<InboxInfo> getSMSConversations(Context context, String sender)
    {
        boolean noSender = Objects.equals(sender, "") || sender == null;

        ArrayList<InboxInfo> messages = new ArrayList<>();
        Cursor inboxCursor = context.getContentResolver().query(
                Telephony.Sms.Inbox.CONTENT_URI,
                new String[] { Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.DATE, },
                noSender ? null : Telephony.Sms.Inbox.ADDRESS + " = ?",
                noSender ? null : new String[] { sender },
                "DATE DESC");

        if (inboxCursor != null) {

            int addressIndex = inboxCursor.getColumnIndex("address");
            int bodyIndex = inboxCursor.getColumnIndex("body");
            int dateIndex = inboxCursor.getColumnIndex("date");

            while (inboxCursor.moveToNext())
            {
                String address = inboxCursor.getString(addressIndex);
                String body = inboxCursor.getString(bodyIndex);

                try {
                    Date date = simpleDate.parse(simpleDate.format(inboxCursor.getLong(dateIndex)));
                    messages.add(new InboxInfo(address, body, date, true));
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.d("Error", e.toString());
                }
            }
            inboxCursor.close();
        }

        TelephonyManager systemService = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        String line1Number = systemService.getLine1Number();
        boolean validNumber = line1Number != null && !Objects.equals(line1Number, "");

        Cursor sentCursor = context.getContentResolver().query(
                Telephony.Sms.Sent.CONTENT_URI,
                new String[] { Telephony.Sms.Sent.ADDRESS, Telephony.Sms.Sent.BODY, Telephony.Sms.Sent.DATE, },
                !validNumber ? Telephony.Sms.Sent.ADDRESS + " = ?" : null,
                !validNumber ? new String[] { line1Number }: null,
                "DATE DESC");

        if (sentCursor != null) {

            int addressIndex = sentCursor.getColumnIndex("address");
            int bodyIndex = sentCursor.getColumnIndex("body");
            int dateIndex = sentCursor.getColumnIndex("date");

            while (sentCursor.moveToNext())
            {
                String address = sentCursor.getString(addressIndex);
                String body = sentCursor.getString(bodyIndex);

                try {
                    Date date = simpleDate.parse(simpleDate.format(sentCursor.getLong(dateIndex)));
                    messages.add(new InboxInfo(address, body, date, false));
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.d("Error", e.toString());
                }
            }
            sentCursor.close();
        }

        Collections.sort(messages, new Comparator<InboxInfo>() {
            @Override
            public int compare(InboxInfo inboxInfo, InboxInfo t1) {
                return inboxInfo.Date.compareTo(t1.Date);
            }
        });

        return messages;
    }

    public static ArrayList<InboxInfo> getSMSConversations(Context context) {
        return getSMSConversations(context, null);
    }

    public static ArrayList<InboxInfo> getSMSSent(Context context) {
        ArrayList<InboxInfo> messages = new ArrayList<>();
        Cursor cur = context.getContentResolver().query(Telephony.Sms.Sent.CONTENT_URI, null, null, null, null);

        if (cur == null) return messages;

        int addressIndex = cur.getColumnIndex("address");
        int bodyIndex = cur.getColumnIndex("body");
        int dateIndex = cur.getColumnIndex("date");

        while (cur.moveToNext())
        {
            String address = cur.getString(addressIndex);
            String body = cur.getString(bodyIndex);
            try {
                Date date = simpleDate.parse(simpleDate.format(cur.getLong(dateIndex)));
                messages.add(new InboxInfo(address, body, date, false));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        cur.close();
        return messages;
    }

    public static ArrayList<ThreadInfo> getSMSThreads(Context context)
    {
        ArrayList<ConversationInfo> messages = new ArrayList<>();
        Cursor cur = context.getContentResolver().query(Telephony.Sms.Conversations.CONTENT_URI, null, null, null, null);

        int thread_index = cur.getColumnIndex("thread_id");
        int snippet_index = cur.getColumnIndex("snippet");
        int msg_count_index = cur.getColumnIndex("msg_count");

        while (cur.moveToNext())
        {
            String snippet = cur.getString(snippet_index);
            String msg_count = cur.getString(msg_count_index);
            String thread_id = cur.getString(thread_index);
            messages.add(new ConversationInfo(snippet, msg_count, thread_id));
        }

        cur.close();

        ArrayList<ThreadInfo> threads = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(Telephony.Sms.CONTENT_URI, new String[] { Telephony.Sms.THREAD_ID, Telephony.Sms.ADDRESS, Telephony.Sms.DATE, Telephony.Sms.BODY }, null, null, "date desc");

        HashSet<String> addresses = new HashSet<>();

        while (cursor.moveToNext())
        {
            String thread_id = cursor.getString(cursor.getColumnIndex("thread_id"));
            String address = cursor.getString(cursor.getColumnIndex("address"));
            Date date = null;
            try {
                date = simpleDate.parse(simpleDate.format(cursor.getLong(cursor.getColumnIndex("date"))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            
            if (!addresses.contains(thread_id)) {
                threads.add(new ThreadInfo(thread_id, address, null, null, date));
                addresses.add(thread_id);
            }
        }

        cursor.close();

        for (ThreadInfo thread : threads)
        {
            for (ConversationInfo conversation: messages)
            {
                if (Objects.equals(thread.Thread_ID, conversation.Thread_ID))
                {
                    thread.Message_Count = conversation.Message_Count;
                    thread.Snippet = conversation.Snippet;
                    break;
                }
            }
        }

        return threads;
    }
}
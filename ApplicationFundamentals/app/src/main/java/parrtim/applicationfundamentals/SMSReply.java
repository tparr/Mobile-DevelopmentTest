package parrtim.applicationfundamentals;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.support.v4.content.FileProvider.getUriForFile;

public class SMSReply extends Activity {

    String receivedMessage;
    String receivedNumber;
    String msg = "ReplyActivity";
    DictionaryOpenHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsreply);

        Intent intent = getIntent();
        receivedMessage = intent.getStringExtra("message");
        receivedNumber = intent.getStringExtra("number");

        TextView messageViewById = (TextView) findViewById(R.id.inboxMessage);
        messageViewById.setText(receivedMessage);
        TextView numberViewById = (TextView) findViewById(R.id.inboxNumber);
        numberViewById.setText(receivedNumber);

        database = new DictionaryOpenHelper(getApplicationContext());

        SQLiteDatabase readableDatabase = database.getReadableDatabase();

        Cursor results = readableDatabase.query("Draft", new String[]{ "Number", "ReceivedMessage", "DraftMessage" }, "Number = " + receivedNumber + " AND " + "ReceivedMessage = \"" + receivedMessage + "\"", null, null, null, null);

        if (results.moveToNext()) {
            TextView draftMessage = (TextView) findViewById(R.id.replyMessage);
            String replyMessage = results.getString(2);
            draftMessage.setText(replyMessage);
        }

        results.close();

        Log.d(msg, "The Reply onCreate() event");
    }

    /** Called when the activity is no longer visible. */
    @Override
    protected void onStop() {
        super.onStop();

        TextView draftMessage = (TextView) findViewById(R.id.replyMessage);

        SQLiteDatabase writeableDatabase = database.getWritableDatabase();
        writeableDatabase.execSQL(
                "UPDATE Draft\n" +
                "SET DraftMessage='" + draftMessage.getText().toString() + "'\n" +
                "WHERE Number=\"" + receivedNumber + "\" AND ReceivedMessage=\"" + receivedMessage + "\";" +

                "\n" +
                "INSERT INTO Draft (Number, ReceivedMessage, DraftMessage)\n" +
                "SELECT '" + receivedNumber + "', \"" + receivedMessage + "\", '" + draftMessage.getText().toString() + "  \n" +
                "WHERE (Select Changes() = 0);");

        database.close();

        Log.d(msg, "The Reply onStop() event");
    }

    /** Called just before the activity is destroyed. */
    @Override
    public void onDestroy() {
        super.onDestroy();
        database.close();
        Log.d(msg, "The Reply onDestroy() event");
    }

    public void OnCameraButtonClick(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }
            catch (IOException ex)
            {
                Toast.makeText(getApplicationContext(), "Photo was not saved", Toast.LENGTH_LONG).show();
            }

            if (photoFile != null) {
                Uri photoURI = getUriForFile(this,
                        "parrtim.applicationfundamentals.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}

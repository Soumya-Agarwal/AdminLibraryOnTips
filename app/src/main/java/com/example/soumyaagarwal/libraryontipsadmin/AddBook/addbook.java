package com.example.soumyaagarwal.libraryontipsadmin.AddBook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soumyaagarwal.libraryontipsadmin.Internet.ConnectivityReceiver;
import com.example.soumyaagarwal.libraryontipsadmin.MyApplication;
import com.example.soumyaagarwal.libraryontipsadmin.R;
import com.example.soumyaagarwal.libraryontipsadmin.Services.UploadBookData;
import com.example.soumyaagarwal.libraryontipsadmin.Services.UploadPhotoAndFile;
import com.example.soumyaagarwal.libraryontipsadmin.ViewBook.AddBook;
import com.example.soumyaagarwal.libraryontipsadmin.admin_page;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class addbook extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {
    private ArrayList<String> docPaths;
    private String selectedFilePath;
    private String csvExt[] = {".csv"};
    private String CSVFileName = "CSVFileName";

    DatabaseReference mDatabase, dbr;
    EditText booktitle, bookauthor, bookISBN;
    AutoCompleteTextView booksubject, bookshelfnumber, bookbranch;
    Button done;
    String[] subjects, branches, shelves;
    LinearLayout parent;

    String author, title, ISBN, subject, shelfnumber, branch;

    private static final int PICK_IMAGE_REQUEST = 234;
    private static final int PICK_PDF_REQUEST = 235;
    private ImageButton addimg, addpdf;
    private Uri filePath = Uri.EMPTY;
    private Uri filePathPDF = Uri.EMPTY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addbook);

        bookauthor = (EditText) findViewById(R.id.bookauthor);
        bookISBN = (EditText) findViewById(R.id.bookISBN);
        bookshelfnumber = (AutoCompleteTextView) findViewById(R.id.bookshelfnumber);
        booktitle = (EditText) findViewById(R.id.booktitle);
        booksubject = (AutoCompleteTextView) findViewById(R.id.booksubject);
        bookbranch = (AutoCompleteTextView) findViewById(R.id.bookbranch);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        addimg = (ImageButton) findViewById(R.id.uploadimage);
        addpdf = (ImageButton) findViewById(R.id.uploadpdf);
        done = (Button) findViewById(R.id.done);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPost();
            }
        });

        subjects = getResources().getStringArray(R.array.subjects);
        ArrayAdapter<String> adaptersubject = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, subjects);
        booksubject.setAdapter(adaptersubject);
        booksubject.setThreshold(1);//will start working from first character
        booksubject.setTextColor(Color.BLACK);

        branches = getResources().getStringArray(R.array.branches);
        ArrayAdapter<String> adapterbranch = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, branches);
        bookbranch.setAdapter(adapterbranch);
        bookbranch.setThreshold(1);//will start working from first character
        bookbranch.setTextColor(Color.BLACK);

        shelves = getResources().getStringArray(R.array.shelves);
        ArrayAdapter<String> adaptershelf = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, shelves);
        bookshelfnumber.setAdapter(adaptershelf);
        bookshelfnumber.setThreshold(1);//will start working from first character
        bookshelfnumber.setTextColor(Color.BLACK);

        addimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        addpdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Upload PDF
                showPdfChooser();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.csvupload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.csvupload:
                //TODO:UPLOADCSV
                FilePickerBuilder.getInstance().setMaxCount(1)
                        .setActivityTheme(R.style.AppTheme).enableDocSupport(false)
                        .addFileSupport("CSV",csvExt, R.drawable.csv)
                        .pickFile(addbook.this);

        }
                return true;
    }

    private void startPost() {

        if (!Arrays.asList(subjects).contains(booksubject.getText().toString().trim())) {
            booksubject.setText("");
        }

        if (!Arrays.asList(branches).contains(bookbranch.getText().toString().trim())) {
            bookbranch.setText("");
        }

        if (!Arrays.asList(subjects).contains(booksubject.getText().toString().trim())) {
            booksubject.setText("");
        }

        author = bookauthor.getText().toString().trim();
        title = booktitle.getText().toString().trim();
        ISBN = bookISBN.getText().toString().trim();
        subject = booksubject.getText().toString().trim();
        shelfnumber = bookshelfnumber.getText().toString().trim();
        branch = bookbranch.getText().toString().trim();

        if (!(TextUtils.isEmpty(author) || TextUtils.isEmpty(title) || TextUtils.isEmpty(ISBN) || TextUtils.isEmpty(subject) || TextUtils.isEmpty(shelfnumber) || TextUtils.isEmpty(branch)) && filePath == Uri.EMPTY && filePathPDF == Uri.EMPTY) {
            Toast.makeText(addbook.this, "Choose an Image", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(author) || TextUtils.isEmpty(title) || TextUtils.isEmpty(ISBN) || TextUtils.isEmpty(subject) || TextUtils.isEmpty(shelfnumber) || TextUtils.isEmpty(branch)) {
            Toast.makeText(addbook.this, "Field cannot be Left Empty", Toast.LENGTH_SHORT).show();
        } else {
            boolean isConnected = ConnectivityReceiver.isConnected();
            showSnack(isConnected);

        }
    }

    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            dbr = mDatabase.child("Book").child(ISBN);

            AddBook addBook = new AddBook(title,author,shelfnumber,subject,"0","0","0","0",branch);
            dbr.setValue(addBook);

            Intent serviceIntent = new Intent(this, UploadPhotoAndFile.class);
            serviceIntent.putExtra("filepathpdf",filePathPDF);
            serviceIntent.putExtra("filepathimg",filePath);
            serviceIntent.putExtra("isbn",ISBN);
            startService(serviceIntent);

            Toast.makeText(addbook.this, "Book Added Successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(addbook.this, addcopies.class);
            intent.putExtra("ISBN_No", ISBN);
            startActivity(intent);
        } else {
            message = "Sorry! Not connected to internet";
            color = Color.RED;

            Snackbar snackbar = Snackbar
                    .make(parent, message, Snackbar.LENGTH_LONG);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void showPdfChooser() {
        Intent intent = new Intent();
        intent.setType("pdf/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FilePickerConst.REQUEST_CODE_DOC) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    docPaths = new ArrayList<>();
                    docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                    selectedFilePath = docPaths.get(0);
                    if(selectedFilePath.substring(selectedFilePath.lastIndexOf('.')).equals(".csv"))
                    {
                        Intent intent = new Intent(addbook.this,UploadBookData.class);
                        intent.putExtra(CSVFileName,selectedFilePath);
                        startService(intent);
                    }
                    else
                    {
                        Toast.makeText(addbook.this,"Only CSV files are allowed",Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            //data.getType()
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                addimg.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePathPDF = data.getData();
            addpdf.setImageResource(R.drawable.addpdf);

        }
    }

    /*private void uploadImage() {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            StorageReference riversRef = mStorageRef.child("image").child(ISBN);
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
    }

    private void uploadPDF() {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            StorageReference riversRef = mStorageRef.child("pdf").child(ISBN);
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
    }*/

    @Override
    public void onBackPressed() {
        startActivity(new Intent(addbook.this, admin_page.class));
        finish();
        //  db.removeEventListener(childEventListener);

    }
}
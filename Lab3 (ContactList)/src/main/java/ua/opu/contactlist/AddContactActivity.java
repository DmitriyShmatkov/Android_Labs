package ua.opu.contactlist;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddContactActivity extends AppCompatActivity {

    private static final int IMAGE_CAPTURE_REQUEST_CODE = 7777;

    private ImageView profileImage; // элемент UI для фотографии контакта
    private Uri profileImageUri;    // путь к файлу с фотографией

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        setWindow();

        // получаем ссылки на элементы UI
        profileImage = findViewById(R.id.profile_image);
        EditText contactName = findViewById(R.id.name_et);      // поле имени контакта
        EditText contactEmail = findViewById(R.id.email_et);    // поле почты контакта
        EditText contactPhone = findViewById(R.id.phone_et);    // поле номера телефона контакта
        Button addButton = findViewById(R.id.button_add);       // кнопка добавления контакта
        Button photoButton = findViewById(R.id.button_camera);  // кнопка для фото
        Button cancelButton = findViewById(R.id.button_cancel); // кнопка возврата


        // вешаем слушатели на кнопки

        addButton.setOnClickListener(v -> {
            // проверяем чтоб все поля были заполнены
            if (contactName.getText().toString().equals("")) {
                Toast.makeText(this, "Name field is empty", Toast.LENGTH_SHORT).show();
            } else if (contactEmail.getText().toString().equals("")) {
                Toast.makeText(this, "Email field is empty", Toast.LENGTH_SHORT).show();
            } else if (contactPhone.getText().toString().equals("")) {
                Toast.makeText(this, "Phone field is empty", Toast.LENGTH_SHORT).show();
            } else {
                // формируем Intent и отправляем данные обратно
                Intent intent = new Intent();

                intent.putExtra(Intent.EXTRA_USER, contactName.getText().toString());
                intent.putExtra(Intent.EXTRA_EMAIL, contactEmail.getText().toString());
                intent.putExtra(Intent.EXTRA_PHONE_NUMBER, contactPhone.getText().toString());
                // если фото не было сделано, передаём null
                intent.putExtra(Intent.EXTRA_ORIGINATING_URI,
                        profileImageUri == null ? null : profileImageUri.toString());

                setResult(RESULT_OK, intent);
                finish();
            }
        });

        photoButton.setOnClickListener(v -> {
            // запрашиваем ОС открыть камеру, чтобы сделать фото
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Error while trying to open camera app", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // проверяем было ли сделано фото
        if (requestCode == IMAGE_CAPTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // получаем сделанное фото
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                // меняем ImageView с изображением контакта
                profileImage.setImageBitmap(imageBitmap);

                String filename = "contact_" + System.currentTimeMillis() + ".png";

                File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), filename);
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(outputFile);
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

                    // сохраняем путь к файлу в формате Uri
                    profileImageUri = Uri.fromFile(outputFile);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }

    private void setWindow() {
        // Метод устанавливает StatusBar в цвет фона
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.activity_background));

        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}

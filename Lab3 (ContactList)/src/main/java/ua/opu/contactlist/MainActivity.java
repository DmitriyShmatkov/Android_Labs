package ua.opu.contactlist;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int ADD_CONTACT_REQUEST_CODE = 200;

    private List<Contact> contactList;          // модель источника данных
    private ContactsAdapter contactsAdapter;    // адаптер для спискового элемента

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setWindow();

        // получаем ссылки на элементы UI
        RecyclerView recyclerView = findViewById(R.id.list);
        FloatingActionButton addContactButton = findViewById(R.id.fab);

        // инициализируем поля класса
        contactList = new ArrayList<>();
        contactsAdapter = new ContactsAdapter(contactList);

        // усталавниваем слушатель на кнопку "+" (открытие Activity добавления контакта)
        addContactButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddContactActivity.class);
            startActivityForResult(intent, ADD_CONTACT_REQUEST_CODE);
        });

        // настраиваем RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(contactsAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // необходимые проверки, создание нового объекта Contact и добавление его в список,
        // оповещение адептера об изменении источника данных
        if (requestCode == ADD_CONTACT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    // получаем данные из Intent
                    String name = data.getStringExtra(Intent.EXTRA_USER);
                    String email = data.getStringExtra(Intent.EXTRA_EMAIL);
                    String phone = data.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    // если URI фото равен null, записываем null, иначе парсим URI
                    Uri imageUri = data.getStringExtra(Intent.EXTRA_ORIGINATING_URI) == null ?
                            null : Uri.parse(data.getStringExtra(Intent.EXTRA_ORIGINATING_URI));

                    // создаём новый контакт, добавляем его в список и уведомляем адаптер
                    // об изменении источника данных
                    Contact contact = new Contact(name, email, phone, imageUri);
                    contactList.add(contact);
                    contactsAdapter.notifyDataSetChanged();
                }
            }
        }
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

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public void verifyStoragePermissions() {
        // Проверяем наличие разрешения на запись во внешнее хранилище
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // Запрашиваем разрешение у пользователя
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        verifyStoragePermissions();
    }
}
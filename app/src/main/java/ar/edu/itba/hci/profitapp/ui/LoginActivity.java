package ar.edu.itba.hci.profitapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import ar.edu.itba.hci.profitapp.App;
import ar.edu.itba.hci.profitapp.databinding.ActivityLoginBinding;
import ar.edu.itba.hci.profitapp.repository.Resource;
import ar.edu.itba.hci.profitapp.repository.Status;
import ar.edu.itba.hci.profitapp.api.model.Credentials;
import ar.edu.itba.hci.profitapp.api.model.Error;

public class LoginActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "ar.edu.itba.hci.profitapp.message";

    public static final String TAG = "UI";
    private App app;
    private ActivityLoginBinding activityLoginBinding;
    private boolean didLoginFromLink = false;
    private int routineId = - 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = ((App) getApplication());

        Intent linkIntent = getIntent();

        if(linkIntent != null && linkIntent.getData() != null){
            didLoginFromLink = true;
            Uri linkData = linkIntent.getData();
            routineId = Integer.parseInt(linkData.getLastPathSegment()); //se inicia por un link
        }

        if(app.getPreferences().getAuthToken() != null) {
            if(didLoginFromLink){
                startDetailActivity();
            }
            else {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            finish();
        }

        activityLoginBinding = ActivityLoginBinding.inflate(getLayoutInflater());

        setContentView(activityLoginBinding.getRoot());

//        VideoView videoview = (VideoView) findViewById(R.id.videoView);
//        Uri uri = Uri.parse("https://assets.mixkit.co/videos/preview/mixkit-man-training-by-the-sea-14065-large.mp4");
//        videoview.setVideoURI(uri);
//        videoview.start();

        activityLoginBinding.usernameInput.setOnClickListener(v -> {
            activityLoginBinding.usernameContainer.setError(null);
        });

        activityLoginBinding.loginButton.setOnClickListener(v -> {
            //TODO validar user y password antes de llamar a la api. Mostrar mensajes de error en caso que esté mal el input
            Credentials credentials = new Credentials(activityLoginBinding.usernameContainer.getEditText().getText().toString(), activityLoginBinding.passwordContainer.getEditText().getText().toString());
            app.getUserRepository().login(credentials).observe(this, r -> {
                if (r.getStatus() == Status.SUCCESS) {
                    app.getPreferences().setAuthToken(r.getData().getToken());

                    if(didLoginFromLink){
                        startDetailActivity();
                    } else {
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                    }

                } else {
                    defaultResourceHandler(r);
                }
            });
        });
    }

    private void startDetailActivity(){
        Intent detailIntent = new Intent(this, RoutineActivity.class);
        detailIntent.putExtra(EXTRA_MESSAGE, routineId);
        startActivity(detailIntent);
    }

    private void defaultResourceHandler(Resource<?> resource) {
        switch (resource.getStatus()) {
            case LOADING:
                Log.d(TAG, "Loading");
                break;
            case ERROR:
                Error error = resource.getError();
                String message = "Error";
                Log.d(TAG, message);
                activityLoginBinding.usernameContainer.setError(resource.getError().getDescription());

                break;
        }
    }
}
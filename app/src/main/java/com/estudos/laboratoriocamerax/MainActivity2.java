package com.estudos.laboratoriocamerax;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.common.util.concurrent.ListenableFuture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Teste da api CameraX em Java.
 */
public class MainActivity2 extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 10;

    private ImageCapture mImageCapture;

    private File mOutputDirectory;
    private ExecutorService mCameraExecutor;

    private PreviewView mViewFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        if (allPermissionsGranted()){
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    REQUEST_CODE_PERMISSION
            );
        }

        mViewFinder = findViewById(R.id.viewFinder2);
        Button btn = findViewById(R.id.camera_capture_button2);
        btn.setOnClickListener(x -> {
            takePhoto();
        });

        mOutputDirectory = getOutputDirectory();
        mCameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void takePhoto(){
        if (mImageCapture == null)
            return;

        File file = new File(
                mOutputDirectory,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(System.currentTimeMillis())
        );

         ImageCapture.OutputFileOptions options = new ImageCapture
                 .OutputFileOptions
                 .Builder(file)
                 .build();

         mImageCapture.takePicture(options, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
             @Override
             public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                 Uri uri = Uri.fromFile(file);
                 String msg = "Foto salva com sucesso!";
                 Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
             }

             @Override
             public void onError(@NonNull ImageCaptureException exception) {
                 String msg = String.format("Erro ao salvar a foto: %s", exception.getMessage());
                 Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
             }
         });
    }

    private void startCamera(){
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {

            try {
                // disponibilizando o provedor da camera
                ProcessCameraProvider provider = cameraProviderFuture.get();

                // configurando o caso de uso de visualização
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(mViewFinder.createSurfaceProvider());

                // configurando o caso de uso para captura de fotos
                // aqui é possível habilitar o flash, ou configurar a proporção da tela...
                mImageCapture = new ImageCapture.Builder().build();

                // escolhendo uma das cameras do dispositivo
                CameraSelector cameraSelector = new CameraSelector
                        .Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // desvinculando o provedor com o ciclo de vida da atividade
                provider.unbindAll();

                // vinculando o provedor ao ciclo de vida da atividade
                provider.bindToLifecycle(this, cameraSelector, preview, mImageCapture);

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(this));
    }

    private boolean allPermissionsGranted(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private File getOutputDirectory(){
        File[] arrayDim = ContextCompat.getExternalFilesDirs(this, Environment.DIRECTORY_DCIM);
        return arrayDim[0];
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraExecutor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION){
            if (allPermissionsGranted()){
                startCamera();
            } else {
                String msg = "Permissão não concedida!";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        }
    }
}
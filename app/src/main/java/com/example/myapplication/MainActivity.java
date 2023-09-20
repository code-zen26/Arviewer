package com.example.myapplication;

import static android.os.Build.VERSION_CODES.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_MODEL_REQUEST_CODE = 1;

    private ArFragment arFragment;
    private Uri modelUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
        Button uploadButton = findViewById( R.id.upload_button );

        uploadButton.setOnClickListener( v -> openFileChooser() );

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (modelUri == null) {
                return;
            }

            ModelRenderable.builder()
                    .setSource(this, modelUri)
                    .build()
                    .thenAccept(modelRenderer -> addModelToScene(hitResult.createAnchor(), modelRenderer))
                    .exceptionally(throwable -> {
                        // Handle model loading error
                        return null;
                    });
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_MODEL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_MODEL_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            modelUri = data.getData();
        }
    }

    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.select();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (modelUri == null) {
            arFragment.getPlaneDiscoveryController().show();
            arFragment.getPlaneDiscoveryController().setInstructionView(null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        arFragment.getArSceneView().pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        arFragment.getArSceneView().destroy();
    }
}

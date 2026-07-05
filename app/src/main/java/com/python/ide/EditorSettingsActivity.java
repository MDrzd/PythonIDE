package com.python.ide;

import android.os.Bundle;

import com.google.android.material.slider.Slider;

import androidx.appcompat.app.AppCompatActivity;

import com.python.ide.databinding.ActivityEditorSettingsBinding;

public class EditorSettingsActivity extends AppCompatActivity {

    private ActivityEditorSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditorSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        
        int fontSize = Prefs.getFontSize(this);

binding.sliderFontSize.setValue(fontSize);
binding.txtFontSize.setText(fontSize + " SP");

binding.sliderFontSize.addOnChangeListener(
        (Slider slider, float value, boolean fromUser) -> {

            int size = (int) value;

            binding.txtFontSize.setText(size + " SP");

            Prefs.setFontSize(this, size);

        }
);
        
        binding.switchWordWrap.setChecked(
        Prefs.getWordWrap(this)
);

binding.switchWordWrap.setOnCheckedChangeListener(
        (buttonView, isChecked) ->
                Prefs.setWordWrap(this, isChecked)
);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
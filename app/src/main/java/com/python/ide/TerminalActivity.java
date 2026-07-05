package com.python.ide;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.python.ide.databinding.ActivityTerminalBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TerminalActivity extends AppCompatActivity {

    private ActivityTerminalBinding binding;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding =
                ActivityTerminalBinding.inflate(
                        getLayoutInflater()
                );

        setContentView(binding.getRoot());

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Python.getInstance()
                .getModule("runner")
                .callAttr(
                        "set_callback",
                        new PythonCallback(this)
                );

        binding.terminalInput.setVisibility(
                View.GONE
        );

        binding.terminalInput
                .setOnEditorActionListener(
                        (v, actionId, event) -> {

                            String text =
                                    binding.terminalInput
                                            .getText()
                                            .toString();

                            appendOutput(
                                    text + "\n"
                            );

                            binding.terminalInput.setText("");

                            binding.terminalInput.setVisibility(
                                    View.GONE
                            );

                            Python.getInstance()
                                    .getModule("runner")
                                    .callAttr(
                                            "send_input",
                                            text
                                    );

                            return true;
                        });

        String code =
                getIntent().getStringExtra(
                        "code"
                );

        if(code != null){
            runPythonCode(code);
        }
    }

    private void runPythonCode(String code) {

    binding.output.setText("");

    appendOutput("Python 3.11\n");
    appendOutput("====================\n");
    appendOutput("Running...\n\n");

    long startTime =
            System.currentTimeMillis();

    executor.execute(() -> {

        try {

            Python.getInstance()
                    .getModule("runner")
                    .callAttr(
                            "run",
                            code
                    );

        } catch (Exception e) {

            appendOutput(
                    "\nERROR\n"
            );

            appendOutput(
                    e.toString()
            );

            appendOutput(
                    "\n"
            );

        } finally {

            long elapsed =
                    System.currentTimeMillis()
                            - startTime;

            appendOutput(
                    "\n====================\n"
            );

            appendOutput(
                    "Finished\n"
            );

            appendOutput(
                    "Time: "
                            + elapsed
                            + " ms\n"
            );

        }

    });

}

    public void appendOutput(String text){

    runOnUiThread(() -> {

        binding.output.append(text);

        binding.outputScroll.post(() ->
                binding.outputScroll.fullScroll(
                        View.FOCUS_DOWN
                ));

    });

}

    public void requestPythonInput(String prompt) {

        runOnUiThread(() -> {

            appendOutput(prompt);

            binding.terminalInput
                    .setVisibility(
                            View.VISIBLE
                    );

            binding.terminalInput
                    .requestFocus();

        });
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executor.shutdownNow();

        binding = null;
    }

    @Override
    public boolean onSupportNavigateUp() {

        finish();

        return true;
    }
}
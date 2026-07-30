package com.python.ide;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.python.ide.databinding.ActivityTerminalBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TerminalActivity extends AppCompatActivity {

    private ActivityTerminalBinding binding;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private boolean waitingForInput = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding =
                ActivityTerminalBinding.inflate(
                        getLayoutInflater()
                );

        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() != null) {

            getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(true);
        }

        if (!Python.isStarted()) {

            Python.start(
                    new AndroidPlatform(this)
            );
        }

        Python.getInstance()
                .getModule("runner")
                .callAttr(
                        "set_callback",
                        new PythonCallback(this)
                );

        binding.inputContainer.setVisibility(
                View.GONE
        );

        binding.terminalInput
                .setOnEditorActionListener(
                        (v, actionId, event) -> {

                            if (actionId
                                    == EditorInfo
                                    .IME_ACTION_DONE) {

                                sendInput();

                                return true;
                            }

                            if (event != null
                                    && event.getKeyCode()
                                    == KeyEvent.KEYCODE_ENTER
                                    && event.getAction()
                                    == KeyEvent.ACTION_DOWN) {

                                sendInput();

                                return true;
                            }

                            return false;
                        }
                );

        String code =
                getIntent()
                        .getStringExtra("code");

        if (code != null) {

            runPythonCode(code);

        } else {

            appendOutput(
                    "Python 3.11\n"
            );

            appendOutput(
                    "────────────────────────\n"
            );

            appendOutput(
                    "Ready\n"
            );
        }
    }

    private void runPythonCode(
            String code
    ) {

        if (binding == null) {
            return;
        }

        binding.output.setText("");

        binding.inputContainer.setVisibility(
                View.GONE
        );

        waitingForInput = false;

        appendOutput(
                "Python 3.11\n"
        );

        appendOutput(
                "────────────────────────\n"
        );

        appendOutput(
                "Running...\n\n"
        );

        long startTime =
                System.currentTimeMillis();

        /*
         * Jalankan Python di background
         */
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
                        + "\n"
        );

    } finally {

        long elapsed =
                System.currentTimeMillis()
                        - startTime;

        runOnUiThread(() -> {

            if (binding == null) {
                return;
            }

            if (!waitingForInput) {

                appendOutput(
                        "\n────────────────────────\n"
                );

                appendOutput(
                        "Finished\n"
                );

                appendOutput(
                        "Time: "
                                + elapsed
                                + " ms\n"
                );

                binding.inputContainer
                        .setVisibility(
                                View.GONE
                        );

                hideKeyboard();
            }
        });
    }
});
 }

    public void appendOutput(
            String text
    ) {

        runOnUiThread(() -> {

            if (binding == null) {
                return;
            }

            binding.output.append(text);

            scrollToBottom();
        });
    }

    public void requestPythonInput(
            String prompt
    ) {

        runOnUiThread(() -> {

            if (binding == null) {
                return;
            }

            waitingForInput = true;

            binding.inputPrompt.setText(
                    prompt
            );

            binding.terminalInput.setText(
                    ""
            );

            binding.inputContainer
                    .setVisibility(
                            View.VISIBLE
                    );

            binding.terminalInput.requestFocus();
            scrollToBottom();

            binding.terminalInput.post(() -> {

                InputMethodManager imm =
                        (InputMethodManager)
                                getSystemService(
                                        Context
                                                .INPUT_METHOD_SERVICE
                                );

                if (imm != null) {

                    imm.showSoftInput(
                            binding.terminalInput,
                            InputMethodManager
                                    .SHOW_IMPLICIT
                    );
                }
            });
        });
    }

    private void sendInput() {

    if (binding == null) {
        return;
    }

    if (!waitingForInput) {
        return;
    }

    waitingForInput = false;

    String text =
            binding.terminalInput
                    .getText()
                    .toString();

    String line =
            binding.inputPrompt
                    .getText()
                    .toString()
                    + text
                    + "\n";

    appendOutput(line);

    binding.terminalInput.setText("");

    binding.inputContainer.setVisibility(
            View.GONE
    );

    hideKeyboard();

    try {

        Python.getInstance()
                .getModule("runner")
                .callAttr(
                        "send_input",
                        text
                );

    } catch (Exception e) {

        appendOutput(
                "\nERROR sending input:\n"
        );

        appendOutput(
                e.toString()
                + "\n"
        );
    }

    scrollToBottom();
}

    private void scrollToBottom() {

        if (binding == null) {
            return;
        }

        binding.outputScroll.post(() -> {

            if (binding == null) {
                return;
            }

            binding.outputScroll.fullScroll(
                    View.FOCUS_DOWN
            );
        });
    }

    private void hideKeyboard() {

        if (binding == null) {
            return;
        }

        InputMethodManager imm =
                (InputMethodManager)
                        getSystemService(
                                Context
                                        .INPUT_METHOD_SERVICE
                        );

        if (imm != null) {

            imm.hideSoftInputFromWindow(
                    binding.terminalInput
                            .getWindowToken(),
                    0
            );
        }
    }

    @Override
    public boolean onSupportNavigateUp() {

        finish();

        return true;
    }

    @Override
    protected void onDestroy() {

        hideKeyboard();

        waitingForInput = false;

        executor.shutdownNow();

        binding = null;

        super.onDestroy();
    }
                }

import queue
import builtins
import traceback
import sys
import os

input_queue = queue.Queue()

callback = None


def set_callback(cb):
    global callback
    callback = cb


def send_input(text):
    input_queue.put(text)


def custom_input(prompt=""):
    if callback:
        callback.requestInput(prompt)

    return input_queue.get()


class AndroidStdout:

    def write(self, text):
        if callback and text:
            callback.appendOutput(text)

    def flush(self):
        pass


def load_external_libraries():
    libs_path = "/data/data/com.python.ide/files/python_libs"

    if not os.path.exists(libs_path):
        return

    # Tambahkan folder utama
    if libs_path not in sys.path:
        sys.path.insert(0, libs_path)

    # Tambahkan semua subfolder hasil ekstrak wheel
    for item in os.listdir(libs_path):
        full_path = os.path.join(libs_path, item)

        if os.path.isdir(full_path):
            if full_path not in sys.path:
                sys.path.insert(0, full_path)


def run(code):

    builtins.input = custom_input

    # Load library eksternal
    load_external_libraries()

    old_stdout = sys.stdout
    old_stderr = sys.stderr

    sys.stdout = AndroidStdout()
    sys.stderr = AndroidStdout()

    try:
        exec(code, {})

    except Exception:
        if callback:
            callback.appendOutput(
                traceback.format_exc()
            )

    finally:
        sys.stdout = old_stdout
        sys.stderr = old_stderr
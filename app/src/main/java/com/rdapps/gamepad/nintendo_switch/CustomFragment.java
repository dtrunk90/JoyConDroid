package com.rdapps.gamepad.nintendo_switch;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.fragment.app.FragmentActivity;

import com.erz.joysticklibrary.JoyStick;
import com.rdapps.gamepad.ControllerActivity;
import com.rdapps.gamepad.R;
import com.rdapps.gamepad.button.AxisEnum;
import com.rdapps.gamepad.button.ButtonEnum;
import com.rdapps.gamepad.led.LedState;
import com.rdapps.gamepad.util.ControllerFunctions;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.Phaser;

import static com.rdapps.gamepad.ControllerActivity.CUSTOM_UI_URL;
import static com.rdapps.gamepad.button.ButtonState.BUTTON_DOWN;
import static com.rdapps.gamepad.button.ButtonState.BUTTON_UP;
import static com.rdapps.gamepad.log.JoyConLog.log;

public class CustomFragment extends ControllerFragment {
    private static final String LOG_TAG = CustomFragment.class.getName();

    private String url;

    private WebView webView;

    private ValueCallback<Uri[]> mUploadMessage;
    private Uri[] results;

    public static CustomFragment getInstance(String url) {
        CustomFragment customFragment = new CustomFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(CUSTOM_UI_URL, url);
        customFragment.setArguments(bundle);
        return customFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.custom_joycon_layout, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.url = getArguments().getString(CUSTOM_UI_URL);
        super.onViewCreated(view, savedInstanceState);
        webView = view.findViewById(R.id.webContent);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                webView.loadUrl("file:///android_asset/error.html");
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                log("CONTENT", String.format("%s @ %d: %s",
                        cm.message(), cm.lineNumber(), cm.sourceId()));
                return true;
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                // Double check that we don't have any existing callbacks
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(null);
                }
                mUploadMessage = filePathCallback;

                openFileSelectionDialog();

                return true;
            }
        });

        webView.addJavascriptInterface(new JoyConJSInterface(), "joyconJS");
        webView.addJavascriptInterface(new ControllerFunctions(
                webView,
                () -> {
                    webView.loadUrl(url);
                }
        ), "Controller");
    }

    private class JoyConJSInterface {
        private Phaser phaser = new Phaser(1);

        @JavascriptInterface
        public void onUp(boolean pressed) {
            onButton(ButtonEnum.UP, pressed);
        }

        @JavascriptInterface
        public void onDown(boolean pressed) {
            onButton(ButtonEnum.DOWN, pressed);
        }

        @JavascriptInterface
        public void onLeft(boolean pressed) {
            onButton(ButtonEnum.LEFT, pressed);
        }

        @JavascriptInterface
        public void onRight(boolean pressed) {
            onButton(ButtonEnum.RIGHT, pressed);
        }

        @JavascriptInterface
        public void onLeftSL(boolean pressed) {
            onButton(ButtonEnum.LEFT_SL, pressed);
        }

        @JavascriptInterface
        public void onLeftSR(boolean pressed) {
            onButton(ButtonEnum.LEFT_SR, pressed);
        }

        @JavascriptInterface
        public void onCapture(boolean pressed) {
            onButton(ButtonEnum.CAPTURE, pressed);
        }

        @JavascriptInterface
        public void onMinus(boolean pressed) {
            onButton(ButtonEnum.MINUS, pressed);
        }

        @JavascriptInterface
        public void onSync(boolean pressed) {
            if (pressed) {
                return;
            }

            FragmentActivity activity = getActivity();
            if (activity != null && activity instanceof ControllerActivity) {
                ControllerActivity controllerActivity = (ControllerActivity) activity;
                controllerActivity.sync();
            }
        }

        @JavascriptInterface
        public void onL(boolean pressed) {
            onButton(ButtonEnum.L, pressed);
        }

        @JavascriptInterface
        public void onZL(boolean pressed) {
            onButton(ButtonEnum.ZL, pressed);
        }

        @JavascriptInterface
        public void onLeftJoystick(float power, float angle) {
            if (Objects.isNull(device)) {
                return;
            }

            double x = power * Math.cos(angle);
            double y = power * Math.sin(angle);

            device.setAxis(AxisEnum.LEFT_STICK_X, (int) x);
            device.setAxis(AxisEnum.LEFT_STICK_Y, (int) y);
        }

        @JavascriptInterface
        public void onLeftJoystickPressed(boolean pressed) {
            if (Objects.isNull(device)) {
                return;
            }

            onButton(ButtonEnum.LEFT_STICK_BUTTON, pressed);
        }

        @JavascriptInterface
        public void onB(boolean pressed) {
            onButton(ButtonEnum.B, pressed);
        }

        @JavascriptInterface
        public void onA(boolean pressed) {
            onButton(ButtonEnum.A, pressed);
        }

        @JavascriptInterface
        public void onX(boolean pressed) {
            onButton(ButtonEnum.X, pressed);
        }

        @JavascriptInterface
        public void onY(boolean pressed) {
            onButton(ButtonEnum.Y, pressed);
        }

        @JavascriptInterface
        public void onRightSL(boolean pressed) {
            onButton(ButtonEnum.RIGHT_SL, pressed);
        }

        @JavascriptInterface
        public void onRightSR(boolean pressed) {
            onButton(ButtonEnum.RIGHT_SR, pressed);
        }

        @JavascriptInterface
        public void onHome(boolean pressed) {
            onButton(ButtonEnum.HOME, pressed);
        }

        @JavascriptInterface
        public void onPlus(boolean pressed) {
            onButton(ButtonEnum.PLUS, pressed);
        }

        @JavascriptInterface
        public void onRightJoystick(float power, float angle) {
            if (Objects.isNull(device)) {
                return;
            }

            double x = power * Math.cos(angle);
            double y = power * Math.sin(angle);

            device.setAxis(AxisEnum.RIGHT_STICK_X, (int) x);
            device.setAxis(AxisEnum.RIGHT_STICK_Y, (int) y);
        }

        @JavascriptInterface
        public void onRightJoystickPressed(boolean pressed) {
            if (Objects.isNull(device)) {
                return;
            }

            onButton(ButtonEnum.RIGHT_STICK_BUTTON, pressed);
        }

        @JavascriptInterface
        public void onR(boolean pressed) {
            onButton(ButtonEnum.R, pressed);
        }

        @JavascriptInterface
        public void onZR(boolean pressed) {
            onButton(ButtonEnum.ZR, pressed);
        }


        @JavascriptInterface
        public boolean registerCallback(String callbackFunction) {
            if (Objects.nonNull(device)) {
                device.setCallbackFunction(() -> {
                    log(LOG_TAG, "Notify Callback");
                    phaser.register();
                    getActivity().runOnUiThread(() -> webView.evaluateJavascript(
                            callbackFunction + "();",
                            (value) -> {
                                log(LOG_TAG, "Before Script Completed");
                                phaser.arriveAndDeregister();
                                log(LOG_TAG, "Script Completed");
                            }));
                    log(LOG_TAG, "Waiting Script Completed");
                    phaser.arriveAndAwaitAdvance();
                    log(LOG_TAG, "Callback done");
                });
                return true;
            } else {
                return false;
            }
        }

        @JavascriptInterface
        public void unregisterCallback() {
            if (Objects.nonNull(device)) {
                device.setCallbackFunction(null);
            }
        }

        @JavascriptInterface
        public void unblockCallback() {
            phaser.arriveAndDeregister();
        }

        @JavascriptInterface
        public void setAccelerometerEnabled(boolean enabled) {
            if (Objects.nonNull(device) &&
                    device.isAccelerometerEnabled() != enabled) {
                device.setAccelerometerEnabled(enabled);
                if (enabled) {
                    registerAccelerometerListener();
                } else {
                    unregisterAccelerometerListener();
                }
            }
        }

        @JavascriptInterface
        public void setGyroscopeEnabled(boolean enabled) {
            if (Objects.nonNull(device) &&
                    device.isGyroscopeEnabled() != enabled) {
                device.setGyroscopeEnabled(enabled);
                if (enabled) {
                    registerGyroscopeListener();
                } else {
                    unregisterGyroscopeListener();
                }
            }
        }

        @JavascriptInterface
        public void setMotionControlsEnabled(boolean enabled) {
            setAccelerometerEnabled(enabled);
            setGyroscopeEnabled(enabled);
        }

        @JavascriptInterface
        public boolean isAccelerometerEnabled() {
            return device.isAccelerometerEnabled();
        }

        @JavascriptInterface
        public boolean isGyroscopeEnabled() {
            return device.isGyroscopeEnabled();
        }

        @JavascriptInterface
        public boolean isMotionControlsEnabled() {
            return device.isMotionControlsEnabled();
        }
    }

    public void onButton(ButtonEnum buttonEnum, boolean pressed) {
        if (Objects.isNull(device)) {
            return;
        }

        int buttonState;
        if (pressed) {
            buttonState = BUTTON_DOWN;
        } else {
            buttonState = BUTTON_UP;
        }

        device.setButton(buttonEnum, buttonState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public boolean setLeftStickPress(boolean pressed) {
        return true;
    }

    @Override
    public boolean setRightStickPress(boolean pressed) {
        return false;
    }

    @Override
    public Button getSR() {
        return null;
    }

    @Override
    public Button getSL() {
        return null;
    }

    @Override
    public Button getUp() {
        return null;
    }

    @Override
    public Button getDown() {
        return null;
    }

    @Override
    public Button getLeft() {
        return null;
    }

    @Override
    public Button getRight() {
        return null;
    }

    @Override
    public Button getZL() {
        return null;
    }

    @Override
    public Button getZR() {
        return null;
    }

    @Override
    public Button getL() {
        return null;
    }

    @Override
    public Button getR() {
        return null;
    }

    @Override
    public Button getMinus() {
        return null;
    }

    @Override
    public Button getPlus() {
        return null;
    }

    @Override
    public Button getHome() {
        return null;
    }

    @Override
    public Button getCapture() {
        return null;
    }

    @Override
    public Button getSync() {
        return null;
    }

    @Override
    public JoyStick getLeftJoyStick() {
        return null;
    }

    @Override
    public JoyStick getRightJoyStick() {
        return null;
    }

    @Override
    public Button getA() {
        return null;
    }

    @Override
    public Button getB() {
        return null;
    }

    @Override
    public Button getX() {
        return null;
    }

    @Override
    public Button getY() {
        return null;
    }

    @Override
    public boolean reverseJoyStickXY() {
        return false;
    }

    @Override
    public void onSelectedFilePaths(String[] files) {
        results = new Uri[files.length];
        for (int i = 0; i < files.length; i++) {
            String filePath = new File(files[i]).getAbsolutePath();
            if (!filePath.startsWith("file://")) {
                filePath = "file://" + filePath;
            }
            results[i] = Uri.parse(filePath);
            log(LOG_TAG, "file path: " + filePath);
            log(LOG_TAG, "file uri: " + String.valueOf(results[i]));
        }
        mUploadMessage.onReceiveValue(results);
        mUploadMessage = null;
    }

    @Override
    public void onFileSelectorCanceled(DialogInterface dialog) {
        if (null != mUploadMessage) {
            if (null != results && results.length >= 1) {
                mUploadMessage.onReceiveValue(results);
            } else {
                mUploadMessage.onReceiveValue(null);
            }
        }
        mUploadMessage = null;
        this.dialog = null;
    }

    @Override
    public void onFileSelectorDismissed(DialogInterface dialog) {
        if (null != mUploadMessage) {
            if (null != results && results.length >= 1) {
                mUploadMessage.onReceiveValue(results);
            } else {
                mUploadMessage.onReceiveValue(null);
            }
        }
        mUploadMessage = null;
        this.dialog = null;
    }

    @Override
    public void setPlayerLights(LedState led1, LedState led2, LedState led3, LedState led4) {

    }
}

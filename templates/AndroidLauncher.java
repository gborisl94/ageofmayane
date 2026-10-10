package com.ageofmayane.game.android;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ageofmayane.game.AgeOfMayaneGame;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public class AndroidLauncher extends AndroidApplication {

    static final String CRASH_FILE = "crash.txt";
    static final String STEP_FILE = "step.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        installCrashHandler(this);
        final Context app = getApplicationContext();

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useGL30 = false;
        config.numSamples = 0;
        config.useImmersiveMode = false;
        config.useAccelerometer = false;
        config.useCompass = false;

        write(app, STEP_FILE, "2 - initialize() va demarrer\n", false);
        initialize(new AgeOfMayaneGame() {
            boolean firstFrame = true;

            @Override
            public void create() {
                write(app, STEP_FILE, "3 - create() commence (OpenGL OK)\n", false);
                super.create();
                write(app, STEP_FILE, "4 - create() termine\n", false);
            }

            @Override
            public void render() {
                super.render();
                if (firstFrame) {
                    firstFrame = false;
                    write(app, STEP_FILE, "5 - premier rendu OK\n", false);
                }
            }
        }, config);
    }

    // ---------- Utilitaires fichiers ----------

    static void write(Context c, String name, String text, boolean append) {
        try {
            FileOutputStream out = new FileOutputStream(new File(c.getFilesDir(), name), append);
            out.write(text.getBytes("UTF-8"));
            out.close();
        } catch (Throwable ignored) {
        }
    }

    static String read(Context c, String name, String def) {
        try {
            FileInputStream in = new FileInputStream(new File(c.getFilesDir(), name));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
            in.close();
            return bos.toString("UTF-8");
        } catch (Throwable t) {
            return def;
        }
    }

    // ---------- Capture des crashs Java ----------

    static class CrashHandler implements Thread.UncaughtExceptionHandler {
        private final Context app;
        private final Thread.UncaughtExceptionHandler previous;

        CrashHandler(Context app, Thread.UncaughtExceptionHandler previous) {
            this.app = app;
            this.previous = previous;
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            try {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                write(app, CRASH_FILE, "Thread: " + t.getName() + "\n" + sw + "\n----\n", true);
            } catch (Throwable ignored) {
            }
            if (previous != null) {
                previous.uncaughtException(t, e);
            } else {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);
            }
        }
    }

    static void installCrashHandler(Context ctx) {
        Thread.UncaughtExceptionHandler cur = Thread.getDefaultUncaughtExceptionHandler();
        if (cur instanceof CrashHandler) return;
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(ctx.getApplicationContext(), cur));
    }

    // ---------- Écran de diagnostic ----------

    public static class DiagActivity extends Activity {

        private String report = "";

        @Override
        protected void onCreate(Bundle b) {
            super.onCreate(b);
            installCrashHandler(this);
            report = buildReport();

            int p = (int) (12 * getResources().getDisplayMetrics().density);

            LinearLayout root = new LinearLayout(this);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(p, p, p, p);
            root.setFitsSystemWindows(true);

            Button launch = new Button(this);
            launch.setText("Lancer le jeu");
            launch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    write(DiagActivity.this, CRASH_FILE, "", false);
                    new File(getFilesDir(), CRASH_FILE).delete();
                    write(DiagActivity.this, STEP_FILE, "1 - lancement demande\n", false);
                    startActivity(new Intent(DiagActivity.this, AndroidLauncher.class));
                }
            });

            Button copy = new Button(this);
            copy.setText("Copier le rapport");
            copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText("rapport", report));
                }
            });

            TextView tv = new TextView(this);
            tv.setTypeface(Typeface.MONOSPACE);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            tv.setTextIsSelectable(true);
            tv.setText(report);

            ScrollView sv = new ScrollView(this);
            sv.addView(tv);

            root.addView(launch);
            root.addView(copy);
            root.addView(sv, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
            setContentView(root);
        }

        private String buildReport() {
            StringBuilder sb = new StringBuilder();

            sb.append("=== APPAREIL ===\n");
            sb.append(Build.MANUFACTURER).append(' ').append(Build.MODEL)
                    .append(" | Android ").append(Build.VERSION.RELEASE)
                    .append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
            sb.append("ABIs: ").append(Arrays.toString(Build.SUPPORTED_ABIS)).append('\n');

            sb.append("\n=== LIBS NATIVES DANS L'APK ===\n");
            File dir = new File(getApplicationInfo().nativeLibraryDir);
            sb.append(dir.getPath()).append('\n');
            String[] libs = dir.list();
            if (libs == null || libs.length == 0) {
                sb.append("AUCUNE (!)\n");
            } else {
                for (String s : libs) sb.append(" - ").append(s).append('\n');
            }

            sb.append("\n=== CHARGEMENT libgdx.so ===\n");
            try {
                System.loadLibrary("gdx");
                sb.append("OK\n");
            } catch (Throwable t) {
                sb.append("ECHEC : ").append(t).append('\n');
            }

            sb.append("\n=== DERNIERE ETAPE ATTEINTE ===\n");
            sb.append(read(this, STEP_FILE, "(aucune)\n"));

            sb.append("\n=== RAPPORT DE CRASH JAVA ===\n");
            sb.append(read(this, CRASH_FILE, "(aucun)\n"));

            return sb.toString();
        }
    }
}